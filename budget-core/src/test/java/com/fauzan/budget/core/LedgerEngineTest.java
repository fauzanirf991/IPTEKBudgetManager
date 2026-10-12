package com.fauzan.budget.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Ports of the former server integration scenarios, now against the local pure-Java engine. */
class LedgerEngineTest {
    private LedgerState state; private final Map<String, Record> store = new HashMap<>();
    private final LocalDate today = LocalDate.of(2026, 10, 12);
    private final String yesterday = today.minusDays(1).toString();

    @BeforeEach void setup() { state = LedgerState.fresh(); store.clear(); }
    private LedgerEngine.Outcome post(Command c) {
        var out = LedgerEngine.apply(state, store::get, c, today, 1000L);
        for (Record r : out.inserted) store.put(r.id, r);
        for (Record r : out.updated) store.put(r.id, r);
        return out;
    }
    private Command cash(long amount) {
        Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = "CASH"; c.amount = amount; c.title = "Iuran";
        c.account = "KAS"; c.periodId = "2026"; c.businessDate = yesterday; return c;
    }
    private Command asset(long amount) { Command c = cash(amount); c.kind = "ASSET"; return c; }
    private void rejected(Command c, String fragment) {
        long revision = state.revision; int records = store.size(); long kas = state.balance("KAS");
        var e = assertThrows(IllegalArgumentException.class, () -> post(c));
        assertTrue(e.getMessage().contains(fragment), e.getMessage());
        assertEquals(revision, state.revision); assertEquals(records, store.size()); assertEquals(kas, state.balance("KAS"));
    }

