package com.fauzan.ukmbudgetmanager.divkas;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class kasEditor extends AppCompatActivity {
    private EditText nama_pembayar_ed, jumlah_bayar_ed, tgl_bayar_ed;

    Calendar myCalendar = Calendar.getInstance();


    private String nama_pembayar, tgl_bayar;
    private int id_kas, jumlah_bayar;

    private Menu action;
    private Bitmap bitmap;

    private BaseApiService apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kas_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        nama_pembayar_ed = findViewById(R.id.namaaset_et);
        jumlah_bayar_ed = findViewById(R.id.hargaaset_et);
        tgl_bayar_ed = findViewById(R.id.tglaset_et);

        tgl_bayar_ed.setFocusableInTouchMode(false);
        tgl_bayar_ed.setFocusable(false);
        tgl_bayar_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(kasEditor.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        Intent intent = getIntent();
        id_kas = intent.getIntExtra("id_kas", 0);
        nama_pembayar = intent.getStringExtra("nama_pembayar");
        jumlah_bayar = intent.getIntExtra("jumlah_bayar",0);
        tgl_bayar = intent.getStringExtra("tgl_bayar");

        setDataFromIntentExtra();

    }

    private void setDataFromIntentExtra() {

        if (id_kas != 0) {

            readMode();
            getSupportActionBar().setTitle("Edit Kas".toString());

            nama_pembayar_ed.setText(nama_pembayar);
            jumlah_bayar_ed.setText(String.valueOf(jumlah_bayar));
            tgl_bayar_ed.setText(tgl_bayar);




        } else {
            getSupportActionBar().setTitle("Tambah Kas");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_kas_editor, menu);
        action = menu;
        action.findItem(R.id.menu_save).setVisible(false);

        if (id_kas == 0){

            action.findItem(R.id.menu_edit).setVisible(false);
            action.findItem(R.id.menu_hapus).setVisible(false);
            action.findItem(R.id.menu_save).setVisible(true);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                this.finish();

                return true;
            case R.id.menu_edit:
                //Edit

                editMode();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(nama_pembayar_ed, InputMethodManager.SHOW_IMPLICIT);

                action.findItem(R.id.menu_edit).setVisible(false);
                action.findItem(R.id.menu_hapus).setVisible(false);
                action.findItem(R.id.menu_save).setVisible(true);

                return true;
            case R.id.menu_save:
                //Save

                if (id_kas == 0) {

                    if (TextUtils.isEmpty(nama_pembayar_ed.getText().toString()) ||
                            TextUtils.isEmpty(jumlah_bayar_ed.getText().toString()) ||
                            TextUtils.isEmpty(tgl_bayar_ed.getText().toString()) ){
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

                        postData("insert");
                        action.findItem(R.id.menu_edit).setVisible(true);
                        action.findItem(R.id.menu_save).setVisible(false);
                        action.findItem(R.id.menu_hapus).setVisible(true);

                        readMode();

                    }

                } else {

                    updateData("update", id_kas);
                    action.findItem(R.id.menu_edit).setVisible(true);
                    action.findItem(R.id.menu_save).setVisible(false);
                    action.findItem(R.id.menu_hapus).setVisible(true);

                    readMode();
                }

                return true;
            case R.id.menu_hapus:

                AlertDialog.Builder dialog = new AlertDialog.Builder(kasEditor.this);
                dialog.setMessage("Hapus data ini?");
                dialog.setPositiveButton("Yes" ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteData("delete", id_kas);
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

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setBirth();
        }

    };

    private void setBirth() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        tgl_bayar_ed.setText(sdf.format(myCalendar.getTime()));
    }



    private void postData(final String key) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_pembayar = nama_pembayar_ed.getText().toString().trim();
        String jumlah_bayarx = jumlah_bayar_ed.getText().toString().trim();
        int jumlah_bayar = Integer.parseInt(jumlah_bayarx);
        String tgl_bayar = tgl_bayar_ed.getText().toString().trim();


        apiInterface = UtilsApi.getAPIService();

        Call<kas> call = apiInterface.add_kas(key, nama_pembayar, jumlah_bayar, tgl_bayar);

        call.enqueue(new Callback<kas>() {
            @Override
            public void onResponse(Call<kas> call, Response<kas> response) {

                progressDialog.dismiss();

                Log.i(kasEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(kasEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kas> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(kasEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateData(final String key, final int id_kas) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_pembayar = nama_pembayar_ed.getText().toString().trim();
        String jumlah_bayarx = jumlah_bayar_ed.getText().toString().trim();
        int jumlah_bayar = Integer.parseInt(jumlah_bayarx);
        String tgl_bayar = tgl_bayar_ed.getText().toString().trim();

        apiInterface = UtilsApi.getAPIService();

        Call<kas> call = apiInterface.update_kas(key, id_kas, nama_pembayar, jumlah_bayar, tgl_bayar);

        call.enqueue(new Callback<kas>() {
            @Override
            public void onResponse(Call<kas> call, Response<kas> response) {

                progressDialog.dismiss();

                Log.i(kasEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(kasEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kas> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(kasEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteData(final String key, final int id_kas) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        readMode();

        apiInterface = UtilsApi.getAPIService();

        Call<kas> call = apiInterface.delete_kas(key, id_kas);

        call.enqueue(new Callback<kas>() {
            @Override
            public void onResponse(Call<kas> call, Response<kas> response) {

                progressDialog.dismiss();

                Log.i(kasEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    Toast.makeText(kasEditor.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(kasEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kas> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(kasEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void readMode(){

        nama_pembayar_ed.setFocusableInTouchMode(false);
        jumlah_bayar_ed.setFocusableInTouchMode(false);
        tgl_bayar_ed.setEnabled(false);


    }

    private void editMode(){

        nama_pembayar_ed.setFocusableInTouchMode(true);
        jumlah_bayar_ed.setFocusableInTouchMode(true);
        tgl_bayar_ed.setFocusableInTouchMode(true);
        tgl_bayar_ed.setEnabled(true);

    }

}
