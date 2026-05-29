package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import com.example.ui.icons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmailEntity
import com.example.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartGmailHome(
    viewModel: SmartGmailViewModel,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.allEmails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeCategoryTab by viewModel.activeCategoryTab.collectAsState()
    val selectedEmail by viewModel.selectedEmail.collectAsState()
    val isSummaryLoading by viewModel.isSummaryLoading.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()

    val inboxListState = rememberLazyListState()
    val isInboxScrolled by remember { derivedStateOf { inboxListState.firstVisibleItemScrollOffset > 0 || inboxListState.firstVisibleItemIndex > 0 } }

    var showCategoryMoveDialog by remember { mutableStateOf<EmailEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundPurple,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPurple
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.gmail_assistantlauncher_foreground),
                            contentDescription = "Gmail Assistant Icon",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                        )
                        Text(
                            text = "Gmail Assistant",
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp,
                            color = OnBackgroundPurple
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightSurfaceBg)
                            .clickable {
                                if (currentTab == 3) {
                                    viewModel.setTab(0)
                                } else {
                                    viewModel.setTab(3)
                                }
                            }, // Toggle Vault / Settings view
                        contentAlignment = Alignment.Center
                    ) {
                        if (isConnected) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = account_circle,
                                contentDescription = "OAuth Identity Account",
                                tint = MediumGrayText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LightSurfaceBg,
                tonalElevation = 0.dp,
                modifier = Modifier.border(BorderStroke(1.dp, SurfaceBorder.copy(alpha = 0.5f)))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(imageVector = inbox, contentDescription = "Inbox Tab") },
                    label = { Text("Inbox") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnBackgroundPurple,
                        selectedTextColor = OnBackgroundPurple,
                        indicatorColor = ActivePillBg,
                        unselectedIconColor = MediumGrayText,
                        unselectedTextColor = MediumGrayText
                    ),
                    modifier = Modifier.testTag("nav_inbox_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(imageVector = bucket_check, contentDescription = "Buckets Tab") },
                    label = { Text("Buckets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnBackgroundPurple,
                        selectedTextColor = OnBackgroundPurple,
                        indicatorColor = ActivePillBg,
                        unselectedIconColor = MediumGrayText,
                        unselectedTextColor = MediumGrayText
                    ),
                    modifier = Modifier.testTag("nav_buckets_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(imageVector = cleaning_services, contentDescription = "Cleanup Tab") },
                    label = { Text("Cleanup") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnBackgroundPurple,
                        selectedTextColor = OnBackgroundPurple,
                        indicatorColor = ActivePillBg,
                        unselectedIconColor = MediumGrayText,
                        unselectedTextColor = MediumGrayText
                    ),
                    modifier = Modifier.testTag("nav_cleanup_tab")
                )

            }
        },
        floatingActionButton = {
            if (currentTab != 3) {
                FloatingActionButton(
                    onClick = { viewModel.syncInbox() },
                    containerColor = BrandTertiary,
                    contentColor = DarkPurpleText,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.testTag("fab_sync_reorganize")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Tap to sync with Gemini backend and trigger DB updates",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main reactive loading indicators
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = BrandPrimary,
                        trackColor = BrandSecondary
                    )
                }

                // Header status banner (AI insights) - only visible on Inbox tab to conserve space
                if (currentTab == 0) {
                    GeminiInsightHeader(
                        emails = emails,
                        isConnected = isConnected,
                        syncResult = syncResult,
                        isMinimized = isInboxScrolled,
                        onSyncClick = { viewModel.syncInbox() }
                    )
                }

                // Decouple viewing based on tab
                when (currentTab) {
                    0 -> InboxView(
                        emails = emails,
                        listState = inboxListState,
                        onEmailClick = { viewModel.selectEmail(it) },
                        onToggleRead = { id, current -> viewModel.toggleReadStatus(id, current) },
                        onArchive = { viewModel.archiveEmail(it) },
                        onDelete = { viewModel.deleteEmail(it) },
                        onMove = { showCategoryMoveDialog = it }
                    )
                    1 -> BucketsView(
                        emails = emails,
                        activeCategory = activeCategoryTab,
                        onCategorySelect = { viewModel.setActiveCategoryTab(it) },
                        onEmailClick = { viewModel.selectEmail(it) },
                        onToggleRead = { id, current -> viewModel.toggleReadStatus(id, current) },
                        onBulkAction = { category, list, action -> viewModel.performBulkAction(category, list, action) }
                    )
                    2 -> CleanupView(
                        emails = emails,
                        onDelete = { viewModel.deleteEmail(it) },
                        onArchive = { viewModel.archiveEmail(it) },
                        onBulkUnsubscribe = { unsubscribedIds ->
                            viewModel.performBulkAction("Newsletter", unsubscribedIds, "delete")
                        }
                    )
                    3 -> SettingsView(
                        isConnected = isConnected,
                        userName = userName,
                        oauthEmailInput = userEmail,
                        oauthNameInput = userName,
                        themePreference = themePreference,
                        onThemeChange = { viewModel.setThemePreference(it) },
                        onEmailChange = { },
                        onNameChange = { },
                        onConnect = { name, email -> viewModel.connectOAuth(name, email) },
                        onDisconnect = { viewModel.disconnectOAuth() }
                    )
                }
            }

            // On-demand summary and actions bottom sheet (custom floating panel for compact, safe layouts)
            selectedEmail?.let { email ->
                CardDetailsBottomSheet(
                    email = email,
                    isLoading = isSummaryLoading,
                    onClose = { viewModel.selectEmail(null) },
                    onActionSelected = { action ->
                        if (action.lowercase().contains("delete")) {
                            viewModel.deleteEmail(email.id)
                        } else if (action.lowercase().contains("archive")) {
                            viewModel.archiveEmail(email.id)
                        } else {
                            viewModel.toggleReadStatus(email.id, email.isRead)
                            viewModel.selectEmail(null)
                        }
                    }
                )
            }

            // Prompt Category Mover Dialogue
            showCategoryMoveDialog?.let { email ->
                AlertDialog(
                    onDismissRequest = { showCategoryMoveDialog = null },
                    title = { Text("Move Email Category") },
                    text = {
                        Column {
                            Text("Current: ${email.category}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            val categories = listOf("Work", "Bill", "Newsletter", "Promotion", "Social")
                            categories.forEach { cat ->
                                Button(
                                    onClick = {
                                        viewModel.moveCategory(email.id, cat)
                                        showCategoryMoveDialog = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (cat == email.category) BrandPrimary else LightSurfaceBg,
                                        contentColor = if (cat == email.category) Color.White else OnBackgroundPurple
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(cat)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryMoveDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// Sparkle Gemini Header Card
@Composable
fun GeminiInsightHeader(
    emails: List<EmailEntity>,
    isConnected: Boolean,
    syncResult: String?,
    isMinimized: Boolean = false,
    onSyncClick: () -> Unit
) {
    val unreadCount = emails.count { !it.isRead }
    val categorizedCount = emails.count { it.category != "Uncategorized" }
    
    // Custom dynamic AI insights based on current email demographics
    val workCount = emails.count { it.category == "Work" }
    val billsCount = emails.count { it.category == "Bill" }
    val unsubCount = emails.count { it.isUnsubscribeSuggested }
    
    val insightMessage = remember(workCount, billsCount, unsubCount, isConnected) {
        if (!isConnected) {
            "Simulating in local environment. Click to connect your real Google Workspace Account to fetch direct info."
        } else if (emails.isEmpty()) {
            "Refreshing your workspace mailbox safely. This takes just a moment to classify metadata with Gemini."
        } else if (workCount > 4) {
            "Work emails are spiking rapidly ($workCount unread). Would you like to review urgent threads?"
        } else if (billsCount > 0) {
            "You have $billsCount subscription payment bill due soon. Tapping 'Bills' helps auto-archive them successfully."
        } else if (unsubCount > 0) {
            "Gemini identified $unsubCount low-priority promotional senders. Smart Cleanup allows 1-tap bulk unsubscribe."
        } else {
            "Your mailbox is highly streamlined. 0 outstanding clutter or urgent alerts."
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_insight_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSecondary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GEMINI INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkPurpleText,
                        letterSpacing = 1.25.sp,
                        modifier = Modifier.testTag("gemini_insight_header_text")
                    )
                    Text(
                        text = if (emails.isEmpty()) "Securing connection..." else "$categorizedCount emails categorized",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkPurpleText
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkPurpleText)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isConnected) "OAUTH ACTIVE" else "MOCK VAULT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            AnimatedVisibility(visible = !isMinimized) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = insightMessage,
                        fontSize = 14.sp,
                        color = DarkPurpleText.copy(alpha = 0.85f),
                        lineHeight = 19.sp,
                        modifier = Modifier.testTag("gemini_insight_message")
                    )
                    
                    syncResult?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandPrimary,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

// ---------------- INBOX VIEW (Tab 0) ----------------
@Composable
fun InboxView(
    emails: List<EmailEntity>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleRead: (String, Boolean) -> Unit,
    onArchive: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (EmailEntity) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Your Workspace Inbox",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MediumGrayText.copy(alpha = 0.85f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (emails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = inbox,
                        contentDescription = "Inbox status empty",
                        tint = MediumGrayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Your Inbox is completely clean!",
                        fontWeight = FontWeight.Medium,
                        color = MediumGrayText,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Force refresh using the refresh button.",
                        color = MediumGrayText.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("inbox_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emails, key = { it.id }) { email ->
                    EmailRowItem(
                        email = email,
                        onClick = { onEmailClick(email) },
                        onToggleRead = { onToggleRead(email.id, email.isRead) },
                        onArchive = { onArchive(email.id) },
                        onDelete = { onDelete(email.id) },
                        onMove = { onMove(email) }
                    )
                }
            }
        }
    }
}

// ---------------- BUCKETS VIEW (Tab 1) ----------------
@Composable
fun BucketsView(
    emails: List<EmailEntity>,
    activeCategory: String,
    onCategorySelect: (String) -> Unit,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleRead: (String, Boolean) -> Unit,
    onBulkAction: (String, List<String>, String) -> Unit
) {
    val categories = listOf("Work", "Bill", "Newsletter", "Promotion", "Social")
    val filteredEmails = emails.filter { it.category == activeCategory }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Categories horizontal tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val count = emails.count { it.category == cat }
                val isSelected = activeCategory == cat
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) ActivePillBg else MaterialTheme.colorScheme.surface)
                        .border(
                            BorderStroke(1.dp, if (isSelected) BrandPrimary else SurfaceBorder),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onCategorySelect(cat) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = cat,
                            tint = if (isSelected) BrandPrimary else MediumGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ActivePillText else MediumGrayText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandPrimary else LightRippleBg)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MediumGrayText
                            )
                        }
                    }
                }
            }
        }

        // Suggested 1-tap bulk actions header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurfaceBg),
            border = BorderStroke(1.dp, SurfaceBorder.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "1-TAP BUCKET ACTIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MediumGrayText,
                            letterSpacing = 1.sp
                        )
                    }
                    Icon(
                        imageVector = wand_shine,
                        contentDescription = "Gemini intelligence bulk actions",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val ids = filteredEmails.map { it.id }
                            onBulkAction(activeCategory, ids, "mark_as_read")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        enabled = filteredEmails.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("bulk_read_btn"),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Mark Read", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val ids = filteredEmails.map { it.id }
                            onBulkAction(activeCategory, ids, "archive")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        enabled = filteredEmails.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("bulk_archive_btn"),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Archive", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val ids = filteredEmails.map { it.id }
                            onBulkAction(activeCategory, ids, "delete")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        enabled = filteredEmails.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("bulk_delete_btn"),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Delete All", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filtered Email Lists
        if (filteredEmails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Category pristine",
                        tint = BrandPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Category '$activeCategory' is completely clean!",
                        fontWeight = FontWeight.Medium,
                        color = MediumGrayText,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEmails, key = { it.id }) { email ->
                    EmailRowItem(
                        email = email,
                        onClick = { onEmailClick(email) },
                        onToggleRead = { onToggleRead(email.id, email.isRead) },
                        onArchive = {},
                        onDelete = {},
                        onMove = {},
                        showActionButtons = false // simple listing for cleaner view inside categories
                    )
                }
            }
        }
    }
}

