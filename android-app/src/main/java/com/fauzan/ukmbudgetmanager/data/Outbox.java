package com.fauzan.ukmbudgetmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "outbox", primaryKeys = {"owner", "id"})
public final class Outbox {
    @NonNull public String owner = "";
    @NonNull public String id = "";
    public String payload = "", state = "PENDING", message = "Menunggu sinkronisasi";
    public String receiptFile = "", receiptType = "";
    public long createdAt;
}
