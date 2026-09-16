package com.fauzan.ukmbudgetmanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkManager;
import com.fauzan.ukmbudgetmanager.BudgetApplication;
import com.fauzan.ukmbudgetmanager.BuildConfig;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.databinding.ActivityMainBinding;
import com.fauzan.ukmbudgetmanager.data.SyncWorker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.DateFormat;
import java.util.Date;

public final class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BudgetViewModel vm;
    private String displayedUid;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); binding = ActivityMainBinding.inflate(getLayoutInflater()); setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (view, insets) -> {
            var bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        binding.toolbar.setTitle("IPTEK Budget");
        if (!BudgetApplication.configured) {
            binding.status.setText("Firebase belum dikonfigurasi. Isi firebase.local.properties dan bangun varian production, atau gunakan varian emulator.");
            binding.navigation.setVisibility(View.GONE); return;
        }
        vm = new ViewModelProvider(this).get(BudgetViewModel.class);
        String[] names = {"Beranda", "Transaksi", "Kegiatan", "Aset", "Laporan"};
        int[] icons = {android.R.drawable.ic_menu_view, android.R.drawable.ic_menu_agenda, android.R.drawable.ic_menu_my_calendar, android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_sort_by_size};
        int[] ids = {R.id.nav_home, R.id.nav_transactions, R.id.nav_events, R.id.nav_assets, R.id.nav_reports};
        for (int i = 0; i < names.length; i++) binding.navigation.getMenu().add(0, ids[i], i, names[i]).setIcon(icons[i]);
        binding.navigation.setOnItemSelectedListener(item -> { show(item.getOrder()); return true; });
        binding.toolbar.getMenu().add(0, R.id.action_sync, 0, "Sinkronkan"); binding.toolbar.getMenu().add(0, R.id.action_logout, 1, "Keluar");
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sync) vm.sync();
            else new MaterialAlertDialogBuilder(this).setTitle("Keluar dari akun?")
                    .setMessage("Catatan yang belum sinkron tetap tersimpan aman untuk akun ini. Masuk kembali dengan akun yang sama untuk melanjutkan.")
                    .setNegativeButton("Batal", null).setPositiveButton("Keluar", (d, w) -> vm.logout()).show();
            return true;
        });
        vm.userId.observe(this, uid -> {
            binding.navigation.setVisibility(uid.isEmpty() ? View.GONE : View.VISIBLE);
            binding.toolbar.getMenu().setGroupVisible(0, !uid.isEmpty());
            if (!uid.equals(displayedUid)) {
                displayedUid = uid;
                if (uid.isEmpty()) getSupportFragmentManager().beginTransaction().replace(binding.content.getId(), new LoginFragment()).commit();
                else {
                    var selected = binding.navigation.getMenu().findItem(binding.navigation.getSelectedItemId());
                    show(selected == null ? 0 : selected.getOrder());
                    WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("sync-" + SyncWorker.owner(uid)).observe(this, work -> {
                        if (!uid.equals(displayedUid) || work == null) return;
                        for (var item : work) if (item.getState() == androidx.work.WorkInfo.State.FAILED && item.getOutputData().getString("error") != null)
                            binding.status.setText(item.getOutputData().getString("error"));
                    });
                }
            }
            updateStatus();
        });
        vm.snapshot.observe(this, value -> updateStatus()); vm.queue.observe(this, value -> updateStatus());
        vm.message.observe(this, message -> { if (message != null && !message.isEmpty()) Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show(); });
    }
    private void updateStatus() {
        var data = vm.currentSnapshot(); var queue = vm.currentQueue();
        if (displayedUid == null || displayedUid.isEmpty()) binding.status.setText(BuildConfig.EMULATORS ? "Lingkungan uji lokal • Firebase Emulator" : "Keuangan UKM IPTEK");
        else if (data == null) binding.status.setText("Belum ada data lengkap. Hubungkan internet dan sinkronkan untuk memuat keanggotaan.");
        else binding.status.setText(data.role + " • Data terakhir " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(data.downloadedAt))
                + (queue == null || queue.isEmpty() ? " • Semua tersinkron" : " • " + queue.size() + " catatan lokal"));
    }
    private void show(int section) {
        if (isFinishing()) return;
        getSupportFragmentManager().beginTransaction().replace(binding.content.getId(), BudgetFragment.create(section)).commit();
    }
}
