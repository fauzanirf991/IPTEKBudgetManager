package com.fauzan.ukmbudgetmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** One ledger record. Lookup columns are plain; the full record is sealed in {@code payload}. */
@Entity(tableName = "records", indices = {@Index("businessDate")})
public final class LedgerRecord {
    @PrimaryKey @NonNull public String id = "";
    public String kind = "", businessDate = "", periodId = "", payload = "";
}
