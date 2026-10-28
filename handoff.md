# Handoff: release preparation

Date: 2026-10-28. Read this file first, then PROGRESS.md and ARCHITECTURE.md.

## Session setup
- Model: Sonnet 5.5. Effort: medium.
- Use Opus 5.5 only if a task fails twice with an unclear cause.

## State
- Branch: main. Last commit: b24c40b.
- Not committed: `SmartGmailHome.kt` (Buckets layout fix) and `PROGRESS.md`. Commit these first.
- Buckets fix is done. `BucketControls` (tabs and 3 buttons, 36dp) sits above the grey panel. `BucketsView` is a plain list.
- Verified on a Samsung SM-S931B (adb id RZGL322202Z) with demo data.
- Not verified: light mode, large font sizes, real Gmail data.
- Debug build and unit tests pass.

## Environment (set in each new shell)
```powershell
$j = Get-ChildItem "C:\Program Files\Microsoft\jdk-17*" -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME = $j
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:Path = "$j\bin;$env:Path"
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --console=plain
adb -s RZGL322202Z install -r app\build\outputs\apk\debug\app-debug.apk
```
- `.env.example` must exist (git-ignored). Content: `GEMINI_API_KEY=placeholder`.
- adb is on PATH. No emulator image was checked.

## Next task: release blockers (from mobile-check)
1. Change `applicationId` from `com.example`. Play does not accept it (not verified against current Play rules). Ask the user for the new id. Update the OAuth client package name to match.
2. Fix backup. `backup_rules.xml` and `data_extraction_rules.xml` are empty templates. Exclude the Room database and shared prefs.
3. Enable R8 for release in `app/build.gradle.kts`. Test the release build. Add keep rules for Moshi and Room if needed.
4. Raise `versionCode` per upload. Set a real `versionName`.
5. Remove `debug.keystore.base64` from git (debug key, low risk).
6. Release signing: the user creates the keystore with `keytool`. Do not generate or handle the real key. Gradle reads `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` from the environment.
7. Confirm the current Play target API requirement. `targetSdk` is 36.

## Needs the user
- Google Cloud: OAuth client (Android, final package name, SHA-1), Gmail API enabled, consent screen with a test user.
- `gmail.modify` is a restricted scope. A public release needs Google verification and a privacy policy URL.
- Play Console: privacy policy URL, Data safety form, screenshots, reviewer test account.
- Do not upload, tag or submit anything without a request from the user.

## Known cleanup
- README.md still describes the old Gemini pipeline and `.env` key. Rewrite it.
- `play.services.auth` and `firebase.bom` in `app/build.gradle.kts` may be unused. Check.
- `SmartGmailApp.isSignedIn()` reads the same prefs flag as the ViewModel.
- The phone's old install had another signing key. It was uninstalled once. Debug builds from this PC now update in place.
