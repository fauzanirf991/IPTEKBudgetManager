package com.fauzan.ukmbudgetmanager.data;

import android.app.Application;
import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class OfflineDatabaseTest {
    private Context context;
    @Before public void setup() { context = ApplicationProvider.getApplicationContext(); context.deleteDatabase("outbox-test"); }
    private BudgetDatabase open() { return Room.databaseBuilder(context, BudgetDatabase.class, "outbox-test").allowMainThreadQueries().build(); }
    @Test public void queueSurvivesDatabaseReopenAndIsIsolatedByOwner() {
        BudgetDatabase first = open(); Outbox item = new Outbox(); item.owner = "alice:iptek"; item.id = "durable"; item.payload = "encrypted-payload"; item.receiptFile = "/private/staged-receipt";
        first.dao().enqueue(item); first.dao().state(item.owner, item.id, "SYNCING", "in flight"); first.close();
        BudgetDatabase reopened = open();
        assertEquals(1, reopened.dao().queue("alice:iptek").size()); assertEquals("SYNCING", reopened.dao().queue("alice:iptek").get(0).state);
        assertTrue(reopened.dao().queue("bob:iptek").isEmpty()); assertTrue(reopened.dao().queue("alice:other").isEmpty()); reopened.close();
    }
    @Test public void replacingSnapshotAndAcknowledgingCommandIsAtomic() {
        BudgetDatabase db = open(); Outbox item = new Outbox(); item.owner = "alice:iptek"; item.id = "pending"; db.dao().enqueue(item);
        CachedSnapshot snapshot = new CachedSnapshot(); snapshot.owner = item.owner; snapshot.payload = "revision-2";
        try { db.runInTransaction(() -> { db.dao().cache(snapshot); db.dao().delete(item.owner, item.id); throw new IllegalStateException("disk failure"); }); fail(); }
        catch (IllegalStateException expected) { assertNull(db.dao().snapshot(item.owner)); assertEquals(1, db.dao().queue(item.owner).size()); }
        db.runInTransaction(() -> { db.dao().cache(snapshot); db.dao().delete(item.owner, item.id); });
        assertEquals("revision-2", db.dao().snapshot(item.owner).payload); assertTrue(db.dao().queue(item.owner).isEmpty()); db.close();
    }
}
