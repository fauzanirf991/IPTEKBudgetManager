package com.fauzan.budget.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FinanceTest {
    private Command command(String kind, long amount) {
        Command c = new Command(); c.id = "op-1"; c.kind = kind; c.title = "Test"; c.periodId = "2026";
        c.businessDate = "2026-09-16"; c.account = kind.equals("CASH") ? "KAS" : "PAGUDANA"; c.amount = amount; return c;
    }
    private Record row(String id, String account, String date, long delta) {
        Record r = new Record(); r.id = id; r.account = account; r.kind = "CASH"; r.businessDate = date; r.delta = delta; return r;
    }
    @Test void expenseIsRecomputedAndTamperingRejected() {
        Command c = command("EXPENSE", 120000); c.eventId = "event1"; c.quantity = 60; c.unitPrice = 2000;
        assertEquals(-120000, c.validatedAmount()); c.amount = 1; assertThrows(IllegalArgumentException.class, c::validatedAmount);
    }
    @Test void rejectsInvalidMoneyAndAccount() {
        assertThrows(IllegalArgumentException.class, () -> command("CASH", 0).validatedAmount());
        Command c = command("CASH", 10); c.account = "PAGUDANA"; assertThrows(IllegalArgumentException.class, c::validatedAmount);
        assertThrows(IllegalArgumentException.class, () -> Checks.money(Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> Checks.balance(Long.MAX_VALUE, 1));
        assertThrows(IllegalArgumentException.class, () -> Checks.balance(0, -1));
    }
    @Test void validatesDatesAndPathIds() {
        assertThrows(IllegalArgumentException.class, () -> Checks.date("2026-02-30"));
        assertThrows(IllegalArgumentException.class, () -> Checks.id("../../members"));
    }
    @Test void reportUsesInclusiveDatesAndCorrectOpeningAcrossYears() {
        Snapshot s = new Snapshot(); s.revision = 7;
        s.records.add(row("a", "KAS", "2025-12-30", 100));
        s.records.add(row("b", "KAS", "2025-12-31", 50));
        s.records.add(row("c", "KAS", "2026-01-01", -20));
        s.records.add(row("d", "KAS", "2026-01-02", 999));
        Report report = Report.calculate(s, "2025-12-31", "2026-01-01", "ALL");
        var kas = report.accounts.get("KAS"); assertEquals(100, kas.opening); assertEquals(50, kas.inflow);
        assertEquals(20, kas.outflow); assertEquals(130, kas.closing); assertEquals(2, report.rows.size()); assertEquals(7, report.revision);
    }
    @Test void oneDayAndAccountSelectionExcludeOtherRecords() {
        Snapshot s = new Snapshot();
        s.records.add(row("a", "KAS", "2026-09-16", 10)); s.records.add(row("b", "PAGUDANA", "2026-09-16", 50));
        assertEquals(1, Report.calculate(s, "2026-09-16", "2026-09-16", "KAS").rows.size());
    }
    @Test void missingLedgerIsRejectedAndEmptyLedgerIsZero() {
        assertThrows(IllegalArgumentException.class, () -> Report.calculate(null, "2026-01-01", "2026-12-31", "ALL"));
        Snapshot s = new Snapshot(); assertEquals(0, Report.calculate(s, "2026-01-01", "2026-12-31", "ALL").accounts.get("KAS").closing);
        assertThrows(IllegalArgumentException.class, () -> Report.calculate(s, "2026-12-31", "2026-01-01", "ALL"));
    }
    @Test void originalSampleReconcilesWithoutCombiningKas() {
        Snapshot s = new Snapshot();
        s.records.add(row("fund", "PAGUDANA", "2020-06-16", 12000000));
        s.records.add(row("asset", "PAGUDANA", "2020-08-24", -1200000));
        s.records.add(row("expense", "PAGUDANA", "2020-08-25", -120000));
        s.records.add(row("cash", "KAS", "2020-08-17", 100000));
        Report report = Report.calculate(s, "2020-01-01", "2020-12-31", "ALL");
        assertEquals(10680000, report.accounts.get("PAGUDANA").closing); assertEquals(100000, report.accounts.get("KAS").closing);
    }
    @Test void reversalRemainsInAuditAndRecomputesReport() {
        Snapshot s = new Snapshot();
        s.records.add(row("a", "KAS", "2026-09-01", 100));
        s.records.add(row("b", "KAS", "2026-09-02", -25));
        Record reversal = row("c", "KAS", "2026-09-02", 25); reversal.kind = "VOID"; s.records.add(reversal);
        Report r = Report.calculate(s, "2026-09-01", "2026-09-30", "ALL"); assertEquals(100, r.accounts.get("KAS").closing); assertEquals(3, r.rows.size());
    }
}
