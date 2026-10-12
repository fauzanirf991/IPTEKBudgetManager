package com.fauzan.ukmbudgetmanager.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.fauzan.ukmbudgetmanager.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.*;
import java.text.NumberFormat;
import java.util.Locale;

final class Ui {
    static <T extends android.app.Dialog> T secure(T dialog) {
        if (dialog.getWindow() != null) dialog.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE);
        return dialog;
    }
    static androidx.appcompat.app.AlertDialog show(MaterialAlertDialogBuilder builder) { var dialog = secure(builder.create()); dialog.show(); return dialog; }
    static int dp(Context c, int value) { return (int) (c.getResources().getDisplayMetrics().density * value); }
    
    static LinearLayout column(Context c) {
        LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 16); v.setPadding(p, p, p, p); return v;
    }

    static TextView text(LinearLayout root, String value, int size) {
        TextView view = new TextView(root.getContext()); view.setText(value); view.setTextSize(size);
        view.setTextColor(ContextCompat.getColor(root.getContext(), size >= 20 ? R.color.text_primary : R.color.text_secondary));
        if (size >= 20) view.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        view.setPadding(0, dp(root.getContext(), 4), 0, dp(root.getContext(), 4));
        root.addView(view); return view;
    }

    static TextInputEditText field(LinearLayout root, String label, int input) {
        return field(root, label, input, 0);
    }

    static TextInputEditText field(LinearLayout root, String label, int input, int startIconRes) {
        Context c = root.getContext();
        TextInputLayout box = new TextInputLayout(c, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        box.setHint(label);
        box.setBoxCornerRadii(dp(c, 14), dp(c, 14), dp(c, 14), dp(c, 14));
        box.setBoxStrokeColor(ContextCompat.getColor(c, R.color.primary));
        if (startIconRes != 0) {
            box.setStartIconDrawable(startIconRes);
            box.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.primary)));
        }
        TextInputEditText edit = new TextInputEditText(box.getContext());
        edit.setInputType(input); edit.setId(View.generateViewId());
        edit.setTextColor(ContextCompat.getColor(c, R.color.text_primary));
        box.addView(edit);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, 12);
        root.addView(box, params);
        return edit;
    }

    static MaterialCardView cardWrapper(Context c) {
        MaterialCardView card = new MaterialCardView(c);
        card.setRadius(dp(c, 16));
        card.setCardElevation(dp(c, 1));
        card.setStrokeColor(ContextCompat.getColor(c, R.color.outline_subtle));
        card.setStrokeWidth(dp(c, 1));
        card.setCardBackgroundColor(Color.WHITE);
        return card;
    }

    static Spinner spinnerCard(LinearLayout root, String label, String[] values, int iconRes) {
        Context c = root.getContext();
        MaterialCardView card = new MaterialCardView(c);
        card.setRadius(dp(c, 14));
        card.setCardElevation(0);
        card.setStrokeColor(ContextCompat.getColor(c, R.color.outline_subtle));
        card.setStrokeWidth(dp(c, 1));
        card.setCardBackgroundColor(ContextCompat.getColor(c, R.color.surface_bg));

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(dp(c, 14), dp(c, 8), dp(c, 10), dp(c, 8));
        card.addView(layout);

        if (iconRes != 0) {
            ImageView icon = new ImageView(c);
            icon.setImageResource(iconRes);
            icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.primary)));
            layout.addView(icon, new LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)));
        }

        LinearLayout content = new LinearLayout(c);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(iconRes != 0 ? dp(c, 10) : 0, 0, 0, 0);

        TextView lbl = new TextView(c);
        lbl.setText(label);
        lbl.setTextSize(11);
        lbl.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        lbl.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        content.addView(lbl);

        Spinner spinner = new Spinner(c);
        spinner.setMinimumHeight(dp(c, 36));
        var adapter = new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(adapter);
        spinner.setContentDescription(label);
        content.addView(spinner, new LinearLayout.LayoutParams(-1, -2));

        layout.addView(content, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(c, 12);
        root.addView(card, cardParams);

        return spinner;
    }

    static View infoBanner(Context c, String text, int iconRes, int accentColor, int bgColor) {
        LinearLayout banner = new LinearLayout(c);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackgroundResource(R.drawable.bg_pill_chip);
        banner.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        banner.setPadding(dp(c, 14), dp(c, 10), dp(c, 14), dp(c, 10));

        if (iconRes != 0) {
            ImageView icon = new ImageView(c);
            icon.setImageResource(iconRes);
            icon.setImageTintList(ColorStateList.valueOf(accentColor));
            banner.addView(icon, new LinearLayout.LayoutParams(dp(c, 18), dp(c, 18)));
        }

        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(accentColor);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        tv.setPadding(iconRes != 0 ? dp(c, 10) : 0, 0, 0, 0);
        banner.addView(tv, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, 12);
        banner.setLayoutParams(params);
        return banner;
    }

    static View detailRow(Context c, String label, String value, int valueColor) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(c, 6), 0, dp(c, 6));

        TextView lbl = new TextView(c);
        lbl.setText(label);
        lbl.setTextSize(13);
        lbl.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        row.addView(lbl, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView val = new TextView(c);
        val.setText(value);
        val.setTextSize(14);
        val.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        val.setTextColor(valueColor);
        row.addView(val);

        return row;
    }

    static MaterialButton button(LinearLayout root, String label, View.OnClickListener click) {
        Context c = root.getContext();
        MaterialButton button = new MaterialButton(c);
        button.setText(label);
        button.setCornerRadius(dp(c, 24));
        button.setTextSize(14);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setPadding(dp(c, 20), dp(c, 12), dp(c, 20), dp(c, 12));
        button.setOnClickListener(click);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, 10);
        root.addView(button, params);
        return button;
    }

    static MaterialButton pillButton(Context c, String label, int iconRes, View.OnClickListener click) {
        MaterialButton button = new MaterialButton(c);
        button.setText(label);
        if (iconRes != 0) {
            button.setIconResource(iconRes);
            button.setIconSize(dp(c, 18));
            button.setIconPadding(dp(c, 8));
        }
        button.setCornerRadius(dp(c, 24));
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setOnClickListener(click);
        return button;
    }

    static TextView badge(Context c, String text, int textColor, int bgColor) {
        TextView view = new TextView(c);
        view.setText(text);
        view.setTextSize(11);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        view.setTextColor(textColor);
        view.setPadding(dp(c, 8), dp(c, 3), dp(c, 8), dp(c, 3));
        view.setBackgroundResource(R.drawable.bg_pill_chip);
        view.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        return view;
    }

    static String rupiah(long value) {
        NumberFormat f = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        f.setMaximumFractionDigits(0);
        return f.format(value);
    }

    static String kind(String value) {
        return switch (value == null ? "" : value) {
            case "FUNDING" -> "Pagudana";
            case "CASH" -> "Iuran Kas";
            case "EXPENSE" -> "Pengeluaran";
            case "ASSET" -> "Aset";
            case "EVENT" -> "Kegiatan";
            case "VOID" -> "Batal";
            case "EVENT_EDIT" -> "Ubah Kegiatan";
            case "EVENT_ARCHIVE" -> "Arsip Kegiatan";
            case "ASSET_ARCHIVE" -> "Arsip Aset";
            default -> value;
        };
    }

    static int kindIcon(String kind) {
        return switch (kind == null ? "" : kind) {
            case "FUNDING" -> R.drawable.ic_wallet;
            case "CASH" -> R.drawable.ic_arrow_inflow;
            case "EXPENSE" -> R.drawable.ic_arrow_outflow;
            case "ASSET" -> R.drawable.ic_nav_assets;
            case "EVENT" -> R.drawable.ic_nav_events;
            default -> R.drawable.ic_nav_transactions;
        };
    }

    static int kindColor(Context c, String kind) {
        return switch (kind == null ? "" : kind) {
            case "FUNDING" -> ContextCompat.getColor(c, R.color.pagudana_blue);
            case "CASH" -> ContextCompat.getColor(c, R.color.inflow_green);
            case "EXPENSE" -> ContextCompat.getColor(c, R.color.outflow_red);
            case "ASSET" -> ContextCompat.getColor(c, R.color.asset_amber);
            case "EVENT" -> ContextCompat.getColor(c, R.color.event_purple);
            default -> ContextCompat.getColor(c, R.color.secondary);
        };
    }

    static int kindBgColor(Context c, String kind) {
        return switch (kind == null ? "" : kind) {
            case "FUNDING" -> ContextCompat.getColor(c, R.color.pagudana_bg);
            case "CASH" -> ContextCompat.getColor(c, R.color.inflow_bg);
            case "EXPENSE" -> ContextCompat.getColor(c, R.color.outflow_bg);
            case "ASSET" -> ContextCompat.getColor(c, R.color.asset_bg);
            case "EVENT" -> ContextCompat.getColor(c, R.color.event_bg);
            default -> ContextCompat.getColor(c, R.color.neutral_bg);
        };
    }

    /** Builds the Total Balance Hero Card for Beranda */
    static View heroBalanceCard(Context c, long total, long pagudana, long kas) {
        MaterialCardView card = new MaterialCardView(c);
        card.setRadius(dp(c, 24));
        card.setCardElevation(dp(c, 4));
        card.setStrokeWidth(0);
        card.setBackgroundResource(R.drawable.bg_hero_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, 14);
        card.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(c, 22), dp(c, 20), dp(c, 22), dp(c, 20));
        card.addView(layout);

        // Header label with icon
        LinearLayout header = new LinearLayout(c);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(c);
        icon.setImageResource(R.drawable.ic_wallet);
        icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A3F2E5")));
        header.addView(icon, new LinearLayout.LayoutParams(dp(c, 18), dp(c, 18)));

        TextView subtitle = new TextView(c);
        subtitle.setText("  TOTAL SALDO TERSEDIA");
        subtitle.setTextSize(11);
        subtitle.setLetterSpacing(0.08f);
        subtitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        subtitle.setTextColor(Color.parseColor("#C2F5EC"));
        header.addView(subtitle);
        layout.addView(header);

        // Big nominal
        TextView balanceText = new TextView(c);
        balanceText.setText(rupiah(total));
        balanceText.setTextSize(28);
        balanceText.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        balanceText.setTextColor(Color.WHITE);
        balanceText.setPadding(0, dp(c, 8), 0, dp(c, 16));
        layout.addView(balanceText);

        // Sub-chips row: Pagudana & Kas
        LinearLayout chips = new LinearLayout(c);
        chips.setOrientation(LinearLayout.HORIZONTAL);

        View chipPagu = accountPill(c, "Pagudana", rupiah(pagudana));
        chips.addView(chipPagu, new LinearLayout.LayoutParams(0, -2, 1f));

        View space = new View(c);
        chips.addView(space, new LinearLayout.LayoutParams(dp(c, 10), 1));

        View chipKas = accountPill(c, "Kas", rupiah(kas));
        chips.addView(chipKas, new LinearLayout.LayoutParams(0, -2, 1f));

        layout.addView(chips);
        return card;
    }

    private static View accountPill(Context c, String label, String value) {
        LinearLayout pill = new LinearLayout(c);
        pill.setOrientation(LinearLayout.VERTICAL);
        pill.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
        pill.setBackgroundResource(R.drawable.bg_pill_chip);
        pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#26FFFFFF"))); // 15% white

        TextView l = new TextView(c);
        l.setText(label);
        l.setTextSize(11);
        l.setTextColor(Color.parseColor("#D4F4ED"));
        pill.addView(l);

        TextView v = new TextView(c);
        v.setText(value);
        v.setTextSize(13);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        v.setTextColor(Color.WHITE);
        pill.addView(v);
        return pill;
    }

    /** Two side-by-side cards for Inflow & Outflow stats */
    static View statRow(Context c, long inflow, long outflow) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, 16);
        row.setLayoutParams(params);

        View cardIn = flowCard(c, "Pemasukan", rupiah(inflow), R.drawable.ic_arrow_inflow, ContextCompat.getColor(c, R.color.inflow_green), ContextCompat.getColor(c, R.color.inflow_bg));
        row.addView(cardIn, new LinearLayout.LayoutParams(0, -2, 1f));

        View space = new View(c);
        row.addView(space, new LinearLayout.LayoutParams(dp(c, 12), 1));

        View cardOut = flowCard(c, "Pengeluaran", rupiah(outflow), R.drawable.ic_arrow_outflow, ContextCompat.getColor(c, R.color.outflow_red), ContextCompat.getColor(c, R.color.outflow_bg));
        row.addView(cardOut, new LinearLayout.LayoutParams(0, -2, 1f));

        return row;
    }

    private static View flowCard(Context c, String title, String amount, int iconRes, int accentColor, int bgColor) {
        MaterialCardView card = new MaterialCardView(c);
        card.setRadius(dp(c, 18));
        card.setCardElevation(dp(c, 1));
        card.setStrokeColor(ContextCompat.getColor(c, R.color.outline_subtle));
        card.setStrokeWidth(dp(c, 1));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(c, 14), dp(c, 14), dp(c, 14), dp(c, 14));
        card.addView(layout);

        LinearLayout header = new LinearLayout(c);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(c);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(accentColor));
        icon.setBackgroundResource(R.drawable.bg_circle_icon);
        icon.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        icon.setPadding(dp(c, 6), dp(c, 6), dp(c, 6), dp(c, 6));
        header.addView(icon, new LinearLayout.LayoutParams(dp(c, 28), dp(c, 28)));

        TextView t = new TextView(c);
        t.setText(title);
        t.setTextSize(12);
        t.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        t.setPadding(dp(c, 8), 0, 0, 0);
        header.addView(t);
        layout.addView(header);

        TextView val = new TextView(c);
        val.setText(amount);
        val.setTextSize(16);
        val.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        val.setTextColor(accentColor);
        val.setPadding(0, dp(c, 8), 0, 0);
        layout.addView(val);

        return card;
    }
}
