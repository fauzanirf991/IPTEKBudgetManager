package com.fauzan.ukmbudgetmanager.ui;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Modern visual financial breakdown with segmented progress bar and legend metrics. */
final class BreakdownView extends View {
    private long inflow, outflow;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    BreakdownView(Context c) {
        super(c);
        setMinimumHeight(Ui.dp(c, 80));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    void values(long in, long out) {
        inflow = in; outflow = out;
        setContentDescription("Pemasukan " + Ui.rupiah(in) + ", pengeluaran " + Ui.rupiah(out));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        if (width <= 0) return;

        long total = inflow + outflow;
        int barHeight = Ui.dp(getContext(), 16);
        float cornerRadius = barHeight / 2f;
        int top = Ui.dp(getContext(), 8);

        // Track background
        rect.set(0, top, width, top + barHeight);
        paint.setColor(Color.parseColor("#E5EBE8"));
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        if (total > 0) {
            float inRatio = (float) inflow / total;
            float inWidth = Math.max(0, width * inRatio);

            // Inflow Bar (Green)
            if (inWidth > 0) {
                paint.setColor(Color.parseColor("#1B753C"));
                rect.set(0, top, inWidth, top + barHeight);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
            }

            // Outflow Bar (Red)
            if (inWidth < width) {
                paint.setColor(Color.parseColor("#BA1A1A"));
                rect.set(inWidth, top, width, top + barHeight);
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
            }
        }

        // Legends & Percentage indicators below bar
        int legendTop = top + barHeight + Ui.dp(getContext(), 18);
        int dotRadius = Ui.dp(getContext(), 4);

        // Inflow Legend (Left)
        paint.setColor(Color.parseColor("#1B753C"));
        canvas.drawCircle(dotRadius * 2, legendTop + dotRadius, dotRadius, paint);

        paint.setColor(Color.parseColor("#191C1B"));
        paint.setTextSize(Ui.dp(getContext(), 12));
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        int inPct = total == 0 ? 0 : (int) Math.round((inflow * 100.0) / total);
        canvas.drawText("Masuk " + inPct + "% (" + Ui.rupiah(inflow) + ")", dotRadius * 4 + Ui.dp(getContext(), 4), legendTop + dotRadius * 2, paint);

        // Outflow Legend (Right aligned or second line if narrow)
        paint.setColor(Color.parseColor("#BA1A1A"));
        float rightDotX = width - Ui.dp(getContext(), 160);
        if (rightDotX < width / 2) rightDotX = width / 2;
        canvas.drawCircle(rightDotX, legendTop + dotRadius, dotRadius, paint);

        paint.setColor(Color.parseColor("#191C1B"));
        int outPct = total == 0 ? 0 : 100 - inPct;
        canvas.drawText("Keluar " + outPct + "% (" + Ui.rupiah(outflow) + ")", rightDotX + Ui.dp(getContext(), 10), legendTop + dotRadius * 2, paint);
    }
}
