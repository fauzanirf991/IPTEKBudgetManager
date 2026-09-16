package com.fauzan.ukmbudgetmanager.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.fauzan.ukmbudgetmanager.BuildConfig;

public final class LoginFragment extends Fragment {
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        BudgetViewModel vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        ScrollView scroll = new ScrollView(requireContext()); LinearLayout root = Ui.column(requireContext()); scroll.addView(root);
        Ui.text(root, "Catatan rapi.\nKeuangan transparan.", 30);
        Ui.text(root, "Masuk untuk mengelola pagudana, kegiatan, aset, dan kas UKM. Catatan tersedia offline setelah sinkronisasi pertama.", 16);
        var email = Ui.field(root, "Email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        var password = Ui.field(root, "Kata sandi", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSaveEnabled(false);
        var login = Ui.button(root, "Masuk", v -> {
            if (email.getText().toString().trim().isEmpty() || password.getText().toString().isEmpty()) { vm.message.setValue("Isi email dan kata sandi"); return; }
            vm.login(email.getText().toString(), password.getText().toString());
        });
        vm.busy.observe(requireActivity(), busy -> login.setEnabled(!busy));
        Ui.button(root, "Lupa kata sandi", v -> vm.reset(email.getText().toString()));
        Ui.text(root, "Akun dan hak akses diberikan oleh pengelola organisasi.", 14);
        if (BuildConfig.EMULATORS) {
            Ui.text(root, "Uji lokal setelah menjalankan seed emulator", 14);
            Ui.button(root, "Isi akun Bendahara uji", v -> { email.setText("bendahara@iptek.test"); password.setText("LocalDemo123!"); });
            Ui.button(root, "Isi akun Ketua uji", v -> { email.setText("ketua@iptek.test"); password.setText("LocalDemo123!"); });
        }
        return scroll;
    }
}
