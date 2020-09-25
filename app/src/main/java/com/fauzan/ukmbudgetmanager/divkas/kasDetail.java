package com.fauzan.ukmbudgetmanager.divkas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;

public class kasDetail extends AppCompatActivity {
    private TextView nama_pembayar_ed, jumlah_bayar_ed, tgl_bayar_ed;

    private String nama_pembayar, tgl_bayar;
    private int id_kas, jumlah_bayar;

    private Menu action;
    private Bitmap bitmap;

    private BaseApiService apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kas_detail);

        nama_pembayar_ed = findViewById(R.id.namaaset_et);
        jumlah_bayar_ed = findViewById(R.id.hargaaset_et);
        tgl_bayar_ed = findViewById(R.id.tglaset_et);

        Intent intent = getIntent();
        id_kas = intent.getIntExtra("id_kas", 0);
        nama_pembayar = intent.getStringExtra("nama_pembayar");
        jumlah_bayar = intent.getIntExtra("jumlah_bayar",0);
        tgl_bayar = intent.getStringExtra("tgl_bayar");

        setDataFromIntentExtra();
    }

    private void setDataFromIntentExtra() {

        if (id_kas != 0) {

            getSupportActionBar().setTitle("Detail Kas".toString());

            nama_pembayar_ed.setText(nama_pembayar);
            jumlah_bayar_ed.setText(String.valueOf(jumlah_bayar));
            tgl_bayar_ed.setText(tgl_bayar);




        } else {
            getSupportActionBar().setTitle("Detail Kas");
        }
    }
}
