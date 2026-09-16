package com.fauzan.ukmbudgetmanager.data;

import com.fauzan.ukmbudgetmanager.BuildConfig;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class ProcessorClient {
    public static final Gson JSON = new Gson();
    public static final class ApiException extends IOException {
        public final int code;
        public ApiException(int code, String message) { super(message); this.code = code; }
    }
    public JsonObject post(FirebaseUser user, String route, JsonObject body) throws Exception {
        String token = Tasks.await(user.getIdToken(false), 30, TimeUnit.SECONDS).getToken();
        HttpURLConnection connection = (HttpURLConnection) new URL(BuildConfig.PROCESSOR_URL + route).openConnection();
        connection.setRequestMethod("POST"); connection.setConnectTimeout(15000); connection.setReadTimeout(45000);
        connection.setInstanceFollowRedirects(false); connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        body.addProperty("organization", BuildConfig.ORGANIZATION);
        try {
            try (OutputStream out = connection.getOutputStream()) { out.write(JSON.toJson(body).getBytes(StandardCharsets.UTF_8)); }
            int code = connection.getResponseCode();
            InputStream input = code < 400 ? connection.getInputStream() : connection.getErrorStream();
            if (input == null) throw new ApiException(code, "Respons layanan kosong");
            String text;
            try (input; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int length;
                while ((length = input.read(buffer)) != -1) {
                    if (bytes.size() + length > 12 * 1024 * 1024) throw new IOException("Respons terlalu besar");
                    bytes.write(buffer, 0, length);
                }
                text = bytes.toString(StandardCharsets.UTF_8.name());
            }
            JsonObject result;
            try { result = JsonParser.parseString(text).getAsJsonObject(); }
            catch (Exception e) { throw new IOException("Respons layanan tidak valid"); }
            if (code >= 300) throw new ApiException(code, result.has("error") ? result.get("error").getAsString() : "Layanan tidak tersedia");
            return result;
        } finally { connection.disconnect(); }
    }
}
