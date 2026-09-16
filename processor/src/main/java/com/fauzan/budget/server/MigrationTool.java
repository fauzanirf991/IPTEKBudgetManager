package com.fauzan.budget.server;

import com.fauzan.budget.core.*;
import com.fauzan.budget.core.Record;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Import a reviewed, normalized legacy extract. Dry run is the default. */
public final class MigrationTool {
    private static final Gson JSON = new Gson();
    public static final class Input {
        public String importId, timezone = "Asia/Jakarta";
        public List<Snapshot.Period> periods = new ArrayList<>();
        public List<Record> records = new ArrayList<>();
        public Map<String, Long> expectedBalances = new HashMap<>();
        public Map<String, String> receiptFiles = new HashMap<>();
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3 || (args.length == 3 && !args[2].equals("--apply")))
            throw new IllegalArgumentException("Usage: SOURCE_JSON ORGANIZATION [--apply]; default is dry run");
        Path source = Path.of(args[0]).toAbsolutePath().normalize(); Checks.id(args[1]);
        byte[] raw = Files.readAllBytes(source); Input input = JSON.fromJson(new String(raw, java.nio.charset.StandardCharsets.UTF_8), Input.class);
        validate(input); String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        Map<String, byte[]> images = new HashMap<>(); Map<String, String> contentTypes = new HashMap<>();
        for (var attachment : new TreeMap<>(input.receiptFiles).entrySet()) {
            if (input.records.stream().noneMatch(r -> r.id.equals(attachment.getKey()))) throw new IllegalArgumentException("Receipt has no record: " + attachment.getKey());
            Path file = source.getParent().resolve(attachment.getValue()).normalize().toRealPath();
            if (!file.startsWith(source.getParent().toRealPath())) throw new IllegalArgumentException("Receipt outside import directory");
            if (Files.size(file) < 1 || Files.size(file) > 5 * 1024 * 1024) throw new IllegalArgumentException("Invalid receipt size");
            String type = Files.probeContentType(file);
            if (!Set.of("image/jpeg", "image/png", "image/webp").contains(type == null ? "" : type)) throw new IllegalArgumentException("Unsupported receipt type");
            byte[] bytes = Files.readAllBytes(file); images.put(attachment.getKey(), bytes); contentTypes.put(attachment.getKey(), type);
            digest += HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        final String fingerprint = digest;
        System.out.println("Validated " + input.records.size() + " records, " + input.periods.size() + " periods, " + images.size() + " receipts. Expected balances: " + input.expectedBalances);
        System.out.println("Records without receipt files are preserved as missing evidence; no placeholder receipt is invented.");
        if (args.length != 3) { System.out.println("DRY RUN: no Firebase writes. Review extract and missing evidence before --apply."); return; }
        Server.initialize(); Firestore db = FirestoreClient.getFirestore(); var root = db.collection("organizations").document(args[1]);
        var existing = root.collection("imports").document(input.importId).get().get();
        if (existing.exists()) {
            if (!fingerprint.equals(existing.getString("fingerprint"))) throw new IllegalArgumentException("Import ID already has different content");
            System.out.println("Import already completed; nothing written."); db.close(); return;
        }
        if (!root.collection("records").limit(1).get().get().isEmpty()) throw new IllegalArgumentException("Target already has financial records; no receipts uploaded");
        for (Record r : input.records) if (images.containsKey(r.id)) {
            r.receiptPath = "organizations/" + args[1] + "/receipts/legacy-import/" + r.id + "/receipt";
            var bucket = StorageClient.getInstance().bucket();
            var staged = bucket.get(r.receiptPath);
            String md5 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(images.get(r.id)));
            if (staged != null) {
                if (!md5.equals(staged.getMd5()) || !contentTypes.get(r.id).equals(staged.getContentType())) throw new IllegalArgumentException("Staged receipt conflicts with import; reconcile before retry");
            } else bucket.getStorage().create(com.google.cloud.storage.BlobInfo.newBuilder(bucket.getName(), r.receiptPath).setContentType(contentTypes.get(r.id)).build(), images.get(r.id), com.google.cloud.storage.Storage.BlobTargetOption.doesNotExist());
        }
        db.runTransaction(tx -> {
            var imported = tx.get(root.collection("imports").document(input.importId)).get();
            var org = tx.get(root).get(); var rows = tx.get(root.collection("records").limit(1)).get();
            if (imported.exists()) {
                if (!fingerprint.equals(imported.getString("fingerprint"))) throw new IllegalArgumentException("Import ID conflict");
                return true;
            }
            if (!rows.isEmpty() || (org.exists() && org.getLong("revision") != null && org.getLong("revision") != 0)) throw new IllegalArgumentException("Target organization already has financial data");
            long revision = 0, now = System.currentTimeMillis();
            for (Record r : input.records) { r.revision = ++revision; r.acceptedAt = now; tx.set(root.collection("records").document(r.id), r); }
            for (Snapshot.Period p : input.periods) tx.set(root.collection("periods").document(p.id), p);
            for (String account : new String[]{"KAS", "PAGUDANA"}) tx.set(root.collection("accounts").document(account), Map.of("balance", input.expectedBalances.get(account), "revision", revision));
            tx.set(root, Map.of("timezone", input.timezone, "revision", revision));
            tx.set(root.collection("imports").document(input.importId), Map.of("fingerprint", fingerprint, "records", input.records.size(), "acceptedAt", now));
            tx.set(root.collection("auditEvents").document(input.importId), Map.of("kind", "LEGACY_IMPORT", "actorUid", "trusted-operator", "acceptedAt", now, "records", input.records.size()));
            return true;
        }).get();
        System.out.println("Import committed; retain source backup and receipt checksum manifest."); db.close();
    }
    static void validate(Input input) {
        Checks.id(input.importId); java.time.ZoneId.of(input.timezone);
        if (input.records.size() + input.periods.size() > 400) throw new IllegalArgumentException("Use a reviewed staged importer above 400 documents; refusing partial import");
        Map<String, Snapshot.Period> periods = new HashMap<>(); Set<String> ids = new HashSet<>(); Map<String, Record> events = new HashMap<>();
        for (Snapshot.Period p : input.periods) {
            Checks.id(p.id); if (Checks.date(p.end).isBefore(Checks.date(p.start)) || !Set.of("OPEN", "CLOSED").contains(p.status)) throw new IllegalArgumentException("Invalid period");
            if (periods.put(p.id, p) != null) throw new IllegalArgumentException("Duplicate period");
        }
        for (Record r : input.records) { Checks.id(r.id); if (!ids.add(r.id)) throw new IllegalArgumentException("Duplicate legacy ID"); if (r.kind.equals("EVENT")) events.put(r.id, r); }
        for (Record r : input.records) {
            Snapshot.Period p = periods.get(r.periodId); Checks.date(r.businessDate);
            if (p == null || r.businessDate.compareTo(p.start) < 0 || r.businessDate.compareTo(p.end) > 0) throw new IllegalArgumentException("Record outside period: " + r.id);
            if (r.actorUid.isEmpty()) r.actorUid = "legacy-unknown";
            if (!r.receiptPath.isEmpty()) throw new IllegalArgumentException("Supply local receiptFiles, not external receipt paths");
            Command c = new Command(); c.id = r.id; c.kind = r.kind; c.title = r.title; c.account = r.account; c.businessDate = r.businessDate; c.periodId = r.periodId;
            c.quantity = r.quantity; c.unitPrice = r.unitPrice; c.amount = Math.abs(r.delta); c.eventId = r.eventId;
            if (r.kind.equals("VOID")) throw new IllegalArgumentException("Legacy reversals require manual reconciliation");
            if (c.validatedAmount() != r.delta) throw new IllegalArgumentException("Invalid signed amount: " + r.id);
            if (r.kind.equals("EXPENSE") && (!events.containsKey(r.eventId) || !events.get(r.eventId).periodId.equals(r.periodId))) throw new IllegalArgumentException("Orphan expense: " + r.id);
        }
        Snapshot snapshot = new Snapshot(); snapshot.complete = true; snapshot.records = input.records;
        Report report = Report.calculate(snapshot, "0001-01-01", "9999-12-31", "ALL");
        for (String account : new String[]{"KAS", "PAGUDANA"}) {
            Long expected = input.expectedBalances.get(account);
            if (expected == null || expected < 0 || expected != report.accounts.get(account).closing) throw new IllegalArgumentException("Balance discrepancy: " + account);
        }
    }
}
