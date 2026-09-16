package com.fauzan.ukmbudgetmanager.ui;

import android.app.DatePickerDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Record;
import com.fauzan.budget.core.Report;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.data.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public final class BudgetFragment extends Fragment {
    private BudgetViewModel vm;
    private LinearLayout summaries;
    private RowsAdapter adapter;
    private int section;
    private String search = "", from, to, account = "ALL";
    private View add;
    public static BudgetFragment create(int section) { BudgetFragment f = new BudgetFragment(); Bundle b = new Bundle(); b.putInt("section", section); f.setArguments(b); return f; }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class); section = requireArguments().getInt("section");
        LocalDate today = LocalDate.now(); from = vm.saved.get("reportFrom"); to = vm.saved.get("reportTo");
        if (from == null) from = today.withDayOfMonth(1).toString(); if (to == null) to = today.toString();
        LinearLayout header = Ui.column(requireContext());
        Ui.text(header, new String[]{"Ringkasan keuangan", "Transaksi", "Kegiatan", "Aset", "Laporan"}[section], 28);
        Ui.text(header, new String[]{"Pagudana dan kas dipisahkan agar saldo mudah ditelusuri.", "Pemasukan, pengeluaran, dan catatan yang belum tersinkron.", "Rencanakan kegiatan dan lihat pengeluarannya.", "Riwayat pembelian aset dan bukti transaksi.", "Pilih rentang tanggal. Ringkasan dan rincian berasal dari data terkonfirmasi yang sama."}[section], 14);
        if (section != 4) add = Ui.button(header, section == 2 ? "Tambah kegiatan" : section == 3 ? "Tambah aset" : "Tambah transaksi", v -> EntryDialog.create(section == 2 ? "EVENT" : section == 3 ? "ASSET" : "CASH").show(getParentFragmentManager(), "entry"));
        else {
            var start = Ui.button(header, "Dari: " + from, v -> pickDate(true)); start.setTag("start");
            var end = Ui.button(header, "Sampai: " + to, v -> pickDate(false)); end.setTag("end");
            Ui.button(header, "Bulan ini", v -> { LocalDate now = LocalDate.now(); from = now.withDayOfMonth(1).toString(); to = now.toString(); rememberDates(); start.setText("Dari: " + from); end.setText("Sampai: " + to); render(); });
            Ui.text(header, "Akun laporan", 13); Spinner selector = new Spinner(requireContext());
            selector.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Semua akun", "PAGUDANA", "KAS"})); header.addView(selector);
            selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onNothingSelected(AdapterView<?> p) {}
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { account = new String[]{"ALL", "PAGUDANA", "KAS"}[pos]; render(); }
            });
        }
        var find = Ui.field(header, "Cari nama/keterangan", InputType.TYPE_CLASS_TEXT);
        find.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s, int st, int c, int a) {} public void afterTextChanged(Editable e) {} public void onTextChanged(CharSequence s, int st, int before, int count) { search = s.toString().toLowerCase(Locale.ROOT); render(); } });
        if (section == 4) Ui.text(header, "Pencarian menyaring rincian; ringkasan tetap mencakup akun/rentang di atas.", 12);
        summaries = new LinearLayout(requireContext()); summaries.setOrientation(LinearLayout.VERTICAL); header.addView(summaries);
        RecyclerView list = new RecyclerView(requireContext()); list.setLayoutManager(new LinearLayoutManager(requireContext())); adapter = new RowsAdapter(header); list.setAdapter(adapter); return list;
    }
    @Override public void onViewCreated(View view, Bundle state) {
        vm.snapshot.observe(getViewLifecycleOwner(), data -> render()); vm.queue.observe(getViewLifecycleOwner(), queue -> render()); render();
    }
    private void rememberDates() { vm.saved.set("reportFrom", from); vm.saved.set("reportTo", to); }
    private void pickDate(boolean start) {
        LocalDate date = LocalDate.parse(start ? from : to);
        new DatePickerDialog(requireContext(), (v, y, m, d) -> {
            String value = LocalDate.of(y, m + 1, d).toString(); if (start) from = value; else to = value; rememberDates();
            View button = requireView().findViewWithTag(start ? "start" : "end"); if (button instanceof TextView text) text.setText((start ? "Dari: " : "Sampai: ") + value); render();
        }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
    }
    private void render() {
        if (adapter == null || summaries == null) return;
        summaries.removeAllViews(); if (add != null) add.setVisibility(vm.editor() ? View.VISIBLE : View.GONE);
        Snapshot data = vm.currentSnapshot(); List<RowsAdapter.Row> rows = new ArrayList<>();
        List<Outbox> queue = vm.currentQueue() == null ? Collections.emptyList() : vm.currentQueue();
        if (!queue.isEmpty()) Ui.text(summaries, queue.size() + " catatan lokal • belum masuk saldo/laporan terkonfirmasi", 14);
        if (section == 0 || section == 1) for (Outbox item : queue) {
            try { Command c = ProcessorClient.JSON.fromJson(LocalVault.open(item.payload), Command.class);
                if (c.title.toLowerCase(Locale.ROOT).contains(search)) rows.add(new RowsAdapter.Row("◷ " + c.title, c.businessDate + " • " + item.message, () -> pending(item, c)));
            } catch (Exception e) { rows.add(new RowsAdapter.Row("Data lokal terkunci", "Jangan hapus data sebelum pemulihan", () -> {})); }
        }
        if (data == null || !data.complete) {
            Ui.text(summaries, "Data belum tersedia lengkap. Sinkronkan saat terhubung; data kosong di perangkat bukan berarti saldo nol.", 16);
            Ui.button(summaries, "Sinkronkan", v -> vm.sync()); adapter.submit(rows); return;
        }
        List<Record> records = new ArrayList<>(data.records);
        if (section == 0 || section == 4) {
            try {
                Report report = Report.calculate(data, section == 4 ? from : "0001-01-01", section == 4 ? to : LocalDate.now(ZoneId.of(data.timezone)).toString(), section == 4 ? account : "ALL");
                long inflow = 0, outflow = 0;
                for (var entry : report.accounts.entrySet()) {
                    if (section == 4 && !account.equals("ALL") && !account.equals(entry.getKey())) continue;
                    Report.Totals t = entry.getValue(); Ui.text(summaries, entry.getKey() + " • " + Ui.rupiah(t.closing), 22);
                    Ui.text(summaries, "Saldo awal " + Ui.rupiah(t.opening) + "\nMasuk " + Ui.rupiah(t.inflow) + "  ·  Keluar " + Ui.rupiah(t.outflow), 14);
                    inflow = Math.addExact(inflow, t.inflow); outflow = Math.addExact(outflow, t.outflow);
                }
                if (section == 4) {
                    Ui.text(summaries, "Hijau: pemasukan • Jingga: pengeluaran", 12);
                    BreakdownView chart = new BreakdownView(requireContext()); summaries.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(requireContext(), 84))); chart.values(inflow, outflow);
                    Ui.text(summaries, "Data lengkap untuk rentang terpilih.\nBerdasarkan sinkronisasi terakhir; perubahan perangkat lain belum tentu tersedia saat offline.", 12);
                    records = report.rows;
                }
            } catch (Exception e) { Ui.text(summaries, e.getMessage(), 16); adapter.submit(rows); return; }
        }
        records.sort(Comparator.comparing((Record r) -> r.businessDate).reversed().thenComparing(r -> r.id));
        Set<String> reversed = data.records.stream().filter(r -> "VOID".equals(r.kind)).map(r -> r.targetId).collect(Collectors.toSet());
        int count = 0;
        for (Record record : records) {
            if (record.kind.equals("META")) continue;
            if (section == 2 && !record.kind.equals("EVENT")) continue;
            if (section == 3 && !record.kind.equals("ASSET")) continue;
            if ((section == 0 || section == 1 || section == 4) && record.kind.equals("EVENT")) continue;
            if (!record.title.toLowerCase(Locale.ROOT).contains(search)) continue;
            String description = record.businessDate + " • " + Ui.kind(record.kind);
            if (!record.kind.equals("EVENT")) description += " • " + Ui.rupiah(record.delta);
            if (reversed.contains(record.id)) description += " • dibatalkan";
            if (record.archived) description += " • diarsipkan";
            rows.add(new RowsAdapter.Row(record.title, description, () -> details(record, reversed.contains(record.id))));
            if (section == 0 && ++count >= 10) break;
        }
        if (rows.isEmpty()) Ui.text(summaries, search.isEmpty() ? "Belum ada catatan pada pilihan ini." : "Tidak ada hasil pencarian.", 16);
        adapter.submit(rows);
    }
    private void pending(Outbox item, Command c) {
        var dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle(c.title).setMessage(Ui.kind(c.kind) + "\n" + c.businessDate + "\n" + Ui.rupiah(c.amount) + "\n\n" + item.message).setNegativeButton("Tutup", null);
        if (item.state.equals("REJECTED")) {
            if (!c.kind.equals("VOID") && !c.kind.endsWith("_EDIT") && !c.kind.endsWith("_ARCHIVE")) dialog.setPositiveButton("Salin untuk perbaikan", (d, w) -> {
                if (!vm.editor()) return;
                c.id = UUID.randomUUID().toString(); c.receiptPath = "";
                vm.saved.set("draft", ProcessorClient.JSON.toJson(c)); vm.saved.set("receipt", null);
                EntryDialog.create(c.kind).show(getParentFragmentManager(), "entry");
            });
            dialog.setNeutralButton("Hapus lokal", (d, w) -> new MaterialAlertDialogBuilder(requireContext()).setMessage("Hapus catatan ditolak ini dari perangkat? Data belum pernah dibukukan.").setNegativeButton("Batal", null).setPositiveButton("Hapus", (dd, ww) -> vm.discardRejected(item)).show());
        } else dialog.setPositiveButton("Coba sinkron", (d, w) -> vm.sync());
        dialog.show();
    }
    private void details(Record record, boolean reversed) {
        String description = record.businessDate + " • " + record.periodId + "\n" + Ui.kind(record.kind) + " • " + record.account + "\n" + Ui.rupiah(record.delta);
        if (record.kind.equals("EXPENSE")) description += "\n" + record.quantity + " × " + Ui.rupiah(record.unitPrice);
        if (record.kind.equals("EVENT")) {
            StringBuilder expenses = new StringBuilder("\n\nPengeluaran kegiatan:"); long total = 0;
            Snapshot data = vm.currentSnapshot();
            if (data != null) for (Record child : data.records) if (record.id.equals(child.eventId)) { expenses.append("\n").append(child.title).append(" • ").append(Ui.rupiah(-child.delta)); total += -child.delta; }
            description += expenses + "\nTotal pembelian tercatat: " + Ui.rupiah(total);
        }
        var dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle(record.title).setMessage(description).setNegativeButton("Tutup", null);
        if (!record.receiptPath.isEmpty()) dialog.setNeutralButton("Lihat bukti", (d, w) -> showReceipt(record));
        if (vm.editor() && !reversed && !record.archived && !record.kind.equals("VOID")) dialog.setPositiveButton("Kelola", (d, w) -> manage(record));
        dialog.show();
    }
    private void manage(Record record) {
        if (record.kind.equals("EVENT")) {
            new MaterialAlertDialogBuilder(requireContext()).setTitle(record.title).setItems(new String[]{"Ubah nama kegiatan", "Arsipkan kegiatan"}, (d, index) -> metadata(record, index == 0 ? "EVENT_EDIT" : "EVENT_ARCHIVE")).show();
        } else {
            String[] options = record.kind.equals("ASSET") ? new String[]{"Koreksi transaksi", "Batalkan transaksi", "Arsipkan aset (tanpa pengembalian uang)"} : new String[]{"Koreksi transaksi", "Batalkan transaksi"};
            new MaterialAlertDialogBuilder(requireContext()).setTitle(record.title).setItems(options, (d, index) -> {
                if (index == 1) { reverse(record); return; }
                if (index == 2) { metadata(record, "ASSET_ARCHIVE"); return; }
                Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = record.kind; c.title = record.title;
                c.targetId = record.id; c.account = record.account; c.periodId = record.periodId; c.businessDate = record.businessDate;
                c.amount = Math.abs(record.delta); c.quantity = record.quantity; c.unitPrice = record.unitPrice; c.eventId = record.eventId;
                vm.saved.set("draft", ProcessorClient.JSON.toJson(c)); vm.saved.set("receipt", null);
                EntryDialog.create(c.kind).show(getParentFragmentManager(), "entry");
            }).show();
        }
    }
    private void metadata(Record record, String kind) {
        LinearLayout root = Ui.column(requireContext()); var name = Ui.field(root, kind.equals("EVENT_EDIT") ? "Nama kegiatan" : "Alasan arsip", InputType.TYPE_CLASS_TEXT);
        if (kind.equals("EVENT_EDIT")) name.setText(record.title);
        new MaterialAlertDialogBuilder(requireContext()).setTitle(kind.equals("EVENT_EDIT") ? "Ubah kegiatan" : "Arsipkan catatan")
                .setMessage("Riwayat keuangan tetap tersimpan. Perubahan divalidasi saat sinkronisasi.").setView(root).setNegativeButton("Batal", null).setPositiveButton("Simpan lokal", (d, w) -> {
                    Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = kind; c.title = name.getText().toString();
                    c.periodId = record.periodId; c.businessDate = record.businessDate; c.targetId = record.id; c.expectedRevision = record.revision; vm.save(c, null);
                }).show();
    }
    private void reverse(Record record) {
        LinearLayout root = Ui.column(requireContext()); var reason = Ui.field(root, "Alasan pembatalan", InputType.TYPE_CLASS_TEXT);
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Buat pembalikan transaksi?").setMessage("Riwayat asal tetap tersimpan. Pembalikan divalidasi saat sinkron; periode harus masih terbuka.")
                .setView(root).setNegativeButton("Batal", null).setPositiveButton("Simpan pembalikan", (d, w) -> {
                    Command c = new Command(); c.id = UUID.randomUUID().toString(); c.kind = "VOID"; c.title = reason.getText().toString();
                    c.account = record.account; c.periodId = record.periodId; c.targetId = record.id; c.businessDate = record.businessDate; vm.save(c, null);
                }).show();
    }
    private void showReceipt(Record record) {
        vm.message.setValue("Mengunduh bukti…"); String uid = vm.userId.getValue();
        ReceiptCache.load(requireContext(), uid, record.id, record.receiptPath, bytes -> {
            if (!isAdded() || !Objects.equals(uid, vm.userId.getValue())) return;
            BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true; BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            options.inSampleSize = Math.max(1, Math.max(options.outWidth, options.outHeight) / 1200); options.inJustDecodeBounds = false;
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (bitmap == null) { vm.message.setValue("Gambar tidak valid"); return; }
            ImageView image = new ImageView(requireContext()); image.setImageBitmap(bitmap); image.setAdjustViewBounds(true); image.setContentDescription("Bukti " + record.title);
            new MaterialAlertDialogBuilder(requireContext()).setTitle("Bukti transaksi").setView(image).setPositiveButton("Tutup", null).show();
        }, error -> vm.message.setValue(error));
    }
}
