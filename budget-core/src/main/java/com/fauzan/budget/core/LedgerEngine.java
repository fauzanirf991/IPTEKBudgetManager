package com.fauzan.budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Local accounting rules (ported from the former server-side service). Pure Java, no I/O.
 * All validation happens before any mutation: when this throws {@link IllegalArgumentException}, the
 * {@link LedgerState} is unchanged and nothing needs to be rolled back.
 */
public final class LedgerEngine {
    private LedgerEngine() {}

    public interface Records { Record find(String id); }

    public static final class Audit {
        public String recordId = "", kind = "", targetId = "";
        public long revision, acceptedAt;
        public Record before, after;
    }

    /** What must be persisted after a successful posting. */
    public static final class Outcome {
        public final List<Record> inserted = new ArrayList<>(), updated = new ArrayList<>();
        public Audit audit;
    }

    public static Outcome apply(LedgerState state, Records store, Command command, LocalDate today, long now) {
        try { return post(state, store, command, today, now); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Nominal terlalu besar"); }
    }

    private static Outcome post(LedgerState state, Records store, Command command, LocalDate today, long now) {
        long requestedDelta = command.validatedAmount();
        boolean metadata = Set.of("EVENT_EDIT", "EVENT_ARCHIVE", "ASSET_ARCHIVE").contains(command.kind);
        boolean correction = !command.targetId.isEmpty() && !metadata && !"VOID".equals(command.kind);

        if (store.find(command.id) != null || (correction && store.find(command.id + "-reversal") != null))
            throw new IllegalArgumentException("ID catatan sudah digunakan. Buat operasi baru.");
        LocalDate date = Checks.date(command.businessDate);
        if (date.isAfter(today)) throw new IllegalArgumentException("Tanggal transaksi tidak boleh di masa depan");

        Snapshot.Period period = state.periods.get(command.periodId);
        if (period == null && command.periodId.equals(Integer.toString(date.getYear()))) period = LedgerState.newYearPeriod(date.getYear());
        if (period == null || (!"OPEN".equals(period.status) && !command.kind.endsWith("_ARCHIVE")))
            throw new IllegalArgumentException("Periode sudah ditutup atau tidak tersedia");
        if (command.businessDate.compareTo(period.start) < 0 || command.businessDate.compareTo(period.end) > 0)
            throw new IllegalArgumentException("Tanggal di luar periode pembukuan");

        Record event = command.eventId.isEmpty() ? null : store.find(command.eventId);
        if (!command.eventId.isEmpty() && (event == null || event.archived || !"EVENT".equals(event.kind) || !command.periodId.equals(event.periodId)))
            throw new IllegalArgumentException("Kegiatan tidak ditemukan dalam periode ini");
        Record target = command.targetId.isEmpty() ? null : store.find(command.targetId);
        boolean targetReversed = !command.targetId.isEmpty() && state.reversed.contains(command.targetId);

        if (metadata) {
            String expectedKind = command.kind.startsWith("EVENT") ? "EVENT" : "ASSET";
            if (target == null || !expectedKind.equals(target.kind) || !command.periodId.equals(target.periodId))
                throw new IllegalArgumentException("Catatan asal tidak ditemukan");
            if (target.revision != command.expectedRevision || target.archived)
                throw new IllegalArgumentException("Konflik: catatan sudah berubah. Muat ulang dan ulangi.");
        }
        long delta = requestedDelta, correctionDelta = 0;
        if ("VOID".equals(command.kind)) {
            if (target == null || Set.of("VOID", "EVENT", "META").contains(target.kind) ||
                    !command.account.equals(target.account) || !command.periodId.equals(target.periodId))
                throw new IllegalArgumentException("Transaksi asal tidak dapat dibatalkan");
            if (targetReversed) throw new IllegalArgumentException("Transaksi sudah dibatalkan");
            if (command.businessDate.compareTo(target.businessDate) < 0) throw new IllegalArgumentException("Tanggal pembatalan sebelum transaksi asal");
            delta = Math.negateExact(target.delta);
        }
        if (correction) {
            if (target == null || !command.kind.equals(target.kind) || !command.account.equals(target.account) || !command.periodId.equals(target.periodId) || targetReversed)
                throw new IllegalArgumentException("Koreksi konflik: catatan sudah dibatalkan/berubah atau akun/periode berbeda");
            correctionDelta = Math.negateExact(target.delta);
        }
        long nextBalance = 0;
        boolean touchesAccount = !"EVENT".equals(command.kind) && !metadata;
        if (touchesAccount) nextBalance = Checks.balance(state.balance(command.account), Math.addExact(delta, correctionDelta));

        // ---- All checks passed: from here on only mutations. ----
        if (!state.periods.containsKey(period.id)) state.periods.put(period.id, period);
        long revision = Math.addExact(state.revision, 1);
        Outcome outcome = new Outcome();
        Record record = new Record();
        record.id = command.id; record.kind = metadata ? "META" : command.kind; record.title = command.title.trim();
        record.businessDate = command.businessDate; record.account = command.account; record.periodId = command.periodId;
        record.eventId = command.eventId; record.targetId = command.targetId;
        record.receiptId = command.hasReceipt ? command.id : "";
        record.quantity = command.quantity; record.unitPrice = command.unitPrice; record.delta = delta;
        record.revision = revision; record.acceptedAt = now;
        if (correction && record.receiptId.isEmpty()) record.receiptId = target.receiptId;
        outcome.inserted.add(record);

        Record before = target == null ? null : target.copy();
        if (metadata) {
            Record changed = target.copy();
            if ("EVENT_EDIT".equals(command.kind)) { changed.title = record.title; changed.businessDate = record.businessDate; }
            else changed.archived = true;
            changed.revision = revision;
            outcome.updated.add(changed);
        }
        if (correctionDelta != 0) {
            Record undo = target.copy(); undo.id = command.id + "-reversal"; undo.kind = "VOID";
            undo.title = "Koreksi: " + command.title; undo.delta = correctionDelta; undo.targetId = target.id;
            undo.businessDate = command.businessDate; undo.revision = revision; undo.acceptedAt = now; undo.receiptId = ""; undo.archived = false;
            outcome.inserted.add(undo);
        }
        if (touchesAccount) state.balances.put(command.account, nextBalance);
        if (target != null && !metadata) state.reversed.add(command.targetId);
        state.revision = revision;

        Audit audit = new Audit();
        audit.recordId = command.id; audit.kind = command.kind; audit.revision = revision; audit.acceptedAt = now;
        audit.targetId = command.targetId; audit.before = before; audit.after = record;
        outcome.audit = audit;
        return outcome;
    }

    /** Opens or closes an accounting period. Closing blocks further postings (except archiving). */
    public static void setPeriodStatus(LedgerState state, String periodId, boolean open) {
        Snapshot.Period p = state.periods.get(periodId);
        if (p == null) throw new IllegalArgumentException("Periode tidak ditemukan");
        p.status = open ? "OPEN" : "CLOSED";
        state.revision = Math.addExact(state.revision, 1);
    }
}
