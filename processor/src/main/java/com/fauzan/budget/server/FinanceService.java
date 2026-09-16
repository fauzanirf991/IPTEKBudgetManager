package com.fauzan.budget.server;

import com.fauzan.budget.core.Checks;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Record;
import com.fauzan.budget.core.Snapshot;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public final class FinanceService {
    private static final Gson JSON = new Gson();
    private final Firestore db;
    public FinanceService(Firestore db) { this.db = db; }
    public record Outcome(String id, String state, String message) {}
    public static final class Denied extends RuntimeException { public Denied() { super("Akses organisasi ditolak"); } }

    private static String role(DocumentSnapshot member) {
        String role = member.getString("role");
        if (!member.exists() || !Boolean.TRUE.equals(member.getBoolean("active")) ||
                !("BENDAHARA".equals(role) || "KETUA".equals(role))) throw new Denied();
        return role;
    }
    private DocumentReference org(String id) { Checks.id(id); return db.collection("organizations").document(id); }
    private static long number(DocumentSnapshot doc, String field) { Long n = doc.getLong(field); return n == null ? 0 : n; }
    private static String hash(Command c) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(JSON.toJson(c).getBytes(StandardCharsets.UTF_8)));
    }

    public Outcome submit(String organization, String uid, Command command) throws Exception {
        long requestedDelta = command.validatedAmount();
        boolean metadata = Set.of("EVENT_EDIT", "EVENT_ARCHIVE", "ASSET_ARCHIVE").contains(command.kind);
        String fingerprint = hash(command);
        DocumentReference root = org(organization);
        // Validate ownership before even inspecting uploaded objects.
        if (!"BENDAHARA".equals(role(root.collection("members").document(uid).get().get()))) throw new Denied();
        if (!command.receiptPath.isEmpty()) {
            String prefix = "organizations/" + organization + "/receipts/" + uid + "/" + command.id + "/";
            if (!command.receiptPath.equals(prefix + "receipt")) throw new IllegalArgumentException("Referensi bukti tidak sesuai pemilik");
            Blob blob = StorageClient.getInstance().bucket().get(command.receiptPath);
            if (blob == null || blob.getSize() <= 0 || blob.getSize() > 5 * 1024 * 1024 ||
                    !Set.of("image/jpeg", "image/png", "image/webp").contains(blob.getContentType()))
                throw new IllegalArgumentException("Bukti belum terunggah atau tidak valid");
        }
        return db.runTransaction(tx -> {
            DocumentSnapshot membership = tx.get(root.collection("members").document(uid)).get();
            if (!"BENDAHARA".equals(role(membership))) throw new Denied();
            DocumentReference op = root.collection("operationResults").document(command.id);
            DocumentSnapshot previous = tx.get(op).get();
            if (previous.exists()) {
                if (!uid.equals(previous.getString("actorUid")) || !fingerprint.equals(previous.getString("hash")))
                    throw new IllegalArgumentException("ID operasi sudah dipakai untuk data berbeda");
                return new Outcome(command.id, previous.getString("state"), previous.getString("message"));
            }
            DocumentSnapshot organizationDoc = tx.get(root).get();
            DocumentSnapshot occupiedId = tx.get(root.collection("records").document(command.id)).get();
            DocumentSnapshot occupiedUndoId = !command.targetId.isEmpty() && !metadata && !"VOID".equals(command.kind)
                    ? tx.get(root.collection("records").document(command.id + "-reversal")).get() : null;
            DocumentSnapshot period = tx.get(root.collection("periods").document(command.periodId)).get();
            DocumentSnapshot account = "EVENT".equals(command.kind) || metadata ? null : tx.get(root.collection("accounts").document(command.account)).get();
            DocumentSnapshot event = command.eventId.isEmpty() ? null : tx.get(root.collection("records").document(command.eventId)).get();
            DocumentSnapshot target = command.targetId.isEmpty() ? null : tx.get(root.collection("records").document(command.targetId)).get();
            DocumentSnapshot reversal = command.targetId.isEmpty() ? null : tx.get(root.collection("reversals").document(command.targetId)).get();
            String error = "";
            long delta = requestedDelta, nextBalance = 0, correctionDelta = 0;
            try {
                if (!organizationDoc.exists()) throw new IllegalArgumentException("Organisasi belum disiapkan");
                if (occupiedId.exists() || (occupiedUndoId != null && occupiedUndoId.exists())) throw new IllegalArgumentException("ID catatan sudah digunakan. Buat operasi baru.");
                String zone = organizationDoc.getString("timezone");
                if (Checks.date(command.businessDate).isAfter(LocalDate.now(ZoneId.of(zone == null ? "Asia/Jakarta" : zone))))
                    throw new IllegalArgumentException("Tanggal transaksi tidak boleh di masa depan");
                if (!period.exists() || (!"OPEN".equals(period.getString("status")) && !command.kind.endsWith("_ARCHIVE"))) throw new IllegalArgumentException("Periode sudah ditutup atau tidak tersedia");
                if (command.businessDate.compareTo(period.getString("start")) < 0 || command.businessDate.compareTo(period.getString("end")) > 0)
                    throw new IllegalArgumentException("Tanggal di luar periode pembukuan");
                if (event != null && (!event.exists() || Boolean.TRUE.equals(event.getBoolean("archived")) || !"EVENT".equals(event.getString("kind")) || !command.periodId.equals(event.getString("periodId"))))
                    throw new IllegalArgumentException("Kegiatan tidak ditemukan dalam periode ini");
                if (metadata) {
                    String expectedKind = command.kind.startsWith("EVENT") ? "EVENT" : "ASSET";
                    if (target == null || !target.exists() || !expectedKind.equals(target.getString("kind")) || !command.periodId.equals(target.getString("periodId")))
                        throw new IllegalArgumentException("Catatan asal tidak ditemukan");
                    if (number(target, "revision") != command.expectedRevision || Boolean.TRUE.equals(target.getBoolean("archived")))
                        throw new IllegalArgumentException("Konflik: catatan sudah berubah. Sinkronkan dan ulangi.");
                }
                if ("VOID".equals(command.kind)) {
                    if (target == null || !target.exists() || Set.of("VOID", "EVENT").contains(target.getString("kind")) ||
                            !command.account.equals(target.getString("account")) || !command.periodId.equals(target.getString("periodId")))
                        throw new IllegalArgumentException("Transaksi asal tidak dapat dibatalkan");
                    if (reversal.exists()) throw new IllegalArgumentException("Transaksi sudah dibatalkan");
                    if (command.businessDate.compareTo(target.getString("businessDate")) < 0) throw new IllegalArgumentException("Tanggal pembatalan sebelum transaksi asal");
                    delta = Math.negateExact(number(target, "delta"));
                }
                if (target != null && !metadata && !"VOID".equals(command.kind)) {
                    if (!target.exists() || !command.kind.equals(target.getString("kind")) || !command.account.equals(target.getString("account")) || !command.periodId.equals(target.getString("periodId")) || reversal.exists())
                        throw new IllegalArgumentException("Koreksi konflik: catatan sudah dibatalkan/berubah atau akun/periode berbeda");
                    correctionDelta = Math.negateExact(number(target, "delta"));
                }
                if (account != null) {
                    if (!account.exists()) throw new IllegalArgumentException("Akun belum disiapkan");
                    nextBalance = Checks.balance(number(account, "balance"), Math.addExact(delta, correctionDelta));
                }
            } catch (IllegalArgumentException | ArithmeticException e) { error = e.getMessage(); }
            long now = System.currentTimeMillis();
            if (!error.isEmpty()) {
                tx.set(op, Map.of("actorUid", uid, "hash", fingerprint, "state", "REJECTED", "message", error, "acceptedAt", now));
                return new Outcome(command.id, "REJECTED", error);
            }
            long revision = Math.addExact(number(organizationDoc, "revision"), 1);
            Record record = new Record();
            record.id = command.id; record.kind = metadata ? "META" : command.kind; record.title = command.title.trim();
            record.businessDate = command.businessDate; record.account = command.account; record.periodId = command.periodId;
            record.eventId = command.eventId; record.targetId = command.targetId; record.receiptPath = command.receiptPath;
            record.quantity = command.quantity; record.unitPrice = command.unitPrice; record.delta = delta;
            record.actorUid = uid; record.revision = revision; record.acceptedAt = now;
            if (target != null && !metadata && !"VOID".equals(command.kind) && record.receiptPath.isEmpty()) record.receiptPath = Objects.toString(target.getString("receiptPath"), "");
            tx.set(root.collection("records").document(record.id), record);
            if (metadata) {
                if ("EVENT_EDIT".equals(command.kind)) tx.update(target.getReference(), "title", record.title, "businessDate", record.businessDate, "revision", revision);
                else tx.update(target.getReference(), "archived", true, "revision", revision);
            }
            if (correctionDelta != 0) {
                Record undo = target.toObject(Record.class); undo.id = command.id + "-reversal"; undo.kind = "VOID";
                undo.title = "Koreksi: " + command.title; undo.delta = correctionDelta; undo.targetId = target.getId();
                undo.businessDate = command.businessDate; undo.revision = revision; undo.acceptedAt = now; undo.actorUid = uid; undo.receiptPath = "";
                tx.set(root.collection("records").document(undo.id), undo);
            }
            if (account != null) tx.update(account.getReference(), "balance", nextBalance, "revision", revision);
            if (target != null && !metadata) tx.set(root.collection("reversals").document(command.targetId), Map.of("operationId", command.id, "actorUid", uid));
            tx.update(root, "revision", revision);
            Map<String, Object> audit = new HashMap<>(Map.of("actorUid", uid, "kind", command.kind, "recordId", command.id, "revision", revision, "acceptedAt", now));
            if (target != null) { audit.put("targetId", target.getId()); audit.put("before", target.getData()); }
            audit.put("after", record); tx.set(root.collection("auditEvents").document(command.id), audit);
            tx.set(op, Map.of("actorUid", uid, "hash", fingerprint, "state", "ACCEPTED", "message", "Tersinkron", "acceptedAt", now));
            return new Outcome(command.id, "ACCEPTED", "Tersinkron");
        }).get();
    }

    public Snapshot snapshot(String organization, String uid) throws Exception {
        DocumentReference root = org(organization);
        return db.runTransaction(tx -> {
            Snapshot result = new Snapshot();
            result.role = role(tx.get(root.collection("members").document(uid)).get());
            DocumentSnapshot organizationDoc = tx.get(root).get();
            result.organization = organization; result.ownerUid = uid; result.revision = number(organizationDoc, "revision");
            String zone = organizationDoc.getString("timezone");
            if (zone != null) result.timezone = zone;
            QuerySnapshot records = tx.get(root.collection("records").limit(5001)).get();
            // A bounded full snapshot is suitable for the initial UKM deployment. Never return partial totals.
            if (records.size() > 5000) throw new IllegalArgumentException("Lebih dari 5000 catatan: unduhan bertahap diperlukan sebelum laporan lengkap tersedia");
            for (DocumentSnapshot doc : records) result.records.add(doc.toObject(Record.class));
            for (DocumentSnapshot doc : tx.get(root.collection("periods")).get()) {
                Snapshot.Period p = doc.toObject(Snapshot.Period.class); p.id = doc.getId(); result.periods.add(p);
            }
            result.complete = true; result.downloadedAt = System.currentTimeMillis();
            return result;
        }).get();
    }
}
