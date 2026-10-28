package com.example.ui

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EmailEntity
import com.example.ui.icons.*
import com.example.ui.theme.KitMotion
import com.example.ui.theme.KitShapes
import com.example.ui.theme.kit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Buckets = listOf(
    "Work" to "Work",
    "Bill" to "Bills",
    "Newsletter" to "News",
    "Promotion" to "Promo",
    "Social" to "Social",
)

private val MoveCategories = listOf("Work", "Bill", "Newsletter", "Promotion", "Social")

private data class NavItem(val label: String, val icon: ImageVector, val tag: String)

private val NavItems = listOf(
    NavItem("Inbox", SlimInbox, "nav_inbox_tab"),
    NavItem("Buckets", SlimBuckets, "nav_buckets_tab"),
    NavItem("Cleanup", SlimCleanup, "nav_cleanup_tab"),
    NavItem("Settings", SlimSettings, "nav_settings_tab"),
)

@Composable
fun SmartGmailHome(
    viewModel: SmartGmailViewModel,
    modifier: Modifier = Modifier,
) {
    val emails by viewModel.allEmails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeCategoryTab by viewModel.activeCategoryTab.collectAsState()
    val selectedEmail by viewModel.selectedEmail.collectAsState()
    val isSummaryLoading by viewModel.isSummaryLoading.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isDemo by viewModel.isDemo.collectAsState()
    val pendingConsent by viewModel.pendingConsent.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()

    var moveTarget by remember { mutableStateOf<EmailEntity?>(null) }
    var lastSynced by remember { mutableStateOf<Long?>(null) }
    var wasLoading by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (wasLoading && !isLoading) lastSynced = System.currentTimeMillis()
        wasLoading = isLoading
    }

    val syncFailed = syncResult?.let { it.startsWith("Failed") || it.startsWith("Auth") } == true
    val synced = lastSynced
    val syncLine = when {
        isLoading -> "Syncing..."
        syncFailed -> "Sync failed. Tap Sync to retry."
        synced != null -> "Synced " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(synced))
        else -> "Ready"
    }

    Box(modifier.fillMaxSize().background(kit.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Header(syncLine, isLoading) { viewModel.syncInbox() }

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn(KitMotion.slide()) togetherWith fadeOut(tween(150)) },
                label = "hero",
            ) { tab ->
                when (tab) {
                    0 -> {
                        val bills = emails.count { it.category == "Bill" && !it.isRead }
                        val senders = emails.count { it.isUnsubscribeSuggested }
                        Hero(
                            title = "${emails.size} emails sorted",
                            body = "${plural(bills, "bill")} waiting. Cleanup found ${plural(senders, "sender")} you rarely open.",
                            pill = if (isConnected) "Connected" else if (isDemo) "Demo data" else "Not connected",
                        )
                    }
                    1 -> Hero("Buckets", "Sort mail by category and clear a whole bucket in one tap.")
                    2 -> Hero("Cleanup", "Unsubscribe from senders you rarely open and archive the clutter.")
                    else -> Spacer(Modifier.height(4.dp))
                }
            }

            if (currentTab == 1) {
                BucketControls(
                    emails = emails,
                    activeCategory = activeCategoryTab,
                    onCategorySelect = { viewModel.setActiveCategoryTab(it) },
                    onBulkAction = { category, ids, action -> viewModel.performBulkAction(category, ids, action) },
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(KitShapes.panel)
                    .background(kit.panel),
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn(KitMotion.slide()) togetherWith fadeOut(tween(150)) },
                    label = "panel",
                ) { tab ->
                    when (tab) {
                        0 -> InboxView(
                            emails = emails,
                            onEmailClick = { viewModel.selectEmail(it) },
                            onToggleRead = { viewModel.toggleReadStatus(it.id, it.isRead) },
                            onArchive = { viewModel.archiveEmail(it.id) },
                            onDelete = { viewModel.deleteEmail(it.id) },
                            onMove = { moveTarget = it },
                        )
                        1 -> BucketsView(
                            emails = emails,
                            activeCategory = activeCategoryTab,
                            onEmailClick = { viewModel.selectEmail(it) },
                            onToggleRead = { viewModel.toggleReadStatus(it.id, it.isRead) },
                            onArchive = { viewModel.archiveEmail(it.id) },
                            onDelete = { viewModel.deleteEmail(it.id) },
                            onMove = { moveTarget = it },
                        )
                        2 -> CleanupView(
                            emails = emails,
                            onArchive = { viewModel.archiveEmail(it.id) },
                            onDelete = { viewModel.deleteEmail(it.id) },
                            onBulkUnsubscribe = { ids -> viewModel.performBulkAction("Newsletter", ids, "delete") },
                        )
                        else -> SettingsView(
                            isConnected = isConnected,
                            isDemo = isDemo,
                            pendingConsent = pendingConsent,
                            onConsentResult = { viewModel.onConsentResult(it) },
                            onTryDemo = { viewModel.startDemo() },
                            onExitDemo = { viewModel.exitDemo() },
                            userName = userName,
                            userEmail = userEmail,
                            themePreference = themePreference,
                            onThemeChange = { viewModel.setThemePreference(it) },
                            onConnect = { viewModel.connect() },
                            onDisconnect = { viewModel.disconnectOAuth() },
                        )
                    }
                }
            }

            BottomNav(currentTab) { viewModel.setTab(it) }
        }

        DetailsSheet(
            email = selectedEmail,
            isLoading = isSummaryLoading,
            onClose = { viewModel.selectEmail(null) },
            onMove = { moveTarget = it },
            onActionSelected = { email, action ->
                val a = action.lowercase()
                when {
                    "delete" in a -> viewModel.deleteEmail(email.id)
                    "archive" in a -> viewModel.archiveEmail(email.id)
                    else -> {
                        viewModel.toggleReadStatus(email.id, email.isRead)
                        viewModel.selectEmail(null)
                    }
                }
            },
        )
    }

    moveTarget?.let { target ->
        MoveDialog(
            current = target.category,
            onPick = { viewModel.moveCategory(target.id, it); moveTarget = null },
            onDismiss = { moveTarget = null },
        )
    }
}

