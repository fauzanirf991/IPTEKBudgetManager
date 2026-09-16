package com.fauzan.ukmbudgetmanager.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ReceiptCache {
    private static final java.util.concurrent.ExecutorService IO = Executors.newSingleThreadExecutor();
    public static void load(Context context, String uid, String id, String path, Consumer<byte[]> success, Consumer<String> failure) {
        Context app = context.getApplicationContext();
        IO.execute(() -> {
            Handler main = new Handler(Looper.getMainLooper());
            try {
                File dir = new File(app.getFilesDir(), "receipts/" + uid);
                if (!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Penyimpanan bukti tidak tersedia");
                File file = new File(dir, id + ".sealed"); byte[] bytes;
                if (file.isFile()) bytes = LocalVault.decrypt(Files.readAllBytes(file.toPath()));
                else {
                    bytes = Tasks.await(FirebaseStorage.getInstance().getReference(path).getBytes(5 * 1024 * 1024), 45, TimeUnit.SECONDS);
                    Files.write(file.toPath(), LocalVault.encrypt(bytes));
                }
                file.setLastModified(System.currentTimeMillis()); trim(dir, file);
                byte[] result = bytes; main.post(() -> success.accept(result));
            } catch (Exception e) { main.post(() -> failure.accept("Bukti belum tersedia offline. Hubungkan internet untuk mengunduh.")); }
        });
    }
    private static void trim(File directory, File keep) {
        File[] files = directory.listFiles(); if (files == null) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified)); long total = 0;
        for (File f : files) total += f.length();
        for (File f : files) if (total > 30L * 1024 * 1024 && !f.equals(keep)) { long size = f.length(); if (f.delete()) total -= size; }
    }
}
