package com.fauzan.ukmbudgetmanager.divdana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.RetrofitClient;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {
    private TextView jumlahdana, tglterima;
    private ImageView buktiterima;
    private List<pagudana> pagudanalist;
    private Context context;
    BaseApiService baseApi;
    ProgressBar progress;
    private Menu action;
    private String tgl_diterima, tgl_diterima2, bukti_diterima, bukti_diterima2;
    public int id_pagudana, id_pagudana2, jumlahpagudana, jumlah_pagudana2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        baseApi = UtilsApi.getAPIService();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        jumlahdana = findViewById(R.id.jumdana_txt);
        tglterima = findViewById(R.id.tglterima_txt);
        buktiterima = findViewById(R.id.buktiimg);

        tampilDana();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dana, menu);
        action = menu;
        menu.findItem(R.id.menu_tambah).setVisible(false);

        if (id_pagudana == 0){

            action.findItem(R.id.menu_edit).setVisible(false);
            action.findItem(R.id.menu_hapus).setVisible(false);
            action.findItem(R.id.menu_tambah).setVisible(true);

        } else{
            action.findItem(R.id.menu_edit).setVisible(true);
            action.findItem(R.id.menu_hapus).setVisible(true);
            action.findItem(R.id.menu_tambah).setVisible(false);

        }

        return true;

    }

    public void menuEditMode(){
        action.findItem(R.id.menu_edit).setVisible(true);
        action.findItem(R.id.menu_hapus).setVisible(true);
        action.findItem(R.id.menu_tambah).setVisible(false);

    }
    public void menuAddMode(){
        action.findItem(R.id.menu_edit).setVisible(false);
        action.findItem(R.id.menu_hapus).setVisible(false);
        action.findItem(R.id.menu_tambah).setVisible(true);

    }


    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_tambah:
                startActivity(new Intent(IncomeActivity.this,IncomeEditor.class));
                return true;
            case R.id.menu_edit:
                id_pagudana = 1;

                Intent intent = new Intent(IncomeActivity.this,IncomeEditor.class);
                intent.putExtra("id_pagudana", id_pagudana);
                intent.putExtra("jumlahdana",jumlahpagudana);
                intent.putExtra("tgl_diterima", tgl_diterima);
                intent.putExtra("bukti", bukti_diterima);
                startActivity(intent);
                return true;

            case R.id.menu_hapus:
                AlertDialog.Builder dialog = new AlertDialog.Builder(IncomeActivity.this);
                dialog.setMessage("Anda yakin ingin menghapus data Pagudana? \n(semua riwayat pengeluaran dari pagudana ini akan terhapus)");
                dialog.setPositiveButton("Yes" ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteDana("delete", id_pagudana, bukti_diterima);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                        //menuEditMode();
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

                        Glide.with(IncomeActivity.this).load(bukti_diterima)
                                .apply(requestOptions).into(buktiterima);
                    }else{
                        jumlahdana.setText("0");
                        tglterima.setText("");
                        buktiterima.setBackgroundResource(R.drawable.ic_menu_gallery);
                    }


                }


            }

            @Override
            public void onFailure(Call<List<pagudana>> call, Throwable t) {
                Toast.makeText(IncomeActivity.this, "error :"+t.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void deleteDana(final String key, final int id_pagudana, final String bukti){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menghapus...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        baseApi = UtilsApi.getAPIService();
        Call<pagudana> call = baseApi.delete_dana(key, id_pagudana, bukti);
        call.enqueue(new Callback<pagudana>() {
            @Override
            public void onResponse(Call<pagudana> call, Response<pagudana> response) {
                progressDialog.dismiss();
                Log.i(IncomeEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if(value.equals("1")){
                    finish();
                }else{
                    Toast.makeText(IncomeActivity.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<pagudana> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(IncomeActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        tampilDana();
    }



}
