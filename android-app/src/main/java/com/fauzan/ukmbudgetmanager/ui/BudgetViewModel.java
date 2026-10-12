package com.fauzan.ukmbudgetmanager.ui;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.data.*;
import com.fauzan.ukmbudgetmanager.security.LockState;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class BudgetViewModel extends AndroidViewModel {
    public final MutableLiveData<String> message = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    /** Mirrors {@link LockState}; ledger data is neither decoded nor exposed while this is false. */
    public final MutableLiveData<Boolean> unlocked = new MutableLiveData<>(LockState.INSTANCE.isUnlocked());
    public final MediatorLiveData<Snapshot> snapshot = new MediatorLiveData<>();
    public final SavedStateHandle saved;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LedgerRepository repository;
    private final LiveData<List<LedgerRecord>> rows;
    private final LiveData<LedgerMeta> meta;
    private final Runnable lockListener = () -> unlocked.postValue(LockState.INSTANCE.isUnlocked());

    public BudgetViewModel(@NonNull Application app, SavedStateHandle saved) {
        super(app); this.saved = saved;
        BudgetDatabase db = BudgetDatabase.get(app);
        repository = new LedgerRepository(db, LocalVault.SEALER, new ReceiptStore(app.getFilesDir(), LocalVault.SEALER));
        rows = db.dao().observeRecords(); meta = db.dao().observeMeta("main");
        snapshot.addSource(rows, value -> rebuild()); snapshot.addSource(meta, value -> rebuild()); snapshot.addSource(unlocked, value -> rebuild());
        LockState.INSTANCE.addListener(lockListener);
    }
    private void rebuild() {
        if (!Boolean.TRUE.equals(unlocked.getValue())) {
            repository.clearCache(); saved.set("draft", null); saved.set("receipt", null); snapshot.setValue(null); return;
        }
        List<LedgerRecord> currentRows = rows.getValue(); LedgerMeta currentMeta = meta.getValue();
        if (currentRows == null) return;
        io.execute(() -> {
            try { snapshot.postValue(repository.snapshot(currentRows, currentMeta)); }
            catch (Exception e) { message.postValue(e.getMessage()); }
        });
    }
    public Snapshot currentSnapshot() { return Boolean.TRUE.equals(unlocked.getValue()) ? snapshot.getValue() : null; }

    /** Validates and stores a posting on the device. Rule violations are reported immediately via {@link #message}. */
    public void save(Command c, String receipt, Runnable onSuccess) {
        if (!Boolean.TRUE.equals(unlocked.getValue()) || Boolean.TRUE.equals(busy.getValue())) return;
        busy.setValue(true);
        io.execute(() -> {
            try {
                c.validatedAmount();
                byte[] bytes = receipt == null || receipt.isEmpty() ? null : readReceipt(Uri.parse(receipt));
                repository.post(c, bytes);
                main.post(() -> { saved.set("draft", null); saved.set("receipt", null); message.setValue("Tersimpan di perangkat."); if (onSuccess != null) onSuccess.run(); });
            } catch (Exception e) { message.postValue(e.getMessage() == null ? "Tidak dapat menyimpan" : e.getMessage()); }
            finally { busy.postValue(false); }
        });
    }
    private byte[] readReceipt(Uri uri) throws IOException {
        String type = getApplication().getContentResolver().getType(uri);
        if (!Arrays.asList("image/jpeg", "image/png", "image/webp").contains(type)) throw new IllegalArgumentException("Gunakan gambar JPEG, PNG, atau WebP (maks. 5 MB)");
        try (InputStream in = getApplication().getContentResolver().openInputStream(uri); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IOException("Bukti tidak dapat dibaca");
            byte[] buffer = new byte[8192]; int n;
            while ((n = in.read(buffer)) != -1) { if (out.size() + n > 5 * 1024 * 1024) throw new IOException("Bukti maksimal 5 MB"); out.write(buffer, 0, n); }
            if (out.size() == 0) throw new IOException("Bukti kosong");
            return out.toByteArray();
        }
    }
    public void loadReceipt(String receiptId, Consumer<byte[]> success, Consumer<String> failure) {
        io.execute(() -> {
            try {
                byte[] bytes = repository.receipt(receiptId);
                main.post(() -> { if (bytes == null) failure.accept("Bukti tidak ditemukan di perangkat ini."); else success.accept(bytes); });
            } catch (Exception e) { main.post(() -> failure.accept(e.getMessage())); }
        });
    }
    public void setPeriodOpen(String periodId, boolean open) {
        io.execute(() -> {
            try { repository.setPeriodOpen(periodId, open); message.postValue(open ? "Periode " + periodId + " dibuka." : "Periode " + periodId + " ditutup."); }
            catch (Exception e) { message.postValue(e.getMessage()); }
        });
    }
    @Override protected void onCleared() { LockState.INSTANCE.removeListener(lockListener); io.shutdown(); }
}
