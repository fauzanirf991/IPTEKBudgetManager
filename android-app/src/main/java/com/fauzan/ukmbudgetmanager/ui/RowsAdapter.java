package com.fauzan.ukmbudgetmanager.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.fauzan.ukmbudgetmanager.R;
import com.google.android.material.card.MaterialCardView;
import java.util.*;

final class RowsAdapter extends RecyclerView.Adapter<RowsAdapter.Holder> {
    static final class Row {
        final String title;
        final String date;
        final String kind;
        final String account;
        final long delta;
        final boolean reversed;
        final boolean archived;
        final boolean hasReceipt;
        final Runnable click;

        Row(String title, String date, String kind, String account, long delta, boolean reversed, boolean archived, boolean hasReceipt, Runnable click) {
            this.title = title; this.date = date; this.kind = kind; this.account = account;
            this.delta = delta; this.reversed = reversed; this.archived = archived; this.hasReceipt = hasReceipt;
            this.click = click;
        }

        /** Compatibility constructor for plain notices or search fallbacks */
        Row(String title, String subtitle, Runnable click) {
            this(title, subtitle, "", "", 0, false, false, false, click);
        }
    }

    private final View header;
    private List<Row> rows = new ArrayList<>();

    RowsAdapter(View header) { this.header = header; }
    void submit(List<Row> rows) { this.rows = rows; notifyDataSetChanged(); }

    @Override public int getItemViewType(int position) { return position == 0 ? 0 : 1; }
    @Override public int getItemCount() { return rows.size() + 1; }

    @Override public Holder onCreateViewHolder(ViewGroup parent, int type) {
        if (type == 0) return new Holder(header);
        Context c = parent.getContext();

        MaterialCardView card = new MaterialCardView(c);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -2);
        int marginH = Ui.dp(c, 16), marginV = Ui.dp(c, 5);
        params.setMargins(marginH, marginV, marginH, marginV);
        card.setLayoutParams(params);
        card.setRadius(Ui.dp(c, 16));
        card.setCardElevation(Ui.dp(c, 1));
        card.setStrokeWidth(Ui.dp(c, 1));
        card.setStrokeColor(ContextCompat.getColor(c, R.color.outline_subtle));
        card.setCardBackgroundColor(Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout rowLayout = new LinearLayout(c);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setPadding(Ui.dp(c, 14), Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 12));
        card.addView(rowLayout);

