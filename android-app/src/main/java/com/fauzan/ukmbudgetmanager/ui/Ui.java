package com.fauzan.ukmbudgetmanager.ui;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.*;
import java.text.NumberFormat;
import java.util.Locale;

final class Ui {
    static int dp(Context c, int value) { return (int) (c.getResources().getDisplayMetrics().density * value); }
    static LinearLayout column(Context c) { LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL); int p = dp(c, 20); v.setPadding(p, p, p, p); return v; }
    static TextView text(LinearLayout root, String value, int size) {
        TextView view = new TextView(root.getContext()); view.setText(value); view.setTextSize(size); view.setPadding(0, dp(root.getContext(), 6), 0, dp(root.getContext(), 6)); root.addView(view); return view;
    }
    static TextInputEditText field(LinearLayout root, String label, int input) {
        TextInputLayout box = new TextInputLayout(root.getContext()); box.setHint(label); box.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText edit = new TextInputEditText(box.getContext()); edit.setInputType(input); edit.setId(View.generateViewId());
        box.addView(edit); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.bottomMargin = dp(root.getContext(), 10); root.addView(box, params); return edit;
    }
    static MaterialButton button(LinearLayout root, String label, View.OnClickListener click) {
        MaterialButton button = new MaterialButton(root.getContext()); button.setText(label); button.setOnClickListener(click); root.addView(button); return button;
    }
    static String rupiah(long value) { NumberFormat f = NumberFormat.getCurrencyInstance(new Locale("id", "ID")); f.setMaximumFractionDigits(0); return f.format(value); }
    static String kind(String value) { return switch (value) { case "FUNDING" -> "Pagudana"; case "CASH" -> "Iuran kas"; case "EXPENSE" -> "Pengeluaran kegiatan"; case "ASSET" -> "Pembelian aset"; case "EVENT" -> "Kegiatan"; case "VOID" -> "Pembatalan"; default -> value; }; }
}
