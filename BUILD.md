# Building from source

This guide is for people who want to compile Gmail Assistant themselves. If you only want to use the app, see the [README](README.md).

## Requirements

- JDK 17
- Android SDK with platform 36 (installing Android Studio covers this)
- `ANDROID_HOME` and `JAVA_HOME` set in your shell
- An Android 7.0+ phone or an emulator

## Debug build

```bash
git clone https://github.com/vchhikara/Gmail-Assistant.git
cd Gmail-Assistant
./gradlew :app:installDebug
```

On Windows, use `gradlew.bat`. Open the app and tap **Settings, then Try demo** to use sample data with no Google account.

## Connect your own Gmail inbox

Reading Gmail needs an OAuth client that belongs to a Google Cloud project and is tied to the app's package name and signing key. You create it once:

1. In [Google Cloud Console](https://console.cloud.google.com/), create a project and enable the **Gmail API**.
2. Set up the **OAuth consent screen** and add your Google account as a **test user**.
3. Create an **OAuth client ID** of type **Android** with:
   - package name `com.gmailassistant`
   - the SHA-1 of the key that signs your build. For a debug build, run `./gradlew signingReport`.
4. Install the app and tap **Connect Google account**.

While the consent screen is in testing mode, up to 100 test users can sign in. A release build is signed with a different key, so it needs its own Android OAuth client with that key's SHA-1.

## Release build

R8 shrinking and resource shrinking are enabled for release. Signing reads its values from environment variables or from a `keystore.properties` file in the repo root (git-ignored):

| Name | Meaning | Default |
|---|---|---|
| `STORE_PASSWORD` | Keystore password | none, required |
| `KEYSTORE_PATH` | Path to the keystore | `my-upload-key.jks` in the repo root |
| `KEY_ALIAS` | Key alias | `upload` |
| `KEY_PASSWORD` | Key password | same as `STORE_PASSWORD` |

Create a keystore once with the JDK's `keytool`:

```bash
keytool -genkeypair -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Create `keystore.properties`:

```properties
STORE_PASSWORD=your-password
```

Build the signed APK:

```bash
./gradlew :app:assembleRelease
```

The APK is written to `app/build/outputs/apk/release/`. Back up the keystore and its password. Both are git-ignored and must stay out of the repo.

R8 keep and `dontwarn` rules live in `app/proguard-rules.pro`.

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

## Tech stack

| Area | Choice |
|---|---|
| Language and UI | Kotlin, Jetpack Compose, Material 3 |
| Storage | Room (schema export on, tested migrations) |
| Background work | WorkManager |
| Network | Retrofit, OkHttp, Moshi |
| Auth | Google Identity `AuthorizationClient` |
| On-device AI | ML Kit GenAI (Gemini Nano) |
| Tests | JUnit, Robolectric, Roborazzi |
| SDK levels | `minSdk` 24, `targetSdk` 36 |

## Project layout

```text
app/src/main/java/com/example/
├── auth/       Google sign-in and Gmail authorization
├── gmail/      Retrofit API, models and mapping
├── sync/       SyncEngine: full and incremental sync
├── classify/   Rule-based classifier
├── summary/    Gemini Nano and rule-based summarizers
├── outbox/     Queued write-back to Gmail
├── worker/     WorkManager sync and outbox workers
├── data/       Room database, entities, repository
├── demo/       Sample data for demo mode
└── ui/         Compose screens, theme, components
```

The Kotlin package is `com.example`. The application id is `com.gmailassistant`.

## Troubleshooting

- **SDK location not found.** Set `ANDROID_HOME` (on Windows, `%LOCALAPPDATA%\Android\Sdk`) or create `local.properties` with `sdk.dir=...`.
- **Signing config is missing `storePassword`.** Add `keystore.properties` or export `STORE_PASSWORD` in the same shell that runs Gradle.
- **R8 reports missing classes.** Add the matching `dontwarn` rule to `app/proguard-rules.pro`.
