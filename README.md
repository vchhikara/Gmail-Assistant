# Gmail Assistant

Gmail Assistant is a lightweight, privacy-focused Android application designed to help you manage your inbox intelligently. Built with modern Android development practices and powered by **Google's Gemini Flash AI**, this app categorizes inbox metadata, suggests 1-tap cleanups, and generates on-demand summaries and action plans. 

Instead of constantly reading or uploading raw body contents, Gmail Assistant operates efficiently by analyzing email metadata. This keeps token context small, fast, and secure. Your data never touches a third-party server, only Google's own infrastructure is involved.

---

## Tech Stack & Programming Language

- **Primary Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Local Storage:** Room Database
- **Background Sync:** WorkManager
- **Networking:** Retrofit & OkHttp
- **AI Integration:** Gemini API

---

## Benefits
- **Everyday AI Assistant:** Acts as a quick, daily companion to rapidly sort through incoming mail.
- **Never Miss What's Important:** By automatically filtering out the noise, critical emails from work, family, or billing are brought to the forefront.
- **Reduced Inbox Anxiety:** Transforms an overwhelming inbox into an organized, actionable list.

---

## Features

- **Privacy-First Architecture:** It operates on email metadata. Requests full email bodies only when asked for an in-depth summary explicitly.
- **Background Syncing:** `WorkManager` syncs and processes your inbox in the background, local database stays updated.
- **Offline-First Capabilities:** Data is stored locally using **Room Database**, allowing seamless usage even with poor connectivity.
- **Lightweight and Blazing Fast:** Designed from the ground up to be incredibly snappy. AI-powered features are genuinely fast, all thanks to Gemini Flash.Reading from the local SQLite database is instantaneous compared to IMAP or REST APIs.
- **No Intermediary Servers:** Communicates directly between your Android device, Gmail REST API, and Gemini API.
- **Smart Categorization:** Uses Gemini AI to automatically classify your emails into logical categories like Work, Bill, Newsletter and much more.
- **On-Demand Summaries:** Generate concise summaries and actionable items for important emails or threads.
- **1-Tap Cleanups:** Automatically flags and declutters, offers suggestions to unsubscribe or archive emails.

---

## Limitations

- **Not a Large Dataset Cleaner:** Performance and sync capabilities are subject to your Gemini API and Google OAuth rate limits. The app fetches a limited number of recent emails rather than cloning your entire Gmail dataset. It is strictly meant to be a quick, everyday AI assistant for faster decision-making and keeping the mailbox clean, It is much more than a bulk historical decluttering tool.
- **Platform Lock-in:** Currently, it exclusively supports Gmail (via Google OAuth and the Gmail REST API).
- **Drafts & Outbox:** The current scope focuses on inbox management and organization, not composing or sending emails.

---

## Directory Structure

```text
gmail-assistant/
├── app/
│   ├── build.gradle.kts                # App-level build configurations
│   ├── proguard-rules.pro              # ProGuard/R8 obfuscation rules
│   └── src/
│       ├── test/                       # Unit tests (Robolectric, JUnit, Screenshot Tests)
│       └── main/
│           ├── AndroidManifest.xml     # Application manifest
│           ├── java/com/example/       # Main Kotlin source code
│           │   ├── data/               # Room DB Entities, Daos, Retrofit & Gemini API Clients
│           │   ├── ui/                 # Jetpack Compose UI
│           │   ├── worker/             # Background processing
│           │   ├── MainActivity.kt     # App entry point
│           │   └── SmartGmailApp.kt    # Application class for global state
│           └── res/                    # Android resources
│               ├── drawable/           # Vector assets, background/foreground layers
│               ├── mipmap-*/           # App launcher icons for various screen densities
│               ├── values/             # Colors, strings, themes
│               └── xml/                # Backup and data extraction rules
├── gradle/                             # Gradle Wrapper & version catalogs
│   ├── libs.versions.toml              # Dependency version management
│   └── wrapper/                        # Gradle wrapper files
├── assets/                             # Local assets and AI Studio configs
├── .env.example                        # Example environment variables (API Keys)
├── .gitignore                          # Git ignore rules
├── build.gradle.kts                    # Project-level build configurations
├── gradle.properties                   # Gradle properties
├── local.properties                    # Local SDK paths
├── metadata.json                       # Project metadata
├── package.json                        # NPM package configuration
├── settings.gradle.kts                 # Gradle project settings
└── README.md                           # This documentation file
```

