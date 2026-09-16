# Verification results

Verified 16 September 2026 using a local Temurin JDK 21, Android SDK 35 and Firebase emulators. No live Firebase project was accessed.

| Check | Count | Result |
| --- | --- | --- |
| com.fauzan.budget.core.FinanceTest | 8 | Passed |
| com.fauzan.budget.server.FinanceIntegrationTest | 9 | Passed |
| com.fauzan.budget.server.MigrationTest | 2 | Passed |
| com.fauzan.ukmbudgetmanager.data.OfflineDatabaseTest | 2 | Passed |
| com.fauzan.ukmbudgetmanager.ui.LoginScreenTest | 1 | Passed |
| Firebase Firestore/Storage security rules | 3 | Passed |
| Authenticated HTTP smoke flow | 1 flow | Passed |
| Android emulator-flavor debug APK build | 1 | Passed |
| Android lint | 0 errors, 19 warnings | Passed with warnings |
| Migration sample dry run | 5 records | Reconciled |

Total automated test cases: 25, plus the authenticated HTTP smoke flow. No tests in the listed final suites were skipped. The Rules tests intentionally exercise permission-denied paths.

The HTTP flow starts Firebase emulators, seeds demo identities, starts the Java processor on a test port, authenticates users through Firebase Auth, checks missing-token and viewer-write rejection, submits/retries a contribution, rejects fractional money, and retrieves its complete snapshot as Ketua. The temporary HTTP server and Firebase emulators stop after completion.

The Android checks include Room reopen durability, user-scoped queues, atomic snapshot/acknowledgement rollback and Robolectric login startup. They do not claim real-device execution, Android Keystore hardware verification, receipt-picker rendering or reboot scheduling acceptance.

APK: `android-app/build/outputs/apk/emulator/debug/app-emulator-debug.apk`

- Application ID: `com.fauzan.ukmbudgetmanager.dev`
- Version: `2.0.0` (code 2)
- Size: 12,655,833 bytes
- SHA-256: `4243335880a07c3ea388667e36517fc9244c6bf0e46b530975020caca9afee60`
- Requires the local Firebase/processor setup from README; it is not a production-connected APK.

The original sample dry run reconciles pagudana to Rp10,680,000 and kas to Rp100,000. Missing receipt proof remains a migration discrepancy, not a fabricated attachment.

Final commands:

```text
./gradlew :app:testEmulatorDebugUnitTest :app:assembleEmulatorDebug :app:lintEmulatorDebug
./gradlew :processor:migrate -PmigrationArgs="migration/sample.json iptek"
npx firebase emulators:exec --project demo-iptek --only auth,firestore,storage "node scripts/integration.mjs"
```

Lint warnings include pinned dependency update notices, Indonesian string localization, and adapter refresh efficiency. Production deployment, signing, live-data reconciliation, visual/accessibility device review and backup/restore rehearsal are tracked separately in `IMPLEMENTATION_STATUS.md` and `DEVICE_ACCEPTANCE.md`.
