package com.fauzan.ukmbudgetmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.databinding.ActivityMainBinding;
import com.fauzan.ukmbudgetmanager.security.LockController;
import com.fauzan.ukmbudgetmanager.security.LockState;
import com.fauzan.ukmbudgetmanager.security.ScreenLock;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

/** Hosts the ledger screens behind the device screen lock. Nothing is shown or decoded while locked. */
public final class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BudgetViewModel vm;
    private LockController lock;
    private boolean autoPrompted, contentShown;
    @Override public void onCreate(Bundle state) {
        // Blocks screenshots and the recents thumbnail for the whole app.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(state); binding = ActivityMainBinding.inflate(getLayoutInflater()); setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (view, insets) -> {
            var bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        binding.toolbar.setTitle("IPTEK Budget");
        binding.toolbar.setSubtitle("Penyimpanan Offline Aman");
        vm = new ViewModelProvider(this).get(BudgetViewModel.class);
        lock = new LockController(LockState.INSTANCE, new ScreenLock(this));
        String[] names = {"Beranda", "Transaksi", "Kegiatan", "Aset", "Laporan"};
        int[] icons = {R.drawable.ic_nav_home, R.drawable.ic_nav_transactions, R.drawable.ic_nav_events, R.drawable.ic_nav_assets, R.drawable.ic_nav_reports};
        int[] ids = {R.id.nav_home, R.id.nav_transactions, R.id.nav_events, R.id.nav_assets, R.id.nav_reports};
        for (int i = 0; i < names.length; i++) binding.navigation.getMenu().add(0, ids[i], i, names[i]).setIcon(icons[i]);
        binding.navigation.setOnItemSelectedListener(item -> { show(item.getOrder()); return true; });
        var periodsItem = binding.toolbar.getMenu().add(0, R.id.action_periods, 0, "Periode");
        periodsItem.setIcon(R.drawable.ic_tune);
        periodsItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
        var lockItem = binding.toolbar.getMenu().add(0, R.id.action_lock, 1, "Kunci");
        lockItem.setIcon(R.drawable.ic_lock_closed);
        lockItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_periods) periods(); else { LockState.INSTANCE.lockNow(); autoPrompted = true; }
            return true;
        });
        vm.unlocked.observe(this, open -> refreshLock());
        vm.snapshot.observe(this, value -> updateStatus());
        vm.message.observe(this, message -> { if (message != null && !message.isEmpty() && Boolean.TRUE.equals(vm.unlocked.getValue())) Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show(); });
        binding.lockButton.setOnClickListener(v -> { LockController.Screen screen = lock.evaluate();
            if (screen == LockController.Screen.NEEDS_SCREEN_LOCK) openSecuritySettings(); else promptUnlock(); });
    }
    @Override protected void onStart() { super.onStart(); LockState.INSTANCE.onForeground(); autoPrompted = false; }
    @Override protected void onResume() { super.onResume(); refreshLock(); if (lock.evaluate() == LockController.Screen.LOCKED && !autoPrompted) { autoPrompted = true; promptUnlock(); } }
    @Override protected void onStop() { LockState.INSTANCE.onBackground(); super.onStop(); }

    private void promptUnlock() { lock.unlock((screen, message) -> { refreshLock(); if (!message.isEmpty()) binding.lockMessage.setText(message); }); }
    private void openSecuritySettings() {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) == null) intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
    /** Shows either the ledger or the lock panel, and wipes transient UI whenever the app is locked. */
    private void refreshLock() {
        LockController.Screen screen = lock.evaluate();
        boolean open = screen == LockController.Screen.OPEN;
        binding.main.setVisibility(open ? View.VISIBLE : View.GONE);
        binding.lockPanel.setVisibility(open ? View.GONE : View.VISIBLE);
        if (open) {
            if (!contentShown) { contentShown = true; show(orderOf(binding.navigation.getSelectedItemId())); }
            updateStatus(); return;
        }
        if (contentShown) clearSensitiveUi();
        contentShown = false;
        switch (screen) {
            case NEEDS_SCREEN_LOCK:
                binding.lockTitle.setText("Aktifkan kunci layar");
                binding.lockMessage.setText("Aplikasi ini hanya dilindungi oleh kunci layar perangkat. Atur PIN, pola, sandi, atau biometrik di Pengaturan, lalu kembali ke sini.");
                binding.lockButton.setText("Buka Pengaturan keamanan"); break;
            case UNAVAILABLE:
                binding.lockTitle.setText("Kunci layar tidak tersedia");
                binding.lockMessage.setText("Perangkat tidak dapat memverifikasi kunci layar saat ini. Coba lagi nanti."); binding.lockButton.setText("Coba lagi"); break;
            default:
                binding.lockTitle.setText("IPTEK Budget terkunci");
                if (binding.lockMessage.getText().length() == 0) binding.lockMessage.setText("Data keuangan hanya tersimpan di perangkat ini.");
                binding.lockButton.setText("Buka kunci");
        }
    }
    private void clearSensitiveUi() {
        List<Fragment> fragments = new ArrayList<>(getSupportFragmentManager().getFragments());
        var transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) { if (fragment instanceof DialogFragment dialog) dialog.dismissAllowingStateLoss(); else transaction.remove(fragment); }
        transaction.commitAllowingStateLoss();
    }
    private int orderOf(int itemId) { var item = binding.navigation.getMenu().findItem(itemId); return item == null ? 0 : item.getOrder(); }
    private void updateStatus() {
        Snapshot data = vm.currentSnapshot();
        if (data == null) binding.status.setText("Memuat data terenkripsi…");
        else {
            long count = data.records.stream().filter(r -> !r.kind.equals("META") && !r.id.endsWith("-reversal")).count();
            binding.status.setText("Enkripsi Perangkat Aktif • " + count + " Catatan Tersimpan");
        }
    }
    private void show(int section) {
        if (isFinishing() || !Boolean.TRUE.equals(vm.unlocked.getValue())) return;
        getSupportFragmentManager().beginTransaction().replace(binding.content.getId(), BudgetFragment.create(section)).commitAllowingStateLoss();
    }
    private void periods() {
        Snapshot data = vm.currentSnapshot();
        if (data == null || data.periods.isEmpty()) { vm.message.setValue("Periode dibuat otomatis saat ada transaksi pertama."); return; }
        String[] labels = new String[data.periods.size()];
        for (int i = 0; i < labels.length; i++) { var p = data.periods.get(i); labels[i] = p.id + " • " + ("OPEN".equals(p.status) ? "Terbuka" : "Ditutup") + "  (ketuk untuk " + ("OPEN".equals(p.status) ? "menutup" : "membuka") + ")"; }
        Ui.show(new MaterialAlertDialogBuilder(this).setTitle("Periode pembukuan").setItems(labels, (d, index) -> {
            var p = data.periods.get(index); boolean open = !"OPEN".equals(p.status);
            Ui.show(new MaterialAlertDialogBuilder(this).setTitle((open ? "Buka" : "Tutup") + " periode " + p.id + "?")
                    .setMessage(open ? "Transaksi baru pada tahun ini akan diterima kembali." : "Transaksi baru pada tahun ini ditolak sampai periode dibuka lagi.")
                    .setNegativeButton("Batal", null).setPositiveButton(open ? "Buka" : "Tutup", (dd, w) -> vm.setPeriodOpen(p.id, open)));
        }).setNegativeButton("Tutup", null));
    }
}
