package com.fauzan.budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure Java reporting over the complete local ledger snapshot. */
public final class Report {
    public static final class Totals {
        public long opening, inflow, outflow, closing;
    }
    public final Map<String, Totals> accounts = new LinkedHashMap<>();
    public final List<Record> rows = new ArrayList<>();
    public long revision;

    public static Report calculate(Snapshot data, String start, String end, String account) {
        LocalDate from = Checks.date(start), until = Checks.date(end);
        if (until.isBefore(from)) throw new IllegalArgumentException("Tanggal akhir sebelum tanggal awal");
        if (data == null) throw new IllegalArgumentException("Data belum tersedia.");
        if (!"ALL".equals(account)) Checks.account(account);
        Report report = new Report(); report.revision = data.revision;
        report.accounts.put("PAGUDANA", new Totals()); report.accounts.put("KAS", new Totals());
        for (Record r : data.records) {
            if ("EVENT".equals(r.kind) || "META".equals(r.kind) || (!"ALL".equals(account) && !account.equals(r.account))) continue;
            Checks.account(r.account);
            LocalDate date = Checks.date(r.businessDate);
            Totals t = report.accounts.get(r.account);
            if (date.isBefore(from)) t.opening = Math.addExact(t.opening, r.delta);
            else if (!date.isAfter(until)) {
                if (r.delta >= 0) t.inflow = Math.addExact(t.inflow, r.delta);
                else t.outflow = Math.addExact(t.outflow, Math.negateExact(r.delta));
                report.rows.add(r);
            }
        }
        for (Totals t : report.accounts.values()) t.closing = Math.subtractExact(Math.addExact(t.opening, t.inflow), t.outflow);
        report.rows.sort(Comparator.comparing((Record r) -> r.businessDate).reversed().thenComparing(r -> r.id));
        return report;
    }
}
