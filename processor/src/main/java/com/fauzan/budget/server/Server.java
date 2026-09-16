package com.fauzan.budget.server;

import com.fauzan.budget.core.Command;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.*;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public final class Server {
    private static final Gson JSON = new Gson();
    public static void initialize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;
        String project = System.getenv().getOrDefault("GOOGLE_CLOUD_PROJECT", "demo-iptek");
        boolean emulator = System.getenv("FIRESTORE_EMULATOR_HOST") != null;
        boolean authEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null;
        boolean storageEmulator = System.getenv("STORAGE_EMULATOR_HOST") != null;
        if ((emulator || authEmulator || storageEmulator) && !(emulator && authEmulator && storageEmulator))
            throw new IllegalStateException("Never mix emulated authentication/storage with a production database");
        if (!emulator && project.startsWith("demo-")) throw new IllegalStateException("Production requires an explicit real GOOGLE_CLOUD_PROJECT");
        if (emulator && (!project.startsWith("demo-") || System.getenv("FIREBASE_AUTH_EMULATOR_HOST") == null || System.getenv("STORAGE_EMULATOR_HOST") == null))
            throw new IllegalStateException("Local mode requires a demo project and all Firebase emulators");
        GoogleCredentials credentials = emulator ? GoogleCredentials.create(new com.google.auth.oauth2.AccessToken("owner", null)) : GoogleCredentials.getApplicationDefault();
        FirebaseApp.initializeApp(FirebaseOptions.builder().setProjectId(project).setCredentials(credentials)
                .setStorageBucket(System.getenv().getOrDefault("STORAGE_BUCKET", project + ".appspot.com")).build());
    }
    public static void main(String[] args) throws Exception {
        initialize();
        FinanceService service = new FinanceService(FirestoreClient.getFirestore());
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"))), 0);
        server.createContext("/health", exchange -> send(exchange, 200, Map.of("status", "ok")));
        server.createContext("/v1/", exchange -> {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) { send(exchange, 405, Map.of("error", "POST required")); return; }
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");
                if (authorization == null || !authorization.startsWith("Bearer ")) { send(exchange, 401, Map.of("error", "Silakan masuk")); return; }
                String uid;
                try { uid = FirebaseAuth.getInstance().verifyIdToken(authorization.substring(7), true).getUid(); }
                catch (Exception e) { send(exchange, 401, Map.of("error", "Sesi berakhir. Silakan masuk kembali.")); return; }
                byte[] bytes = exchange.getRequestBody().readNBytes(65537);
                if (bytes.length > 65536) { send(exchange, 413, Map.of("error", "Permintaan terlalu besar")); return; }
                JsonObject body = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                String organization = body.get("organization").getAsString();
                switch (exchange.getRequestURI().getPath()) {
                    case "/v1/snapshot" -> send(exchange, 200, service.snapshot(organization, uid));
                    case "/v1/operations" -> {
                        JsonObject payload = body.getAsJsonObject("command");
                        for (String field : new String[]{"schemaVersion", "amount", "quantity", "unitPrice", "expectedRevision"})
                            if (payload.has(field)) payload.get(field).getAsBigDecimal().longValueExact();
                        Command command = JSON.fromJson(payload, Command.class);
                        send(exchange, 200, service.submit(organization, uid, command));
                    }
                    default -> send(exchange, 404, Map.of("error", "Not found"));
                }
            } catch (Exception e) {
                Throwable cause = e;
                while (cause.getCause() != null) cause = cause.getCause();
                if (cause instanceof FinanceService.Denied) send(exchange, 403, Map.of("error", cause.getMessage()));
                else if (cause instanceof IllegalArgumentException || cause instanceof JsonParseException || cause instanceof ArithmeticException || cause instanceof NullPointerException)
                    send(exchange, 400, Map.of("error", cause.getMessage() == null ? "Data tidak valid" : cause.getMessage()));
                else { System.err.println("Request failed: " + cause.getClass().getSimpleName()); send(exchange, 503, Map.of("error", "Layanan belum tersedia. Coba lagi.")); }
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(8));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1)));
        server.start(); System.out.println("IPTEK processor listening on " + server.getAddress().getPort());
    }
    private static void send(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] bytes = JSON.toJson(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }
}
