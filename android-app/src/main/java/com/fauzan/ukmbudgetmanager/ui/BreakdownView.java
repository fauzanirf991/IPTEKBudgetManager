package com.fauzan.ukmbudgetmanager.ui;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Chart values are visual ratios only; all money arithmetic stays in long. */
final class BreakdownView extends View {
    private long inflow, outflow;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    BreakdownView(Context c) { super(c); setMinimumHeight(Ui.dp(c, 96)); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
    void values(long in, long out) { inflow = in; outflow = out; setContentDescription("Pemasukan " + Ui.rupiah(in) + ", pengeluaran " + Ui.rupiah(out)); invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); float max = Math.max(1, Math.max(inflow, outflow)); int gap = Ui.dp(getContext(), 12), height = Ui.dp(getContext(), 22);
        paint.setColor(Color.rgb(0, 107, 94)); canvas.drawRoundRect(0, gap, getWidth() * (inflow / max), gap + height, 8, 8, paint);
        paint.setColor(Color.rgb(183, 99, 57)); canvas.drawRoundRect(0, gap * 2 + height, getWidth() * (outflow / max), gap * 2 + height * 2, 8, 8, paint);
    }
}
