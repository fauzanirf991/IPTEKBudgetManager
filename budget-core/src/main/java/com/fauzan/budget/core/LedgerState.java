package com.fauzan.budget.core;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Small, always-loaded part of the ledger: balances, periods, reversal index and the revision counter. */
public final class LedgerState {
    public long revision;
    public Map<String, Long> balances = new LinkedHashMap<>();
    public Map<String, Snapshot.Period> periods = new TreeMap<>();
    /** Ids of records that have already been reversed or replaced. */
    public Set<String> reversed = new HashSet<>();

    public static LedgerState fresh() {
        LedgerState s = new LedgerState();
        s.balances.put("PAGUDANA", 0L); s.balances.put("KAS", 0L);
        return s;
    }
    public long balance(String account) { Long v = balances.get(account); return v == null ? 0 : v; }

    /** Builds (without storing) an OPEN period spanning one calendar year. */
    public static Snapshot.Period newYearPeriod(int year) {
        Snapshot.Period p = new Snapshot.Period(); p.id = Integer.toString(year); p.status = "OPEN";
        p.start = LocalDate.of(year, 1, 1).toString(); p.end = LocalDate.of(year, 12, 31).toString();
        return p;
    }
    /** Returns the period for a calendar year, creating an OPEN one on first use. */
    public Snapshot.Period yearPeriod(int year) {
        return periods.computeIfAbsent(Integer.toString(year), id -> newYearPeriod(year));
    }
}
