# Contributing

Thanks for helping. This is a small Kotlin and Jetpack Compose Android app. Contributions that keep it small and readable are the easiest to review.

## Before you start

- Read [ARCHITECTURE.md](ARCHITECTURE.md) for the sync, classification and write-back design.
- Open an issue before starting a larger change so we can agree on the direction.
- Set up your environment with [BUILD.md](BUILD.md).

## Running the tests

```bash
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew :app:assembleDebug       # compile and link check
```

A change is done when both pass. If you change the Room schema, add a migration and a migration test.

## Verification

- Reproduce a bug first with a failing test or a clear set of steps, then fix it and show the same check passing.
- Describe UI changes honestly. "Compiles" and "tapped through on a device" are different claims, so say which one you did.
- Behavior that talks to Gmail needs a real-account check or a clearly stated reason it was not possible.

## Commits and pull requests

- Write prose, imperative subject lines, for example `Pin Buckets tabs above the list`.
- Keep commits small and focused. Each one should build.
- Describe what changed, why, and how you tested it in the pull request.
- Fix unrelated problems you notice in a separate change.

## Code style

Match the surrounding code: naming, comment density and structure. Prefer extending an existing pattern over introducing a new one.

## Privacy and security

The app handles mail metadata and holds the `gmail.modify` permission. Changes to sign-in, token handling, the outbox, backup rules or network calls should say in the pull request what was checked. Never commit keystores, passwords, OAuth client secrets or `google-services.json`. See [SECURITY.md](SECURITY.md).

## Conduct

Everyone taking part follows the [Code of Conduct](CODE_OF_CONDUCT.md).
