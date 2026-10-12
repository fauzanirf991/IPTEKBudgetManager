package com.fauzan.ukmbudgetmanager.data;

import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.LedgerEngine;
import com.fauzan.budget.core.LedgerState;
import com.fauzan.budget.core.Record;
import com.fauzan.budget.core.Snapshot;
import com.google.gson.Gson;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The only writer of the on-device ledger. A posting validates (via {@link LedgerEngine}), updates every row and
 * stores the receipt inside one database transaction, so a failure leaves nothing half-written.
 */
public final class LedgerRepository {
    public static final Gson JSON = new Gson();
    private static final String META = "main";
    private final BudgetDatabase db; private final BudgetDao dao; private final Sealer sealer; private final ReceiptStore receipts;
    /** Decrypted records by sealed payload so unchanged rows are not decrypted again. Cleared whenever the app locks. */
    private final Map<String, Record> cache = new ConcurrentHashMap<>();

    public LedgerRepository(BudgetDatabase db, Sealer sealer, ReceiptStore receipts) {
        this.db = db; this.dao = db.dao(); this.sealer = sealer; this.receipts = receipts;
    }
    public void clearCache() { cache.clear(); }

    private Record decode(String payload) { return cache.computeIfAbsent(payload, p -> JSON.fromJson(sealer.open(p), Record.class)); }
    private LedgerState state() {
        LedgerMeta meta = dao.meta(META);
        return meta == null ? LedgerState.fresh() : JSON.fromJson(sealer.open(meta.payload), LedgerState.class);
    }
    private LedgerRecord row(Record r) {
        LedgerRecord row = new LedgerRecord(); row.id = r.id; row.kind = r.kind; row.businessDate = r.businessDate; row.periodId = r.periodId;
        row.payload = sealer.seal(JSON.toJson(r)); return row;
    }
    private void saveState(LedgerState state) {
        LedgerMeta meta = new LedgerMeta(); meta.name = META; meta.payload = sealer.seal(JSON.toJson(state)); dao.putMeta(meta);
    }

    /** Builds the read-only view shown in the UI from the observed rows. Call off the main thread. */
    public Snapshot snapshot(List<LedgerRecord> rows, LedgerMeta meta) {
        Snapshot snapshot = new Snapshot();
        LedgerState state = meta == null ? LedgerState.fresh() : JSON.fromJson(sealer.open(meta.payload), LedgerState.class);
        snapshot.revision = state.revision; snapshot.periods.addAll(state.periods.values());
        if (rows != null) for (LedgerRecord row : rows) snapshot.records.add(decode(row.payload));
        return snapshot;
    }

    /**
     * Posts a command atomically.
     * @param receipt optional receipt image bytes (already type/size checked by the caller)
     * @throws IllegalArgumentException with a user-readable message when an accounting rule rejects the posting
     */
    public void post(Command command, byte[] receipt) {
        boolean[] written = {false};
        try {
            command.hasReceipt = receipt != null;
            db.runInTransaction(() -> {
                LedgerState state = state();
                LedgerEngine.Outcome out = LedgerEngine.apply(state, id -> { LedgerRecord r = dao.record(id); return r == null ? null : decode(r.payload); },
                        command, LocalDate.now(), System.currentTimeMillis());
                for (Record r : out.inserted) dao.putRecord(row(r));
                for (Record r : out.updated) dao.putRecord(row(r));
                saveState(state);
                AuditEntry audit = new AuditEntry(); audit.recordId = out.audit.recordId; audit.revision = out.audit.revision; audit.payload = sealer.seal(JSON.toJson(out.audit));
                dao.putAudit(audit);
                // Only reached after every rule passed, so an existing receipt can never be overwritten by a rejected posting.
                if (receipt != null) { receipts.write(command.id, receipt); written[0] = true; }
            });
        } catch (UncheckedIOException e) {
            if (written[0]) receipts.delete(command.id);
            throw new IllegalStateException(e.getCause() == null ? "Penyimpanan penuh atau tidak tersedia" : e.getCause().getMessage(), e);
        } catch (RuntimeException e) {
            if (written[0]) receipts.delete(command.id);
            throw e;
        }
    }

    /** Opens or closes an accounting period. */
    public void setPeriodOpen(String periodId, boolean open) {
        db.runInTransaction(() -> { LedgerState state = state(); LedgerEngine.setPeriodStatus(state, periodId, open); saveState(state); });
    }

    public byte[] receipt(String receiptId) { return receipts.read(receiptId); }
}
