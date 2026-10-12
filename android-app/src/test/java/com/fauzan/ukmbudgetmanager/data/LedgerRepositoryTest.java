package com.fauzan.ukmbudgetmanager.data;

import android.app.Application;
import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Report;
import com.fauzan.budget.core.Snapshot;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class LedgerRepositoryTest {
    /** Stand-in for the Android Keystore (unavailable under Robolectric): reversible, but not plaintext. */
    private static final class FakeSealer implements Sealer {
        boolean failLargeEncrypt;
        @Override public byte[] encrypt(byte[] plain) {
            if (failLargeEncrypt && plain.length >= 10_000) throw new IllegalStateException("disk failure");
            byte[] out = plain.clone(); for (int i = 0; i < out.length; i++) out[i] ^= 0x5A; return out;
        }
        @Override public byte[] decrypt(byte[] encrypted) { return encrypt(encrypted); }
    }
    private static final String DB = "ledger-test";
    private Context context; private FakeSealer sealer; private File files; private BudgetDatabase db; private LedgerRepository repo;
    private final LocalDate date = LocalDate.now().minusDays(1);

    @Before public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DB);
        files = Files.createTempDirectory("receipts-test").toFile(); sealer = new FakeSealer(); open();
    }
    @After public void tearDown() { db.close(); context.deleteDatabase(DB); }
    private void open() {
        db = Room.databaseBuilder(context, BudgetDatabase.class, DB).allowMainThreadQueries().build();
        repo = new LedgerRepository(db, sealer, new ReceiptStore(files, sealer));
    }
    private void reopen() { db.close(); open(); }
    private Command command(String kind, long amount) {
        Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = kind; c.title = "Test"; c.amount = amount;
        c.account = kind.equals("CASH") ? "KAS" : kind.equals("FUNDING") ? "PAGUDANA" : "KAS";
        c.periodId = Integer.toString(date.getYear()); c.businessDate = date.toString(); return c;
    }
    private Snapshot snapshot() { return repo.snapshot(db.dao().records(), db.dao().meta("main")); }
    private long kasClosing() { return Report.calculate(snapshot(), "0001-01-01", LocalDate.now().toString(), "KAS").accounts.get("KAS").closing; }

    @Test public void postingSurvivesReopenAndIsAudited() {
        repo.post(command("CASH", 100), null); repo.post(command("ASSET", 30), null);
        reopen();
        assertEquals(2, snapshot().records.size()); assertEquals(70, kasClosing()); assertEquals(2, db.dao().auditCount());
        assertEquals(2, snapshot().revision); assertEquals("OPEN", snapshot().periods.get(0).status);
    }
    @Test public void rowsAreSealedNotPlaintext() {
        Command c = command("CASH", 100); c.title = "Iuran rahasia"; repo.post(c, null);
        assertFalse(db.dao().record(c.id).payload.contains("rahasia"));
        assertFalse(db.dao().meta("main").payload.contains("KAS"));
        assertEquals("Iuran rahasia", snapshot().records.get(0).title);
    }
    @Test public void rejectedPostingLeavesNothingBehind() {
        repo.post(command("CASH", 100), null);
        Command overdraft = command("ASSET", 500);
        try { repo.post(overdraft, new byte[]{1, 2, 3}); fail(); } catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("Saldo")); }
        assertEquals(1, snapshot().records.size()); assertEquals(1, db.dao().auditCount()); assertEquals(100, kasClosing());
        assertNull(repo.receipt(overdraft.id));
    }
    @Test public void receiptIsStoredEncryptedAndReadable() throws Exception {
        Command c = command("CASH", 100); byte[] image = "fake-image-bytes".getBytes(StandardCharsets.UTF_8);
        repo.post(c, image);
        assertArrayEquals(image, repo.receipt(c.id)); assertEquals(c.id, snapshot().records.get(0).receiptId);
        byte[] raw = Files.readAllBytes(new File(new File(files, "receipts"), c.id + ".sealed").toPath());
        assertFalse(new String(raw, StandardCharsets.UTF_8).contains("fake-image"));
    }
    @Test public void rejectedDuplicateNeverOverwritesExistingReceipt() {
        Command c = command("CASH", 100); byte[] original = {1, 1, 1}; repo.post(c, original);
        try { repo.post(c, new byte[]{9, 9, 9}); fail(); } catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("ID catatan")); }
        assertArrayEquals(original, repo.receipt(c.id)); assertEquals(1, snapshot().records.size());
    }
    @Test public void failureWhileStoringReceiptRollsBackTheWholePosting() {
        sealer.failLargeEncrypt = true; Command c = command("CASH", 100);
        try { repo.post(c, new byte[10_000]); fail(); } catch (IllegalStateException expected) { /* receipt could not be stored */ }
        sealer.failLargeEncrypt = false;
        assertTrue(db.dao().records().isEmpty()); assertNull(db.dao().meta("main")); assertEquals(0, db.dao().auditCount()); assertNull(repo.receipt(c.id));
        repo.post(c, null); assertEquals(1, snapshot().records.size());
    }
    @Test public void closedPeriodBlocksPostingUntilReopened() {
        repo.post(command("CASH", 100), null);
        String period = snapshot().periods.get(0).id; repo.setPeriodOpen(period, false);
        try { repo.post(command("CASH", 10), null); fail(); } catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("ditutup")); }
        repo.setPeriodOpen(period, true); repo.post(command("CASH", 10), null); assertEquals(110, kasClosing());
    }
    @Test public void correctionKeepsReceiptAndReplacesAmount() {
        repo.post(command("CASH", 100), null);
        Command asset = command("ASSET", 40); repo.post(asset, new byte[]{7, 7});
        Command fix = command("ASSET", 50); fix.targetId = asset.id; repo.post(fix, null);
        assertEquals(50, kasClosing()); assertArrayEquals(new byte[]{7, 7}, repo.receipt(snapshot().records.stream().filter(r -> r.id.equals(fix.id)).findFirst().get().receiptId));
    }
}
