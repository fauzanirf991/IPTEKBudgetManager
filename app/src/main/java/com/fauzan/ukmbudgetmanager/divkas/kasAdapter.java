package com.fauzan.ukmbudgetmanager.divkas;

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

import com.fauzan.ukmbudgetmanager.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

public class kasAdapter extends RecyclerView.Adapter<com.fauzan.ukmbudgetmanager.divkas.kasAdapter.MyViewHolder> implements Filterable {
    List<kas> kas, filterKas;
    private Context context;
    private RecyclerViewClickListener mListener;
    CustomFilter filter;
    private String namaKas, tglKas;
    private int idKas, jumlahKas;

    public kasAdapter(List<kas> kas, Context context, RecyclerViewClickListener listener) {
        this.kas = kas;
        this.filterKas = kas;
        this.context = context;
        this.mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kas_list, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        jumlahKas = kas.get(position).getJumlah_bayar();

        holder.nama_pembayar_tv.setText(String.valueOf(kas.get(position).getNama_pembayar()));
        holder.jumlah_bayar_tv.setText(formatRupiah.format((double)jumlahKas));
        holder.tgl_bayar_tv.setText(String.valueOf(kas.get(position).getTgl_bayar()));




    }

    @Override
    public int getItemCount() {
        return kas.size();
    }

    public Filter getFilter() {
        if (filter==null) {
            filter=new CustomFilter((ArrayList<kas>) filterKas,this);

        }
        return filter;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RecyclerViewClickListener mListener;
        private TextView nama_pembayar_tv, jumlah_bayar_tv, tgl_bayar_tv;
        private RelativeLayout mRowContainer;

        public MyViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            nama_pembayar_tv = itemView.findViewById(R.id.name);
            jumlah_bayar_tv = itemView.findViewById(R.id.harga);
            tgl_bayar_tv = itemView.findViewById(R.id.date);
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