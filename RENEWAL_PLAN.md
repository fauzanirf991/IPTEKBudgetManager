# IPTEK Budget Manager renewal plan

Prepared 16 September 2026.

Implementation is now present in `android-app/`, `budget-core/`, `processor/` and `firebase/`. See [implementation status](docs/IMPLEMENTATION_STATUS.md), [verification](docs/VERIFICATION.md) and [setup instructions](README.md) for completed work, concrete implementation boundaries and remaining release gates.

**Revised direction:** rebuild the Android application incrementally using Java and XML Views, with Firebase Authentication, Cloud Firestore and Cloud Storage as the backend. Support offline use through temporary local data, queued changes and receipt files that synchronize when connectivity returns. Reports stay inside the app and accept custom start/end dates. Excel export is removed from the renewal scope.

This revision replaces the previous Spring Boot/PHP backend options, online-only financial posting workflow and spreadsheet reporting deliverable. The original source findings remain as migration and regression references.

## Review scope and limits

The repository contains one Android application module, 50 production Java files (6,359 lines), 37 layout XML files including version variants, 37 PHP files, a seven-table MariaDB/MySQL dump, receipt images, an APK, and thesis/proposal documents. Review covered build configuration, manifest, application features, networking, authentication, CRUD and reporting code, database structure, resources, tests, and extracted requirements from the DOCX documents. Binary APK behavior and document page layouts were not validated; PDF copies were not separately compared with DOCX files.

This is a static assessment, not a successful runtime test or an exhaustive proof that every defect has been found. `gradlew.bat --version` could not start: `JAVA_HOME` is unset and `java` is unavailable on PATH. No server was contacted, database imported, APK installed, or application code changed. The deployed database and server may differ from this checkout.

## Existing product and behavior

| Area | Current implementation | Renewal decision |
| --- | --- | --- |
| Users | Registration, two login classes, role selection, username lookup, password change | One login; server assigns capabilities; verified account recovery |
| Dashboard | Separate Bendahara and Ketua activities linking to feature screens | Shared dashboard with capability-based actions and real financial summary |
| Pagudana | One funding record; separate original and remaining balance tables | Funding account and period, with receipts and derived balance |
| Kas | Payer name, contribution amount, payment date, totals and search | Preserve contribution history; optional member reference |
| Kegiatan | Event name/date and associated expenses | Preserve event workflow; enforce parent/child integrity |
| Pengeluaran | Quantity, unit price, date and receipt | Validated line totals, funding source, auditable corrections |
| Aset | Asset name, purchase price, date and receipt | Separate inventory identity from its purchase transaction |
| Laporan | Six independent total requests and a pie chart | In-app summaries, charts and transaction detail for a custom date range, with offline coverage indicators |
| Supporting content | Help/about, logos and academic reports | Retain useful branding; rewrite help for the new workflows |

The thesis describes Bendahara as editor and Ketua as viewer, but also uses Anggota terminology and describes alternative guest navigation. The code differs from those descriptions. Default renewal scope: one organization, authenticated Bendahara and Ketua, Indonesian UI, IDR, and reporting over any chosen date range, including ranges across leadership/budget periods. Budget periods still control financial posting and closure; they do not restrict the report date picker. Public access and an additional member role are separate decisions.

Kas currently sits outside the pagudana balance. Preserve separate accounts initially: do not silently treat contributions as additional pagudana. A combined organization total can show both accounts with explicit labels.

## Prioritized findings

Paths below are relative to this repository. Severity describes consequences if these sources are deployed as written.

