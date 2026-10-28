package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SmartGmailApp
import com.example.data.EmailEntity
import com.example.data.EmailRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Intent
import android.content.IntentSender
import com.example.auth.AuthResult
import com.example.demo.DemoData
import com.example.sync.SyncState
import com.example.worker.OutboxWorker

class SmartGmailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmailRepository
    private val prefs = application.getSharedPreferences("smart_gmail_prefs", Context.MODE_PRIVATE)

    // Theme Preference: "system", "light", "dark"
    private val _themePreference = MutableStateFlow(prefs.getString("theme_preference", "system") ?: "system")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    fun setThemePreference(preference: String) {
        _themePreference.value = preference
        prefs.edit().putString("theme_preference", preference).apply()
    }

    // Base flows
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    private val backend = SmartGmailApp.backend

    private val _userEmail = MutableStateFlow(prefs.getString("oauth_email", "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("oauth_name", "Guest") ?: "Guest")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isConnected = MutableStateFlow(prefs.getBoolean("is_connected", false))
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /** True only after the user taps "Try demo". Demo rows are labeled and never reach Gmail. */
    private val _isDemo = MutableStateFlow(prefs.getBoolean("is_demo", false))
    val isDemo: StateFlow<Boolean> = _isDemo.asStateFlow()

    /** Set when Google needs to show its consent screen; the UI launches it and reports back. */
    private val _pendingConsent = MutableStateFlow<IntentSender?>(null)
    val pendingConsent: StateFlow<IntentSender?> = _pendingConsent.asStateFlow()

    // Navigation and UI state
    // Tabs: 0 -> Inbox, 1 -> Buckets, 2 -> Dynamic Cleanup, 3 -> Configuration Settings
    private val _currentTab = MutableStateFlow(if (_isConnected.value || _isDemo.value) 0 else 3)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _activeCategoryTab = MutableStateFlow("Work")
    val activeCategoryTab: StateFlow<String> = _activeCategoryTab.asStateFlow()

    private val _selectedEmail = MutableStateFlow<EmailEntity?>(null)
    val selectedEmail: StateFlow<EmailEntity?> = _selectedEmail.asStateFlow()

    private val _isSummaryLoading = MutableStateFlow(false)
    val isSummaryLoading: StateFlow<Boolean> = _isSummaryLoading.asStateFlow()

    // Feed values from SQLite reactive table
    val allEmails: StateFlow<List<EmailEntity>>

    init {
        val emailDao = SmartGmailApp.database.emailDao()
        repository = EmailRepository(emailDao)

        allEmails = repository.allEmails
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        if (_isConnected.value) syncInbox()
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setActiveCategoryTab(category: String) {
        _activeCategoryTab.value = category
    }

    fun selectEmail(email: EmailEntity?) {
        _selectedEmail.value = email
        if (email != null && email.summary == null) {
            fetchEmailSummary(email)
        }
    }

    /** Starts Google sign-in and the Gmail grant. Shows Google's consent screen when needed. */
    fun connect() {
        viewModelScope.launch {
            _isLoading.value = true
            handleAuth(backend.auth.authorize())
            _isLoading.value = false
        }
    }

    fun onConsentResult(data: Intent?) {
        _pendingConsent.value = null
        viewModelScope.launch {
            _isLoading.value = true
            handleAuth(backend.auth.completeConsent(data))
            _isLoading.value = false
        }
    }

    private suspend fun handleAuth(result: AuthResult) {
        when (result) {
            is AuthResult.NeedsConsent -> _pendingConsent.value = result.intentSender
            is AuthResult.Failure -> _syncResult.value = "Auth failed: ${result.message}"
            is AuthResult.Success -> {
                val name = result.name ?: result.email ?: "Google user"
                val email = result.email ?: ""
                _userName.value = name
                _userEmail.value = email
                _isConnected.value = true
                _isDemo.value = false
                prefs.edit().putString("oauth_name", name).putString("oauth_email", email)
                    .putBoolean("is_connected", true).putBoolean("is_demo", false).apply()
                DemoData.clear(SmartGmailApp.database)
                runSync()
            }
        }
    }

    fun disconnectOAuth() {
        viewModelScope.launch {
            backend.auth.signOut()
            _isConnected.value = false
            _userName.value = "Guest"
            _userEmail.value = ""
            prefs.edit().remove("oauth_name").remove("oauth_email").putBoolean("is_connected", false).apply()
            repository.clearCache()
            SmartGmailApp.database.syncStateDao().remove(com.example.sync.SyncEngine.HISTORY_KEY)
            _currentTab.value = 3
        }
    }

    fun startDemo() {
        viewModelScope.launch {
            _isDemo.value = true
            prefs.edit().putBoolean("is_demo", true).apply()
            DemoData.load(SmartGmailApp.database, backend.classifier)
            _syncResult.value = "Demo data loaded"
            _currentTab.value = 0
        }
    }

    fun exitDemo() {
        viewModelScope.launch {
            _isDemo.value = false
            prefs.edit().putBoolean("is_demo", false).apply()
            DemoData.clear(SmartGmailApp.database)
        }
    }

    /** Pulls Gmail into the local cache. Without a Gmail connection it does nothing; demo data stays as loaded. */
    fun syncInbox() {
        if (!_isConnected.value) {
            _syncResult.value = "Failed: connect a Google account to sync."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            runSync()
            _isLoading.value = false
        }
    }

    private suspend fun runSync() {
        _syncResult.value = null
        _syncResult.value = when (val state = backend.syncAll()) {
            is SyncState.Done -> "Synchronized. ${state.result.added} new emails."
            SyncState.SignInRequired -> {
                _isConnected.value = false
                prefs.edit().putBoolean("is_connected", false).apply()
                "Auth failed: sign in again to keep syncing."
            }
            is SyncState.Error -> "Failed to synchronize: ${state.message}"
            else -> null
        }
    }

    private fun queueWriteBack() = OutboxWorker.enqueue(getApplication())

    // --- Action Methods ---

    fun toggleReadStatus(emailId: String, currentRead: Boolean) {
        viewModelScope.launch {
            repository.updateReadStatus(emailId, !currentRead)
            queueWriteBack()
            
            // Sync with selection
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = _selectedEmail.value?.copy(isRead = !currentRead)
            }
        }
    }

    fun archiveEmail(emailId: String) {
        viewModelScope.launch {
            repository.archiveEmail(emailId)
            queueWriteBack()
            
            // Close details if in view
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = null
            }
        }
    }

    fun deleteEmail(emailId: String) {
        viewModelScope.launch {
            repository.deleteEmail(emailId)
            queueWriteBack()
            
            // Close details if in view
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = null
            }
        }
    }

    /**
     * Perform bulk 1-tap operation across all items within a specific category bucket
     */
    fun performBulkAction(category: String, emailIds: List<String>, action: String) {
        viewModelScope.launch {
            if (emailIds.isEmpty()) return@launch
            _isLoading.value = true
            when (action) {
                "mark_as_read" -> repository.updateReadStatusForBulk(emailIds, true)
                "archive" -> repository.archiveEmailsForBulk(emailIds)
                "delete" -> repository.deleteEmailsForBulk(emailIds)
            }
            queueWriteBack()
            _isLoading.value = false
        }
    }

    /**
     * Instantly update category for an email item
     */
    fun moveCategory(emailId: String, newCategory: String) {
        viewModelScope.launch {
            repository.setCategory(emailId, newCategory)
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = _selectedEmail.value?.copy(category = newCategory)
            }
        }
    }

    /** Summarizes on demand: Gemini Nano on supported phones, rules elsewhere. Runs on the device. */
    private fun fetchEmailSummary(email: EmailEntity) {
        viewModelScope.launch {
            _isSummaryLoading.value = true
            val meta = com.example.classify.EmailMeta(
                email.sender, email.senderEmail, email.subject, email.snippet,
                email.labelIds.split(",").filter { it.isNotEmpty() }
            )
            val summary = backend.summarizer.summarize(meta, email.category)
            repository.saveSummary(email.id, summary.text, summary.actions)
            if (_selectedEmail.value?.id == email.id) {
                _selectedEmail.value = email.copy(summary = summary.text, suggestedActions = summary.actions.joinToString(","))
            }
            _isSummaryLoading.value = false
        }
    }
}
