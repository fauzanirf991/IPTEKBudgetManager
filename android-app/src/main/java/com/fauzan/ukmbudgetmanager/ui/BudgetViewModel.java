package com.fauzan.ukmbudgetmanager.ui;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import androidx.work.WorkManager;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.BuildConfig;
import com.fauzan.ukmbudgetmanager.data.*;
import com.google.firebase.auth.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BudgetViewModel extends AndroidViewModel {
    public final MutableLiveData<String> userId = new MutableLiveData<>("");
    public final MutableLiveData<String> message = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    public final LiveData<Snapshot> snapshot;
    public final LiveData<List<Outbox>> queue;
    public final SavedStateHandle saved;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final BudgetDao dao;
    private final FirebaseAuth.AuthStateListener listener;
    private com.google.firebase.firestore.ListenerRegistration revisionListener;
    public BudgetViewModel(@NonNull Application app, SavedStateHandle saved) {
        super(app); this.saved = saved; dao = BudgetDatabase.get(app).dao();
        snapshot = Transformations.switchMap(userId, uid -> uid.isEmpty() ? new MutableLiveData<>(null) :
            Transformations.map(dao.observeSnapshot(SyncWorker.owner(uid)), cache -> {
                try { return cache == null ? null : ProcessorClient.JSON.fromJson(LocalVault.open(cache.payload), Snapshot.class); }
                catch (Exception e) { message.postValue(e.getMessage()); return null; }
            }));
        queue = Transformations.switchMap(userId, uid -> uid.isEmpty() ? new MutableLiveData<>(Collections.emptyList()) : dao.observeQueue(SyncWorker.owner(uid)));
        listener = auth -> {
            if (revisionListener != null) { revisionListener.remove(); revisionListener = null; }
            FirebaseUser user = auth.getCurrentUser(); String uid = user == null ? "" : user.getUid();
            if (!uid.equals(userId.getValue())) { saved.set("draft", null); saved.set("receipt", null); }
            userId.setValue(uid);
            if (user != null) {
                SyncWorker.schedule(app, uid);
                revisionListener = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("organizations").document(BuildConfig.ORGANIZATION)
                        .addSnapshotListener((document, error) -> {
                            if (!Objects.equals(uid, userId.getValue())) return;
                            if (error != null) {
                                if (error.getCode() == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    io.execute(() -> dao.clearSnapshot(SyncWorker.owner(uid)));
                                    message.postValue("Hak akses belum tersedia atau dicabut. Hubungi pengelola.");
                                }
                                return;
                            }
                            if (document != null && document.exists() && !document.getMetadata().isFromCache()) {
                                Long revision = document.getLong("revision"); Snapshot local = currentSnapshot();
                                if (revision != null && (local == null || revision > local.revision)) SyncWorker.schedule(app, uid);
                            }
                        });
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(listener);
    }
    public Snapshot currentSnapshot() {
        Snapshot data = snapshot.getValue();
        return data != null && Objects.equals(data.ownerUid, userId.getValue()) && BuildConfig.ORGANIZATION.equals(data.organization) ? data : null;
    }
    public List<Outbox> currentQueue() {
        String uid = userId.getValue();
        if (uid == null || uid.isEmpty() || queue.getValue() == null) return Collections.emptyList();
        List<Outbox> result = new ArrayList<>();
        for (Outbox item : queue.getValue()) if (SyncWorker.owner(uid).equals(item.owner)) result.add(item);
        return result;
    }
    public boolean editor() { Snapshot data = currentSnapshot(); return data != null && "BENDAHARA".equals(data.role); }
    public void login(String email, String password) {
        busy.setValue(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener(task -> {
            busy.setValue(false); message.setValue(task.isSuccessful() ? "Berhasil masuk. Mengunduh data…" : "Tidak dapat masuk. Periksa email, sandi, dan koneksi.");
        });
    }
    public void reset(String email) {
        if (email.trim().isEmpty()) { message.setValue("Isi email terlebih dahulu"); return; }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim()).addOnCompleteListener(task ->
            message.setValue(task.isSuccessful() ? "Jika akun tersedia, instruksi pemulihan dikirim ke email." : "Pemulihan belum tersedia. Periksa koneksi/email."));
    }
    public void sync() {
        String uid = userId.getValue();
        if (uid != null && !uid.isEmpty()) { SyncWorker.schedule(getApplication(), uid); message.setValue("Sinkronisasi dijadwalkan; berjalan saat koneksi tersedia."); }
    }
    public void logout() {
        String uid = userId.getValue();
        if (uid != null) WorkManager.getInstance(getApplication()).cancelUniqueWork("sync-" + SyncWorker.owner(uid));
        FirebaseAuth.getInstance().signOut();
    }
    public void save(Command c, String receipt) {
        if (!editor() || Boolean.TRUE.equals(busy.getValue())) { message.setValue("Unduh hak akses Bendahara terlebih dahulu"); return; }
        String uid = userId.getValue(); String owner = SyncWorker.owner(uid);
        busy.setValue(true);
        io.execute(() -> {
            File staged = null;
            try {
                c.validatedAmount();
                Outbox item = new Outbox(); item.id = c.id; item.owner = owner; item.createdAt = System.currentTimeMillis();
                if (receipt != null && !receipt.isEmpty()) {
                    Uri uri = Uri.parse(receipt); String type = getApplication().getContentResolver().getType(uri);
                    if (!Arrays.asList("image/jpeg", "image/png", "image/webp").contains(type)) throw new IllegalArgumentException("Gunakan gambar JPEG, PNG, atau WebP (maks. 5 MB)");
                    byte[] bytes;
                    try (InputStream in = getApplication().getContentResolver().openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        if (in == null) throw new IOException("Bukti tidak dapat dibaca");
                        byte[] buffer = new byte[8192]; int n;
                        while ((n = in.read(buffer)) != -1) { if (out.size() + n > 5 * 1024 * 1024) throw new IOException("Bukti maksimal 5 MB"); out.write(buffer, 0, n); }
                        bytes = out.toByteArray();
                    }
                    if (bytes.length == 0) throw new IOException("Bukti kosong");
                    File dir = new File(getApplication().getFilesDir(), "staging/" + uid); if (!dir.exists() && !dir.mkdirs()) throw new IOException("Tidak dapat menyimpan bukti");
                    staged = new File(dir, c.id + ".sealed"); Files.write(staged.toPath(), LocalVault.encrypt(bytes));
                    item.receiptFile = staged.getAbsolutePath(); item.receiptType = type;
                    c.receiptPath = "organizations/" + BuildConfig.ORGANIZATION + "/receipts/" + uid + "/" + c.id + "/receipt";
                }
                if (FirebaseAuth.getInstance().getCurrentUser() == null || !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) throw new IllegalStateException("Sesi berubah. Simpan kembali setelah masuk.");
                item.payload = LocalVault.seal(ProcessorClient.JSON.toJson(c)); dao.enqueue(item);
                SyncWorker.schedule(getApplication(), uid);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> { saved.set("draft", null); saved.set("receipt", null); message.setValue("Tersimpan di perangkat. Akan disinkronkan saat terhubung."); });
            } catch (Exception e) { if (staged != null) staged.delete(); message.postValue(e.getMessage()); }
            finally { busy.postValue(false); }
        });
    }
    public void discardRejected(Outbox item) {
        if (!"REJECTED".equals(item.state) || !item.owner.equals(SyncWorker.owner(userId.getValue()))) return;
        io.execute(() -> { dao.delete(item.owner, item.id); if (!item.receiptFile.isEmpty()) new File(item.receiptFile).delete(); });
    }
    @Override protected void onCleared() { FirebaseAuth.getInstance().removeAuthStateListener(listener); if (revisionListener != null) revisionListener.remove(); io.shutdown(); }
}
