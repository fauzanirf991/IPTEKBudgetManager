package com.fauzan.ukmbudgetmanager.divaset;

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
import retrofit2.Response;

public class asetAdapter extends RecyclerView.Adapter<asetAdapter.MyViewHolder> implements Filterable {
    List<aset> aset, filterAset;
    private Context context;
    private RecyclerViewClickListener mListener;
    CustomFilter filter;
    private String namaAset, tglAset, bukti;
    private int idAset, hargaAset;

    public asetAdapter(List<aset> aset, Context context, RecyclerViewClickListener listener) {
        this.aset = aset;
        this.filterAset = aset;
        this.context = context;
        this.mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aset_list, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        hargaAset = aset.get(position).getHarga_aset();

        holder.mName.setText(String.valueOf(aset.get(position).getNama_aset()));
        holder.mHarga.setText(formatRupiah.format((double)hargaAset));
        holder.mDate.setText(String.valueOf(aset.get(position).getTgl_aset()));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(true);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
        requestOptions.placeholder(R.drawable.ic_menu_gallery);
        requestOptions.error(R.drawable.ic_menu_gallery);

        Glide.with(context)
                .load(aset.get(position).getBukti())
                .apply(requestOptions)
                .into(holder.mPicture);



    }

    @Override
    public int getItemCount() {
        return aset.size();
    }

    public Filter getFilter() {
        if (filter==null) {
            filter=new CustomFilter((ArrayList<aset>) filterAset,this);

        }
        return filter;
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