| Priority | Evidence | Impact and required response |
| --- | --- | --- |
| Critical | `API/ukmbudget/changepassword.php:7` and `include/DB_Functions.php:135` accept username and replacement password | Account password can be changed without current-password verification, authenticated session, or reset token. Replace recovery before exposure. |
| Critical | `add_*.php`, `update_*.php`, `delete_*.php` and financial reads have no authenticated principal or role checks; `BaseApiService.java` supplies no access token | The UI role is not a security boundary. Enforce authentication and authorization on every API operation and receipt download. |
| Critical | `API/ukmbudget/delete_dana.php:13` truncates six tables | Deleting funding can erase events, expenses, cash and assets globally. Replace with explicit period closure/archive and controlled transaction reversal. |
| High | Financial CRUD interpolates POST values into SQL, e.g. `add_event.php`, `update_aset.php` | SQL injection and malformed queries. Retire these endpoints after Firebase migration; use parameter binding for any temporary legacy maintenance and trusted validation for new writes. |
| High | `include/DB_Functions.php:68` uses assignment in `if($usertype = $tipeuser)`; registration accepts client-selected role | Requested and stored roles are not compared. Privileged enrollment is uncontrolled. Use server-managed role membership. |
| High | `divuser/UserType.java` sends the Ketua path to `LoginActivity` with `user_ketua`, while that activity reads `user_bendahara` and opens Dashboard1 | Incorrect role routing. Replace both login flows with one server-driven session. |
| High | `add_aset.php`, `update_aset.php`, `add_eventneeds.php`, `update_eventneeds.php`, `delete_eventneeds.php` perform multiple balance and record writes without transactions | Partial failure and concurrent requests can corrupt balances; retries can duplicate postings. Use atomic transactions and idempotency. |
| High | `delete_aset.php` removes an asset without restoring the deducted balance; `update_dana.php:24` has a comma immediately before WHERE | Financial totals drift; updating the original funding amount fails. Establish accounting semantics before migration. |
| High | `add_aset.php:32` retrieves the inserted ID after a separate UPDATE | Receipt association is unreliable because insert ID is not captured immediately. New API must return explicit record IDs. |
| High | `API/dompetukm.sql` has no `usertype` column although login/registration expect it; no foreign-key constraints; samples have `id_user=0` | Schema is not a reproducible match to the application. Resolve actual production schema and ownership before importing. Inserts omit required ownership columns and rely on legacy SQL behavior. |
| High | `helper/UtilsApi.java` uses a LAN HTTP endpoint; `RetrofitClient.java:14` enables BODY logging unconditionally | Credentials/receipts can travel unencrypted and appear in logs. HTTPS, environment configuration and sanitized debug logging are required. Verify Android cleartext policy during baseline work. |
| High | `divaset/CustomFilter.java` replaces adapter data; `asetList.java:57` indexes the original list by clicked position; same pattern in Kas/Kegiatan | A filtered result can open/edit a different record. Pass the bound item's stable ID, not a position into another list. |
| Medium | `laporanActivity.java:122`, similar cash/report branches use `setText('0')`; editors use unchecked `Integer.parseInt` and `response.body()` | Character argument resolves to a resource-ID overload; invalid amounts and failed responses can crash. Introduce validated input and explicit UI error states. |
| Medium | `laporanActivity.java` has an empty export listener and independently loads six totals | Totals/chart can represent different instants or partially loaded data. Replace with coherent in-app reports; remove the unused export action and CSV dependencies. |
| Medium | Receipt code uses base64 payloads, full bitmap loading, filesystem paths and independent file/database writes | Large-image memory pressure, failed uploads, orphan files and inconsistent references. Use bounded Cloud Storage uploads, durable local staging and attachment lifecycle management. |
| Medium | `include/DB_Functions.php:101` uses salted SHA-1; `DB_connect.php` / `DB_Functions.php` references differ from actual filename case | Replace password hashes; verify deployment on case-sensitive filesystems. |
| Medium | Java Activities combine forms, formatting, navigation and HTTP callbacks; editor/list classes reach 386–488 lines | Repeated role screens and mixed responsibilities make changes risky. Introduce feature packages, ViewModels and repositories. |
| Medium | Test files contain only `2 + 2` and package-name checks; README has no setup instructions | No business regression protection or reproducible onboarding. Add focused tests and clean setup documentation. |

Build baseline: AGP 3.5.2, Gradle 5.4.1, Java source level 8, compile/target SDK 29, minimum SDK 19. Dependencies include JCenter, old AndroidX components, duplicate Glide declarations with a different compiler version, ButterKnife, old charting code and overlapping CSV dependencies. `google-services.json` exists, but the build does not establish an active Firebase integration. Do not assume Firebase is the current authentication service.

## Target architecture while keeping Java

