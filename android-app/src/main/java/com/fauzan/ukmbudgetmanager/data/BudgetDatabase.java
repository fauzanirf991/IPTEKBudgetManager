package com.fauzan.ukmbudgetmanager.data;

import android.content.Context;
import androidx.room.*;

@Database(entities = {LedgerRecord.class, LedgerMeta.class, AuditEntry.class}, version = 1, exportSchema = true)
public abstract class BudgetDatabase extends RoomDatabase {
    public abstract BudgetDao dao();
    private static volatile BudgetDatabase instance;
    public static BudgetDatabase get(Context context) {
        if (instance == null) synchronized (BudgetDatabase.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), BudgetDatabase.class, "ledger.db").build();
        }
        return instance;
    }
}
