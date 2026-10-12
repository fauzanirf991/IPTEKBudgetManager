package com.fauzan.ukmbudgetmanager.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.fauzan.budget.core.Command;
import com.fauzan.ukmbudgetmanager.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.time.LocalDate;
import java.util.UUID;

/** Dedicated dialog for creating and managing organization events/activities on its own screen. */
public final class EventDialog extends DialogFragment {
    private BudgetViewModel vm;
    private TextInputEditText title, date;

    public static EventDialog create() {
        return new EventDialog();
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout root = Ui.column(requireContext());
        scroll.addView(root);

        // Header info banner explaining the purpose of activities
        root.addView(Ui.infoBanner(requireContext(),
                "Program & Kegiatan: Buat nama kegiatan di sini. Pengeluaran anggaran nantinya dapat dialokasikan ke kegiatan ini.",
                R.drawable.ic_nav_events,
                ContextCompat.getColor(requireContext(), R.color.event_purple),
                ContextCompat.getColor(requireContext(), R.color.event_bg)));

        // Title field
        title = Ui.field(root, "Nama Kegiatan (contoh: Seminar Nasional, Pelatihan)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
                R.drawable.ic_nav_events);

        // Date field
        date = Ui.field(root, "Tanggal Pelaksanaan",
                InputType.TYPE_CLASS_DATETIME,
                R.drawable.ic_calendar);
        date.setText(LocalDate.now().toString());
        date.setFocusable(false);
        date.setOnClickListener(v -> {
            LocalDate d;
            try { d = LocalDate.parse(date.getText()); } catch (Exception e) { d = LocalDate.now(); }
            Ui.secure(new DatePickerDialog(requireContext(), (view, y, m, day) -> {
                date.setText(LocalDate.of(y, m + 1, day).toString());
            }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth())).show();
        });

        // Security notice banner
        root.addView(Ui.infoBanner(requireContext(),
                "Penyimpanan Offline • Diamankan Kunci Layar & Enkripsi",
                R.drawable.ic_shield_lock,
                ContextCompat.getColor(requireContext(), R.color.primary),
                ContextCompat.getColor(requireContext(), R.color.primary_container)));

        return Ui.secure(new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tambah Kegiatan Baru")
                .setView(scroll)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan Kegiatan", null)
                .create());
    }

    @Override public void onStart() {
        super.onStart();
        ((androidx.appcompat.app.AlertDialog) requireDialog()).getButton(-1).setOnClickListener(v -> {
            try {
                String titleText = title.getText() != null ? title.getText().toString().trim() : "";
                if (titleText.isEmpty()) {
                    throw new IllegalArgumentException("Nama kegiatan tidak boleh kosong");
                }
                String dateText = date.getText() != null ? date.getText().toString().trim() : "";
                if (dateText.isEmpty()) {
                    throw new IllegalArgumentException("Tanggal pelaksanaan tidak boleh kosong");
                }

                Command c = new Command();
                c.id = UUID.randomUUID().toString();
                c.kind = "EVENT";
                c.title = titleText;
                c.businessDate = dateText;
                try {
                    c.periodId = Integer.toString(LocalDate.parse(dateText).getYear());
                } catch (Exception e) {
                    c.periodId = "0000";
                }
                c.account = "";
                c.amount = 0;
                c.quantity = 0;
                c.unitPrice = 0;
                c.eventId = "";

                vm.save(c, null, () -> {
                    if (isAdded()) dismissAllowingStateLoss();
                });
            } catch (Exception e) {
                vm.message.setValue("Periksa isian: " + e.getMessage());
            }
        });
    }
}
