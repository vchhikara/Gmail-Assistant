# Handoff

Date: 2026-10-28. Start a fresh session. Read `ARCHITECTURE.md` first, then `PROGRESS.md`.

## Session settings
Sonnet 5.5, normal effort. Escalate to Opus 5.5 only after a task fails twice with an unclear cause.

## State
Backend rewrite is code complete (see `PROGRESS.md`). Build and unit tests pass. Remaining work needs the user: Google Cloud OAuth client setup and an on-device run. After that, fix whatever the device run finds, then commit.

## Build
```
export ANDROID_HOME=/home/vipul/Android/Sdk
sh ./gradlew -q :app:assembleDebug --console=plain
```
Run from the project root. Offline mode does not work. Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Notes
- `material-icons-extended` is not a dependency. New icons go in `ui/icons/SlimIcons.kt`.
- UI test tags must stay unchanged.
- If the launcher shows the old app icon, uninstall and reinstall.
