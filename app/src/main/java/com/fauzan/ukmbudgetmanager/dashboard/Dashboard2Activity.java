package com.fauzan.ukmbudgetmanager.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.divaset.asetList2;
import com.fauzan.ukmbudgetmanager.divdana.IncomeActivity;
import com.fauzan.ukmbudgetmanager.divdana.IncomeActivity2;
import com.fauzan.ukmbudgetmanager.divkas.kasList2;
import com.fauzan.ukmbudgetmanager.divkegiatan.kegiatanList2;
import com.fauzan.ukmbudgetmanager.divlaporan.laporanActivity;
import com.fauzan.ukmbudgetmanager.divuser.UserType;

public class Dashboard2Activity extends AppCompatActivity {

    Button btn_logout, btn_kegiatan, btn_dana, btn_kas, btn_stats, btn_laporan;
    TextView txt_id, txt_username;
    private static long back_pressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard2);
        btn_logout = findViewById(R.id.btn_logout);

        initComponent();
        btn_logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard2Activity.this, UserType.class);
                finish();
                startActivity(intent);
            }
        });

        btn_stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard2Activity.this, asetList2.class);
                startActivity(intent);
            }
        });
        btn_dana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard2Activity.this, IncomeActivity2.class));
            }
        });

        btn_kegiatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard2Activity.this, kegiatanList2.class));
            }
        });

        btn_kas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard2Activity.this, kasList2.class));
            }
        });

        btn_laporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard2Activity.this, laporanActivity.class));
            }
        });




    }
    private void initComponent(){
        btn_logout = findViewById(R.id.btn_logout);
        btn_kegiatan = findViewById(R.id.button_kegiatan);
        btn_dana = findViewById(R.id.button_dana);
        btn_kas = findViewById(R.id.button_kas);
        btn_laporan = findViewById(R.id.button_laporan);
        btn_stats = findViewById(R.id.button_stats);
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit",
                    Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }
}
