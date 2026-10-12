package com.fauzan.ukmbudgetmanager.data;

import android.content.Context;
import java.io.File;

/** Removes data left by the former Firebase/sync build (it was never released, so nothing is migrated). */
public final class LegacyCleanup {
    private LegacyCleanup() {}
    public static void run(Context context) {
        for (String name : new String[]{"iptek-v2.db", "androidx.work.workdb"}) context.deleteDatabase(name);
        delete(new File(context.getFilesDir(), "staging"));
        File receipts = new File(context.getFilesDir(), "receipts");
        File[] children = receipts.listFiles();
        // Old layout: receipts/<account uid>/<id>.sealed. The new layout keeps files directly in receipts/.
        if (children != null) for (File child : children) if (child.isDirectory()) delete(child);
    }
    private static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }
}
