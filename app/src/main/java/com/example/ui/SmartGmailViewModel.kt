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
import com.google.android.gms.auth.GoogleAuthUtil

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

    // OAuth Session: simple simulated Google Identity Integration
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userEmail = MutableStateFlow<String>(prefs.getString("oauth_email", "guest@example.com") ?: "guest@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String>(prefs.getString("oauth_name", "Guest User") ?: "Guest User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isConnected = MutableStateFlow(prefs.getBoolean("is_connected", false))
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Navigation and UI state
    // Tabs: 0 -> Inbox, 1 -> Buckets, 2 -> Dynamic Cleanup, 3 -> Configuration Settings
    private val _currentTab = MutableStateFlow(0)
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

        val isConnectedPref = prefs.getBoolean("is_connected", false)
        val emailPref = prefs.getString("oauth_email", null)

        if (isConnectedPref && emailPref != null) {
            connectOAuth(_userName.value, emailPref)
        } else {
            // Automatically trigger sync for initial state setup
            syncInbox()
        }
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

    /**
     * Integrates simulated or real OAuth 2.0 and retrieves mock access keys
     */
    fun connectOAuth(userName: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _userName.value = userName
            _userEmail.value = email
            
            prefs.edit()
                .putString("oauth_name", userName)
                .putString("oauth_email", email)
                .putBoolean("is_connected", true)
                .apply()
            try {
                val token = withContext(Dispatchers.IO) {
                    GoogleAuthUtil.getToken(
                        getApplication(),
                        email,
                        "oauth2:https://www.googleapis.com/auth/gmail.readonly"
                    )
                }
                _accessToken.value = token
                _isConnected.value = true
                syncInbox()
            } catch (e: Exception) {
                Log.e("SmartGmailViewModel", "Failed to get auth token", e)
                _syncResult.value = "Auth failed: ${e.message}"
                _isConnected.value = false
                _accessToken.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disconnectOAuth() {
        viewModelScope.launch {
            _accessToken.value = null
            _isConnected.value = false
            _userName.value = "Guest Reader"
            _userEmail.value = ""
            
            prefs.edit()
                .remove("oauth_name")
                .remove("oauth_email")
                .putBoolean("is_connected", false)
                .apply()
            
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getApplication<Application>(), gso)
            googleSignInClient.signOut()

            repository.clearCache()
        }
    }

    /**
     * Syronizes local Room Database cache with the Gmail API / simulated server dataset
     */
    fun syncInbox() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncResult.value = null
            
            val result = repository.syncInbox(
                accessToken = _accessToken.value,
                forceMock = !_isConnected.value // Use beautiful structured samples if no dynamic OAuth active
            )
            
            if (result.isSuccess) {
                _syncResult.value = "Successfully synchronized inbox! ${result.getOrNull()} emails loaded."
            } else {
                _syncResult.value = "Failed to synchronize: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    // --- Action Methods ---

    fun toggleReadStatus(emailId: String, currentRead: Boolean) {
        viewModelScope.launch {
            repository.updateReadStatus(emailId, !currentRead)
            
            // Sync with selection
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = _selectedEmail.value?.copy(isRead = !currentRead)
            }
        }
    }

    fun archiveEmail(emailId: String) {
        viewModelScope.launch {
            repository.archiveEmail(emailId)
            
            // Close details if in view
            if (_selectedEmail.value?.id == emailId) {
                _selectedEmail.value = null
            }
        }
    }

    fun deleteEmail(emailId: String) {
        viewModelScope.launch {
            repository.deleteEmail(emailId)
            
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
            // Simple visual wait to represent Gemini operations
            kotlinx.coroutines.delay(600)
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

    /**
     * Requests on-demand Gemini thread summaries
     */
    private fun fetchEmailSummary(email: EmailEntity) {
        viewModelScope.launch {
            _isSummaryLoading.value = true
            val updated = repository.summarizeThread(email)
            if (updated != null && _selectedEmail.value?.id == email.id) {
                _selectedEmail.value = updated
            }
            _isSummaryLoading.value = false
        }
    }
}
