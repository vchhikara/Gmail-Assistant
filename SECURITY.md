# Security

## What the app can access

Gmail Assistant asks Google for the `gmail.modify` permission. With it the app can read mail metadata and change labels on messages (archive, mark read) and move messages to trash. It does not send mail, and it does not download message bodies.

## Data handling

- Mail metadata (sender, subject, date, labels, snippet) is stored in the app's local database on your phone.
- The database and preferences are excluded from Android cloud backup and device transfer.
- Access tokens are requested from Google when needed and are never stored.
- The only network traffic goes to Google's Gmail API. There is no analytics, no third-party server and no cloud AI.
- Summaries run on the device, using Gemini Nano where the phone supports it and rules otherwise.
- Demo mode uses built-in sample data and makes no Gmail calls.

## What this does not protect against

- Someone who unlocks your phone and opens the app can see the cached mail metadata and act on your inbox. Use a screen lock.
- A rooted or compromised device can read the app's local database.
- You can revoke the app's access at any time in your Google Account under Security, Third-party access.

## Handling secrets in this repo

Keystores, passwords, OAuth client secrets and `google-services.json` are git-ignored and must never be committed. If you find a secret in the repo or its history, report it as described below.

## Reporting a vulnerability

This is a personal project without a dedicated disclosure program. If you find a vulnerability, open a GitHub issue describing it. If the details should not be public, such as a working exploit, contact the maintainer [@vchhikara](https://github.com/vchhikara) through their GitHub profile instead.

Please include the app version, Android version, and steps to reproduce.