private fun plural(n: Int, word: String) = if (n == 1) "1 $word" else "$n ${word}s"

// ---------- Chrome ----------

@Composable
private fun Header(syncLine: String, isLoading: Boolean, onSync: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Gmail Assistant", color = kit.text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text(syncLine, color = kit.muted, fontSize = 12.sp, modifier = Modifier.testTag("sync_status"))
        }
        KitButton(
            text = "Sync",
            onClick = onSync,
            icon = SlimSync,
            spinIcon = isLoading,
            height = 40.dp,
            modifier = Modifier.testTag("fab_sync_reorganize"),
        )
    }
}

@Composable
private fun Hero(title: String, body: String, pill: String? = null) {
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                color = kit.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false).testTag("gemini_insight_header_text"),
            )
            if (pill != null) {
                Spacer(Modifier.width(10.dp))
                KitPill(pill, accent = true)
            }
        }
        Spacer(Modifier.height(6.dp))
        mutedText(body, Modifier.testTag("gemini_insight_message"))
    }
}

@Composable
private fun BottomNav(selected: Int, onSelect: (Int) -> Unit) {
    KitSegmented(
        count = NavItems.size,
        selected = selected,
        onSelect = onSelect,
        trayShape = KitShapes.nav,
        thumbShape = KitShapes.navThumb,
        cellHeight = 56.dp,
        tags = NavItems.map { it.tag },
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
    ) { i, isSelected ->
        val tint by animateColorAsState(
            if (isSelected) kit.onAccent else kit.text.copy(alpha = 0.7f),
            KitMotion.fill(),
            label = "navTint",
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(NavItems[i].icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(NavItems[i].label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------- Mail cards ----------

private fun senderName(email: EmailEntity) =
    email.sender.split("<")[0].trim().ifEmpty { email.senderEmail }

@Composable
private fun EmailCard(
    email: EmailEntity,
    onClick: () -> Unit,
    onToggleRead: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(if (email.isRead) kit.cardPlain else kit.card, KitMotion.fill(), label = "cardBg")
    Column(
        modifier = modifier
            .testTag("email_row_item_${email.id}")
            .kitClickable(scaleTo = 0.98f, onClick = onClick)
            .fillMaxWidth()
            .clip(KitShapes.card)
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                senderName(email),
                color = kit.text,
                fontSize = 14.sp,
                fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(formatDate(email.timestamp), color = kit.muted, fontSize = 11.sp)
        }
        Text(email.subject, color = kit.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            email.snippet,
            color = kit.muted,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KitPill(
                email.category,
                Modifier.clip(CircleShape).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMove,
                ).testTag("action_move_${email.id}"),
            )
            if (email.isClutter) KitPill("Low engagement")
            Spacer(Modifier.weight(1f))
            KitIconButton(
                if (email.isRead) SlimMarkUnread else SlimMarkRead,
                if (email.isRead) "Mark unread" else "Mark read",
                onToggleRead,
                Modifier.testTag("action_toggle_read_${email.id}"),
                size = 36.dp,
            )
            KitIconButton(SlimArchive, "Archive", onArchive, Modifier.testTag("action_archive_${email.id}"), size = 36.dp)
            KitIconButton(
                SlimDelete, "Delete", onDelete,
                Modifier.testTag("action_delete_${email.id}"), danger = true, size = 36.dp,
            )
        }
    }
}

private fun formatDate(ts: Long) = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))

private val ListPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)

