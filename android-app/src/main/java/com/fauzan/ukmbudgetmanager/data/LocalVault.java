package com.fauzan.ukmbudgetmanager.data;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

/** Payloads and staged receipts are encrypted with a non-exportable device key. */
public final class LocalVault {
    private static final String ALIAS = "iptek-local-v2";
    private static synchronized javax.crypto.SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        if (!store.containsAlias(ALIAS)) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
            generator.generateKey();
        }
        return (javax.crypto.SecretKey) store.getKey(ALIAS, null);
    }
    public static byte[] encrypt(byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key());
        byte[] body = cipher.doFinal(plain), iv = cipher.getIV();
        byte[] result = new byte[1 + iv.length + body.length]; result[0] = (byte) iv.length;
        System.arraycopy(iv, 0, result, 1, iv.length); System.arraycopy(body, 0, result, 1 + iv.length, body.length);
        return result;
    }
    public static byte[] decrypt(byte[] encrypted) throws Exception {
        if (encrypted.length < 30 || encrypted[0] != 12) throw new IllegalArgumentException("Data lokal tidak dapat dibaca");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, encrypted, 1, 12));
        return cipher.doFinal(encrypted, 13, encrypted.length - 13);
    }
    public static String seal(String value) {
        try { return Base64.encodeToString(encrypt(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)), Base64.NO_WRAP); }
        catch (Exception e) { throw new IllegalStateException("Penyimpanan aman tidak tersedia", e); }
    }
    public static String open(String value) {
        try { return new String(decrypt(Base64.decode(value, Base64.NO_WRAP)), java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { throw new IllegalStateException("Data lokal terkunci; jangan hapus data yang belum sinkron", e); }
    }
}
