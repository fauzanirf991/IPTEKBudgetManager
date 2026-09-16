package com.fauzan.ukmbudgetmanager.ui;

import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.*;

final class RowsAdapter extends RecyclerView.Adapter<RowsAdapter.Holder> {
    record Row(String title, String subtitle, Runnable click) {}
    private final View header;
    private List<Row> rows = new ArrayList<>();
    RowsAdapter(View header) { this.header = header; }
    void submit(List<Row> rows) { this.rows = rows; notifyDataSetChanged(); }
    @Override public int getItemViewType(int position) { return position == 0 ? 0 : 1; }
    @Override public int getItemCount() { return rows.size() + 1; }
    @Override public Holder onCreateViewHolder(ViewGroup parent, int type) {
        if (type == 0) return new Holder(header);
        MaterialCardView card = new MaterialCardView(parent.getContext());
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -2); int margin = Ui.dp(parent.getContext(), 16); params.setMargins(margin, 0, margin, margin / 2); card.setLayoutParams(params);
        card.setClickable(true); card.setFocusable(true);
        LinearLayout content = Ui.column(parent.getContext()); card.addView(content);
        Ui.text(content, "", 16); Ui.text(content, "", 13); return new Holder(card);
    }
    @Override public void onBindViewHolder(Holder holder, int position) {
        if (position == 0) return;
        Row row = rows.get(position - 1); LinearLayout content = (LinearLayout) ((MaterialCardView) holder.itemView).getChildAt(0);
        ((TextView) content.getChildAt(0)).setText(row.title); ((TextView) content.getChildAt(1)).setText(row.subtitle);
        // Bind the record itself. Never index an unfiltered list using adapter position.
        holder.itemView.setOnClickListener(v -> row.click.run());
    }
    static final class Holder extends RecyclerView.ViewHolder { Holder(View item) { super(item); } }
}
