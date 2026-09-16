package com.fauzan.ukmbudgetmanager.data;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.BudgetApplication;
import com.fauzan.ukmbudgetmanager.BuildConfig;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.*;
import com.google.firebase.storage.*;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }
    public static String owner(String uid) { return uid + ":" + BuildConfig.ORGANIZATION; }
    public static void schedule(Context context, String uid) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setInputData(new Data.Builder().putString("uid", uid).build())
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork("sync-" + owner(uid), ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }
    private boolean current(String uid) {
        FirebaseUser active = FirebaseAuth.getInstance().getCurrentUser();
        return !isStopped() && active != null && uid.equals(active.getUid());
    }
    @NonNull @Override public Result doWork() {
        if (!BudgetApplication.configured) return Result.failure();
        String uid = getInputData().getString("uid");
        if (uid == null || !current(uid)) return Result.success();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String owner = owner(uid); BudgetDatabase db = BudgetDatabase.get(getApplicationContext()); BudgetDao dao = db.dao();
        ProcessorClient api = new ProcessorClient();
        try {
            for (Outbox item : dao.queue(owner)) {
                if (!current(uid)) return Result.success();
                if (item.state.equals("ACCEPTED") || item.state.equals("REJECTED")) continue;
                dao.state(owner, item.id, "SYNCING", "Menyinkronkan…");
                Command command = ProcessorClient.JSON.fromJson(LocalVault.open(item.payload), Command.class);
                try {
                    if (!item.receiptFile.isEmpty()) {
                        File file = new File(item.receiptFile);
                        if (!file.isFile()) { dao.state(owner, item.id, "REJECTED", "Bukti lokal hilang. Buat ulang catatan."); continue; }
                        StorageReference reference = FirebaseStorage.getInstance().getReference(command.receiptPath);
                        boolean uploaded = false;
                        try { Tasks.await(reference.getMetadata(), 30, TimeUnit.SECONDS); uploaded = true; }
                        catch (Exception e) {
                            Throwable cause = e.getCause() == null ? e : e.getCause();
                            if (!(cause instanceof StorageException se) || se.getErrorCode() != StorageException.ERROR_OBJECT_NOT_FOUND) throw e;
                        }
                        if (!uploaded) Tasks.await(reference.putBytes(LocalVault.decrypt(java.nio.file.Files.readAllBytes(file.toPath())), new StorageMetadata.Builder().setContentType(item.receiptType).build()), 120, TimeUnit.SECONDS);
                    }
                    if (!current(uid)) return Result.success();
                    JsonObject body = new JsonObject(); body.add("command", ProcessorClient.JSON.toJsonTree(command));
                    JsonObject result = api.post(user, "/v1/operations", body);
                    String state = result.get("state").getAsString();
                    if (!state.equals("ACCEPTED") && !state.equals("REJECTED")) throw new IllegalStateException("Status layanan tidak valid");
                    dao.state(owner, item.id, state, result.get("message").getAsString());
                } catch (ProcessorClient.ApiException e) {
                    if (e.code == 400 || e.code == 409) { dao.state(owner, item.id, "REJECTED", e.getMessage()); continue; }
                    throw e;
                }
            }
            if (!current(uid)) return Result.success();
            Snapshot snapshot = ProcessorClient.JSON.fromJson(api.post(user, "/v1/snapshot", new JsonObject()), Snapshot.class);
            if (!snapshot.complete || !uid.equals(snapshot.ownerUid) || !BuildConfig.ORGANIZATION.equals(snapshot.organization)) throw new IllegalStateException("Unduhan laporan belum lengkap");
            List<Outbox> acknowledged = dao.queue(owner).stream().filter(o -> o.state.equals("ACCEPTED") && snapshot.records.stream().anyMatch(r -> r.id.equals(o.id))).collect(java.util.stream.Collectors.toList());
            CachedSnapshot cache = new CachedSnapshot(); cache.owner = owner; cache.payload = LocalVault.seal(ProcessorClient.JSON.toJson(snapshot));
            db.runInTransaction(() -> { dao.cache(cache); for (Outbox item : acknowledged) dao.delete(owner, item.id); });
            for (Outbox item : acknowledged) if (!item.receiptFile.isEmpty()) {
                File source = new File(item.receiptFile), cacheDir = new File(getApplicationContext().getFilesDir(), "receipts/" + uid);
                try {
                    if ((cacheDir.exists() || cacheDir.mkdirs()) && source.isFile()) {
                        java.nio.file.Files.copy(source.toPath(), new File(cacheDir, item.id + ".sealed").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        source.delete();
                    }
                } catch (java.io.IOException ignored) { /* Remote copy is confirmed; retain encrypted staging file if local caching fails. */ }
            }
            return Result.success();
        } catch (Exception e) {
            int code = e instanceof ProcessorClient.ApiException apiError ? apiError.code : 0;
            if (code == 403) dao.clearSnapshot(owner);
            for (Outbox item : dao.queue(owner)) if (!item.state.equals("ACCEPTED") && !item.state.equals("REJECTED"))
                dao.state(owner, item.id, code == 401 || code == 403 ? "AUTH" : "PENDING", code == 401 || code == 403 ? "Masuk kembali / periksa hak akses" : "Tersimpan di perangkat. Menunggu koneksi.");
            Data output = new Data.Builder().putString("error", e.getMessage() == null ? "Sinkronisasi belum selesai" : e.getMessage()).build();
            return code == 401 || code == 403 || code == 400 ? Result.failure(output) : Result.retry();
        }
    }
}