@Composable
private fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        mutedText(text)
    }
}

@Composable
private fun InboxView(
    emails: List<EmailEntity>,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleRead: (EmailEntity) -> Unit,
    onArchive: (EmailEntity) -> Unit,
    onDelete: (EmailEntity) -> Unit,
    onMove: (EmailEntity) -> Unit,
) {
    if (emails.isEmpty()) {
        EmptyState("Your inbox is empty. Tap Sync to fetch mail.")
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().testTag("inbox_lazy_column"),
        contentPadding = ListPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(emails, key = { it.id }) { email ->
            EmailCard(
                email,
                onClick = { onEmailClick(email) },
                onToggleRead = { onToggleRead(email) },
                onArchive = { onArchive(email) },
                onDelete = { onDelete(email) },
                onMove = { onMove(email) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/** Bucket tabs and bulk actions. Lives above the grey panel so it never scrolls or clips the mail list. */
@Composable
private fun BucketControls(
    emails: List<EmailEntity>,
    activeCategory: String,
    onCategorySelect: (String) -> Unit,
    onBulkAction: (String, List<String>, String) -> Unit,
) {
    val ids = emails.filter { it.category == activeCategory }.map { it.id }
    val selectedIndex = Buckets.indexOfFirst { it.first == activeCategory }.coerceAtLeast(0)

    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KitSegmented(
            count = Buckets.size,
            selected = selectedIndex,
            onSelect = { onCategorySelect(Buckets[it].first) },
            tags = Buckets.map { "bucket_tab_${it.first}" },
        ) { i, isSelected ->
            SegmentLabel(Buckets[i].second, isSelected, emails.count { it.category == Buckets[i].first })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KitButton("Mark read", { onBulkAction(activeCategory, ids, "mark_as_read") },
                Modifier.weight(1f).testTag("bulk_read_btn"), enabled = ids.isNotEmpty(), height = 36.dp)
            KitButton("Archive", { onBulkAction(activeCategory, ids, "archive") },
                Modifier.weight(1f).testTag("bulk_archive_btn"), enabled = ids.isNotEmpty(), height = 36.dp)
            KitButton("Delete all", { onBulkAction(activeCategory, ids, "delete") },
                Modifier.weight(1f).testTag("bulk_delete_btn"), danger = true, enabled = ids.isNotEmpty(), height = 36.dp)
        }
    }
}

@Composable
private fun BucketsView(
    emails: List<EmailEntity>,
    activeCategory: String,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleRead: (EmailEntity) -> Unit,
    onArchive: (EmailEntity) -> Unit,
    onDelete: (EmailEntity) -> Unit,
    onMove: (EmailEntity) -> Unit,
) {
    val filtered = emails.filter { it.category == activeCategory }

    LazyColumn(
        Modifier.fillMaxSize().testTag("buckets_lazy_column"),
        contentPadding = ListPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (filtered.isEmpty()) {
            item { mutedText("Nothing in this bucket.", Modifier.padding(vertical = 24.dp, horizontal = 8.dp)) }
        }
        items(filtered, key = { it.id }) { email ->
            EmailCard(
                email,
                onClick = { onEmailClick(email) },
                onToggleRead = { onToggleRead(email) },
                onArchive = { onArchive(email) },
                onDelete = { onDelete(email) },
                onMove = { onMove(email) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

// ---------- Cleanup ----------

@Composable
private fun CleanupView(
    emails: List<EmailEntity>,
    onArchive: (EmailEntity) -> Unit,
    onDelete: (EmailEntity) -> Unit,
    onBulkUnsubscribe: (List<String>) -> Unit,
) {
    val unsubs = emails.filter { it.isUnsubscribeSuggested }
    val clutter = emails.filter { it.isClutter }
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    val chosen = unsubs.filter { checked[it.id] ?: true }

    LazyColumn(
        Modifier.fillMaxSize().testTag("cleanup_lazy_column"),
        contentPadding = ListPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.kitCard().padding(horizontal = 8.dp, vertical = 14.dp)) {
                CardTitle("Unsubscribe suggestions", unsubs.size)
                if (unsubs.isEmpty()) {
                    mutedText("No senders to unsubscribe from.", Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
                unsubs.forEach { email ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .kitClickable(scaleTo = 0.98f) { checked[email.id] = !(checked[email.id] ?: true) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        KitCheckbox(checked[email.id] ?: true, { checked[email.id] = it })
                        Column(Modifier.weight(1f)) {
                            Text(senderName(email), color = kit.text, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(email.subject, color = kit.muted, fontSize = 12.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (unsubs.isNotEmpty()) {
                    KitButton(
                        text = "Unsubscribe from ${chosen.size}",
                        onClick = { onBulkUnsubscribe(chosen.map { it.id }) },
                        enabled = chosen.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 8.dp)
                            .testTag("bulk_unsubscribe_btn"),
                    )
                }
            }
        }
        item {
            Column(Modifier.kitCard().padding(horizontal = 8.dp, vertical = 14.dp)) {
                CardTitle("Low-priority clutter", clutter.size)
                if (clutter.isEmpty()) {
                    mutedText("Nothing to clean up.", Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
                clutter.forEach { email ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(senderName(email), color = kit.text, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(email.subject, color = kit.muted, fontSize = 12.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        KitIconButton(SlimArchive, "Archive", { onArchive(email) },
                            Modifier.testTag("clutter_archive_${email.id}"), size = 36.dp)
                        KitIconButton(SlimDelete, "Delete", { onDelete(email) },
                            Modifier.testTag("clutter_delete_${email.id}"), danger = true, size = 36.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardTitle(title: String, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = kit.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        CountBadge(count, selected = false)
    }
}

// ---------- Settings ----------

@Composable
private fun SettingsView(
    isConnected: Boolean,
    isDemo: Boolean,
    pendingConsent: IntentSender?,
    onConsentResult: (Intent?) -> Unit,
    onTryDemo: () -> Unit,
    onExitDemo: () -> Unit,
    userName: String,
    userEmail: String,
    themePreference: String,
    onThemeChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        onConsentResult(if (result.resultCode == Activity.RESULT_OK) result.data else null)
    }
    LaunchedEffect(pendingConsent) {
        pendingConsent?.let { launcher.launch(IntentSenderRequest.Builder(it).build()) }
    }

    LazyColumn(
        Modifier.fillMaxSize().testTag("settings_view"),
        contentPadding = ListPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (isConnected) {
                Column(
                    Modifier.kitCard().padding(16.dp).testTag("oauth_connected_card"),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Avatar(userName)
                        Column(Modifier.weight(1f)) {
                            Text(userName, color = kit.text, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(userEmail, color = kit.muted, fontSize = 12.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        KitPill("Connected", accent = true)
                    }
                    Box(Modifier.testTag("oauth_disconnect_card")) {
                        KitButton("Disconnect", onDisconnect, Modifier.fillMaxWidth().testTag("disconnect_oauth_btn"),
                            danger = true)
                    }
                }
            } else {
                Column(Modifier.kitCard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Avatar(userName)
                        Column(Modifier.weight(1f)) {
                            Text("Not connected", color = kit.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            mutedText("Sign in with Google to sort your inbox. Archive and delete update Gmail.", size = 12)
                        }
                    }
                    KitButton(
                        "Connect Google account",
                        onConnect,
                        Modifier.fillMaxWidth().testTag("connect_oauth_btn"),
                        solid = true,
                    )
                    if (isDemo) {
                        KitButton("Exit demo", onExitDemo, Modifier.fillMaxWidth().testTag("exit_demo_btn"))
                    } else {
                        KitButton("Try demo", onTryDemo, Modifier.fillMaxWidth().testTag("try_demo_btn"))
                    }
                }
            }
        }
        item {
            val values = listOf("light" to "Light", "system" to "System", "dark" to "Dark")
            Column(
                Modifier.kitCard().padding(16.dp).testTag("theme_selection_card"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Theme", color = kit.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                KitSegmented(
                    count = values.size,
                    selected = values.indexOfFirst { it.first == themePreference }.coerceAtLeast(0),
                    onSelect = { onThemeChange(values[it].first) },
                    tags = values.map { "theme_btn_${it.first}" },
                ) { i, isSelected -> SegmentLabel(values[i].second, isSelected) }
            }
        }
        item {
            Column(
                Modifier.kitCard().padding(16.dp).testTag("settings_about_card"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("About", color = kit.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                mutedText("Gmail Assistant sorts your inbox into buckets and suggests what to clean up. Everything runs on your phone. It never sends mail on your behalf.")
            }
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(kit.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            color = kit.onAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ---------- Sheet and dialog ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSheet(
    email: EmailEntity?,
    isLoading: Boolean,
    onClose: () -> Unit,
    onMove: (EmailEntity) -> Unit,
    onActionSelected: (EmailEntity, String) -> Unit,
) {
    var shown by remember { mutableStateOf<EmailEntity?>(null) }
    LaunchedEffect(email) { if (email != null) shown = email }
    val visible = email != null
    val content = email ?: shown

    BackHandler(enabled = visible) { onClose() }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(KitMotion.slide()), exit = fadeOut(KitMotion.slide())) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() },
            )
        }
        AnimatedVisibility(
            visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(KitMotion.slide()) { it },
            exit = slideOutVertically(KitMotion.slide()) { it },
        ) {
            if (content != null) {
                Column(
                    Modifier
                        .testTag("details_bottom_sheet")
                        .fillMaxWidth()
                        .clip(KitShapes.sheet)
                        .background(kit.panel)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .navigationBarsPadding()
                        .heightIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KitPill(
                            content.category,
                            Modifier.clip(CircleShape).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onMove(content) },
                        )
                        Spacer(Modifier.weight(1f))
                        Text(formatDate(content.timestamp), color = kit.muted, fontSize = 12.sp)
                    }
                    Text(content.subject, color = kit.text, fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp)
                    mutedText("From ${senderName(content)}", size = 12)

                    Column(Modifier.kitCard(kit.mist).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Summary", color = kit.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        val summary = when {
                            content.summary != null -> content.summary
                            isLoading -> "Summarizing this thread..."
                            else -> "No summary available."
                        }
                        Text(summary ?: "", color = kit.text, fontSize = 14.sp, lineHeight = 20.sp)
                    }

                    Text(content.snippet, color = kit.muted, fontSize = 14.sp, lineHeight = 21.sp)

                    val actions = content.suggestedActions
                        ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                        ?.takeIf { it.isNotEmpty() }
                        ?: listOf("Reply directly", "Snooze 1 Day", "Archive thread")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        actions.forEach { action ->
                            KitButton(
                                action,
                                { onActionSelected(content, action) },
                                Modifier.testTag("recommended_action_${action.replace(" ", "_")}"),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().clip(KitShapes.card).background(kit.panel).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Move to", color = kit.text, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp))
            MoveCategories.forEach { category ->
                KitButton(
                    category,
                    { onPick(category) },
                    Modifier.fillMaxWidth().testTag("move_to_$category"),
                    solid = category == current,
                )
            }
        }
    }
}