        // Icon Avatar Container (40x40dp)
        ImageView iconView = new ImageView(c);
        iconView.setId(View.generateViewId());
        iconView.setBackgroundResource(R.drawable.bg_circle_icon);
        iconView.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 40));
        iconParams.setMarginEnd(Ui.dp(c, 12));
        rowLayout.addView(iconView, iconParams);

        // Middle Content (Title + Chips/Date)
        LinearLayout center = new LinearLayout(c);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView titleView = new TextView(c);
        titleView.setId(View.generateViewId());
        titleView.setTextSize(15);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        titleView.setTextColor(ContextCompat.getColor(c, R.color.text_primary));
        titleView.setMaxLines(2);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        center.addView(titleView);

        LinearLayout tagsRow = new LinearLayout(c);
        tagsRow.setId(View.generateViewId());
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        tagsRow.setGravity(Gravity.CENTER_VERTICAL);
        tagsRow.setPadding(0, Ui.dp(c, 4), 0, 0);
        center.addView(tagsRow);

        rowLayout.addView(center);

        // Right side (Amount + Chevron)
        LinearLayout right = new LinearLayout(c);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        right.setPadding(Ui.dp(c, 8), 0, 0, 0);

        TextView amountView = new TextView(c);
        amountView.setId(View.generateViewId());
        amountView.setTextSize(14);
        amountView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        right.addView(amountView);

        ImageView chevron = new ImageView(c);
        chevron.setImageResource(R.drawable.ic_chevron_right);
        chevron.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.text_tertiary)));
        LinearLayout.LayoutParams chevParams = new LinearLayout.LayoutParams(Ui.dp(c, 14), Ui.dp(c, 14));
        chevParams.topMargin = Ui.dp(c, 4);
        right.addView(chevron, chevParams);

        rowLayout.addView(right);

        return new Holder(card, iconView, titleView, tagsRow, amountView);
    }

    @Override public void onBindViewHolder(Holder holder, int position) {
        if (position == 0) return;
        Context c = holder.itemView.getContext();
        Row row = rows.get(position - 1);

        holder.title.setText(row.title);
        holder.tagsRow.removeAllViews();

        if (row.kind.isEmpty()) {
            // Simple generic row
            holder.icon.setImageResource(R.drawable.ic_nav_transactions);
            holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.primary)));
            holder.amount.setVisibility(View.GONE);
            TextView sub = new TextView(c);
            sub.setText(row.date); // used as subtitle
            sub.setTextSize(12);
            sub.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
            holder.tagsRow.addView(sub);
        } else {
            holder.amount.setVisibility(View.VISIBLE);

            // Icon & Background tint
            int iconRes = Ui.kindIcon(row.kind);
            int accentColor = Ui.kindColor(c, row.kind);
            int bgColor = Ui.kindBgColor(c, row.kind);

            holder.icon.setImageResource(iconRes);
            holder.icon.setImageTintList(ColorStateList.valueOf(accentColor));
            holder.icon.setBackgroundTintList(ColorStateList.valueOf(bgColor));

            // Tags: Date
            TextView dateView = new TextView(c);
            dateView.setText(row.date + " ");
            dateView.setTextSize(12);
            dateView.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
            holder.tagsRow.addView(dateView);

            // Account pill (if any)
            if (!row.account.isEmpty()) {
                int accColor = "PAGUDANA".equals(row.account) ? ContextCompat.getColor(c, R.color.pagudana_blue) : ContextCompat.getColor(c, R.color.kas_teal);
                int accBg = "PAGUDANA".equals(row.account) ? ContextCompat.getColor(c, R.color.pagudana_bg) : ContextCompat.getColor(c, R.color.kas_bg);
                TextView accBadge = Ui.badge(c, row.account, accColor, accBg);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
                p.setMarginEnd(Ui.dp(c, 4));
                holder.tagsRow.addView(accBadge, p);
            }

            // Category tag (if not already redundant with account)
            if (!row.kind.equals("CASH") && !row.kind.equals("FUNDING")) {
                TextView kindBadge = Ui.badge(c, Ui.kind(row.kind), accentColor, bgColor);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
                p.setMarginEnd(Ui.dp(c, 4));
                holder.tagsRow.addView(kindBadge, p);
            }

            // Badges for Status
            if (row.reversed) {
                TextView revBadge = Ui.badge(c, "Dibatalkan", ContextCompat.getColor(c, R.color.outflow_red), ContextCompat.getColor(c, R.color.outflow_bg));
                holder.tagsRow.addView(revBadge);
            } else if (row.archived) {
                TextView archBadge = Ui.badge(c, "Diarsipkan", ContextCompat.getColor(c, R.color.neutral_badge), ContextCompat.getColor(c, R.color.neutral_bg));
                holder.tagsRow.addView(archBadge);
            } else if (row.hasReceipt) {
                TextView recBadge = Ui.badge(c, "📎 Bukti", ContextCompat.getColor(c, R.color.primary), ContextCompat.getColor(c, R.color.primary_container));
                holder.tagsRow.addView(recBadge);
            }

            // Amount formatting
            if (row.kind.equals("EVENT")) {
                if (row.delta != 0) {
                    holder.amount.setText(Ui.rupiah(Math.abs(row.delta)));
                    holder.amount.setTextColor(ContextCompat.getColor(c, R.color.outflow_red));
                } else {
                    holder.amount.setText("Rp 0");
                    holder.amount.setTextColor(ContextCompat.getColor(c, R.color.text_tertiary));
                }
            } else if (row.delta > 0) {
                holder.amount.setText("+ " + Ui.rupiah(row.delta));
                holder.amount.setTextColor(ContextCompat.getColor(c, R.color.inflow_green));
            } else if (row.delta < 0) {
                holder.amount.setText(Ui.rupiah(row.delta)); // formatted with minus
                holder.amount.setTextColor(ContextCompat.getColor(c, R.color.text_primary));
            } else {
                holder.amount.setText("Rp 0");
                holder.amount.setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
            }

            if (row.reversed) {
                holder.amount.setPaintFlags(holder.amount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.amount.setTextColor(ContextCompat.getColor(c, R.color.text_tertiary));
            } else {
                holder.amount.setPaintFlags(holder.amount.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        holder.itemView.setOnClickListener(v -> row.click.run());
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final LinearLayout tagsRow;
        final TextView amount;

        Holder(View item) {
            super(item);
            icon = null; title = null; tagsRow = null; amount = null;
        }

        Holder(View item, ImageView icon, TextView title, LinearLayout tagsRow, TextView amount) {
            super(item);
            this.icon = icon; this.title = title; this.tagsRow = tagsRow; this.amount = amount;
        }
    }
}
