package com.fauzan.ukmbudgetmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void putRecord(LedgerRecord record);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void putMeta(LedgerMeta meta);
    @Insert(onConflict = OnConflictStrategy.ABORT) void putAudit(AuditEntry entry);
    @Query("SELECT * FROM records WHERE id=:id") LedgerRecord record(String id);
    @Query("SELECT * FROM records") List<LedgerRecord> records();
    @Query("SELECT * FROM records") LiveData<List<LedgerRecord>> observeRecords();
    @Query("SELECT * FROM meta WHERE name=:name") LedgerMeta meta(String name);
    @Query("SELECT * FROM meta WHERE name=:name") LiveData<LedgerMeta> observeMeta(String name);
    @Query("SELECT COUNT(*) FROM audit") int auditCount();
}
