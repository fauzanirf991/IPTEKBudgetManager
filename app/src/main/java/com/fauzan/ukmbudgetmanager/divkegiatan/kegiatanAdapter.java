package com.fauzan.ukmbudgetmanager.divkegiatan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fauzan.ukmbudgetmanager.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class kegiatanAdapter extends RecyclerView.Adapter<com.fauzan.ukmbudgetmanager.divkegiatan.kegiatanAdapter.MyViewHolder> implements Filterable {
    List<kegiatan> kegiatan, filterKegiatan;
    private Context context;
    private RecyclerViewClickListener mListener;
    CustomFilter filter;
    private String namaKegiatan, tglKegiatan;
    private int idKegiatan;

    public kegiatanAdapter(List<kegiatan> kegiatan, Context context, RecyclerViewClickListener listener) {
        this.kegiatan = kegiatan;
        this.filterKegiatan = kegiatan;
        this.context = context;
        this.mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kegiatan_list, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.nama_kegiatan_tv.setText(String.valueOf(kegiatan.get(position).getNama_kegiatan()));
        holder.tgl_kegiatan_tv.setText(String.valueOf(kegiatan.get(position).getTgl_kegiatan()));




    }

    @Override
    public int getItemCount() {
        return kegiatan.size();
    }

    public Filter getFilter() {
        if (filter==null) {
            filter=new CustomFilter((ArrayList<kegiatan>) filterKegiatan,this);

        }
        return filter;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RecyclerViewClickListener mListener;
        private TextView nama_kegiatan_tv, tgl_kegiatan_tv;
        private RelativeLayout mRowContainer;

        public MyViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            nama_kegiatan_tv = itemView.findViewById(R.id.name);
            tgl_kegiatan_tv = itemView.findViewById(R.id.date);
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