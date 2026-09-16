# IPTEK Budget Manager

Java Android renewal with Firebase Authentication, Cloud Firestore, private receipt storage, encrypted offline work, and in-app date-range reports. The old Android sources in `app/` and PHP backend in `API/` are retained for migration reference; Gradle `:app` now builds `android-app/`.

## Implemented

- One Firebase login, password recovery and server-managed Bendahara/Ketua access.
- Pagudana, kas contributions, events, event expenses and asset purchases.
- Auditable reversal/replacement corrections; event rename/archive and asset archive without a cash refund.
- Room outbox and complete report snapshot, with AES-GCM payload/receipt encryption using Android Keystore.
- WorkManager reconnect/retry handling, immutable operation UUIDs, server-side validation and atomic Firestore accounting.
- Pending, syncing, rejected and acknowledged states; old-account snapshots/queues are never rendered for a new account.
- Reports with inclusive custom dates, per-account opening/inflow/outflow/closing balances, charts, search and receipt details. No Excel/CSV export.
- Encrypted receipt cache for offline viewing (downloaded images); explicit last-sync labels.
- Firebase Rules, emulator tests, a Java processor, migration dry run and CI.

## Prerequisites

JDK 21, Android SDK platform 35 / build tools 35.0.0, and Node 22 LTS. Set `JAVA_HOME` and `sdk.dir` in `local.properties`. The workspace's ignored `.tools/` installation was used for verification; it is not shipped or required if your environment is already configured.

```
npm ci
./gradlew :app:assembleEmulatorDebug :app:lintEmulatorDebug :app:testEmulatorDebugUnitTest :budget-core:test
```

On Windows use `gradlew.bat`. The debug APK is `android-app/build/outputs/apk/emulator/debug/app-emulator-debug.apk` and installs as `com.fauzan.ukmbudgetmanager.dev`, alongside the old app. The Java library sources are in `budget-core/`; custom trusted Firebase processing is in `processor/`.

## Run locally on Windows

Open separate terminals in the repository:

```powershell
# Terminal 1: start Firebase services (all data is disposable local test data)
./scripts/dev.ps1 emulators

# Terminal 2: create demo users, organization, accounts and an open current-year period
./scripts/dev.ps1 seed
./scripts/dev.ps1 processor

# Terminal 3: build the Android app
./scripts/dev.ps1 build
```

Local ports: Auth 9099, Firestore 8180, Storage 9199, Java processor 8081, Firebase UI 4000. Firestore uses 8180 because another service already occupied 8080 in the inspected environment. All development commands explicitly target `demo-iptek`; the historical `app/google-services.json` is not loaded.

Use an Android emulator with the default host `10.0.2.2`. For a USB Android device, copy `firebase.local.properties.example` to `firebase.local.properties`, set `emulatorHost=127.0.0.1` and `emulatorProcessorUrl=http://127.0.0.1:8081`, rebuild, and run:

```powershell
adb reverse tcp:9099 tcp:9099
adb reverse tcp:8180 tcp:8180
adb reverse tcp:9199 tcp:9199
adb reverse tcp:8081 tcp:8081
adb install -r android-app/build/outputs/apk/emulator/debug/app-emulator-debug.apk
```

Demo accounts: `bendahara@iptek.test` and `ketua@iptek.test`, password `LocalDemo123!`. These are emulator-only fixtures, never production credentials. New installations need connectivity for first login and membership/data download. After that, cached records and saved entries are available offline.

## Verify

```powershell
./scripts/dev.ps1 test
```

This starts disposable Firebase emulators and runs Rules tests, shared Java accounting/report tests, Java transaction/migration tests, and an HTTP smoke flow that authenticates demo users, verifies authorization, submits/retries a contribution, and reads its snapshot as Ketua. Expected permission-denied messages are part of negative tests. The processor smoke server is stopped afterward.

`./gradlew :app:testEmulatorDebugUnitTest` runs Robolectric tests for local outbox durability, atomic acknowledgement, user isolation and Android login startup. These are not a substitute for hardware testing. See `docs/IMPLEMENTATION_STATUS.md` and `docs/DEVICE_ACCEPTANCE.md` for remaining release gates.

