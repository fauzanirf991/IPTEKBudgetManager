package com.fauzan.ukmbudgetmanager.data;

import android.content.Context;
import androidx.room.*;

@Database(entities = {Outbox.class, CachedSnapshot.class}, version = 1, exportSchema = true)
public abstract class BudgetDatabase extends RoomDatabase {
    public abstract BudgetDao dao();
    private static volatile BudgetDatabase instance;
    public static BudgetDatabase get(Context context) {
        if (instance == null) synchronized (BudgetDatabase.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), BudgetDatabase.class, "iptek-v2.db").build();
        }
        return instance;
    }
}
