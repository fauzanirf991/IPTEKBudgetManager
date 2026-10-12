package com.fauzan.ukmbudgetmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Append-only audit trail (before/after images) for every accepted posting. */
@Entity(tableName = "audit")
public final class AuditEntry {
    @PrimaryKey @NonNull public String recordId = "";
    public long revision;
    public String payload = "";
}
