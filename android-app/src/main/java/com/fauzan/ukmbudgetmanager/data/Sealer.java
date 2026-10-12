package com.fauzan.ukmbudgetmanager.data;

import android.util.Base64;
import java.nio.charset.StandardCharsets;

/** At-rest encryption seam: the Android Keystore in production, a fake in unit tests. */
public interface Sealer {
    byte[] encrypt(byte[] plain) throws Exception;
    byte[] decrypt(byte[] encrypted) throws Exception;

    default String seal(String value) {
        try { return Base64.encodeToString(encrypt(value.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP); }
        catch (Exception e) { throw new IllegalStateException("Penyimpanan aman tidak tersedia", e); }
    }
    default String open(String value) {
        try { return new String(decrypt(Base64.decode(value, Base64.NO_WRAP)), StandardCharsets.UTF_8); }
        catch (Exception e) { throw new IllegalStateException("Data lokal tidak dapat dibaca. Jangan hapus data aplikasi.", e); }
    }
}