## Production configuration

Production is intentionally not connected to the historical Firebase project. Confirm the target project and existing data before deployment.

1. Create/confirm a Firebase project, Firestore database, Storage bucket and Email/Password Authentication. Choose location and billing settings with the project owner.
2. Register Android package `com.fauzan.ukmbudgetmanager`. Fill `firebase.local.properties` from that Android registration: `projectId`, `appId`, `apiKey`, `storageBucket`, `processorUrl` and `organization`. The client configuration is not an Admin credential.
3. Build the Java service with `./gradlew :processor:installDist`, then build a container using `processor/Dockerfile` from the repository root. Deploy to Cloud Run with a dedicated managed service identity, `GOOGLE_CLOUD_PROJECT`, `STORAGE_BUCKET` and `PORT`. Do not set any emulator environment variables in production. The HTTPS endpoint must be reachable by the mobile client; application requests are authenticated with Firebase ID tokens and rechecked against membership.
4. Review the least-privilege IAM permissions for the Admin service and Storage Rules' Firestore access. Deploy the reviewed Rules with `npx firebase deploy --only firestore:rules,firestore:indexes,storage --project YOUR_CONFIRMED_PROJECT`.
5. Create Firebase Auth users through trusted administration, then provision the organization/members/periods with the Admin tool using Application Default Credentials. Do not embed service-account files in the app.
6. Build `:app:assembleProductionRelease` with completed configuration and configure release signing with the owner's key outside source control. Never substitute the emulator debug key for an existing app's signing key. Check current distribution requirements before publication.

Trusted operator examples (with production credentials configured separately; these commands write data):

```
./gradlew :processor:admin -PadminArgs="organization iptek Asia/Jakarta"
./gradlew :processor:admin -PadminArgs="membership iptek FIREBASE_UID BENDAHARA"
./gradlew :processor:admin -PadminArgs="period iptek 2026 2026-01-01 2026-12-31 OPEN"
```

Changing a period status to CLOSED prevents subsequent financial posting, including queued offline submissions. Account recovery uses verified Firebase email addresses; legacy username-like email values must be reviewed.

## Migration

Back up the actual SQL database and receipt files first. `migration/sample.json` is a normalized fixture from the committed 2020 sample, not a live database backup. Produce and review a normalized JSON extract using that schema. Preserve legacy IDs, business dates and unknown actors; list real local receipt files in `receiptFiles`. Missing proof must remain an explicit migration discrepancy.

```
./gradlew :processor:migrate -PmigrationArgs="migration/sample.json iptek"
```

This validates references, dates, integer amounts and account reconciliation without contacting Firebase. To import a reviewed extract, use the same command with `--apply` inside the argument value under the intended credentials. The importer requires an empty financial target, uses deterministic IDs/checksums, and recognizes completed imports. Receipt uploads are staged before the atomic Firestore import; retrying identical staged files is safe. Preserve staged-file evidence if an import fails.

The initial importer accepts at most 400 records plus periods in one atomic import. Larger datasets need the staged migration work described in the implementation status. Never apply the sample as real organization data. Live cutover, Auth onboarding, reconciliation approval, backup/restore and rollback remain operator-controlled release steps.

## Current boundaries

The initial report download is a complete, transactionally consistent organization snapshot capped at 5,000 records (including audit metadata records). It refuses larger responses instead of showing partial totals. Date filtering thereafter is local and includes all downloaded history and opening balances. A paginated, revision-stable range downloader is required before exceeding that limit.

Reports and balances are as of the last completed sync. Pending submissions can be rejected after reconnect if funds, roles, periods or target versions changed. Unsynced entries are stored on that device; uninstalling/clearing app storage can lose them. Logout retains protected pending work for the originating account.

Device acceptance, live Firebase/Cloud Run deployment, real-data migration, release signing, cost/retention settings, and production backup/restore are not claimed complete. No live project has been modified.
