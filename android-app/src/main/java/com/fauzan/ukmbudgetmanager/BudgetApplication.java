package com.fauzan.ukmbudgetmanager;

import android.app.Application;
import com.fauzan.ukmbudgetmanager.data.LegacyCleanup;

public final class BudgetApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        LegacyCleanup.run(this);
    }
}
