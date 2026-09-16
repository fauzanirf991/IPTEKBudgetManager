package com.fauzan.budget.core;

import java.util.ArrayList;
import java.util.List;

/** Complete organization snapshot at a single transaction revision; never a partial page. */
public final class Snapshot {
    public String role = "", organization = "", ownerUid = "", timezone = "Asia/Jakarta";
    public long revision, downloadedAt;
    public boolean complete;
    public List<Record> records = new ArrayList<>();
    public List<Period> periods = new ArrayList<>();
    public static final class Period {
        public String id = "", start = "", end = "", status = "";
    }
}
