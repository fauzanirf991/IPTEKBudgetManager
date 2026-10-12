# Device acceptance checklist (offline build)

Run on API 26 and a current Android device, including one small-screen device, **in airplane mode**. Robolectric cannot exercise `BiometricPrompt`, the Android Keystore, the document picker or real rendering, so these steps are required before relying on the app. Do not use real financial records during tests.

## Screen lock

1. With **no** screen lock set, open the app. It must show "Aktifkan kunci layar" and no financial data. Tapping the button opens Security settings.
2. Set a PIN, return to the app. The unlock prompt appears. Cancel it: the lock panel stays, with no data visible. Tap "Buka kunci", enter the PIN: data appears.
3. Repeat with a fingerprint or face enrolled, where the device has one.
4. Press Home, wait more than 60 s, reopen: the app is locked. Reopen within 60 s: it stays open.
5. Use the toolbar action "Kunci sekarang": the app locks at once and any open dialog is dismissed.
6. While unlocked, remove the screen lock in Settings, return to the app: it must lock and ask for a new lock. After setting a lock again, all data must still be there.
7. Open Recents and try a screenshot, on the main screen and while a dialog is open: the thumbnail is blank and screenshots are blocked.
8. Rotate the screen while unlocked: the app must not lock.

## Ledger

9. Add a pagudana and a kas contribution. Totals must change immediately and stay separate per account.
10. Create an event, then an expense with quantity and unit price. Try an expense larger than the balance: it is rejected at once with "Saldo tidak mencukupi" and nothing changes.
11. Add an asset with a receipt photo. Open the receipt. Try cancelling the picker, an oversized file and a non-image file: each fails safely.
12. Correct an asset amount: the old entry, its reversal, the replacement and the new balance are all visible, and the receipt is kept. Cancel a transaction twice: the second attempt is refused.
13. Rename and archive an event. An archived event cannot take new expenses. Archive an asset: no cash is refunded.
14. Close a period from the toolbar menu: new postings for that year are rejected. Reopen it: they work again.
15. Run one-day, cross-month and cross-year reports. Opening + inflow - outflow must equal closing for each account. Search must filter details without changing the summaries.
16. Force-stop the app and reopen it, then reboot and reopen it. Data and receipts must persist after unlock.
17. Fill a long form, press Home, then reopen after more than 60 s: after unlock, no unsaved draft from before the lock should be shown.

## Device and quality checks

18. Confirm the installed app lists no network permission in system App info.
19. Increase font size, enable dark mode and use TalkBack. Check clipped controls, keyboard overlap and navigation.
20. Fill the storage almost completely and try saving a receipt: the posting must fail as a whole, with nothing half-saved.
21. Check the upgrade from a build signed with the owner's release key. Uninstalling must warn that data is lost (documented in README).

Record device model, Android version, build hash, outcome and screenshots or crash logs. Acceptance stays open until these pass; passing unit tests does not complete this list.
