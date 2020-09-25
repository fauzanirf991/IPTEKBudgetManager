package com.fauzan.ukmbudgetmanager.divdana;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class IncomeEditor extends AppCompatActivity {
    private EditText jumlahdana_ed, tglditerima_ed;
    private Button btn_upload;
    private ImageView buktiimg;
    private Bitmap bitmap;
    private Menu action;
    private String tgl_diterima, bukti;
    private int id_pagudana, jumlahdana;
    Calendar myCalendar = Calendar.getInstance();
    private BaseApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_editor);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        jumlahdana_ed = findViewById(R.id.jumlahdana_et);
        tglditerima_ed = findViewById(R.id.tglterima_et);
        btn_upload = findViewById(R.id.button_upload);
        buktiimg = findViewById(R.id.buktishow_img);

        tglditerima_ed.setFocusableInTouchMode(false);
        tglditerima_ed.setFocusable(false);
        tglditerima_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(IncomeEditor.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        Intent intent = getIntent();
        id_pagudana = intent.getIntExtra("id_pagudana",0);
        jumlahdana = intent.getIntExtra("jumlahdana",0);
        tgl_diterima = intent.getStringExtra("tgl_diterima");
        bukti = intent.getStringExtra("bukti");

        setDataFromIntentExtra();

    }


    private void setDataFromIntentExtra(){
        if(id_pagudana != 0){
            getSupportActionBar().setTitle("Edit Pagudana");

            jumlahdana_ed.setText(String.valueOf(jumlahdana));
            tglditerima_ed.setText(tgl_diterima);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.skipMemoryCache(true);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.placeholder(R.drawable.ic_menu_gallery);
            requestOptions.error(R.drawable.ic_menu_gallery);

            Glide.with(IncomeEditor.this).load(bukti).apply(requestOptions).into(buktiimg);
        }else{
            getSupportActionBar().setTitle("Masukan Data Pagudana");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        action = menu;
        action.findItem(R.id.menu_save).setVisible(true);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_save:
                if(id_pagudana == 0){


                    if (TextUtils.isEmpty(jumlahdana_ed.getText().toString()) || TextUtils.isEmpty(tglditerima_ed.getText().toString())){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setMessage("Kolom Isian Tidak Boleh Kosong!");
                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }

                    else {

                        postDana("insert");


                    }
                }else{
                    updateDana("update", id_pagudana);


                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTgl_diterima();
        }

    };

    private void setTgl_diterima() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        tglditerima_ed.setText(sdf.format(myCalendar.getTime()));
    }




    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                buktiimg.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void postDana(final String key){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String jumlahdan = jumlahdana_ed.getText().toString().trim();
        int jumlah_dana = Integer.parseInt(jumlahdan);
        String tgl_diterima = tglditerima_ed.getText().toString().trim();
        String bukti = getStringImage(bitmap);

        apiService = UtilsApi.getAPIService();
        Call<pagudana> call = apiService.add_dana(key, jumlah_dana, tgl_diterima, bukti);
        call.enqueue(new Callback<pagudana>() {
            @Override
            public void onResponse(Call<pagudana> call, Response<pagudana> response) {
                progressDialog.dismiss();

                Log.i(IncomeEditor.class.getSimpleName(), response.toString());
                String value = response.body().getValue();
                String message = response.body().getMessage();

                if(value.equals("1")){
                    finish();
                }else {
                    Toast.makeText(IncomeEditor.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<pagudana> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(IncomeEditor.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateDana(final String key, final int id_pagudana){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String jumlahdan = jumlahdana_ed.getText().toString().trim();
        int jumlah_dana = Integer.parseInt(jumlahdan);
        String tgl_diterima = tglditerima_ed.getText().toString().trim();
        String bukti = getStringImage(bitmap);

        apiService = UtilsApi.getAPIService();
        Call<pagudana> call = apiService.update_dana(key, id_pagudana, jumlah_dana, tgl_diterima, bukti);
        call.enqueue(new Callback<pagudana>() {
            @Override
            public void onResponse(Call<pagudana> call, Response<pagudana> response) {
                progressDialog.dismiss();

                Log.i(IncomeEditor.class.getSimpleName(), response.toString());
                String value = response.body().getValue();
                String message = response.body().getMessage();

                if(value.equals("1")){
                    finish();
                }else {
                    Toast.makeText(IncomeEditor.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<pagudana> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(IncomeEditor.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
