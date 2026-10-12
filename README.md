# IPTEK Budget Manager

Offline Android budget manager for the IPTEK student unit. **All data stays on the user's phone.** There is no server, no account and no login. The app is protected only by the phone's own screen lock (PIN, pattern, password or biometric). The app has no `INTERNET` permission, so it cannot send data anywhere.

> The old Android sources in `app/` and the PHP backend in `API/` are kept untouched as historical reference. Gradle `:app` builds `android-app/`.

## What it does

- Pagudana and kas accounts kept separate, with contributions, events, event expenses and asset purchases.
- Auditable corrections: a correction or cancellation never edits history. It adds a reversal (and a replacement) and keeps every original record.
- Event rename/archive and asset archive (archiving an asset does not refund cash).
- Accounting periods are created automatically per calendar year. A period can be closed or reopened from the toolbar menu "Periode pembukuan"; closed periods reject new postings.
- Receipt photos (JPEG/PNG/WebP, up to 5 MB), stored encrypted on the device.
- Reports for any custom date range, with opening, inflow, outflow and closing balance per account, a chart, search and receipt viewing. No Excel/CSV export.
- Every posting is validated and saved in one database transaction. A rejected posting changes nothing and shows its reason immediately.

## Security model

| Topic | Behaviour |
| --- | --- |
| Access | The app opens only after the device screen lock is confirmed (`BiometricPrompt`, biometric or device credential). |
| No screen lock set | The app stays blocked and offers to open Security settings. Data is never shown without a lock. |
| Re-lock | On cold start, after more than 60 s in the background, when the screen lock is removed, and via the toolbar action "Kunci sekarang". |
| Screenshots | Blocked, and the recents thumbnail is blank (`FLAG_SECURE`, also applied to dialogs). |
| At rest | Ledger rows and receipts are encrypted with an AES-GCM key held in the Android Keystore. The key is **not** bound to the screen lock, so changing or removing the lock never destroys your data. |
| Network | None. No Firebase, no analytics, no backup (`allowBackup=false`, device-transfer excluded). |

Limits to be aware of:

- **Uninstalling the app, clearing its storage or a factory reset permanently deletes all data.** There is no backup or sync.
- Anyone who knows the phone's screen lock can open the app. On a rooted device the gate can be bypassed, although stored data remains encrypted.

## Build

Prerequisites: JDK 17 or newer (CI uses 21) and Android SDK platform 35. Set `JAVA_HOME` and `sdk.dir` in `local.properties`.

```powershell
./gradlew :budget-core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On Windows use `gradlew.bat`. The debug APK is `android-app/build/outputs/apk/debug/app-debug.apk` (application ID `com.fauzan.ukmbudgetmanager`). In Android Studio, open the repository root as a Gradle project and run the `app` module.

Release builds need a signing key kept outside source control. Configure signing locally; never commit keystores.

## Project layout

- `budget-core/` is pure Java: commands, validation, `LedgerEngine` (all accounting rules), `LedgerState` and `Report`. It has no Android or I/O dependencies and carries most unit tests.
- `android-app/` is the Java/XML app: `data/` (Room database, `LedgerRepository`, encrypted `ReceiptStore`, `LocalVault`), `security/` (`LockState`, `LockController`, `ScreenLock`) and `ui/`.

## Verify

```powershell
./gradlew :budget-core:test :app:testDebugUnitTest
```

Unit tests cover the accounting engine, atomic local posting with rollback, encrypted receipts, persistence across a database reopen and the lock logic. `BiometricPrompt` and the Android Keystore cannot run under Robolectric, so see `docs/DEVICE_ACCEPTANCE.md` for the on-device checklist. Results are in `docs/VERIFICATION.md`.

## First launch after updating from the old Firebase build

The Firebase/sync build was never released, so nothing is migrated. On first launch the app deletes its old local database, work queue and staged receipts.
