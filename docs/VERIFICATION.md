# Verification results

Verified 12 October 2026 using Temurin JDK 21 and Android SDK 35. The app is fully offline; no network, Firebase emulators, or server services were used.

| Check | Count | Result |
| --- | --- | --- |
| com.fauzan.budget.core.FinanceTest | 8 | Passed |
| com.fauzan.budget.core.LedgerEngineTest | 13 | Passed |
| com.fauzan.ukmbudgetmanager.data.LedgerRepositoryTest | 8 | Passed |
| com.fauzan.ukmbudgetmanager.security.LockGateTest | 10 | Passed |
| Android debug APK build | 1 | Passed |
| Android lint (`:app:lintDebug`) | 0 errors | Passed |
| Merged manifest network permissions check | 0 `INTERNET` permissions | Passed |

Total automated test cases: 39. All passed.

- `LedgerEngineTest` verifies that all core accounting rules ported from the server (atomic posting, rollback on error, double-reversal prevention, correction chaining, event archivals, period restrictions) behave correctly without network/server dependencies.
- `LedgerRepositoryTest` verifies atomic transaction handling in Room, AES-GCM at-rest sealing via Keystore abstraction, rollback on receipt failure, and persistence across database reopens.
- `LockGateTest` verifies screen-lock evaluation, biometric/credential prompt handling, lock timeouts on backgrounding, and immediate lockout when a device has no screen lock set.

APK: `android-app/build/outputs/apk/debug/app-debug.apk`

- Application ID: `com.fauzan.ukmbudgetmanager`
- Version: `3.0.0` (code 3)
- Size: 6,403,027 bytes (reduced by ~50% after removing Firebase SDK dependencies)
- SHA-256: `C7DEDED9F2D24EC4693148FEAC75E36AC0196B4141F18116CF97F55134E53A47`

Verification command:

```text
./gradlew :budget-core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```
