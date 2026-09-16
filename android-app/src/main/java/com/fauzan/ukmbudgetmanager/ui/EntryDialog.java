package com.fauzan.ukmbudgetmanager.ui;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.data.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.time.LocalDate;
import java.util.*;

public final class EntryDialog extends DialogFragment {
    private BudgetViewModel vm;
    private TextInputEditText title, date, amount, quantity, price;
    private Spinner kind, account, period, event;
    private TextView receiptLabel;
    private Command draft;
    private boolean ready, submitting;
    private final String[] kinds = {"CASH", "FUNDING", "EXPENSE", "ASSET", "EVENT"};
    private final List<String> eventIds = new ArrayList<>(), periodIds = new ArrayList<>();
    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
        if (uri == null) return;
        try {
            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            vm.saved.set("receipt", uri.toString()); receiptLabel.setText("Bukti dipilih • disimpan aman saat catatan disimpan");
        } catch (Exception e) { vm.message.setValue("Bukti tidak dapat dibaca. Pilih gambar lain."); }
    });
    public static EntryDialog create(String initialKind) { EntryDialog f = new EntryDialog(); Bundle b = new Bundle(); b.putString("kind", initialKind); f.setArguments(b); return f; }
    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        String savedDraft = vm.saved.get("draft");
        draft = savedDraft == null ? new Command() : ProcessorClient.JSON.fromJson(savedDraft, Command.class);
        if (draft.id.isEmpty()) { draft.id = UUID.randomUUID().toString(); draft.kind = requireArguments().getString("kind", "CASH"); draft.businessDate = LocalDate.now().toString(); }
        ScrollView scroll = new ScrollView(requireContext()); LinearLayout root = Ui.column(requireContext()); scroll.addView(root);
        Ui.text(root, "Disimpan di perangkat terlebih dahulu. Saldo resmi berubah setelah sinkronisasi diterima.", 14);
        kind = spinner(root, "Jenis catatan", Arrays.stream(kinds).map(Ui::kind).toArray(String[]::new));
        int initial = Arrays.asList(kinds).indexOf(draft.kind); kind.setSelection(Math.max(initial, 0));
        if (!draft.targetId.isEmpty()) { kind.setEnabled(false); Ui.text(root, "Koreksi akan membalik transaksi asal dan membukukan pengganti secara atomik. Bukti lama dipertahankan bila tidak memilih bukti baru.", 14); }
        title = Ui.field(root, "Nama / keterangan", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); title.setText(draft.title);
        date = Ui.field(root, "Tanggal transaksi", InputType.TYPE_CLASS_DATETIME); date.setText(draft.businessDate); date.setFocusable(false);
        date.setOnClickListener(v -> { LocalDate d; try { d = LocalDate.parse(date.getText()); } catch (Exception e) { d = LocalDate.now(); }
            new DatePickerDialog(requireContext(), (view, y, m, day) -> date.setText(LocalDate.of(y, m + 1, day).toString()), d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth()).show(); });
        Snapshot data = vm.currentSnapshot(); List<String> periods = new ArrayList<>();
        if (data != null) for (Snapshot.Period p : data.periods) if ("OPEN".equals(p.status)) { periodIds.add(p.id); periods.add(p.id + " • " + p.start + " — " + p.end); }
        period = spinner(root, "Periode pembukuan", periods.toArray(new String[0]));
        if (periodIds.contains(draft.periodId)) period.setSelection(periodIds.indexOf(draft.periodId));
        account = spinner(root, "Sumber dana", new String[]{"KAS", "PAGUDANA"}); account.setSelection("PAGUDANA".equals(draft.account) ? 1 : 0);
        amount = Ui.field(root, "Nominal rupiah (tanpa titik/koma)", InputType.TYPE_CLASS_NUMBER); if (draft.amount > 0) amount.setText(Long.toString(draft.amount));
        quantity = Ui.field(root, "Jumlah item", InputType.TYPE_CLASS_NUMBER); if (draft.quantity > 0) quantity.setText(Long.toString(draft.quantity));
        price = Ui.field(root, "Harga satuan rupiah", InputType.TYPE_CLASS_NUMBER); if (draft.unitPrice > 0) price.setText(Long.toString(draft.unitPrice));
        List<String> events = new ArrayList<>();
        if (data != null) for (var r : data.records) if ("EVENT".equals(r.kind)) { eventIds.add(r.id); events.add(r.title + " • " + r.periodId); }
        if (vm.currentQueue() != null) for (Outbox o : vm.currentQueue()) try {
            Command c = ProcessorClient.JSON.fromJson(LocalVault.open(o.payload), Command.class);
            if ("EVENT".equals(c.kind) && !"REJECTED".equals(o.state) && !eventIds.contains(c.id)) { eventIds.add(c.id); events.add(c.title + " • lokal"); }
        } catch (Exception ignored) { /* Corrupt queue items remain visible in the local queue. */ }
        event = spinner(root, "Kegiatan (untuk pengeluaran)", events.toArray(new String[0])); if (eventIds.contains(draft.eventId)) event.setSelection(eventIds.indexOf(draft.eventId));
        receiptLabel = Ui.text(root, vm.saved.get("receipt") == null ? "Belum ada bukti • opsional" : "Bukti dipilih", 14);
        var receiptButton = Ui.button(root, "Pilih bukti gambar", v -> picker.launch(new String[]{"image/jpeg", "image/png", "image/webp"}));
        Ui.button(root, "Lepas bukti", v -> { vm.saved.set("receipt", null); receiptLabel.setText("Belum ada bukti"); });
        TextWatcher watcher = new TextWatcher() { public void beforeTextChanged(CharSequence s, int st, int c, int a) {} public void onTextChanged(CharSequence s, int st, int b, int c) { remember(); } public void afterTextChanged(Editable e) {} };
        for (var field : new TextInputEditText[]{title, date, amount, quantity, price}) field.addTextChangedListener(watcher);
        AdapterView.OnItemSelectedListener selected = new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = kinds[kind.getSelectedItemPosition()];
                account.setEnabled(draft.targetId.isEmpty() && (type.equals("ASSET") || type.equals("EXPENSE")));
                if (type.equals("CASH")) account.setSelection(0); if (type.equals("FUNDING")) account.setSelection(1);
                amount.setEnabled(!type.equals("EVENT") && !type.equals("EXPENSE"));
                quantity.setEnabled(type.equals("EXPENSE")); price.setEnabled(type.equals("EXPENSE")); event.setEnabled(type.equals("EXPENSE"));
                receiptButton.setEnabled(!type.equals("EVENT")); remember();
            }
        };
        for (Spinner spinner : new Spinner[]{kind, account, period, event}) spinner.setOnItemSelectedListener(selected);
        ready = true;
        vm.message.observe(this, message -> { if (submitting && message != null && message.startsWith("Tersimpan di perangkat.")) dismiss(); });
        return new MaterialAlertDialogBuilder(requireContext()).setTitle("Tambah catatan").setView(scroll).setNegativeButton("Simpan draf", (d, w) -> remember()).setPositiveButton("Simpan lokal", null).create();
    }
    @Override public void onStart() {
        super.onStart();
        ((androidx.appcompat.app.AlertDialog) requireDialog()).getButton(-1).setOnClickListener(v -> {
            try {
                if (periodIds.isEmpty()) throw new IllegalArgumentException("Belum ada periode terbuka. Hubungi pengelola.");
                remember(); Command c = ProcessorClient.JSON.fromJson(vm.saved.<String>get("draft"), Command.class);
                if (c.kind.equals("EXPENSE")) c.amount = Math.multiplyExact(c.quantity, c.unitPrice);
                c.validatedAmount(); submitting = true; vm.save(c, c.kind.equals("EVENT") ? null : vm.saved.get("receipt"));
            } catch (Exception e) { vm.message.setValue("Periksa isian: " + e.getMessage()); }
        });
    }
    private void remember() {
        if (!ready) return;
        draft.kind = kinds[kind.getSelectedItemPosition()]; draft.title = title.getText().toString(); draft.businessDate = date.getText().toString();
        draft.account = draft.kind.equals("EVENT") ? "" : account.getSelectedItem().toString();
        draft.periodId = periodIds.isEmpty() ? "" : periodIds.get(Math.max(0, period.getSelectedItemPosition()));
        draft.eventId = draft.kind.equals("EXPENSE") && !eventIds.isEmpty() ? eventIds.get(Math.max(0, event.getSelectedItemPosition())) : "";
        draft.amount = draft.kind.equals("EVENT") ? 0 : numeric(amount); draft.quantity = draft.kind.equals("EXPENSE") ? numeric(quantity) : 0; draft.unitPrice = draft.kind.equals("EXPENSE") ? numeric(price) : 0;
        vm.saved.set("draft", ProcessorClient.JSON.toJson(draft));
    }
    private long numeric(TextInputEditText text) { try { return Long.parseLong(text.getText().toString().trim()); } catch (Exception e) { return 0; } }
    private Spinner spinner(LinearLayout root, String label, String[] values) {
        Ui.text(root, label, 13); Spinner spinner = new Spinner(requireContext()); spinner.setMinimumHeight(Ui.dp(requireContext(), 48));
        var adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, values); spinner.setAdapter(adapter); spinner.setContentDescription(label); root.addView(spinner); return spinner;
    }
}