---

## 🗄️ Database Structure

The app utilizes **Room Database** (`AppDatabase`) to cache emails locally for fast retrieval. The core of the database is the `EmailEntity` table (`emails`), which stores the following fields:

| Field | Type | Description |
|---|---|---|
| `id` | String (Primary Key) | Gmail API's unique message ID. |
| `threadId` | String | Groups related messages into conversations. |
| `sender` | String | The display name of the sender. |
| `senderEmail` | String | The exact email address of the sender. |
| `subject` | String | Email subject line. |
| `snippet` | String | A short preview snippet of the email body. |
| `timestamp` | Long | Epoch time the email was received. |
| `isRead` / `isArchived` | Boolean | Local sync state of the email. |
| `category` | String | AI-assigned category (e.g., *Work, Newsletter, Promotion*). |
| `isUnsubscribeSuggested` | Boolean | Flag indicating if this is a good candidate to unsubscribe. |
| `isClutter` | Boolean | Flag indicating if this is low-priority noise. |
| `summary` | String (Nullable) | AI-generated summary of the email content. |
| `suggestedActions` | String (Nullable) | AI-generated actionable steps from the email. |

---

## Installation & Usage

### Prerequisites

Before you begin, ensure you have the following installed and configured:
1. **[Android Studio](https://developer.android.com/studio)** (Latest stable version recommended).
2. **JDK 17** (Usually bundled with modern Android Studio).
3. **A Gemini API Key:** Get a free API key from [Google AI Studio](https://aistudio.google.com/).
4. **Google Cloud Console (Optional):** If you want to connect to a real Gmail inbox, you will need to set up an **OAuth 2.0 Client ID for Android**. 

---

### Step-by-Step Setup

#### 1. Clone the Repository
Open your terminal and clone the project to your local machine:
```bash
git clone https://github.com/yourusername/gmail-assistant.git
cd gmail-assistant
```

#### 2. Open in Android Studio
- Launch **Android Studio**.
- Click **Open** (or **File > Open**) and select the `gmail-assistant` folder.
- Wait for Android Studio to index the project and for **Gradle** to finish syncing. *(This may take a few minutes the first time to download dependencies).*

#### 3. Configure the Gemini API Key
This app uses the Secrets Gradle Plugin to keep your API keys safe from source control.
1. In the **root directory** of the project, duplicate the `.env.example` file and rename it to `.env`.
2. Open the newly created `.env` file.
3. Replace the placeholder with your actual key:
   ```env
   GEMINI_API_KEY=your_actual_api_key_here
   ```
   *Note: Do not wrap the key in quotes. The `.env` file is already added to `.gitignore` to prevent accidental uploads.*

#### 4. Configure Google OAuth (For Real Gmail Access)
If you only want to test the UI with mock data, you can skip this step!
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project and navigate to **APIs & Services > OAuth consent screen**. Configure it as an "External" or "Internal" app.
3. Go to **Credentials > Create Credentials > OAuth client ID**.
4. Select **Android** as the application type.
5. Provide your app's package name (`com.example` - or your custom package name if you changed it) and your debug SHA-1 signing certificate fingerprint. *(You can get your SHA-1 key by running the `signingReport` Gradle task in Android Studio).*
6. Save the credentials, and ensure you also enable the **Gmail API** in your Google Cloud Library.

#### 5. Build and Run the App
- Connect a physical Android device via USB/Wi-Fi, or start an **Android Emulator** from the Device Manager.
- Click the **Run 'app'** button in the top toolbar (or press `Shift + F10`).
- The app will compile and launch on your device.

#### 6. Using the App
- **Exploration Mode:** On the first launch, the app populates with **Simulated Mock Data** so you can test the UI, swiping, and categorization without needing to log in.
- **Live Mode:** Tap **"CONNECT TO GMAIL VIA OAUTH"** to log in securely with your Google account. 
- Once authorized, the local `SyncWorker` will kick in to fetch the latest inbox metadata, and the Gemini AI engine will begin categorizing your emails automatically!

---

## Development Transparency

During the ideation and development of this project, various AI tools such as but not limited to Google AI Studio, were utilized to assist in generating code snippets and brainstorming. 

However, this does not make the project sloppy or completely AI-generated. **Everything from the UI/UX design to the backend architecture was meticulously reviewed, customized, and personalized by me.** I carefully molded the code to ensure it perfectly fits my vision for a fast, reliable, and user-centric Android application.