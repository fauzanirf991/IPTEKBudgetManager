package com.fauzan.ukmbudgetmanager.divdana;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class IncomeActivity2 extends AppCompatActivity {
    private TextView jumlahdana, tglterima;
    private ImageView buktiterima;
    private List<pagudana> pagudanalist;
    private Context context;
    BaseApiService baseApi;
    ProgressBar progress;
    private Menu action;
    private String tgl_diterima, bukti_diterima;
    public int id_pagudana, jumlahpagudana;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income2);

        baseApi = UtilsApi.getAPIService();

        jumlahdana = findViewById(R.id.jumdana_txt);
        tglterima = findViewById(R.id.tglterima_txt);
        buktiterima = findViewById(R.id.buktiimg);

        tampilDana();



    }

    public void tampilDana(){
        Call<List<pagudana>> call = baseApi.get_dana();
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        call.enqueue(new Callback<List<pagudana>>() {
            @Override
            public void onResponse(Call<List<pagudana>> call, Response<List<pagudana>> response) {
                if (response.isSuccessful()) {


                    List<pagudana> addlist = response.body();
                    if (addlist != null && addlist.size() !=0) {

                        id_pagudana = addlist.get(0).getId_pagudana();
                        jumlahpagudana = addlist.get(0).getJumlahdana();
                        tgl_diterima = addlist.get(0).getTgl_diterima();
                        bukti_diterima = addlist.get(0).getBukti();


                        jumlahdana.setText(formatRupiah.format((double)jumlahpagudana));
                        tglterima.setText(tgl_diterima);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.skipMemoryCache(true);
                        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
                        requestOptions.placeholder(R.drawable.ic_menu_gallery);
                        requestOptions.error(R.drawable.ic_menu_gallery);

                        Glide.with(IncomeActivity2.this).load(bukti_diterima).apply(requestOptions).into(buktiterima);
                    }else{
                        jumlahdana.setText("0");
                        tglterima.setText("");
                        buktiterima.setBackgroundResource(R.drawable.ic_menu_gallery);
                    }


                }


            }

            @Override
            public void onFailure(Call<List<pagudana>> call, Throwable t) {
                Toast.makeText(IncomeActivity2.this, "error :"+t.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tampilDana();
    }
}
