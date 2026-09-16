package com.fauzan.ukmbudgetmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "snapshots")
public final class CachedSnapshot {
    @PrimaryKey @NonNull public String owner = "";
    public String payload = "";
}
