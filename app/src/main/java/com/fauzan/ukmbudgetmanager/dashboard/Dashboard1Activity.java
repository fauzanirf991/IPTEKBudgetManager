package com.fauzan.ukmbudgetmanager.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.divabout.aboutActivity;
import com.fauzan.ukmbudgetmanager.divaset.asetList;
import com.fauzan.ukmbudgetmanager.divdana.IncomeActivity;
import com.fauzan.ukmbudgetmanager.divkas.kasList;
import com.fauzan.ukmbudgetmanager.divkegiatan.kegiatanList;
import com.fauzan.ukmbudgetmanager.divlaporan.laporanActivity;
import com.fauzan.ukmbudgetmanager.divuser.UserType;

public class Dashboard1Activity extends AppCompatActivity {

    Button btn_logout, btn_kegiatan, btn_dana, btn_kas, btn_stats, btn_laporan, btn_about, btn_help;
    TextView txt_id, tvResultNama;
    String resultNama;
    int resultId, idbend;
    Intent intent;
    private static long back_pressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard1);

        initComponents();

        // untuk mendapatkan data dari activity sebelumnya, yaitu activity login.
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            resultNama = extras.getString("result_nama");
            //resultId = extras.getInt("id_bend");
            //idbend = resultId;
            tvResultNama.setText(resultNama);


    }
    private void initComponents(){
        tvResultNama = (TextView) findViewById(R.id.txt_username);
        btn_logout = findViewById(R.id.btn_logout);
        btn_kegiatan = findViewById(R.id.button_kegiatan);
        btn_dana = findViewById(R.id.button_dana);
        btn_kas = findViewById(R.id.button_kas);
        btn_laporan = findViewById(R.id.button_laporan);
        btn_stats = findViewById(R.id.button_stats);
        btn_about = findViewById(R.id.btn_about);
        btn_help = findViewById(R.id.btn_help);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Dashboard1Activity.this, UserType.class);
                finish();
                startActivity(intent);
            }
        });

        btn_dana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, IncomeActivity.class));
            }
        });

        btn_stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, asetList.class));
            }
        });

        btn_kas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, kasList.class));
            }
        });

        btn_kegiatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, kegiatanList.class));
            }
        });

        btn_laporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, laporanActivity.class));
            }
        });

        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard1Activity.this, aboutActivity.class));
            }
        });


    }


    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit",
                    Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }






}
