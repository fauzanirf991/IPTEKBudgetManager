# Device acceptance before production release

Run on API 26 and a current supported Android device, including one small-screen device. The app is Java/XML; JVM/Robolectric verification does not establish real-device rendering, Android Keystore behavior, WorkManager scheduling or picker integration.

Use the local setup in README first. Repeat against a dedicated staging Firebase project before production. Do not use real credentials or real financial records in demo tests.

1. Sign in as Bendahara, complete initial sync, and verify the open period. Sign in as Ketua on a second device and verify all financial modification actions are hidden.
2. Add pagudana and kas separately. Create an event, an expense with quantity/unit price, and an asset with a receipt. Verify totals and receipt access as both users.
3. Enable airplane mode. Create an event followed by its expense, stage a receipt, and save contributions. Verify local status rather than confirmed totals changing.
4. Force-stop and reopen the app while offline. Confirm the saved queue and encrypted receipt staging remain available. Reboot and repeat. Do not clear application storage.
5. Restore connectivity. Check each operation is accepted exactly once and appears on the second device. Interrupt connectivity during image upload and after server acceptance; retries must not duplicate postings.
6. Use the second device to spend the remaining balance before the first reconnects. Verify the losing expense is rejected with its input retained and no negative balance. Repeat with a closed period and revoked/downgraded membership.
7. Correct a purchase amount. Verify the old entry, its reversal, the replacement and the new balance. Retry the correction and ensure it cannot be applied twice. Archive an asset and verify that no money is refunded.
8. Rename/archive an event on one device while another has stale details. Verify stale edits are rejected and archived events cannot receive new expenses.
9. Select a one-day report, a cross-month report and a cross-year report. Compare opening + inflow - outflow with closing for each account. Pending entries must be visibly separate. Search must filter details without silently altering account summaries.
10. Download a report, go offline and view the same/custom ranges within the complete snapshot. Verify the last-sync label. On a fresh installation with no snapshot, reports must say data is unavailable rather than zero.
11. Open a receipt online, then offline. Cancel the photo picker, select an oversized/non-image file and simulate a missing local staging file; each path must fail safely without losing other work.
12. Logout with pending work, sign in as another user, and verify no old-user records, drafts, receipts or queue entries are displayed. Sign back into the original user and resume that user's work.
13. Rotate during login, entry, queue and reporting. Increase font size, enable dark mode and use TalkBack. Check clipped controls, keyboard visibility, chart text alternatives and correct navigation.
14. Test an expired session, server downtime and a rejected receipt upload. No endless disabled form, duplicate transaction or crash should result.
15. Verify signed production APK upgrade from the actual installed app using the owner's signing key. Check target SDK/distribution policy, Firebase environment separation and release logs.

Record device model, Android version, build hash, scenario outcome and any screenshots/crash logs. Production acceptance remains open until these scenarios pass; do not equate passing unit tests with completing this checklist.
