package com.fauzan.budget.server;

import com.fauzan.budget.core.Command;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "FIRESTORE_EMULATOR_HOST", matches = ".+")
class FinanceIntegrationTest {
    private Firestore db; private FinanceService service; private String org;
    @BeforeEach void setup() throws Exception {
        Server.initialize(); db = FirestoreClient.getFirestore(); service = new FinanceService(db); org = "test-" + UUID.randomUUID();
        var root = db.collection("organizations").document(org);
        root.set(Map.of("revision", 0, "timezone", "Asia/Jakarta")).get();
        root.collection("members").document("editor").set(Map.of("role", "BENDAHARA", "active", true)).get();
        root.collection("members").document("viewer").set(Map.of("role", "KETUA", "active", true)).get();
        root.collection("periods").document("period").set(Map.of("start", "2000-01-01", "end", "2099-12-31", "status", "OPEN")).get();
        root.collection("accounts").document("KAS").set(Map.of("balance", 0)).get();
        root.collection("accounts").document("PAGUDANA").set(Map.of("balance", 0)).get();
    }
    private Command cash(long amount) { Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = "CASH"; c.amount = amount; c.title = "Iuran"; c.account = "KAS"; c.periodId = "period"; c.businessDate = LocalDate.now().minusDays(1).toString(); return c; }
    @Test void retryPostsExactlyOnceAndRejectsChangedPayload() throws Exception {
        Command c = cash(100); assertEquals("ACCEPTED", service.submit(org, "editor", c).state()); service.submit(org, "editor", c);
        assertEquals(1, service.snapshot(org, "viewer").records.size());
        assertEquals(100, db.collection("organizations").document(org).collection("accounts").document("KAS").get().get().getLong("balance"));
        c.amount = 200; assertThrows(Exception.class, () -> service.submit(org, "editor", c));
    }
    @Test void deniesViewerAndUnknownMembership() {
        assertThrows(Exception.class, () -> service.submit(org, "viewer", cash(100)));
        assertThrows(Exception.class, () -> service.snapshot(org, "outsider"));
    }
    @Test void concurrentSpendingCannotOverdraw() throws Exception {
        service.submit(org, "editor", cash(100));
        Command a = cash(80); a.kind = "ASSET"; Command b = cash(80); b.kind = "ASSET";
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> service.submit(org, "editor", a)); var second = pool.submit(() -> service.submit(org, "editor", b));
            List<String> states = List.of(first.get().state(), second.get().state());
            assertEquals(1, states.stream().filter("ACCEPTED"::equals).count());
            assertEquals(20, db.collection("organizations").document(org).collection("accounts").document("KAS").get().get().getLong("balance"));
        } finally { pool.shutdownNow(); }
    }
    @Test void closedPeriodRejectsOfflineArrivalAndResultIsStable() throws Exception {
        Command c = cash(100); var period = db.collection("organizations").document(org).collection("periods").document("period");
        period.update("status", "CLOSED").get(); assertEquals("REJECTED", service.submit(org, "editor", c).state());
        period.update("status", "OPEN").get(); assertEquals("REJECTED", service.submit(org, "editor", c).state());
        assertEquals(0, service.snapshot(org, "editor").records.size());
    }
    @Test void reversalIsAuditedAndCannotBeRepeated() throws Exception {
        service.submit(org, "editor", cash(100)); Command asset = cash(25); asset.kind = "ASSET"; service.submit(org, "editor", asset);
        Command reverse = cash(0); reverse.kind = "VOID"; reverse.targetId = asset.id;
        assertEquals("ACCEPTED", service.submit(org, "editor", reverse).state());
        reverse.id = UUID.randomUUID().toString(); assertEquals("REJECTED", service.submit(org, "editor", reverse).state());
        assertEquals(3, service.snapshot(org, "viewer").records.size());
    }
    @Test void expenseNeedsAnEventInSamePeriod() throws Exception {
        service.submit(org, "editor", cash(100)); Command e = cash(20); e.kind = "EXPENSE"; e.eventId = "missing"; e.quantity = 2; e.unitPrice = 10;
        assertEquals("REJECTED", service.submit(org, "editor", e).state());
    }
    @Test void correctionReversesAndReplacesAtomically() throws Exception {
        service.submit(org, "editor", cash(100)); Command asset = cash(80); asset.kind = "ASSET"; service.submit(org, "editor", asset);
        Command correction = cash(95); correction.kind = "ASSET"; correction.targetId = asset.id;
        assertEquals("ACCEPTED", service.submit(org, "editor", correction).state());
        assertEquals(4, service.snapshot(org, "viewer").records.size());
        assertEquals(5, db.collection("organizations").document(org).collection("accounts").document("KAS").get().get().getLong("balance"));
        correction.id = UUID.randomUUID().toString(); assertEquals("REJECTED", service.submit(org, "editor", correction).state());
    }
    @Test void eventMetadataUsesVersionChecksAndDoesNotAlterBalances() throws Exception {
        Command event = cash(0); event.kind = "EVENT"; event.account = ""; service.submit(org, "editor", event);
        Command edit = cash(0); edit.kind = "EVENT_EDIT"; edit.account = ""; edit.targetId = event.id; edit.expectedRevision = 1; edit.title = "Updated event";
        assertEquals("ACCEPTED", service.submit(org, "editor", edit).state());
        edit.id = UUID.randomUUID().toString(); assertEquals("REJECTED", service.submit(org, "editor", edit).state());
        assertEquals("Updated event", service.snapshot(org, "viewer").records.stream().filter(r -> r.id.equals(event.id)).findFirst().orElseThrow().title);
        assertEquals(0, db.collection("organizations").document(org).collection("accounts").document("KAS").get().get().getLong("balance"));
    }
    @Test void importedRecordCannotBeOverwrittenByReusingItsId() throws Exception {
        Command c = cash(100);
        db.collection("organizations").document(org).collection("records").document(c.id).set(Map.of("title", "Preserved import", "delta", 77)).get();
        assertEquals("REJECTED", service.submit(org, "editor", c).state());
        assertEquals("Preserved import", db.collection("organizations").document(org).collection("records").document(c.id).get().get().getString("title"));
    }
}
