package com.fauzan.ukmbudgetmanager;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.storage.FirebaseStorage;

public final class BudgetApplication extends Application {
    public static boolean configured;
    @Override public void onCreate() {
        super.onCreate();
        configured = !BuildConfig.PROJECT_ID.trim().isEmpty() && !BuildConfig.FIREBASE_APP_ID.trim().isEmpty()
                && !BuildConfig.API_KEY.trim().isEmpty() && !BuildConfig.STORAGE_BUCKET.trim().isEmpty()
                && (BuildConfig.EMULATORS || BuildConfig.PROCESSOR_URL.startsWith("https://"));
        if (!configured) return;
        FirebaseApp.initializeApp(this, new FirebaseOptions.Builder().setProjectId(BuildConfig.PROJECT_ID)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID).setApiKey(BuildConfig.API_KEY)
                .setStorageBucket(BuildConfig.STORAGE_BUCKET).build());
        // Room owns durable snapshots/outbox. Firebase's memory cache is never report coverage.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build());
        if (BuildConfig.EMULATORS) {
            FirebaseAuth.getInstance().useEmulator(BuildConfig.EMULATOR_HOST, 9099);
            db.useEmulator(BuildConfig.EMULATOR_HOST, 8180);
            FirebaseStorage.getInstance().useEmulator(BuildConfig.EMULATOR_HOST, 9199);
        }
    }
}