// ---------------- CLEANUP VIEW (Tab 2) ----------------
@Composable
fun CleanupView(
    emails: List<EmailEntity>,
    onDelete: (String) -> Unit,
    onArchive: (String) -> Unit,
    onBulkUnsubscribe: (List<String>) -> Unit
) {
    val unsubsList = emails.filter { it.isUnsubscribeSuggested }
    val clutterList = emails.filter { it.isClutter }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Clean Cleanup Console",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MediumGrayText.copy(alpha = 0.85f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Bulk Unsubscribe suggestions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UNSUBSCRIBE SUGGESTIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MediumGrayText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${unsubsList.size} lists with low engagement",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackgroundPurple
                        )
                    }
                    if (unsubsList.isNotEmpty()) {
                        TextButton(
                            onClick = { onBulkUnsubscribe(unsubsList.map { it.id }) },
                            modifier = Modifier.testTag("bulk_unsubscribe_btn")
                        ) {
                            Text("Unsubscribe All", color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (unsubsList.isEmpty()) {
                    Text(
                        text = "Nice! No low-priority subscription newsletters flagged.",
                        fontSize = 12.sp,
                        color = MediumGrayText
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unsubsList, key = { it.id }) { email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LightSurfaceBg)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = email.sender.split("<")[0].trim(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = OnBackgroundPurple,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = email.subject,
                                        fontSize = 10.sp,
                                        color = MediumGrayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(email.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = delete_sweep,
                                        contentDescription = "Delete and Unsubscribe",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Low priority clutter listing
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LOW-PRIORITY CLUTTER FLAGGED BY GEMINI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediumGrayText,
                    letterSpacing = 1.sp
                )

                if (clutterList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zero clutter emails detected in your inbox.",
                            fontWeight = FontWeight.Medium,
                            color = MediumGrayText,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(clutterList, key = { it.id }) { email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, SurfaceBorder), shape = RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(LightRippleBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = delete_sweep, // represents clutter clearing
                                        contentDescription = "Quick Delete",
                                        tint = MediumGrayText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = email.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = OnBackgroundPurple,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "From: ${email.sender.split("<")[0].trim()}",
                                        fontSize = 10.sp,
                                        color = MediumGrayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { onArchive(email.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = archive,
                                            contentDescription = "Archive",
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDelete(email.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = delete_sweep,
                                            contentDescription = "Delete",
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SETTINGS VIEW (Tab 3 - Google OAuth Configuration Vault) ----------------
@Composable
fun SettingsView(
    isConnected: Boolean,
    userName: String,
    oauthEmailInput: String,
    oauthNameInput: String,
    themePreference: String,
    onThemeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onConnect: (String, String) -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                onConnect(account.displayName ?: "Unknown User", account.email ?: "")
            } catch (e: ApiException) {
                Log.e("SettingsView", "Google sign in failed", e)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("settings_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Authorization & API Vault",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            if (isConnected) {
                // Verified connected visual identity card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("oauth_connected_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = oauthEmailInput,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightRippleBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "OAuth Verification Safe",
                                tint = BrandPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Verified Google Vault Connection",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("disconnect_oauth_btn")
                        ) {
                            Text("Disconnect Account")
                        }
                    }
                }
            } else {
                // Not connected: Auth inputs
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("oauth_disconnect_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "CONNECT TO GMAIL VIA OAUTH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        
                        Text(
                            text = "Unlock smart categorization by linking your real Gmail inbox through Google OAuth.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                launcher.launch(googleSignInClient.signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("connect_oauth_btn")
                        ) {
                            Text("Sign in with Google")
                        }
                    }
                }
            }
        }

        // Feature: APP THEME Selection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("theme_selection_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "APP THEME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("light", "Light Mode", light_mode),
                            Triple("dark", "Dark Mode", dark_mode),
                            Triple("system", "System", Icons.Default.Settings)
                        ).forEach { (modeValue, modeLabel, modeIcon) ->
                            val isSelected = themePreference == modeValue
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrandPrimary else SurfaceBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onThemeChange(modeValue) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("theme_btn_$modeValue"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = modeLabel,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = modeLabel,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_about_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ABOUT GMAIL ASSISTANT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Gmail Assistant utilizes Gemini text models and Room Db configurations directly on Android. By default, it never reads or uploads raw body contents—keeping token context small, lightweight, fast, and completely secure. Your data never touches a third-party server. Only Google's own infrastructure is involved.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ---------------- HELPER COMPONENTS ----------------

@Composable
fun EmailRowItem(
    email: EmailEntity,
    onClick: () -> Unit,
    onToggleRead: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    showActionButtons: Boolean = true
) {
    val dateText = remember(email.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(email.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("email_row_item_${email.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (email.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (email.isRead) SurfaceBorder.copy(alpha = 0.5f) else BrandSecondary)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Meta Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(getCategoryColor(email.category).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = email.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = getCategoryColor(email.category)
                        )
                    }

                    if (email.isClutter) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE53935).copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "LOW ENGAGEMENT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                Text(
                    text = dateText,
                    fontSize = 11.sp,
                    color = MediumGrayText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subject / Sender
            Text(
                text = email.sender.split("<")[0].trim(),
                fontWeight = if (email.isRead) FontWeight.Medium else FontWeight.Bold,
                fontSize = 14.sp,
                color = OnBackgroundPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = email.subject,
                fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.Bold,
                fontSize = 13.sp,
                color = if (email.isRead) MediumGrayText else OnBackgroundPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Snippet
            Text(
                text = email.snippet,
                fontSize = 12.sp,
                color = MediumGrayText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            if (showActionButtons) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleRead,
                        modifier = Modifier.size(32.dp).testTag("action_toggle_read_${email.id}")
                    ) {
                        Icon(
                            imageVector = if (email.isRead) Icons.Default.Check else Icons.Default.Email,
                            contentDescription = "Toggle Read",
                            tint = BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        TextButton(
                            onClick = onMove,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Move", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(32.dp).testTag("action_archive_${email.id}")
                        ) {
                            Icon(
                                imageVector = archive,
                                contentDescription = "Archive thread",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("action_delete_${email.id}")
                        ) {
                            Icon(
                                imageVector = delete_sweep,
                                contentDescription = "Delete thread",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Bottom details view for thread summary on-demand
@Composable
fun CardDetailsBottomSheet(
    email: EmailEntity,
    isLoading: Boolean,
    onClose: () -> Unit,
    onActionSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("details_bottom_sheet")
            .padding(16.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = wand_shine,
                            contentDescription = "Gemini AI",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Smarter Thread Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackgroundPurple
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close panel", tint = MediumGrayText)
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(getCategoryColor(email.category).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = email.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = getCategoryColor(email.category)
                    )
                }
                Text(
                    text = email.sender.split("<")[0].trim(),
                    fontSize = 12.sp,
                    color = MediumGrayText,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = email.subject,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackgroundPurple
            )

            // Real Stateless summary presentation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurfaceBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GEMINI SUMMARY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            letterSpacing = 1.sp
                        )
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = BrandPrimary)
                        }
                    }
                    
                    if (isLoading) {
                        Text(
                            text = "Consulting Gemini AI to summarize thread header...",
                            fontSize = 12.sp,
                            color = MediumGrayText
                        )
                    } else {
                        Text(
                            text = email.summary ?: "Successfully generated. Summarizing metadata securely.",
                            fontSize = 13.sp,
                            color = OnBackgroundPurple,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Stateles client snippet disclaimer
            Column {
                Text(
                    text = "PARTIAL METADATA BODY SNIPPET",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediumGrayText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email.snippet,
                    fontSize = 12.sp,
                    color = MediumGrayText,
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Smart Next Actions Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "GEMINI RECOMMENDED ACTIONS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediumGrayText,
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val fallbackActions = listOf("Reply directly", "Snooze 1 Day", "Archive thread")
                    val actions = remember(email.suggestedActions) {
                        if (email.suggestedActions.isNullOrBlank()) fallbackActions else email.suggestedActions.split(",")
                    }
                    
                    actions.forEach { action ->
                        Button(
                            onClick = { onActionSelected(action) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recommended_action_${action.replace(" ", "_")}"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = action,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(cat: String): ImageVector {
    return when (cat) {
        "Work" -> work
        "Bill" -> currency_rupee
        "Newsletter" -> newspaper
        "Promotion" -> campaign
        "Social" -> groups
        else -> account_circle
    }
}

@Composable
private fun getCategoryColor(cat: String): Color {
    val isDark = MaterialTheme.colorScheme.primary == Color(0xFF8AB4F8)
    return when (cat) {
        "Work" -> if (isDark) Color(0xFF7DD3FC) else Color(0xFF4285F4)
        "Bill" -> if (isDark) Color(0xFFEE675C) else Color(0xFFEA4335)
        "Newsletter" -> if (isDark) Color(0xFFFDD663) else Color(0xFFFBBC05)
        "Promotion" -> if (isDark) Color(0xFF81C995) else Color(0xFF34A853)
        else -> if (isDark) Color(0xFF9AA0A6) else Color(0xFF5F6368)
    }
}
