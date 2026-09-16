package com.fauzan.ukmbudgetmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert void enqueue(Outbox command);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void cache(CachedSnapshot snapshot);
    @Query("SELECT * FROM snapshots WHERE owner=:owner") CachedSnapshot snapshot(String owner);
    @Query("SELECT * FROM snapshots WHERE owner=:owner") LiveData<CachedSnapshot> observeSnapshot(String owner);
    @Query("SELECT * FROM outbox WHERE owner=:owner ORDER BY createdAt, id") LiveData<List<Outbox>> observeQueue(String owner);
    @Query("SELECT * FROM outbox WHERE owner=:owner ORDER BY createdAt, id") List<Outbox> queue(String owner);
    @Query("UPDATE outbox SET state=:state, message=:message WHERE owner=:owner AND id=:id") void state(String owner, String id, String state, String message);
    @Query("DELETE FROM outbox WHERE owner=:owner AND id=:id") void delete(String owner, String id);
    @Query("DELETE FROM snapshots WHERE owner=:owner") void clearSnapshot(String owner);
}