**Android:** Java application sources, XML layouts, Material Components for Views, View Binding, Fragments, ViewModel and LiveData, RecyclerView/ListAdapter, Firebase Android SDKs, Room and WorkManager. Use Java Executors and callbacks for background work; avoid adding another reactive framework without a concrete need. Android documents Java-compatible ViewModels and suggests Executors/callbacks or RxJava for Java codebases. [ViewModel guidance](https://developer.android.com/topic/libraries/architecture/views/viewmodel), [LiveData guidance](https://developer.android.com/topic/libraries/architecture/livedata).

Flow: `Fragment → ViewModel → Repository → Room → sync worker / Firebase`. Room is the UI's local source for downloaded records, drafts, pending operations and report coverage. Firebase is authoritative for accepted shared records. Firebase listeners and explicit range downloads update Room; do not maintain two independent write queues for the same operation. Keep UI state immutable where practical. ViewModels must not retain Activities or Views. Pass record IDs through navigation. Use one screen implementation per feature with capabilities supplied by the authenticated membership.

Suggested packages under the existing application namespace:

```text
core/firebase, core/session, core/database, core/sync, core/ui
feature/auth, feature/dashboard, feature/funding, feature/cash
feature/events, feature/expenses, feature/assets, feature/reports
```

Start with one Android module and clear package boundaries. Extract modules only when build time, ownership or reuse warrants it. No Kotlin application rewrite or Compose migration is required by this plan.

**Firebase backend:** Firebase Authentication handles identity and account recovery; Cloud Firestore stores organization memberships, periods, accounts, financial records, operation outcomes and audit history; Cloud Storage stores private receipts. Use separate development/staging/production Firebase projects, version-controlled Rules and indexes, and a compatible pinned Firebase Android BoM. Verify the existing `google-services.json` against the intended project rather than assuming it is usable.

Firestore and Storage Security Rules enforce organization membership, read access, attachment ownership and permitted client fields. Clients cannot assign roles or directly alter accepted ledger entries, account totals, operation outcomes or audit records. App Check is an additional abuse control, not a replacement for authorization. [Storage rules](https://firebase.google.com/docs/storage/security).

**Trusted financial processing:** keep Firebase as the data/authentication backend and use a small Java service on Cloud Run with the Firebase Admin SDK to validate and commit queued financial commands. This preserves Java for application and custom server logic without retaining the legacy PHP API or a SQL backend. The worker calls its authenticated HTTPS endpoint after reconnecting. The service verifies Firebase ID tokens, checks current membership and period status, and commits a Firestore transaction. Admin SDK access bypasses client Rules, so the service must enforce these checks itself. [Admin SDK setup](https://firebase.google.com/docs/admin/setup), [ID token verification](https://firebase.google.com/docs/auth/admin/verify-id-tokens).

Configure service-account access through managed identity, never an Admin key in the APK. Include Firestore/Storage/Cloud Run location, billing setup, budget alerts, retention and restore procedures in the deployment plan. Do not assume a free tier covers the final deployment.

## Offline storage and synchronization

Offline support is part of version one. A user who has signed in and downloaded data can read available records, create entries, prepare corrections and stage receipts without connectivity. First sign-in, account recovery, permission changes and authoritative financial acceptance require connectivity.

Firestore supports a persistent Android cache, but that cache only covers retrieved data and does not establish that an arbitrary report range is complete. Use Room for explicit report coverage and the durable outbox; configure the Firestore client cache deliberately to avoid treating it as a second authoritative store. [Firestore offline behavior](https://firebase.google.com/docs/firestore/manage-data/enable-offline).

1. Save each draft/command and its stable UUID to Room atomically before displaying **Saved on this device**. Store receipt bytes in app-private persistent files, not an evictable temporary cache. Record user UID, organization, dependencies, expected record version and local creation time.
2. WorkManager schedules unique work when connectivity is available. Restore unfinished work after process death/reboot, and retry transient failures with backoff. Sync only for the originating signed-in user and organization; refresh authentication or request sign-in when needed.
3. Upload staged receipts to a deterministic private staging path, recording resumable progress where supported. Keep local bytes until upload and related financial posting are confirmed. Firestore's offline cache does not upload receipt files for the app. [Android Storage uploads](https://firebase.google.com/docs/storage/android/upload-files).
4. Submit the immutable command payload with its UUID to the trusted processor. Commit the accepted ledger change, account projection, audit record and operation outcome together. Retrying the same UUID/payload returns its previous result; a different payload requires a new UUID. Process dependent commands in order, such as event creation before its expense.
5. Reflect **Pending sync**, **Syncing**, **Synced**, **Needs sign-in**, **Conflict** or **Rejected** in the UI. Remove accepted commands from the pending queue only after their authoritative result is mirrored locally. Retain rejected input for correction. A timeout after server acceptance is recovered by retrying/querying the same UUID.
6. Refresh confirmed records and invalidate affected report snapshots/coverage before presenting new totals. Pending amounts appear separately as a clearly labeled estimate; they never silently change confirmed balances.

Firestore client transactions fail offline. Therefore an offline save is durable local submission, not a promise that spending is accepted: another device may spend the available balance or an administrator may close the period before sync. The processor rejects such commands with a recoverable explanation. Never resolve financial conflicts using silent last-write-wins. [Firestore transactions](https://firebase.google.com/docs/firestore/manage-data/transactions).

Temporary storage means a recoverable local working copy, not automatic deletion of unsynced work. Cap downloaded history and receipt previews, allow users to download a report range for offline use, and evict only acknowledged data that can be fetched again. Show cached coverage and last successful sync. Uninstalling/clearing application storage can lose unsynced work; communicate that in pending-work management.

Partition local data by UID/organization. On logout stop listeners/workers and hide the previous account's data; keep unsynced work protected for that same account or require an explicit discard choice. Configure local database/files protection and backup exclusions, and clear SDK caches safely on account switching. Revocation cannot remotely erase a disconnected device's existing cache: validate authorization again on reconnect and lock/purge accessible cached data when revocation is learned.

**Toolchain:** choose and pin an Android Studio/AGP/Gradle/JDK combination using the official compatibility tables; distinguish the JDK running Gradle from Android Java source/API compatibility. Replace JCenter with Google/Maven Central where artifacts exist. Raise compile/target SDK after reviewing platform behavior changes, namespace/exported declarations, edge-to-edge layout and permissions. Propose minSdk 26, subject to the actual device fleet; keeping older devices changes dependency and test choices. [Android build compatibility](https://developer.android.com/build/releases/about-agp).

## Product and interface renewal

Use five primary destinations: **Beranda, Transaksi, Kegiatan, Aset, Laporan**. Put funding and cash under Transaksi, and account/help settings in the top-level menu.

- Beranda: active period, pagudana remaining, kas balance, spending, recent records and contextual add actions.
- Transaksi: funding receipts and cash contributions with account/date/category filters; distinguish budget allocation from actual receipt if the organization uses both concepts.
- Kegiatan: searchable event list, event detail, expenses, totals and receipts; retain original event/expense relationship.
- Aset: inventory list, acquisition details and linked purchase; disposal must not silently refund cash.
- Laporan: custom start/end dates, account/category/event filters, accessible numeric summaries, charts and transaction drill-down, all displayed inside the app.

Use consistent spacing, readable typography, Indonesian currency/date formatting, dark/light themes and text alternatives to charts. Every list needs loading, empty, failure, retry and stale-data states. Every form needs inline validation, disabled duplicate submission, preserved draft input and an explicit successful-save state. Test large font sizes and screen rotation.

Replace broad gallery access with the system photo picker/appropriate fallback. Copy selected receipts into protected local staging for reliable later upload. [Photo picker](https://developer.android.com/training/data-storage/shared/photo-picker).

Version-one scope includes all existing core workflows, reliable receipts, in-app custom-period reports, roles, audit history, budget periods and offline reading/data entry with automatic synchronization. Remove the Excel/CSV export button, export dependencies and file-export implementation tasks. Approval chains, notifications, multiple organizations, bank connections, receipt OCR and advanced depreciation are later options.

## In-app reports for custom periods

The user selects any start and end date, with optional shortcuts for this month, last month, this year or an existing budget period. Support one-day and cross-month/year/leadership-period ranges. Reject a reversed date range with an inline message. Report selection does not reopen a closed posting period.

Use the transaction's business date for inclusion, not its upload time. Store an ISO business date separately from server acceptance timestamps. Interpret both chosen endpoints inclusively in the organization's configured timezone; where timestamp queries are used, translate them to start-of-day inclusive and day-after-end exclusive. Delayed offline uploads then appear in the intended business-date report after acceptance.

Display opening balance, inflows, outflows and closing balance separately for pagudana and kas; expense breakdowns; trend charts; event/asset subtotals; and a transaction list with receipt drill-down. Opening balance includes accepted activity before the selected start; closing balance equals opening plus in-range inflows minus outflows. Category/event filters describe filtered activity and must not relabel those subtotals as the whole account balance.

Compute all displayed totals, chart values and transaction rows from one defined accepted-data revision. Paginated online downloads must use a stable revision/cutoff so concurrent changes cannot produce mixed totals. Provide a trusted report snapshot query through the processor when needed, then persist the report dataset and opening balances locally. Index Firestore for organization/account, business date and supported query combinations; avoid downloading the entire history for each date-picker change.

| State | In-app behavior |
| --- | --- |
| Online and fully loaded | Confirmed report for the selected range, revision and update time |
| Offline with complete downloaded range and opening balances | Full report as of the saved revision, labeled offline/last synced |
| Offline with only part of the range or no opening balance | Explicit incomplete/unavailable report; show only labeled available records and offer download on reconnect |
| Pending local entries in the range | Separate pending count/amount or preview; confirmed totals remain distinct |
| Accepted backdated entry or correction | Invalidate affected range/opening-balance caches and refresh; show the previous snapshot as stale until complete |

An empty cache is not evidence of zero transactions. Track downloaded interval coverage, account scope, pagination completion and snapshot revision in Room. Use the same Java calculation rules over complete local datasets for offline reports. Firestore server aggregation queries do not support offline queries, so they cannot be the only reporting implementation. [Aggregation limitations](https://firebase.google.com/docs/firestore/query-data/aggregation-queries).

## Financial model and Firebase data contract

Introduce these concepts rather than reproducing mutable singleton balance tables:

| Entity | Purpose |
| --- | --- |
| User and membership | Identity and server-managed Bendahara/Ketua capabilities |
| Budget period | Leadership/fiscal interval, open/closed status, retained history |
| Fund account | Separate pagudana and kas balances |
| Ledger entry | Posted receipt/payment/reversal, account, date, amount, actor and source |
| Event and expense item | Event grouping, quantity, unit price and calculated total |
| Asset | Inventory record linked to acquisition ledger entry |
| Attachment | Private storage key, MIME type, size, checksum and ownership |
| Audit event | Actor, action, timestamp, reason and relevant before/after values |
| Operation outcome | Idempotency UUID, immutable request hash, accepted/rejected result and linked records |
| Local outbox and report coverage | Room-only pending work, dependencies, downloaded date intervals and report revisions |

Use whole-rupiah Java `long` and Firestore integer fields, with checked arithmetic and explicit limits. If fractional amounts become necessary, define a fixed minor-unit scale and use `BigDecimal` for calculation rather than storing floating-point money. Store ISO business dates plus server timestamps; localize only in the UI.

Balance is derived as opening balance plus posted inflows minus posted outflows for each account/period. Do not import both original funding and remaining funding as receipts. Compute quantity × unit price on the server; reject invalid quantities, overflow and unsupported negative values. Decide whether overspending is allowed; default to rejecting it with an explanatory error.

Posted financial records are corrected by auditable reversal/replacement. Draft records can be edited. Archiving an asset/event does not itself reverse a payment. Refunding a purchase requires an explicit refund transaction. Closed periods reject ordinary writes.

Use Firestore transactions that read/check the account projection and period status and atomically write ledger entries, balance projections, audit events and operation outcomes. Transaction retries must be safe; receipt uploads and other external side effects occur outside the transaction callback. Use record versions for conflicts and derive projections from the accepted ledger for reconciliation. Stage attachments and finalize references with posting; Cloud Storage and Firestore do not share a transaction, so include compensation and abandoned-upload cleanup.

Suggested Firestore paths under `organizations/{orgId}`: `members/{uid}`, `periods/{id}`, `accounts/{id}`, `ledgerEntries/{id}`, `events/{id}`, `expenseItems/{id}`, `assets/{id}`, `attachments/{id}`, `operationResults/{uuid}` and `auditEvents/{id}`. Include schema version, business date, period/account references, actor, accepted revision and server timestamps where applicable. Validate referenced documents in the trusted processor: Firestore does not provide SQL foreign keys. Match Storage paths to organization/owner/operation IDs and protect both staged and accepted receipts.

Use Firebase SDK reads/listeners for authorized records. Keep the custom HTTPS contract small: submit a versioned operation, recover its outcome, request a consistent report snapshot and administer privileged membership/period actions. Return typed validation/conflict results and stable IDs. Client-supplied roles, balances and acceptance timestamps are never authoritative.

Use Firebase Authentication's session lifecycle, reauthentication for sensitive account changes and verified password-reset flow. Let the SDK manage auth credentials rather than copying passwords or tokens into Room. The legacy email field contains username-like data: verify recovery contacts or use administrator-assisted onboarding rather than assuming it contains valid email addresses. Privileged membership must be invitation/administrator controlled and checked again when offline commands arrive.

## Data migration and rollout

1. Obtain the actual database schema, complete database backup and receipt directory backup. Confirm signing-key ownership, application ID and installed versions. The committed dump is a sample, not evidence of current production state.
2. Profile records for orphan event links, invalid dates, `id_user=0`, duplicate IDs, missing receipt files and inline base64 versus path-based receipt values. Record unresolved ownership as legacy/unknown rather than inventing an actor.
3. Map `kegiatan` to events, `pengeluarankegiatan` to event expenses/payments, `aset` to assets plus purchase payments, and `kas` to contributions in the cash account. Treat `pagudanastatic` as evidence of original funding and `pagudana` as a reconciliation target, not two separate incomes.
4. Build a repeatable Java Admin SDK importer into staging Firestore and Cloud Storage with legacy-ID mappings, an import manifest, record counts and attachment checksums. Preserve original business dates, assign deterministic IDs and migrate users to Firebase Authentication with explicit UID mappings. Run against a source copy and produce a discrepancy report. Require explicit resolution of material discrepancies; never silently adjust balances.
5. Reconcile totals by period/account and compare event/asset totals with source records and trusted reports. The supplied sample satisfies 12,000,000 − 1,200,000 − 120,000 = 10,680,000 in pagudana; its 100,000 kas remains separate. Use this as one fixture, not proof that live data is clean.
6. Provision Firebase Authentication accounts through verified onboarding/reset; do not copy the legacy salted SHA-1 values into new user documents. Grant verified memberships through a trusted administrator and preserve unknown legacy actors as unknown.
7. Pilot with Bendahara and Ketua using staging data, including offline entry and reports. Schedule a write freeze, final backup/import, reconciliation and release. Avoid independent writes to both old and new financial stores; drain or explicitly reconcile pending device queues before any subsequent cutover/rollback.
8. Retain the old database as a protected read-only archive. Rollback after new financial writes requires preserving/reconciling those writes; reverting an APK alone is insufficient. Restrict legacy API access so old clients cannot continue modifying obsolete balances.

## Delivery sequence and acceptance gates

Effort below is a planning estimate for one experienced developer working full time with timely stakeholder access. It is not a fixed delivery promise. Revised total: approximately 11–17 developer-weeks plus external waiting time. Removing spreadsheet generation saves work, but reliable offline synchronization and complete custom-range reports add implementation and validation work.

| Phase | Effort | Deliverables | Exit gate |
| --- | --- | --- | --- |
| 0. Establish baseline | 0.5–1 week | Legacy build or documented blockers; source schema; financial rules; Firebase project/region inventory | Roles, kas treatment, timezone, devices and offline retention defaults recorded |
| 1. Foundations | 1.5–2 weeks | Java app shell, Room/outbox, Firebase Auth/Firestore/Storage, Rules/indexes, Java processor and CI | Clean checkout builds; emulator setup works; session and local persistence demonstrated |
| 2. Identity, accounting and sync | 2.5–4 weeks | Membership, recovery, ledger/audit, transactional processor, WorkManager retries and conflicts | Unauthorized writes rejected; offline work survives restart; retries/concurrency preserve balances |
| 3. Core workflows | 3–4 weeks | Funding → cash → events/expenses → assets; local receipt staging/upload; shared viewer/editor screens | Each feature works offline and online with dependencies, errors, conflicts and search tested |
| 4. In-app reporting and migration | 2–3 weeks | Custom date ranges, charts/details, local coverage and report snapshots, Firebase importer and dry run | Opening/closing balances reconcile; incomplete offline ranges are labeled; imported records/files reconcile |
| 5. Pilot and release | 1.5–3 weeks | Device testing, offline pilot, Firebase backup/restore rehearsal, signing and cutover | Sync/report gates pass; account-switch isolation and recovery documented |

Build one vertical slice first: Firebase login → download data → go offline → create a cash contribution → restart app and verify pending work → reconnect → accept it exactly once → read it as Ketua → view it in a custom-date report. This proves permissions, local durability, sync, accounting and reporting before repeating them across receipt/event features.

## Required validation

- Unit tests: money overflow, quantity multiplication, receipt/payment/reversal rules, period closure and IDR/date formatting.
- Firebase tests: Rules deny anonymous/other-organization access, viewer writes, self-assigned roles and direct ledger/audit changes; processor checks missing/expired/revoked sessions, current membership, conflicts, duplicate commands, simultaneous spending and transaction failure.
- Regression cases from this review: filtered item opens correct ID; empty totals render zero; malformed/empty response does not crash; asset correction preserves balance; funding deletion cannot erase unrelated records.
- Attachment tests: local files survive restart, missing/large/invalid/interrupted uploads, retry after upload-before-post failure, unauthorized download, safe cleanup and restore of bytes plus metadata.
- Android tests: rotation/process recreation, slow or unavailable network, expired session, picker cancellation, large text, supported Android versions and release build behavior.
- Offline tests: airplane-mode entry and correction, process death/reboot, duplicate worker execution, same UUID with changed payload, dependency order, lost acknowledgment, expired authentication, role revocation, period closure and competing device updates; unsynced data survives until acknowledged or explicitly discarded.
- Report tests: inclusive custom endpoints, one-day and cross-year ranges, timezone boundaries, opening/closing balances, late-synced backdated entries, filtered activity versus account balances, pending-work separation, zero data versus missing coverage, stable pagination/revision and matching chart/detail totals. Reports render inside the app; no export action remains.
- Local isolation tests: account switching/logout does not expose another user's cached data or submit their queued work; eviction preserves unsynced commands/files; stale reports are marked after updates.
- Migration tests: repeat import without duplication, orphan quarantine, legacy ownership handling, checksum verification and reconciliation of the supplied sample plus a representative production copy.

CI should run Java calculation/processor tests, Firebase Emulator Suite Rules and integration tests, Android unit tests/lint, dependency checks and a build. Run instrumented sync/report tests on the agreed device matrix before release; verify managed-service behavior in staging where emulator parity is insufficient. Keep Admin credentials and generated APKs out of source changes; document Firebase setup, Rules/index deployment, local schema upgrades, sync recovery, import, backup and release procedures.

## Decisions to settle before implementation

These do not block the analysis or planning deliverable:

1. Which Firebase project, deployment region and organization timezone should be used? Firebase backend, Java Android, offline support and in-app-only reporting are already decided.
2. Is this still one UKM, and is the second role Ketua only or also ordinary members?
3. Are there live records, deployed servers, installed users and an available release signing key?
4. Which devices must remain supported, and is distribution through Play or direct APK installation?
5. Is kas strictly separate, can it fund expenses, and are budget allocations different from cash received?
6. Which in-app report breakdowns and date shortcuts are most useful, and how much history/receipt data should be downloaded by default for offline access? Custom start/end dates are required regardless of these preferences.

The first implementation milestone should be the reproducible Java/Firebase foundation and tested offline cash-contribution slice with a custom-date in-app report. Visual polish and the remaining workflows then build on the same sync and financial rules.
