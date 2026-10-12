# Implementation status

## Done

- Firebase Authentication, Firestore, Cloud Storage, the Cloud Run processor, WorkManager sync, the outbox and the migration tooling are removed. The app has no `INTERNET` permission.
- All accounting rules that ran on the server now run on the device in `budget-core` (`LedgerEngine`), in one Room transaction (`LedgerRepository`).
- Room database `ledger.db`: records, one meta row (balances, periods, reversal index, revision) and an append-only audit table. Rows are sealed with the Android Keystore.
- Receipts are encrypted files in app-private storage. They are written only after every accounting rule has passed, atomically, and removed again if the transaction fails.
- Screen-lock gate: `BiometricPrompt` with `BIOMETRIC_WEAK | DEVICE_CREDENTIAL`, blocked when no screen lock exists, 60 s re-lock, manual lock, `FLAG_SECURE` on the activity and dialogs, and sensitive UI/draft/cache cleared on lock.
- Accounting periods are created per calendar year and can be closed or reopened.
- Roles (Bendahara/Ketua) are removed: the device owner has full access.

## Decisions

- The Keystore key is not bound to the screen lock, so changing the lock never loses data. The gate is enforced by the app.
- There is no backup, export or cloud sync. Uninstalling or clearing storage deletes all data.
- No migration from the old Firebase build, because it was never released. Old local files are deleted on first launch.
- The legacy `app/` and `API/` folders are untouched.

## Open items

- Complete `DEVICE_ACCEPTANCE.md` on real devices (lock prompt, Keystore, picker, rendering).
- Release signing and distribution are the owner's responsibility.
- A password-protected backup/restore file could be added later if device-only storage proves too risky. It is not implemented.
- The ledger is loaded whole into memory for lists and reports. This suits a student unit's volume (thousands of records). Much larger volumes would need paging.