    @Test void postingUpdatesBalanceRevisionAndCreatesYearPeriod() {
        assertTrue(state.periods.isEmpty());
        post(cash(100));
        assertEquals(100, state.balance("KAS")); assertEquals(0, state.balance("PAGUDANA")); assertEquals(1, state.revision);
        assertEquals("OPEN", state.periods.get("2026").status);
        assertEquals("2026-01-01", state.periods.get("2026").start);
    }
    @Test void reusingAnIdNeverOverwritesExistingRecord() {
        Command c = cash(100); post(c);
        rejected(c, "ID catatan sudah digunakan");
        Record imported = new Record(); imported.id = "imported"; imported.title = "Preserved"; store.put("imported", imported);
        Command clash = cash(5); clash.id = "imported"; rejected(clash, "ID catatan sudah digunakan");
        assertEquals("Preserved", store.get("imported").title);
        assertEquals(100, state.balance("KAS"));
    }
    @Test void overdraftIsRejectedAtomically() {
        post(cash(100)); post(asset(80));
        rejected(asset(80), "Saldo tidak mencukupi");
        assertEquals(20, state.balance("KAS"));
    }
    @Test void closedPeriodRejectsPostingsAndReopenAllowsThem() {
        post(cash(100)); LedgerEngine.setPeriodStatus(state, "2026", false);
        rejected(cash(10), "Periode sudah ditutup");
        LedgerEngine.setPeriodStatus(state, "2026", true); post(cash(10));
        assertEquals(110, state.balance("KAS"));
        assertThrows(IllegalArgumentException.class, () -> LedgerEngine.setPeriodStatus(state, "1999", false));
    }
    @Test void futureDatesAndWrongYearPeriodAreRejected() {
        Command future = cash(10); future.businessDate = today.plusDays(1).toString(); rejected(future, "masa depan");
        Command wrongPeriod = cash(10); wrongPeriod.periodId = "2025"; rejected(wrongPeriod, "Periode sudah ditutup atau tidak tersedia");
        assertTrue(state.periods.isEmpty(), "a failed posting must not leave a period behind");
    }
    @Test void reversalIsAuditedAndCannotBeRepeated() {
        post(cash(100)); Command asset = asset(25); post(asset);
        Command reverse = cash(0); reverse.kind = "VOID"; reverse.targetId = asset.id; reverse.amount = 0; reverse.title = "Salah input";
        var out = post(reverse);
        assertEquals(100, state.balance("KAS")); assertEquals(asset.id, out.audit.targetId); assertNotNull(out.audit.before);
        Command again = cash(0); again.kind = "VOID"; again.targetId = asset.id; again.amount = 0; again.title = "Lagi";
        rejected(again, "sudah dibatalkan");
        assertEquals(3, store.size());
    }
    @Test void expenseNeedsAnEventInSamePeriod() {
        post(cash(100));
        Command e = cash(20); e.kind = "EXPENSE"; e.eventId = "missing"; e.quantity = 2; e.unitPrice = 10;
        rejected(e, "Kegiatan tidak ditemukan");
        Command event = cash(0); event.kind = "EVENT"; event.account = ""; event.amount = 0; event.title = "Seminar"; post(event);
        e.eventId = event.id; post(e);
        assertEquals(80, state.balance("KAS"));
    }
    @Test void correctionReversesAndReplacesAtomically() {
        post(cash(100)); Command asset = asset(80); asset.hasReceipt = true; post(asset);
        Command correction = asset(95); correction.targetId = asset.id; post(correction);
        assertEquals(5, state.balance("KAS")); assertEquals(4, store.size());
        assertEquals(asset.id, store.get(correction.id).receiptId, "correction keeps the original receipt");
        assertEquals("VOID", store.get(correction.id + "-reversal").kind);
        Command twice = asset(10); twice.targetId = asset.id; rejected(twice, "Koreksi konflik");
    }
    @Test void correctionThatOverdrawsLeavesLedgerUntouched() {
        post(cash(100)); Command asset = asset(80); post(asset);
        Command correction = asset(150); correction.targetId = asset.id; rejected(correction, "Saldo tidak mencukupi");
        assertFalse(state.reversed.contains(asset.id));
    }
    @Test void eventMetadataUsesVersionChecksAndDoesNotAlterBalances() {
        Command event = cash(0); event.kind = "EVENT"; event.account = ""; event.amount = 0; post(event);
        Command edit = cash(0); edit.kind = "EVENT_EDIT"; edit.account = ""; edit.amount = 0; edit.targetId = event.id; edit.expectedRevision = 1; edit.title = "Updated event";
        post(edit);
        assertEquals("Updated event", store.get(event.id).title); assertEquals(2, store.get(event.id).revision);
        Command stale = cash(0); stale.kind = "EVENT_EDIT"; stale.account = ""; stale.amount = 0; stale.targetId = event.id; stale.expectedRevision = 1; stale.title = "Stale";
        rejected(stale, "Konflik");
        assertEquals(0, state.balance("KAS"));
    }
    @Test void archivedEventCannotReceiveExpenses() {
        post(cash(100)); Command event = cash(0); event.kind = "EVENT"; event.account = ""; event.amount = 0; post(event);
        Command archive = cash(0); archive.kind = "EVENT_ARCHIVE"; archive.account = ""; archive.amount = 0; archive.targetId = event.id; archive.expectedRevision = store.get(event.id).revision; archive.title = "Selesai"; post(archive);
        assertTrue(store.get(event.id).archived);
        Command e = cash(20); e.kind = "EXPENSE"; e.eventId = event.id; e.quantity = 2; e.unitPrice = 10; rejected(e, "Kegiatan tidak ditemukan");
    }
    @Test void archivingAnAssetKeepsTheCash() {
        post(cash(100)); Command asset = asset(40); post(asset);
        Command archive = cash(0); archive.kind = "ASSET_ARCHIVE"; archive.account = ""; archive.amount = 0; archive.targetId = asset.id; archive.expectedRevision = store.get(asset.id).revision; archive.title = "Rusak"; post(archive);
        assertEquals(60, state.balance("KAS")); assertTrue(store.get(asset.id).archived);
    }
    @Test void stateSurvivesJsonRoundTrip() {
        post(cash(100)); post(asset(30)); LedgerEngine.setPeriodStatus(state, "2026", false);
        var gson = new com.google.gson.Gson();
        LedgerState copy = gson.fromJson(gson.toJson(state), LedgerState.class);
        assertEquals(70, copy.balance("KAS")); assertEquals("CLOSED", copy.periods.get("2026").status); assertEquals(state.revision, copy.revision);
    }
}
