# Renewal implementation status

This repository now contains a buildable Java Android/Firebase implementation. It is a development build, not a deployed or signed production release. The original Android/PHP sources remain available for source comparison and migration.

## Implemented in this change

| Plan area | Result |
| --- | --- |
| Java foundation | Gradle 8.11.1 / AGP 8.9.2, SDK 35, minimum API 26, new Android source folder, shared Java financial library |
| Firebase backend | Auth, Firestore and private Storage integration; server-only financial writes; Java Admin SDK processor with managed identity support |
| Local work | Room outbox, encrypted payloads/receipt files, retryable WorkManager sync, explicit rejection and account partitioning |
| Core workflows | Funding, contributions, events/expenses, assets, atomic financial corrections/reversals, event edits and archival |
| Reporting | Inclusive arbitrary date ranges, separate account opening/closing balances, in-app summaries/charts/details and offline complete snapshots; no spreadsheet export |
| Receipts | Private staging/upload, immutable object rules, encrypted downloaded receipt cache and bounded image loading |
| Migration | Reviewed JSON import contract, sample dry run, balance/reference validation, deterministic import IDs/checksums and atomic small-dataset import |
| Operations | Emulator setup/seed, trusted organization/member/period administration, Docker packaging and CI |

## Verification

The final verification results are recorded in `docs/VERIFICATION.md`. Tests include the original sample reconciliation, exact-once retry outcomes, concurrent spending, corrections, metadata conflicts, imported-ID protection, Rules authorization, local queue persistence and an authenticated HTTP flow. The Android app is also compiled and linted.

No physical device was connected during implementation, and the machine reported no Android Emulator hypervisor driver. Robolectric exercised Android login startup and Room behavior; real-device acceptance is explicitly outstanding.

## Release dependencies and bounded implementation choices

- The user has not yet confirmed a target Firebase project, region, live dataset or signing key. Development uses `demo-iptek`. No historical Firebase project, live API or SQL database was modified.
- Configure and deploy Firebase Rules/Storage/Firestore and the Java Cloud Run processor before a production client can work. Production builds reject missing configuration. App Check enforcement, IAM review, budget alerts and managed-service restore rehearsal remain staging/release tasks.
- The initial snapshot endpoint reads one consistent organization dataset capped at 5,000 records and rejects a larger dataset without replacing the local snapshot. This includes metadata records. It does not yet implement the proposed paginated range-download protocol. For this small organization baseline, any custom range is computed locally from the complete downloaded history.
- The importer validates up to 400 records plus periods in one atomic import. Larger live datasets require a reviewed staged importer. The supplied SQL snapshot is not a production backup. Real receipt paths, ownership, verified Auth accounts and discrepancies still need actual source data.
- Reports currently expose account and text filters, inflow/outflow charts and per-event details. Additional category selectors, trend charts and leadership-period shortcut choices are future UI refinements, not implemented controls.
- Localization is Indonesian-first; some strings remain in Java and produce lint translation warnings. Full string-resource extraction and device accessibility/visual review remain release polish.
- Account creation, membership and period lifecycle are trusted operator functions, not public mobile enrollment screens. The authenticated app supports password recovery.
- Asset inventory archival is distinct from financial cancellation. Corrections retain the original account/period. Moving an entry between accounts/closed periods needs a separately designed transfer/reopening policy.
- Pending queue entries survive logout and are not evicted. Deleting app storage/uninstalling can lose unsynced work. Device-to-device/cloud backup is excluded because ciphertext depends on the device Keystore.

## Next release gate

Confirm the Firebase project and real-data situation, deploy a staging environment, complete `DEVICE_ACCEPTANCE.md`, reconcile a production-copy migration, then rehearse backup/restore and arrange a signed release/cutover. These external steps are not represented as completed by this code change.
