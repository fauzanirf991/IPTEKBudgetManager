package com.fauzan.ukmbudgetmanager.ui;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.fauzan.budget.core.Command;
import com.fauzan.budget.core.Record;
import com.fauzan.budget.core.Report;
import com.fauzan.budget.core.Snapshot;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.data.LedgerRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public final class BudgetFragment extends Fragment {
    private BudgetViewModel vm;
    private LinearLayout summaries;
    private RowsAdapter adapter;
    private int section;
    private String search = "", from, to, account = "ALL";
    private MaterialButton add;

    public static BudgetFragment create(int section) {
        BudgetFragment f = new BudgetFragment();
        Bundle b = new Bundle();
        b.putInt("section", section);
        f.setArguments(b);
        return f;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        vm = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        section = requireArguments().getInt("section");
        LocalDate today = LocalDate.now();
        from = vm.saved.get("reportFrom");
        to = vm.saved.get("reportTo");
        if (from == null) from = today.withDayOfMonth(1).toString();
        if (to == null) to = today.toString();

        LinearLayout header = Ui.column(requireContext());

        // Header Title & Subtitle
        String[] titles = {"Dashboard Keuangan", "Daftar Transaksi", "Program & Kegiatan", "Inventaris & Aset", "Laporan Keuangan"};
        String[] subtitles = {
            "Pantau ringkasan saldo pagudana dan kas secara real-time.",
            "Semua transaksi pemasukan dan pengeluaran tersimpan.",
            "Rencana kegiatan organisasi dan pengeluaran terkait.",
            "Daftar aset fisik dan bukti pembelian transaksi.",
            "Analisis arus kas masuk dan keluar berdasarkan periode."
        };

        TextView titleView = new TextView(requireContext());
        titleView.setText(titles[section]);
        titleView.setTextSize(22);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleView.setPadding(0, 0, 0, Ui.dp(requireContext(), 2));
        header.addView(titleView);

        TextView subView = new TextView(requireContext());
        subView.setText(subtitles[section]);
        subView.setTextSize(13);
        subView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        subView.setPadding(0, 0, 0, Ui.dp(requireContext(), 14));
        header.addView(subView);

        // Action Button (Except reports section)
        if (section != 4) {
            String btnText = section == 2 ? "Tambah Kegiatan" : section == 3 ? "Tambah Aset" : "Catat Transaksi";
            add = new MaterialButton(requireContext());
            add.setText(btnText);
            add.setIconResource(R.drawable.ic_add);
            add.setIconSize(Ui.dp(requireContext(), 20));
            add.setIconPadding(Ui.dp(requireContext(), 8));
            add.setCornerRadius(Ui.dp(requireContext(), 24));
            add.setTextSize(14);
            add.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            add.setPadding(Ui.dp(requireContext(), 20), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 20), Ui.dp(requireContext(), 12));
            if (section == 2) {
                add.setOnClickListener(v -> EventDialog.create().show(getParentFragmentManager(), "event_dialog"));
            } else if (section == 3) {
                add.setOnClickListener(v -> EntryDialog.create("ASSET").show(getParentFragmentManager(), "entry"));
            } else {
                add.setOnClickListener(v -> EntryDialog.create("EXPENSE").show(getParentFragmentManager(), "entry"));
            }
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, -2);
            btnParams.bottomMargin = Ui.dp(requireContext(), 14);
            header.addView(add, btnParams);
        } else {
            // Modern Filter Bar for Reports
            LinearLayout dateRow = new LinearLayout(requireContext());
            dateRow.setOrientation(LinearLayout.HORIZONTAL);
            dateRow.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

            MaterialButton startBtn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            startBtn.setText("Dari: " + from);
            startBtn.setIconResource(R.drawable.ic_calendar);
            startBtn.setIconSize(Ui.dp(requireContext(), 16));
            startBtn.setCornerRadius(Ui.dp(requireContext(), 16));
            startBtn.setTag("start");
            startBtn.setOnClickListener(v -> pickDate(true));
            dateRow.addView(startBtn, new LinearLayout.LayoutParams(0, -2, 1f));

            View spacer = new View(requireContext());
            dateRow.addView(spacer, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 8), 1));

            MaterialButton endBtn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            endBtn.setText("Sampai: " + to);
            endBtn.setIconResource(R.drawable.ic_calendar);
            endBtn.setIconSize(Ui.dp(requireContext(), 16));
            endBtn.setCornerRadius(Ui.dp(requireContext(), 16));
            endBtn.setTag("end");
            endBtn.setOnClickListener(v -> pickDate(false));
            dateRow.addView(endBtn, new LinearLayout.LayoutParams(0, -2, 1f));

            header.addView(dateRow);

            // Quick Filter Row: "Bulan ini" + Spinner for Account
            LinearLayout filterRow = new LinearLayout(requireContext());
            filterRow.setOrientation(LinearLayout.HORIZONTAL);
            filterRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(-1, -2);
            filterParams.topMargin = Ui.dp(requireContext(), 8);
            filterParams.bottomMargin = Ui.dp(requireContext(), 12);
            filterRow.setLayoutParams(filterParams);

            MaterialButton thisMonthBtn = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.borderlessButtonStyle);
            thisMonthBtn.setText("Bulan Ini");
            thisMonthBtn.setCornerRadius(Ui.dp(requireContext(), 16));
            thisMonthBtn.setOnClickListener(v -> {
                LocalDate now = LocalDate.now();
                from = now.withDayOfMonth(1).toString();
                to = now.toString();
                rememberDates();
                startBtn.setText("Dari: " + from);
                endBtn.setText("Sampai: " + to);
                render();
            });
            filterRow.addView(thisMonthBtn);

            Spinner selector = new Spinner(requireContext());
            selector.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Semua Akun", "PAGUDANA", "KAS"}));
            selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onNothingSelected(AdapterView<?> p) {}
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    account = new String[]{"ALL", "PAGUDANA", "KAS"}[pos];
                    render();
                }
            });
            filterRow.addView(selector, new LinearLayout.LayoutParams(0, -2, 1f));

            header.addView(filterRow);
        }

        // Modern Search Input
        var find = Ui.field(header, "Cari nama atau keterangan…", InputType.TYPE_CLASS_TEXT);
        find.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void afterTextChanged(Editable e) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) {
                search = s.toString().toLowerCase(Locale.ROOT);
                render();
            }
        });

        summaries = new LinearLayout(requireContext());
        summaries.setOrientation(LinearLayout.VERTICAL);
        header.addView(summaries);

        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RowsAdapter(header);
        list.setAdapter(adapter);
        return list;
    }

    @Override public void onViewCreated(View view, Bundle state) {
        vm.snapshot.observe(getViewLifecycleOwner(), data -> render());
        render();
    }

    private void rememberDates() {
        vm.saved.set("reportFrom", from);
        vm.saved.set("reportTo", to);
    }

    private void pickDate(boolean start) {
        LocalDate date = LocalDate.parse(start ? from : to);
        Ui.secure(new DatePickerDialog(requireContext(), (v, y, m, d) -> {
            String value = LocalDate.of(y, m + 1, d).toString();
            if (start) from = value; else to = value;
            rememberDates();
            View button = requireView().findViewWithTag(start ? "start" : "end");
            if (button instanceof TextView text) text.setText((start ? "Dari: " : "Sampai: ") + value);
            render();
        }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth())).show();
    }

    private void render() {
        if (adapter == null || summaries == null) return;
        summaries.removeAllViews();
        if (add != null) add.setVisibility(vm.currentSnapshot() != null ? View.VISIBLE : View.GONE);
        Snapshot data = vm.currentSnapshot();
        List<RowsAdapter.Row> rows = new ArrayList<>();

        if (data == null) {
            Ui.text(summaries, "Memuat data dari penyimpanan perangkat…", 14);
            adapter.submit(rows);
            return;
        }

        List<Record> records = new ArrayList<>(data.records);

        if (section == 0 || section == 4) {
            try {
                Report report = Report.calculate(data, section == 4 ? from : "0001-01-01", section == 4 ? to : LocalDate.now().toString(), section == 4 ? account : "ALL");
                long inflow = 0, outflow = 0;
                long pagudanaClosing = report.accounts.containsKey("PAGUDANA") ? report.accounts.get("PAGUDANA").closing : 0;
                long kasClosing = report.accounts.containsKey("KAS") ? report.accounts.get("KAS").closing : 0;

                for (var entry : report.accounts.entrySet()) {
                    Report.Totals t = entry.getValue();
                    inflow = Math.addExact(inflow, t.inflow);
                    outflow = Math.addExact(outflow, t.outflow);
                }

                if (section == 0) {
                    // Beranda Hero Balance & Stat Cards
                    summaries.addView(Ui.heroBalanceCard(requireContext(), pagudanaClosing + kasClosing, pagudanaClosing, kasClosing));
                    summaries.addView(Ui.statRow(requireContext(), inflow, outflow));

                    TextView recentLabel = new TextView(requireContext());
                    recentLabel.setText("Aktivitas Terkini");
                    recentLabel.setTextSize(17);
                    recentLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                    recentLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    recentLabel.setPadding(0, Ui.dp(requireContext(), 8), 0, Ui.dp(requireContext(), 8));
                    summaries.addView(recentLabel);
                } else {
                    // Laporan Visual Breakdown
                    MaterialCardView chartCard = new MaterialCardView(requireContext());
                    chartCard.setRadius(Ui.dp(requireContext(), 20));
                    chartCard.setCardElevation(Ui.dp(requireContext(), 2));
                    chartCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
                    chartCard.setStrokeWidth(Ui.dp(requireContext(), 1));
                    chartCard.setCardBackgroundColor(Color.WHITE);

                    LinearLayout chartLayout = new LinearLayout(requireContext());
                    chartLayout.setOrientation(LinearLayout.VERTICAL);
                    chartLayout.setPadding(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16));
                    chartCard.addView(chartLayout);

                    TextView chartTitle = new TextView(requireContext());
                    chartTitle.setText("Proporsi Arus Kas");
                    chartTitle.setTextSize(16);
                    chartTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                    chartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    chartLayout.addView(chartTitle);

                    BreakdownView chart = new BreakdownView(requireContext());
                    chartLayout.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(requireContext(), 72)));
                    chart.values(inflow, outflow);

                    // Net Surplus / Defisit Chip
                    long net = inflow - outflow;
                    LinearLayout netRow = new LinearLayout(requireContext());
                    netRow.setOrientation(LinearLayout.HORIZONTAL);
                    netRow.setGravity(Gravity.CENTER_VERTICAL);
                    netRow.setPadding(0, Ui.dp(requireContext(), 8), 0, 0);

                    TextView netLabel = new TextView(requireContext());
                    netLabel.setText("Selisih Bersih (Net): ");
                    netLabel.setTextSize(13);
                    netLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                    netRow.addView(netLabel);

                    int netColor = net >= 0 ? ContextCompat.getColor(requireContext(), R.color.inflow_green) : ContextCompat.getColor(requireContext(), R.color.outflow_red);
                    int netBg = net >= 0 ? ContextCompat.getColor(requireContext(), R.color.inflow_bg) : ContextCompat.getColor(requireContext(), R.color.outflow_bg);
                    TextView netBadge = Ui.badge(requireContext(), (net >= 0 ? "+ " : "") + Ui.rupiah(net), netColor, netBg);
                    netRow.addView(netBadge);
                    chartLayout.addView(netRow);

                    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
                    cardParams.bottomMargin = Ui.dp(requireContext(), 16);
                    summaries.addView(chartCard, cardParams);

                    // Account Breakdown Cards
                    for (var entry : report.accounts.entrySet()) {
                        if (!account.equals("ALL") && !account.equals(entry.getKey())) continue;
                        Report.Totals t = entry.getValue();

                        MaterialCardView accCard = new MaterialCardView(requireContext());
                        accCard.setRadius(Ui.dp(requireContext(), 18));
                        accCard.setCardElevation(Ui.dp(requireContext(), 1));
                        accCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
                        accCard.setStrokeWidth(Ui.dp(requireContext(), 1));
                        accCard.setCardBackgroundColor(Color.WHITE);

                        LinearLayout accLayout = new LinearLayout(requireContext());
                        accLayout.setOrientation(LinearLayout.VERTICAL);
                        accLayout.setPadding(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 14));
                        accCard.addView(accLayout);

                        LinearLayout hRow = new LinearLayout(requireContext());
                        hRow.setOrientation(LinearLayout.HORIZONTAL);
                        hRow.setGravity(Gravity.CENTER_VERTICAL);

                        TextView accName = new TextView(requireContext());
                        accName.setText("Akun " + entry.getKey());
                        accName.setTextSize(15);
                        accName.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                        accName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                        hRow.addView(accName, new LinearLayout.LayoutParams(0, -2, 1f));

                        TextView accClose = new TextView(requireContext());
                        accClose.setText(Ui.rupiah(t.closing));
                        accClose.setTextSize(16);
                        accClose.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                        accClose.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                        hRow.addView(accClose);
                        accLayout.addView(hRow);

                        TextView subDetails = new TextView(requireContext());
                        subDetails.setText("Saldo Awal: " + Ui.rupiah(t.opening) + "   ·   Masuk: " + Ui.rupiah(t.inflow) + "   ·   Keluar: " + Ui.rupiah(t.outflow));
                        subDetails.setTextSize(12);
                        subDetails.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                        subDetails.setPadding(0, Ui.dp(requireContext(), 6), 0, 0);
                        accLayout.addView(subDetails);

                        LinearLayout.LayoutParams accParams = new LinearLayout.LayoutParams(-1, -2);
                        accParams.bottomMargin = Ui.dp(requireContext(), 12);
                        summaries.addView(accCard, accParams);
                    }

                    records = report.rows;
                }
            } catch (Exception e) {
                Ui.text(summaries, e.getMessage(), 14);
                adapter.submit(rows);
                return;
            }
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

            boolean isRev = reversed.contains(record.id);
            long rowDelta = record.delta;
            if (record.kind.equals("EVENT")) {
                long totalExp = 0;
                for (Record child : data.records) {
                    if (record.id.equals(child.eventId) && !reversed.contains(child.id)) {
                        totalExp += -child.delta;
                    }
                }
                rowDelta = -totalExp;
            }

            rows.add(new RowsAdapter.Row(
                record.title,
                record.businessDate,
                record.kind,
                record.account,
                rowDelta,
                isRev,
                record.archived,
                !record.receiptId.isEmpty(),
                () -> details(record, isRev)
            ));

            if (section == 0 && ++count >= 10) break;
        }

        if (rows.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText(search.isEmpty() ? "Belum ada catatan pada pilihan ini." : "Tidak ada transaksi yang cocok dengan kata kunci.");
            emptyText.setTextSize(14);
            emptyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
            emptyText.setPadding(0, Ui.dp(requireContext(), 16), 0, Ui.dp(requireContext(), 16));
            emptyText.setGravity(Gravity.CENTER);
            summaries.addView(emptyText);
        }

        adapter.submit(rows);
    }

    private void details(Record record, boolean reversed) {
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout content = Ui.column(requireContext());
        scroll.addView(content);

        // Header Card with Category Avatar and Amount
        MaterialCardView topCard = Ui.cardWrapper(requireContext());
        LinearLayout topLayout = new LinearLayout(requireContext());
        topLayout.setOrientation(LinearLayout.VERTICAL);
        topLayout.setPadding(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 16));
        topCard.addView(topLayout);

        LinearLayout rowAvatar = new LinearLayout(requireContext());
        rowAvatar.setOrientation(LinearLayout.HORIZONTAL);
        rowAvatar.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(requireContext());
        int iconRes = Ui.kindIcon(record.kind);
        int accent = Ui.kindColor(requireContext(), record.kind);
        int bg = Ui.kindBgColor(requireContext(), record.kind);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(accent));
        icon.setBackgroundResource(R.drawable.bg_circle_icon);
        icon.setBackgroundTintList(ColorStateList.valueOf(bg));
        icon.setPadding(Ui.dp(requireContext(), 8), Ui.dp(requireContext(), 8), Ui.dp(requireContext(), 8), Ui.dp(requireContext(), 8));
        rowAvatar.addView(icon, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 42), Ui.dp(requireContext(), 42)));

        LinearLayout titleBlock = new LinearLayout(requireContext());
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(requireContext(), 12), 0, 0, 0);

        TextView tTitle = new TextView(requireContext());
        tTitle.setText(record.title);
        tTitle.setTextSize(17);
        tTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleBlock.addView(tTitle);

        TextView tSubtitle = new TextView(requireContext());
        tSubtitle.setText(record.businessDate + " • " + Ui.kind(record.kind));
        tSubtitle.setTextSize(12);
        tSubtitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        titleBlock.addView(tSubtitle);
        rowAvatar.addView(titleBlock, new LinearLayout.LayoutParams(0, -2, 1f));
        topLayout.addView(rowAvatar);

        // Large Amount
        TextView amountView = new TextView(requireContext());
        amountView.setPadding(0, Ui.dp(requireContext(), 14), 0, Ui.dp(requireContext(), 6));
        amountView.setTextSize(22);
        amountView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        if (record.kind.equals("EVENT")) {
            amountView.setText("Program Kegiatan");
            amountView.setTextColor(ContextCompat.getColor(requireContext(), R.color.event_purple));
        } else if (record.delta > 0) {
            amountView.setText("+ " + Ui.rupiah(record.delta));
            amountView.setTextColor(ContextCompat.getColor(requireContext(), R.color.inflow_green));
        } else if (record.delta < 0) {
            amountView.setText(Ui.rupiah(record.delta));
            amountView.setTextColor(ContextCompat.getColor(requireContext(), R.color.outflow_red));
        } else {
            amountView.setText("Rp 0");
            amountView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
        if (reversed) {
            amountView.setPaintFlags(amountView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            amountView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
        }
        topLayout.addView(amountView);

        // Badges row
        LinearLayout badgeRow = new LinearLayout(requireContext());
        badgeRow.setOrientation(LinearLayout.HORIZONTAL);
        badgeRow.setGravity(Gravity.CENTER_VERTICAL);
        badgeRow.setPadding(0, Ui.dp(requireContext(), 4), 0, 0);

        if (!record.account.isEmpty()) {
            int accColor = "PAGUDANA".equals(record.account) ? ContextCompat.getColor(requireContext(), R.color.pagudana_blue) : ContextCompat.getColor(requireContext(), R.color.kas_teal);
            int accBg = "PAGUDANA".equals(record.account) ? ContextCompat.getColor(requireContext(), R.color.pagudana_bg) : ContextCompat.getColor(requireContext(), R.color.kas_bg);
            TextView b = Ui.badge(requireContext(), "Akun " + record.account, accColor, accBg);
            badgeRow.addView(b);
        }
        if (reversed) {
            View sp = new View(requireContext()); badgeRow.addView(sp, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 6), 1));
            badgeRow.addView(Ui.badge(requireContext(), "Dibatalkan", ContextCompat.getColor(requireContext(), R.color.outflow_red), ContextCompat.getColor(requireContext(), R.color.outflow_bg)));
        } else if (record.archived) {
            View sp = new View(requireContext()); badgeRow.addView(sp, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 6), 1));
            badgeRow.addView(Ui.badge(requireContext(), "Diarsipkan", ContextCompat.getColor(requireContext(), R.color.neutral_badge), ContextCompat.getColor(requireContext(), R.color.neutral_bg)));
        }
        if (!record.receiptId.isEmpty()) {
            View sp = new View(requireContext()); badgeRow.addView(sp, new LinearLayout.LayoutParams(Ui.dp(requireContext(), 6), 1));
            badgeRow.addView(Ui.badge(requireContext(), "📎 Ada Bukti", ContextCompat.getColor(requireContext(), R.color.primary), ContextCompat.getColor(requireContext(), R.color.primary_container)));
        }
        topLayout.addView(badgeRow);

        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(-1, -2);
        topParams.bottomMargin = Ui.dp(requireContext(), 12);
        content.addView(topCard, topParams);

        // Detail Breakdown Card
        MaterialCardView detailCard = Ui.cardWrapper(requireContext());
        LinearLayout detailLayout = new LinearLayout(requireContext());
        detailLayout.setOrientation(LinearLayout.VERTICAL);
        detailLayout.setPadding(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 12));
        detailCard.addView(detailLayout);

        detailLayout.addView(Ui.detailRow(requireContext(), "Tanggal Pembukuan", record.businessDate, ContextCompat.getColor(requireContext(), R.color.text_primary)));
        detailLayout.addView(Ui.detailRow(requireContext(), "Periode Anggaran", record.periodId, ContextCompat.getColor(requireContext(), R.color.text_primary)));

        if (record.kind.equals("EXPENSE")) {
            detailLayout.addView(Ui.detailRow(requireContext(), "Kuantitas", record.quantity + " item", ContextCompat.getColor(requireContext(), R.color.text_primary)));
            detailLayout.addView(Ui.detailRow(requireContext(), "Harga Satuan", Ui.rupiah(record.unitPrice), ContextCompat.getColor(requireContext(), R.color.text_primary)));
        }

        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.bottomMargin = Ui.dp(requireContext(), 12);
        content.addView(detailCard, detailParams);

        // If EVENT: Sub-card listing related expenses
        if (record.kind.equals("EVENT")) {
            Snapshot data = vm.currentSnapshot();
            long totalExp = 0;
            List<Record> children = new ArrayList<>();
            if (data != null) {
                for (Record c : data.records) {
                    if (record.id.equals(c.eventId)) {
                        children.add(c);
                        totalExp += -c.delta;
                    }
                }
            }

            MaterialCardView eventCard = Ui.cardWrapper(requireContext());
            LinearLayout eventLayout = new LinearLayout(requireContext());
            eventLayout.setOrientation(LinearLayout.VERTICAL);
            eventLayout.setPadding(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 14));
            eventCard.addView(eventLayout);

            TextView eLabel = new TextView(requireContext());
            eLabel.setText("Rincian Pengeluaran Kegiatan (" + children.size() + ")");
            eLabel.setTextSize(14);
            eLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            eLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            eventLayout.addView(eLabel);

            if (children.isEmpty()) {
                TextView empty = new TextView(requireContext());
                empty.setText("Belum ada pengeluaran dicatat untuk kegiatan ini.");
                empty.setTextSize(12);
                empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
                empty.setPadding(0, Ui.dp(requireContext(), 6), 0, 0);
                eventLayout.addView(empty);
            } else {
                for (Record child : children) {
                    eventLayout.addView(Ui.detailRow(requireContext(), "• " + child.title, Ui.rupiah(-child.delta), ContextCompat.getColor(requireContext(), R.color.outflow_red)));
                }
                View div = new View(requireContext());
                div.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.outline_subtle));
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, Ui.dp(requireContext(), 1));
                dp.setMargins(0, Ui.dp(requireContext(), 6), 0, Ui.dp(requireContext(), 6));
                eventLayout.addView(div, dp);
                eventLayout.addView(Ui.detailRow(requireContext(), "Total Pengeluaran", Ui.rupiah(totalExp), ContextCompat.getColor(requireContext(), R.color.outflow_red)));
            }

            if (!reversed && !record.archived) {
                MaterialButton addExpBtn = new MaterialButton(requireContext());
                addExpBtn.setText("+ Catat Pengeluaran Kegiatan Ini");
                addExpBtn.setIconResource(R.drawable.ic_arrow_outflow);
                addExpBtn.setIconSize(Ui.dp(requireContext(), 18));
                addExpBtn.setCornerRadius(Ui.dp(requireContext(), 20));
                addExpBtn.setOnClickListener(v -> {
                    EntryDialog.createForEvent(record.id).show(getParentFragmentManager(), "entry");
                });
                LinearLayout.LayoutParams btnP = new LinearLayout.LayoutParams(-1, -2);
                btnP.topMargin = Ui.dp(requireContext(), 12);
                eventLayout.addView(addExpBtn, btnP);
            }

            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, -2);
            ep.bottomMargin = Ui.dp(requireContext(), 12);
            content.addView(eventCard, ep);
        }

        // Security footer pill
        content.addView(Ui.infoBanner(requireContext(),
                "Data tersimpan lokal di perangkat dan terlindungi enkripsi.",
                R.drawable.ic_shield_lock,
                ContextCompat.getColor(requireContext(), R.color.primary),
                ContextCompat.getColor(requireContext(), R.color.primary_container)));

        String dTitle = record.kind.equals("EVENT") ? "Rincian Kegiatan" : "Rincian Transaksi";
        String posText = record.kind.equals("EVENT") ? "Kelola Kegiatan" : "Kelola Transaksi";

        var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(dTitle)
                .setView(scroll)
                .setNegativeButton("Tutup", null);

        if (!record.receiptId.isEmpty()) dialog.setNeutralButton("Lihat Bukti Foto", (d, w) -> showReceipt(record));
        if (!reversed && !record.archived && !record.kind.equals("VOID")) dialog.setPositiveButton(posText, (d, w) -> manage(record));
        Ui.show(dialog);
    }

    private void manage(Record record) {
        if (record.kind.equals("EVENT")) {
            Ui.show(new MaterialAlertDialogBuilder(requireContext()).setTitle(record.title).setItems(new String[]{"Ubah Nama Kegiatan", "Arsipkan Kegiatan"}, (d, index) -> metadata(record, index == 0 ? "EVENT_EDIT" : "EVENT_ARCHIVE")));
        } else {
            String[] options = record.kind.equals("ASSET") ? new String[]{"Koreksi Transaksi", "Batalkan Transaksi", "Arsipkan Aset (Tanpa Pengembalian Uang)"} : new String[]{"Koreksi Transaksi", "Batalkan Transaksi"};
            Ui.show(new MaterialAlertDialogBuilder(requireContext()).setTitle(record.title).setItems(options, (d, index) -> {
                if (index == 1) { reverse(record); return; }
                if (index == 2) { metadata(record, "ASSET_ARCHIVE"); return; }
                Command c = new Command();
                c.id = UUID.randomUUID().toString();
                c.kind = record.kind;
                c.title = record.title;
                c.targetId = record.id;
                c.account = record.account;
                c.periodId = record.periodId;
                c.businessDate = record.businessDate;
                c.amount = Math.abs(record.delta);
                c.quantity = record.quantity;
                c.unitPrice = record.unitPrice;
                c.eventId = record.eventId;
                vm.saved.set("draft", LedgerRepository.JSON.toJson(c));
                vm.saved.set("receipt", null);
                EntryDialog.create(c.kind).show(getParentFragmentManager(), "entry");
            }));
        }
    }

    private void metadata(Record record, String kind) {
        LinearLayout root = Ui.column(requireContext());
        boolean isEdit = kind.equals("EVENT_EDIT");
        root.addView(Ui.infoBanner(requireContext(),
                isEdit ? "Nama kegiatan akan diperbarui di seluruh catatan." : "Riwayat keuangan tetap tersimpan aman sebagai audit.",
                R.drawable.ic_info,
                ContextCompat.getColor(requireContext(), R.color.primary),
                ContextCompat.getColor(requireContext(), R.color.primary_container)));
        var name = Ui.field(root, isEdit ? "Nama Kegiatan Baru" : "Alasan Pengarsipan", InputType.TYPE_CLASS_TEXT, isEdit ? R.drawable.ic_nav_events : R.drawable.ic_tune);
        if (isEdit) name.setText(record.title);
        Ui.show(new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEdit ? "Ubah Nama Kegiatan" : "Arsipkan Catatan")
                .setView(root)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", (d, w) -> {
                    Command c = new Command();
                    c.id = UUID.randomUUID().toString();
                    c.kind = kind;
                    c.title = name.getText() != null ? name.getText().toString() : "";
                    c.periodId = record.periodId;
                    c.businessDate = record.businessDate;
                    c.targetId = record.id;
                    c.expectedRevision = record.revision;
                    vm.save(c, null, null);
                }));
    }

    private void reverse(Record record) {
        LinearLayout root = Ui.column(requireContext());
        root.addView(Ui.infoBanner(requireContext(),
                "Pembalikan akan membukukan catatan saldo kebalikan secara atomik.",
                R.drawable.ic_info,
                ContextCompat.getColor(requireContext(), R.color.outflow_red),
                ContextCompat.getColor(requireContext(), R.color.outflow_bg)));
        var reason = Ui.field(root, "Alasan Pembatalan Transaksi", InputType.TYPE_CLASS_TEXT, R.drawable.ic_close);
        Ui.show(new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Batalkan Transaksi?")
                .setView(root)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan Pembalikan", (d, w) -> {
                    Command c = new Command();
                    c.id = UUID.randomUUID().toString();
                    c.kind = "VOID";
                    c.title = reason.getText() != null ? reason.getText().toString() : "";
                    c.account = record.account;
                    c.periodId = record.periodId;
                    c.targetId = record.id;
                    c.businessDate = record.businessDate;
                    vm.save(c, null, null);
                }));
    }

    private void showReceipt(Record record) {
        vm.loadReceipt(record.receiptId, bytes -> {
            if (!isAdded()) return;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            options.inSampleSize = Math.max(1, Math.max(options.outWidth, options.outHeight) / 1200);
            options.inJustDecodeBounds = false;
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (bitmap == null) { vm.message.setValue("Gambar tidak valid"); return; }

            ScrollView scroll = new ScrollView(requireContext());
            LinearLayout root = Ui.column(requireContext());
            scroll.addView(root);

            MaterialCardView card = Ui.cardWrapper(requireContext());
            LinearLayout cardLayout = new LinearLayout(requireContext());
            cardLayout.setOrientation(LinearLayout.VERTICAL);
            cardLayout.setPadding(Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 12));
            card.addView(cardLayout);

            ImageView image = new ImageView(requireContext());
            image.setImageBitmap(bitmap);
            image.setAdjustViewBounds(true);
            image.setContentDescription("Bukti " + record.title);
            cardLayout.addView(image);

            root.addView(card);

            root.addView(Ui.infoBanner(requireContext(),
                    "Disimpan terenkripsi dalam brankas lokal perangkat.",
                    R.drawable.ic_shield_lock,
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    ContextCompat.getColor(requireContext(), R.color.primary_container)));

            Ui.show(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Bukti: " + record.title)
                    .setView(scroll)
                    .setPositiveButton("Tutup", null));
        }, error -> vm.message.setValue(error));
    }
}
