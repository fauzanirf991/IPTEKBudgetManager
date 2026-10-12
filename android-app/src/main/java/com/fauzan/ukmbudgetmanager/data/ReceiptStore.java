package com.fauzan.ukmbudgetmanager.data;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Receipt images, encrypted, in app-private storage: {@code files/receipts/<receiptId>.sealed}. */
public final class ReceiptStore {
    private final File dir; private final Sealer sealer;
    public ReceiptStore(File filesDir, Sealer sealer) { this.dir = new File(filesDir, "receipts"); this.sealer = sealer; }
    private File file(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) throw new IllegalArgumentException("ID bukti tidak valid");
        return new File(dir, id + ".sealed");
    }
    /** Writes via a temp file and atomic rename so a crash never leaves a half-written receipt. */
    public void write(String id, byte[] bytes) {
        try {
            if (!dir.exists() && !dir.mkdirs()) throw new IOException("Tidak dapat menyimpan bukti");
            File target = file(id), temp = new File(dir, id + ".tmp");
            Files.write(temp.toPath(), sealer.encrypt(bytes));
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        catch (Exception e) { throw new IllegalStateException("Penyimpanan aman tidak tersedia", e); }
    }
    /** Returns the decrypted image or null when this device has no such receipt. */
    public byte[] read(String id) {
        File f = file(id);
        if (!f.isFile()) return null;
        try { return sealer.decrypt(Files.readAllBytes(f.toPath())); }
        catch (Exception e) { throw new IllegalStateException("Bukti tidak dapat dibaca", e); }
    }
    public void delete(String id) { file(id).delete(); File temp = new File(dir, id + ".tmp"); temp.delete(); }
}
