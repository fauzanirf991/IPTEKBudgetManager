package com.fauzan.ukmbudgetmanager.divaset;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;

public class asetDetail extends AppCompatActivity {
    private TextView nama_aset_ed, harga_aset_ed, tgl_aset_ed;
    private ImageView bukti_img;
    private String nama_aset, tgl_aset, bukti;
    private int id_aset, harga_aset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aset_detail);

        nama_aset_ed = findViewById(R.id.namaaset_et);
        harga_aset_ed = findViewById(R.id.hargaaset_et);
        tgl_aset_ed = findViewById(R.id.tglaset_et);
        bukti_img = findViewById(R.id.buktiimg);

        Intent intent = getIntent();
        id_aset = intent.getIntExtra("id_aset", 0);
        nama_aset = intent.getStringExtra("nama_aset");
        harga_aset = intent.getIntExtra("harga_aset",0);
        tgl_aset = intent.getStringExtra("tgl_aset");
        bukti = intent.getStringExtra("bukti");

        setDataFromIntentExtra();
    }

    private void setDataFromIntentExtra() {

        if (id_aset != 0) {
            getSupportActionBar().setTitle("Detail Aset".toString());

            nama_aset_ed.setText(nama_aset);
            harga_aset_ed.setText(String.valueOf(harga_aset));
            tgl_aset_ed.setText(tgl_aset);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.skipMemoryCache(true);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.placeholder(R.drawable.ic_menu_gallery);
            requestOptions.error(R.drawable.ic_menu_gallery);

            Glide.with(asetDetail.this)
                    .load(bukti)
                    .apply(requestOptions)
                    .into(bukti_img);


        } else {
            nama_aset_ed.setText("");
            harga_aset_ed.setText("");
            tgl_aset_ed.setText("");
        }
    }
}
