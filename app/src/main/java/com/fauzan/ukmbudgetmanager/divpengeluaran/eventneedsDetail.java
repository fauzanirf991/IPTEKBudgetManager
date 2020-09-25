package com.fauzan.ukmbudgetmanager.divpengeluaran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;

public class eventneedsDetail extends AppCompatActivity {
    private TextView nama_pengeluaran_ed, harga_satuan_ed, jumlah_item_ed, tgl_pengeluaran_ed;
    private ImageView bukti_img;

    private String nama_pengeluaran, tgl_pengeluaran, bukti;
    private int id_pengeluaran, harga_satuan, jumlah_item, id_kegiatan;

    private Bitmap bitmap;

    private BaseApiService apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventneeds_detail);

        nama_pengeluaran_ed = findViewById(R.id.namabrg_et);
        harga_satuan_ed = findViewById(R.id.hargabrg_et);
        jumlah_item_ed = findViewById(R.id.jumlahbrg_et);
        tgl_pengeluaran_ed = findViewById(R.id.tglbrg_et);
        bukti_img = findViewById(R.id.buktishow_img);

        Intent intent = getIntent();
        id_pengeluaran = intent.getIntExtra("id_pengeluaran", 0);
        nama_pengeluaran = intent.getStringExtra("nama_pengeluaran");
        harga_satuan = intent.getIntExtra("harga_satuan",0);
        jumlah_item = intent.getIntExtra("jumlah_item", 0);
        tgl_pengeluaran = intent.getStringExtra("tgl_pengeluaran");
        bukti = intent.getStringExtra("bukti");
        id_kegiatan = intent.getIntExtra("id_kegiatan",0);

        setDataFromIntentExtra();

    }

    private void setDataFromIntentExtra() {

        if (id_pengeluaran != 0) {

            getSupportActionBar().setTitle("Detail Pengeluaran".toString());

            nama_pengeluaran_ed.setText(nama_pengeluaran);
            harga_satuan_ed.setText(String.valueOf(harga_satuan));
            jumlah_item_ed.setText(String.valueOf(jumlah_item));
            tgl_pengeluaran_ed.setText(tgl_pengeluaran);


            RequestOptions requestOptions = new RequestOptions();
            requestOptions.skipMemoryCache(true);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.placeholder(R.drawable.ic_menu_gallery);
            requestOptions.error(R.drawable.ic_menu_gallery);

            Glide.with(eventneedsDetail.this)
                    .load(bukti)
                    .apply(requestOptions)
                    .into(bukti_img);


        } else {
            getSupportActionBar().setTitle("Detail Pengeluaran");
        }
    }
}
