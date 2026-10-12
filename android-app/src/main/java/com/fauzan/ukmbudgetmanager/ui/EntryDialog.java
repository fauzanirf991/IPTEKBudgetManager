package com.fauzan.ukmbudgetmanager.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.data.LedgerRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.time.LocalDate;
import java.util.*;

public final class EntryDialog extends DialogFragment {
    private BudgetViewModel vm;
    private TextInputEditText title, date, amount, quantity, price;
    private Spinner kind, account, event;
    private View accountContainer, amountContainer, expenseContainer, receiptCard;
    private TextView receiptLabel, subtotalText;
    private MaterialButton clearReceiptBtn;
    private Command draft;
    private boolean ready;
    private final String[] kinds = {"EXPENSE", "CASH", "FUNDING", "ASSET"};
    private final List<String> eventIds = new ArrayList<>();

    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    vm.saved.set("receipt", uri.toString());
                    updateReceiptUi();
                } catch (Exception e) {
                    vm.message.setValue("Bukti tidak dapat dibaca. Pilih gambar lain.");
                }
            });

    public static EntryDialog create(String initialKind) {
        return create(initialKind, "");
    }

    public static EntryDialog createForEvent(String eventId) {
        return create("EXPENSE", eventId);
    }

    public static EntryDialog create(String initialKind, String initialEventId) {
        EntryDialog f = new EntryDialog();
        Bundle b = new Bundle();
        b.putString("kind", initialKind != null ? initialKind : "EXPENSE");
        b.putString("eventId", initialEventId != null ? initialEventId : "");
        f.setArguments(b);
        return f;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        String savedDraft = vm.saved.get("draft");
        draft = savedDraft == null ? new Command() : LedgerRepository.JSON.fromJson(savedDraft, Command.class);
        if (draft.id.isEmpty()) {
            draft.id = UUID.randomUUID().toString();
            draft.kind = requireArguments().getString("kind", "EXPENSE");
            draft.eventId = requireArguments().getString("eventId", "");
            draft.businessDate = LocalDate.now().toString();
        } else if (draft.eventId.isEmpty() && requireArguments().containsKey("eventId")) {
            draft.eventId = requireArguments().getString("eventId", "");
        }

        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout root = Ui.column(requireContext());
        scroll.addView(root);

        // Security / Offline notice banner
        if (draft.targetId.isEmpty()) {
            root.addView(Ui.infoBanner(requireContext(),
                    "Penyimpanan Offline • Diamankan Kunci Layar & Enkripsi",
                    R.drawable.ic_shield_lock,
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    ContextCompat.getColor(requireContext(), R.color.primary_container)));
        } else {
            root.addView(Ui.infoBanner(requireContext(),
                    "Koreksi Transaksi: Transaksi asal dibalik & pengganti dibukukan atomik.",
                    R.drawable.ic_tune,
                    ContextCompat.getColor(requireContext(), R.color.asset_amber),
                    ContextCompat.getColor(requireContext(), R.color.asset_bg)));
        }

        // Kategori Transaksi Spinner Card
        kind = Ui.spinnerCard(root, "Jenis Transaksi", Arrays.stream(kinds).map(Ui::kind).toArray(String[]::new), R.drawable.ic_tune);
        int initial = Arrays.asList(kinds).indexOf(draft.kind);
        kind.setSelection(Math.max(initial, 0));
        if (!draft.targetId.isEmpty()) kind.setEnabled(false);

        // Title Input Field
        title = Ui.field(root, "Nama / Keterangan Transaksi", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, R.drawable.ic_nav_transactions);
        title.setText(draft.title);

        // Date Field
        date = Ui.field(root, "Tanggal Transaksi", InputType.TYPE_CLASS_DATETIME, R.drawable.ic_calendar);
        date.setText(draft.businessDate);
        date.setFocusable(false);
        date.setOnClickListener(v -> {
            LocalDate d;
            try { d = LocalDate.parse(date.getText()); } catch (Exception e) { d = LocalDate.now(); }
            Ui.secure(new DatePickerDialog(requireContext(), (view, y, m, day) -> {
                date.setText(LocalDate.of(y, m + 1, day).toString());
                remember();
            }, d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth())).show();
        });

        // Account / Sumber Dana
        accountContainer = new LinearLayout(requireContext());
        ((LinearLayout) accountContainer).setOrientation(LinearLayout.VERTICAL);
        account = Ui.spinnerCard((LinearLayout) accountContainer, "Sumber Dana / Akun", new String[]{"KAS", "PAGUDANA"}, R.drawable.ic_wallet);
        account.setSelection("PAGUDANA".equals(draft.account) ? 1 : 0);
        root.addView(accountContainer);

        // Amount Field (for Cash, Funding, Asset)
        amountContainer = new LinearLayout(requireContext());
        ((LinearLayout) amountContainer).setOrientation(LinearLayout.VERTICAL);
        amount = Ui.field((LinearLayout) amountContainer, "Nominal Rupiah (Rp)", InputType.TYPE_CLASS_NUMBER, R.drawable.ic_arrow_inflow);
        if (draft.amount > 0) amount.setText(Long.toString(draft.amount));
        root.addView(amountContainer);

        // Expense Specific Container
        expenseContainer = new LinearLayout(requireContext());
        ((LinearLayout) expenseContainer).setOrientation(LinearLayout.VERTICAL);

        // Query activities already entered
        List<String> eventLabels = new ArrayList<>();
        Snapshot data = vm.currentSnapshot();
        if (data != null) {
            for (var r : data.records) {
                if ("EVENT".equals(r.kind) && !r.archived) {
                    eventIds.add(r.id);
                    eventLabels.add(r.title + " (" + r.businessDate + ")");
                }
            }
        }

        if (eventLabels.isEmpty()) {
            // Inform user that events must be created first, with direct action button
            MaterialCardView noEventCard = Ui.cardWrapper(requireContext());
            LinearLayout noEventLayout = new LinearLayout(requireContext());
            noEventLayout.setOrientation(LinearLayout.VERTICAL);
            noEventLayout.setPadding(Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 12));
            noEventCard.addView(noEventLayout);

            TextView alertTitle = new TextView(requireContext());
            alertTitle.setText("Belum Ada Kegiatan");
            alertTitle.setTextSize(14);
            alertTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            alertTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.event_purple));
            noEventLayout.addView(alertTitle);

            TextView alertDesc = new TextView(requireContext());
            alertDesc.setText("Pengeluaran harus dialokasikan ke kegiatan tertentu. Buat kegiatan terlebih dahulu.");
            alertDesc.setTextSize(12);
            alertDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            alertDesc.setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 8));
            noEventLayout.addView(alertDesc);

            MaterialButton createEventBtn = new MaterialButton(requireContext());
            createEventBtn.setText("+ Buat Kegiatan Baru");
            createEventBtn.setIconResource(R.drawable.ic_nav_events);
            createEventBtn.setIconSize(Ui.dp(requireContext(), 18));
            createEventBtn.setCornerRadius(Ui.dp(requireContext(), 20));
            createEventBtn.setOnClickListener(v -> EventDialog.create().show(getParentFragmentManager(), "event_dialog"));
            noEventLayout.addView(createEventBtn);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.bottomMargin = Ui.dp(requireContext(), 12);
            ((LinearLayout) expenseContainer).addView(noEventCard, p);
        } else {
            // Select option dropdown for existing activities
            event = Ui.spinnerCard((LinearLayout) expenseContainer, "Pilih Kegiatan (Wajib)", eventLabels.toArray(new String[0]), R.drawable.ic_nav_events);
            if (!draft.eventId.isEmpty() && eventIds.contains(draft.eventId)) {
                event.setSelection(eventIds.indexOf(draft.eventId));
            }
        }

        quantity = Ui.field((LinearLayout) expenseContainer, "Jumlah Item / Kuantitas", InputType.TYPE_CLASS_NUMBER, R.drawable.ic_tune);
        if (draft.quantity > 0) quantity.setText(Long.toString(draft.quantity));

        price = Ui.field((LinearLayout) expenseContainer, "Harga Satuan Rupiah (Rp)", InputType.TYPE_CLASS_NUMBER, R.drawable.ic_arrow_outflow);
        if (draft.unitPrice > 0) price.setText(Long.toString(draft.unitPrice));

        // Live Subtotal Card for Expense
        MaterialCardView subtotalCard = new MaterialCardView(requireContext());
        subtotalCard.setRadius(Ui.dp(requireContext(), 14));
        subtotalCard.setCardElevation(0);
        subtotalCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.outflow_bg));
        subtotalCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outflow_red));
        subtotalCard.setStrokeWidth(Ui.dp(requireContext(), 1));

        LinearLayout subLayout = new LinearLayout(requireContext());
        subLayout.setOrientation(LinearLayout.HORIZONTAL);
        subLayout.setGravity(Gravity.CENTER_VERTICAL);
        subLayout.setPadding(Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 10), Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 10));

        ImageView subIcon = new ImageView(requireContext());
        subIcon.setImageResource(R.drawable.ic_arrow_outflow);
        subIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.outflow_red)));
        subLayout.addView(subIcon, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 18), Ui.dp(requireContext(), 18)));

        subtotalText = new TextView(requireContext());
        subtotalText.setText("Estimasi Total Pengeluaran: Rp 0");
        subtotalText.setTextSize(13);
        subtotalText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        subtotalText.setTextColor(ContextCompat.getColor(requireContext(), R.color.outflow_red));
        subtotalText.setPadding(Ui.dp(requireContext(), 10), 0, 0, 0);
        subLayout.addView(subtotalText);
        subtotalCard.addView(subLayout);

        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.bottomMargin = Ui.dp(requireContext(), 12);
        ((LinearLayout) expenseContainer).addView(subtotalCard, subParams);

        root.addView(expenseContainer);

        // Receipt Attachment Card
        receiptCard = buildReceiptCard();
        root.addView(receiptCard);

        // TextWatcher to sync draft and live subtotal
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                updateSubtotal();
                remember();
            }
            public void afterTextChanged(Editable e) {}
        };
        for (var field : new TextInputEditText[]{title, date, amount, quantity, price}) {
            field.addTextChangedListener(watcher);
        }

        // Category selection behavior
        AdapterView.OnItemSelectedListener selected = new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyKindSelection();
                remember();
            }
        };
        kind.setOnItemSelectedListener(selected);
        account.setOnItemSelectedListener(selected);
        if (event != null) event.setOnItemSelectedListener(selected);

        ready = true;
        applyKindSelection();
        updateSubtotal();
        updateReceiptUi();

        String dialogTitle = draft.targetId.isEmpty() ? "Catat Transaksi" : "Koreksi Transaksi";
        return Ui.secure(new MaterialAlertDialogBuilder(requireContext())
                .setTitle(dialogTitle)
                .setView(scroll)
                .setNegativeButton("Simpan Draf", (d, w) -> remember())
                .setPositiveButton("Simpan", null)
                .create());
    }

    private View buildReceiptCard() {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(Ui.dp(requireContext(), 14));
        card.setCardElevation(0);
        card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
        card.setStrokeWidth(Ui.dp(requireContext(), 1));
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_bg));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 12));
        card.addView(layout);

        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_receipt);
        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
        topRow.addView(icon, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 20), Ui.dp(requireContext(), 20)));

        TextView titleView = new TextView(requireContext());
        titleView.setText("Lampiran Bukti Transaksi");
        titleView.setTextSize(13);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleView.setPadding(Ui.dp(requireContext(), 10), 0, 0, 0);
        topRow.addView(titleView);
        layout.addView(topRow);

        receiptLabel = new TextView(requireContext());
        receiptLabel.setTextSize(12);
        receiptLabel.setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 10));
        layout.addView(receiptLabel);

        LinearLayout btnsRow = new LinearLayout(requireContext());
        btnsRow.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton pickBtn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        pickBtn.setText("Pilih Foto Bukti");
        pickBtn.setIconResource(R.drawable.ic_receipt);
        pickBtn.setIconSize(Ui.dp(requireContext(), 16));
        pickBtn.setCornerRadius(Ui.dp(requireContext(), 16));
        pickBtn.setOnClickListener(v -> picker.launch(new String[]{"image/jpeg", "image/png", "image/webp"}));
        btnsRow.addView(pickBtn);

        View space = new View(requireContext());
        btnsRow.addView(space, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 8), 1));

        clearReceiptBtn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.borderlessButtonStyle);
        clearReceiptBtn.setText("Hapus");
        clearReceiptBtn.setIconResource(R.drawable.ic_close);
        clearReceiptBtn.setIconSize(Ui.dp(requireContext(), 16));
        clearReceiptBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.outflow_red));
        clearReceiptBtn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.outflow_red)));
        clearReceiptBtn.setOnClickListener(v -> {
            vm.saved.set("receipt", null);
            updateReceiptUi();
        });
        btnsRow.addView(clearReceiptBtn);

        layout.addView(btnsRow);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = Ui.dp(requireContext(), 12);
        card.setLayoutParams(params);
        return card;
    }

    private void updateReceiptUi() {
        if (receiptLabel == null) return;
        boolean hasReceipt = vm.saved.get("receipt") != null;
        if (hasReceipt) {
            receiptLabel.setText("✓ Bukti gambar dipilih • disimpan terenkripsi di brankas perangkat");
            receiptLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.inflow_green));
            if (clearReceiptBtn != null) clearReceiptBtn.setVisibility(View.VISIBLE);
        } else {
            receiptLabel.setText("Belum ada bukti lampiran • Dokumen opsional");
            receiptLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            if (clearReceiptBtn != null) clearReceiptBtn.setVisibility(View.GONE);
        }
    }

    private void updateSubtotal() {
        if (subtotalText == null || quantity == null || price == null) return;
        long q = numeric(quantity);
        long p = numeric(price);
        long total = q * p;
        subtotalText.setText("Estimasi Total Pengeluaran: " + Ui.rupiah(total));
    }

    private void applyKindSelection() {
        if (kind == null) return;
        String type = kinds[kind.getSelectedItemPosition()];

        boolean isExpense = type.equals("EXPENSE");
        boolean isAsset = type.equals("ASSET");
        boolean isCash = type.equals("CASH");
        boolean isFunding = type.equals("FUNDING");

        account.setEnabled(draft.targetId.isEmpty() && (isAsset || isExpense));
        if (isCash) account.setSelection(0);
        if (isFunding) account.setSelection(1);
        if (accountContainer != null) accountContainer.setVisibility(View.VISIBLE);

        amount.setEnabled(!isExpense);
        if (amountContainer != null) amountContainer.setVisibility(isExpense ? View.GONE : View.VISIBLE);

        quantity.setEnabled(isExpense);
        price.setEnabled(isExpense);
        if (event != null) event.setEnabled(isExpense);
        if (expenseContainer != null) expenseContainer.setVisibility(isExpense ? View.VISIBLE : View.GONE);
    }

    @Override public void onStart() {
        super.onStart();
        ((androidx.appcompat.app.AlertDialog) requireDialog()).getButton(-1).setOnClickListener(v -> {
            try {
                remember();
                Command c = LedgerRepository.JSON.fromJson(vm.saved.<String>get("draft"), Command.class);
                if (c.kind.equals("EXPENSE") && eventIds.isEmpty()) {
                    throw new IllegalArgumentException("Belum ada kegiatan. Buat kegiatan terlebih dahulu di menu Kegiatan.");
                }
                if (c.kind.equals("EXPENSE")) {
                    c.amount = Math.multiplyExact(c.quantity, c.unitPrice);
                }
                c.validatedAmount();
                vm.save(c, vm.saved.get("receipt"), () -> {
                    if (isAdded()) dismissAllowingStateLoss();
                });
            } catch (Exception e) {
                vm.message.setValue("Periksa isian: " + e.getMessage());
            }
        });
    }

    private void remember() {
        if (!ready) return;
        draft.kind = kinds[kind.getSelectedItemPosition()];
        draft.title = title.getText() != null ? title.getText().toString() : "";
        draft.businessDate = date.getText() != null ? date.getText().toString() : "";
        draft.account = account.getSelectedItem().toString();

        if (draft.targetId.isEmpty()) {
            try {
                draft.periodId = Integer.toString(LocalDate.parse(draft.businessDate).getYear());
            } catch (Exception e) {
                draft.periodId = "0000";
            }
        }
        draft.eventId = draft.kind.equals("EXPENSE") && event != null && !eventIds.isEmpty()
                ? eventIds.get(Math.max(0, event.getSelectedItemPosition())) : "";
        draft.amount = draft.kind.equals("EXPENSE") ? 0 : numeric(amount);
        draft.quantity = draft.kind.equals("EXPENSE") ? numeric(quantity) : 0;
        draft.unitPrice = draft.kind.equals("EXPENSE") ? numeric(price) : 0;
        vm.saved.set("draft", LedgerRepository.JSON.toJson(draft));
    }

    private long numeric(TextInputEditText text) {
        if (text == null || text.getText() == null) return 0;
        try {
            return Long.parseLong(text.getText().toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
