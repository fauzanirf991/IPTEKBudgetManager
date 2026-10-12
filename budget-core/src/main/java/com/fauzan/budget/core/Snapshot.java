package com.fauzan.budget.core;

import java.util.ArrayList;
import java.util.List;

/** Read-only view of the whole local ledger at one revision; used for reports and lists. */
public final class Snapshot {
    public long revision;
    public List<Record> records = new ArrayList<>();
    public List<Period> periods = new ArrayList<>();
    public static final class Period {
        public String id = "", start = "", end = "", status = "";
    }
}
