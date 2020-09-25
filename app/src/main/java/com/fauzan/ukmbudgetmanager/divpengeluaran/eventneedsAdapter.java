package com.fauzan.ukmbudgetmanager.divpengeluaran;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

public class eventneedsAdapter extends RecyclerView.Adapter<eventneedsAdapter.MyViewHolder> {
    List<eventneeds> eventneeds;
    private Context context;
    private RecyclerViewClickListener mListener;
    private String namaPengeluaran, tglPengeluaran, bukti;
    private int idPengeluaran;

    public eventneedsAdapter(List<eventneeds> eventneeds, Context context, RecyclerViewClickListener listener) {
        this.eventneeds = eventneeds;
        this.context = context;
        this.mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eventneeds_list, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        int jumlahItem = eventneeds.get(position).getJumlah_item();
        int hargaSatuan = eventneeds.get(position).getHarga_satuan();

        holder.mName.setText(String.valueOf(eventneeds.get(position).getNama_pengeluaran()));
        holder.mHarga.setText(formatRupiah.format((double) hargaSatuan)+" x"+formatRupiah.format((double) jumlahItem));
        holder.mDate.setText(String.valueOf(eventneeds.get(position).getTgl_pengeluaran()));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(true);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
        requestOptions.placeholder(R.drawable.ic_menu_gallery);
        requestOptions.error(R.drawable.ic_menu_gallery);

        Glide.with(context)
                .load(eventneeds.get(position).getBukti())
                .apply(requestOptions)
                .into(holder.mPicture);



    }

    @Override
    public int getItemCount() {
        return eventneeds.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RecyclerViewClickListener mListener;
        private ImageView mPicture;
        private TextView mName, mHarga, mDate;
        private RelativeLayout mRowContainer;

        public MyViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mPicture = itemView.findViewById(R.id.picture);
            mName = itemView.findViewById(R.id.name);
            mHarga = itemView.findViewById(R.id.harga);
            mDate = itemView.findViewById(R.id.date);
            mRowContainer = itemView.findViewById(R.id.row_container);

            mListener = listener;
            mRowContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.row_container:
                    mListener.onRowClick(mRowContainer, getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
    }

    public interface RecyclerViewClickListener {
        void onRowClick(View view, int position);
    }
}