package com.fauzan.budget.server;

import com.fauzan.budget.core.Checks;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import java.time.LocalDate;
import java.util.Map;

/** Trusted operator tool. No public self-registration or client-assigned roles. */
public final class AdminTool {
    public static void main(String[] args) throws Exception {
        Server.initialize();
        if (args.length == 1 && args[0].equals("seed-demo")) {
            if (!System.getenv().getOrDefault("GOOGLE_CLOUD_PROJECT", "").startsWith("demo-") || System.getenv("FIREBASE_AUTH_EMULATOR_HOST") == null)
                throw new IllegalStateException("Demo accounts may only be created in local emulators");
            for (String role : new String[]{"BENDAHARA", "KETUA"}) {
                String uid = role.toLowerCase(), email = uid + "@iptek.test";
                try { FirebaseAuth.getInstance().getUser(uid); }
                catch (FirebaseAuthException e) {
                    if (e.getAuthErrorCode() != AuthErrorCode.USER_NOT_FOUND) throw e;
                    FirebaseAuth.getInstance().createUser(new UserRecord.CreateRequest().setUid(uid).setEmail(email).setPassword("LocalDemo123!").setEmailVerified(true));
                }
                membership("iptek", uid, role);
            }
            organization("iptek", "Asia/Jakarta");
            String year = Integer.toString(LocalDate.now().getYear()); period("iptek", year, year + "-01-01", year + "-12-31", "OPEN");
            System.out.println("Demo ready: bendahara@iptek.test / ketua@iptek.test, password LocalDemo123!");
        } else if (args.length == 3 && args[0].equals("organization")) organization(args[1], args[2]);
        else if (args.length == 4 && args[0].equals("membership")) membership(args[1], args[2], args[3]);
        else if (args.length == 6 && args[0].equals("period")) period(args[1], args[2], args[3], args[4], args[5]);
        else throw new IllegalArgumentException("Usage: seed-demo | organization ORG TIMEZONE | membership ORG FIREBASE_UID BENDAHARA/KETUA | period ORG ID START END OPEN/CLOSED");
        FirestoreClient.getFirestore().close();
    }
    private static void organization(String org, String timezone) throws Exception {
        Checks.id(org); java.time.ZoneId.of(timezone); var db = FirestoreClient.getFirestore(); var root = db.collection("organizations").document(org);
        db.runTransaction(tx -> {
            var existing = tx.get(root).get(); var kas = tx.get(root.collection("accounts").document("KAS")).get(); var funding = tx.get(root.collection("accounts").document("PAGUDANA")).get();
            if (!existing.exists()) tx.set(root, Map.of("revision", 0, "timezone", timezone));
            if (!kas.exists()) tx.set(kas.getReference(), Map.of("balance", 0, "revision", 0));
            if (!funding.exists()) tx.set(funding.getReference(), Map.of("balance", 0, "revision", 0));
            return true;
        }).get();
    }
    private static void membership(String org, String uid, String role) throws Exception {
        Checks.id(org); Checks.id(uid);
        if (!role.equals("BENDAHARA") && !role.equals("KETUA")) throw new IllegalArgumentException("Invalid role");
        FirebaseAuth.getInstance().getUser(uid);
        FirestoreClient.getFirestore().collection("organizations").document(org).collection("members").document(uid)
                .set(Map.of("role", role, "active", true)).get();
    }
    private static void period(String org, String id, String start, String end, String status) throws Exception {
        Checks.id(org); Checks.id(id);
        if (Checks.date(end).isBefore(Checks.date(start)) || !(status.equals("OPEN") || status.equals("CLOSED"))) throw new IllegalArgumentException("Invalid period");
        var root = FirestoreClient.getFirestore().collection("organizations").document(org);
        var ref = root.collection("periods").document(id);
        FirestoreClient.getFirestore().runTransaction(tx -> {
            var previous = tx.get(ref).get();
            var organization = tx.get(root).get();
            if (previous.exists() && (!start.equals(previous.getString("start")) || !end.equals(previous.getString("end"))))
                throw new IllegalArgumentException("Existing period boundaries are immutable");
            tx.set(ref, Map.of("start", start, "end", end, "status", status));
            if (organization.exists()) tx.update(root, "revision", Math.addExact(organization.getLong("revision") == null ? 0 : organization.getLong("revision"), 1));
            tx.set(root.collection("auditEvents").document(), Map.of("kind", "PERIOD_" + status, "periodId", id, "actorUid", "trusted-operator", "acceptedAt", System.currentTimeMillis()));
            return true;
        }).get();
    }
}
