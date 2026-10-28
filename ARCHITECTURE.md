# Architecture (v2 backend rewrite)

Decided with the user on 2026-10-28. This replaces the Gemini-API pipeline.

## Requirements
- The user signs in with Google and starts using the app. No keys, no setup, no extra steps.
- No Gemini API and no cloud AI. All intelligence runs on the device.
- Archive and delete change the real Gmail inbox (`gmail.modify` scope).
- Summaries use Gemini Nano (ML Kit GenAI) on supported phones. Other phones use rules.

## Fixed constraint
Reading Gmail needs an OAuth client in a Google Cloud project, tied to the package name and signing SHA-1. The developer creates it once. The end user never sees it. A public release needs Google's restricted-scope verification for `gmail.modify`. Personal and test use (up to 100 test users) does not.

## Pipeline

```
Sign-in (Credential Manager + Identity AuthorizationClient)
   -> AuthRepository (token provider, refresh, sign-out)
   -> SyncEngine (Gmail API, metadata only, incremental via historyId)
   -> Room (source of truth, user state preserved across syncs)
   -> Classifier (Gmail labels + headers first, then local rules)
   -> Summarizer (Gemini Nano if available, else rules)
   -> ViewModel -> Compose UI
Outbox: user actions -> Room (instant) -> Gmail modify (queued, retried by WorkManager)
```

## Components
| Component | Job |
|---|---|
| `auth/AuthRepository` | Google sign-in, Gmail authorization, access token on demand (never stored in prefs), sign-out. Handles the consent resolution intent. |
| `gmail/GmailApi` | Retrofit: `messages.list` (labelIds=INBOX), `messages.get?format=metadata&metadataHeaders=From,Subject,Date,List-Unsubscribe,List-Id,Precedence`, `history.list`, `messages.batchModify`, `messages.trash`. |
| `sync/SyncEngine` | First run: fetch latest N. After: `history.list` from stored `historyId`. Falls back to full fetch if the historyId expires (404). Merges into Room by id, never `REPLACE`. |
| `classify/Classifier` | Interface. `RuleClassifier` is the default: Gmail `CATEGORY_*` labels, `List-Unsubscribe`, `Precedence: bulk`, sender and subject patterns for bills. Returns category, clutter, unsubscribe suggestion. |
| `summary/Summarizer` | Interface. `NanoSummarizer` (ML Kit GenAI, availability checked at runtime) and `RuleSummarizer` (trimmed snippet plus extracted amount and due date). |
| `outbox/OutboxWorker` | Queued Gmail writes (archive, trash, mark read) so the UI stays instant and offline-safe. |
| `worker/SyncWorker` | Periodic real sync using the stored authorization. Never uses mock data. |
| `data/` | Room v2: adds `labelIds`, `isUnread` from labels, `pendingAction`, plus a `sync_state` table (historyId). Real migration, schema export on. |

## Rules for the rewrite
- Demo mode exists only when the user taps "Try demo". It is labeled in the UI. It is never a fallback.
- Failures surface as state (`SyncState.Error`), not as silent fake data.
- Local user state (read, archived, manual category) survives every sync.
- Remove: `GeminiNetwork`, `BuildConfig.GEMINI_API_KEY`, the secrets plugin, `GET_ACCOUNTS`, `GoogleAuthUtil`, the simulated OAuth in the ViewModel.

## Build order
1. Room v2 + non-destructive merge + `Classifier`/`Summarizer` interfaces with rule implementations (unit tests with fixtures).
2. `GmailApi` + `SyncEngine` with a fake API in tests.
3. `AuthRepository` and the sign-in screen; wire the real token.
4. Outbox + write-back.
5. `NanoSummarizer` (verify the ML Kit GenAI API and device support in the docs first).
6. Remove Gemini code and the key plumbing.

## Open questions to verify
- Exact ML Kit GenAI / AICore artifact and the supported device list.
- The current Credential Manager plus `AuthorizationClient` flow for Gmail scopes.
- Google Cloud setup steps for the developer (OAuth client, consent screen, test users).
