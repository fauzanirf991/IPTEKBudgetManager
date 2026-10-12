package com.fauzan.ukmbudgetmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Single sealed row holding balances, periods, the reversal index and the revision counter. */
@Entity(tableName = "meta")
public final class LedgerMeta {
    @PrimaryKey @NonNull public String name = "";
    public String payload = "";
}
