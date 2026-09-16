package com.fauzan.budget.core;

public final class Record {
    public String id = "", kind = "", title = "", businessDate = "", account = "", periodId = "";
    public String eventId = "", targetId = "", receiptPath = "", actorUid = "";
    public long delta, quantity, unitPrice, revision, acceptedAt;
    public boolean archived;
}
