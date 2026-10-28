# Progress

## UI makeover (RQuickShare design kit)

Status: implemented, `:app:assembleDebug` passes. Not yet run on a device or emulator.

Spec: `mockups/index.html` (approved). Kit: `~/Applications/design-kit`.

### Done
- `ui/theme/Color.kt`: kit tokens (`KitColors`, light and dark), `LocalKit`.
- `ui/theme/Motion.kt`: kit timings (press 150, fill 250, slide 300, spring) and shapes.
- `ui/theme/Theme.kt`, `Type.kt`: Material scheme mapped from the kit, typography.
- `ui/KitComponents.kt`: button, icon button, pill, count badge, sliding segmented tray, checkbox, press shrink.
- `ui/SmartGmailHome.kt`: rewritten. Header with Sync, hero blocks, white panel with 48dp corner, card lists, 4-tab sliding nav (Settings is a tab), Buckets toggle with count badges, Cleanup in two cards, custom bottom sheet with backdrop, move dialog.
- ViewModel, data layer and test tags are unchanged. New tags: `nav_settings_tab`, `bucket_tab_*`, `action_move_*`, `clutter_archive_*`, `clutter_delete_*`, `move_to_*`, `sync_status`.

- Palette: new light tokens (paper canvas, neutral panel, sage cards, mint mist), danger red muted (#B94E58 light, #ED9499 dark).
- Icons: all replaced with Material Symbols Outlined weight 200 in `ui/icons/SlimIcons.kt`; old thick icons removed.

### Open
- Font: Montserrat variable bundled (`res/font/montserrat_variable.ttf`); weights apply on API 26+.
- Run on a device and compare against the mockup in light and dark.
- Build needs a git-ignored `.env.example` (for example `GEMINI_API_KEY=placeholder`) and `ANDROID_HOME`.
- Not committed.

## Backend rewrite (code complete 2026-10-28)

Status: build steps 1 to 6 in `ARCHITECTURE.md` are implemented. `:app:assembleDebug` and all unit tests pass. Not run on a device. Not committed.

### Done
- Room v2 with a real migration, schema export, non-destructive merge, `sync_state` table.
- `classify/` rules, `summary/` rules plus Gemini Nano (ML Kit GenAI, used only when AVAILABLE, API 26+).
- `gmail/` Retrofit API, `sync/SyncEngine` (full and incremental via historyId), `outbox/` write-back with WorkManager retry.
- `auth/AuthRepository` (Identity AuthorizationClient, `gmail.modify`, token never stored).
- ViewModel and Settings rewired: Connect, Try demo / Exit demo (labeled, never a fallback), pill shows Connected, Demo data or Not connected.
- Removed: Gemini network code, API key, secrets plugin, `GET_ACCOUNTS`, legacy GoogleSignIn.
- Manifest uses `tools:overrideLibrary` for the genai libraries (minSdk 26 vs app 24); Nano is skipped below API 26.

### Needs the user
- Google Cloud: create an OAuth client (Android, package `com.example`, debug SHA-1), enable the Gmail API, set up the consent screen with your account as a test user.
- Run on a device: sign-in and consent flow, sync, archive and delete reaching Gmail, Nano summary on a supported phone.
- The `Greeting` screenshot test and UI test tags were not rerun on a device.
