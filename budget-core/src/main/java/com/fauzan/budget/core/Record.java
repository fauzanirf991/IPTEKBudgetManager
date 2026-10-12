package com.fauzan.budget.core;

public final class Record {
    public String id = "", kind = "", title = "", businessDate = "", account = "", periodId = "";
    public String eventId = "", targetId = "";
    /** Id under which the encrypted receipt file is stored on the device; empty when there is no receipt. */
    public String receiptId = "";
    public long delta, quantity, unitPrice, revision, acceptedAt;
    public boolean archived;

    public Record copy() {
        Record c = new Record();
        c.id = id; c.kind = kind; c.title = title; c.businessDate = businessDate; c.account = account; c.periodId = periodId;
        c.eventId = eventId; c.targetId = targetId; c.receiptId = receiptId;
        c.delta = delta; c.quantity = quantity; c.unitPrice = unitPrice; c.revision = revision; c.acceptedAt = acceptedAt; c.archived = archived;
        return c;
    }
}
