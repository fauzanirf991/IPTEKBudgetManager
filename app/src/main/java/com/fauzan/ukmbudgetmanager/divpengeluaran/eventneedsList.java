package com.fauzan.ukmbudgetmanager.divpengeluaran;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.divkegiatan.kegiatan;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class eventneedsList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private eventneedsAdapter adapter;
    private List<eventneeds> eventneedslist;
    BaseApiService apiInterface;
    eventneedsAdapter.RecyclerViewClickListener listener;
    ProgressBar progressBar;
    Calendar myCalendar = Calendar.getInstance();
    private EditText nama_kegiatan_ed, tgl_kegiatan_ed;
    private Button saveButton;
    private Menu action;
    private int id_kegiatan;
    private String nama_kegiatan, tgl_kegiatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventneeds_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        apiInterface = UtilsApi.getAPIService();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        nama_kegiatan_ed = findViewById(R.id.namakeg_et);
        tgl_kegiatan_ed = findViewById(R.id.tglkeg_et);
        saveButton = findViewById(R.id.save_btn);

        tgl_kegiatan_ed.setFocusableInTouchMode(false);
        tgl_kegiatan_ed.setFocusable(false);
        tgl_kegiatan_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(eventneedsList.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Intent intent2 = getIntent();
        id_kegiatan = intent2.getIntExtra("id_kegiatan",0);
        nama_kegiatan = intent2.getStringExtra("nama_kegiatan");
        tgl_kegiatan = intent2.getStringExtra("tgl_kegiatan");
        setDataFromIntentExtra();


        listener = new eventneedsAdapter.RecyclerViewClickListener(){

            @Override
            public void onRowClick(View view, final int position){
                Intent intent = new Intent(eventneedsList.this, eventneedsEditor.class);
                intent.putExtra("id_pengeluaran", eventneedslist.get(position).getId_pengeluaran());
                intent.putExtra("nama_pengeluaran", eventneedslist.get(position).getNama_pengeluaran());
                intent.putExtra("jumlah_item", eventneedslist.get(position).getJumlah_item());
                intent.putExtra("harga_satuan", eventneedslist.get(position).getHarga_satuan());
                intent.putExtra("tgl_pengeluaran", eventneedslist.get(position).getTgl_pengeluaran());
                intent.putExtra("bukti", eventneedslist.get(position).getBukti());
                intent.putExtra("id_kegiatan", id_kegiatan);
                startActivity(intent);
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(eventneedsList.this, eventneedsEditor.class);
                intent3.putExtra("id_kegiatan", id_kegiatan);
                startActivity(intent3);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveKegiatan(id_kegiatan);
            }
        });

    }


    private void setDataFromIntentExtra() {

        if (id_kegiatan != 0) {

            readMode();
            getSupportActionBar().setTitle("Daftar pengeluaran".toString());

            nama_kegiatan_ed.setText(nama_kegiatan);
            tgl_kegiatan_ed.setText(tgl_kegiatan);


        } else {
            getSupportActionBar().setTitle("Tambah Kegiatan");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_kegiatan_editor, menu);
        action = menu;
        menu.findItem(R.id.menu_save).setVisible(false);

        if (id_kegiatan == 0){

            action.findItem(R.id.menu_edit).setVisible(false);
            action.findItem(R.id.menu_hapus).setVisible(false);
            action.findItem(R.id.menu_save).setVisible(false);


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
                imm.showSoftInput(nama_kegiatan_ed, InputMethodManager.SHOW_IMPLICIT);

                action.findItem(R.id.menu_edit).setVisible(false);
                action.findItem(R.id.menu_hapus).setVisible(false);
                action.findItem(R.id.menu_save).setVisible(true);

                return true;
            /*case R.id.menu_save:
                //Save

                if (id_kegiatan == 0) {

                    if (TextUtils.isEmpty(nama_kegiatan_ed.getText().toString()) ||
                            TextUtils.isEmpty(tgl_kegiatan_ed.getText().toString()) ){
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

                        postKegiatan("insert");
                        action.findItem(R.id.menu_edit).setVisible(true);
                        action.findItem(R.id.menu_save).setVisible(false);
                        action.findItem(R.id.menu_hapus).setVisible(true);

                        readMode();

                    }

                } else {

                    updateKegiatan("update", id_kegiatan);
                    action.findItem(R.id.menu_edit).setVisible(true);
                    action.findItem(R.id.menu_save).setVisible(false);
                    action.findItem(R.id.menu_hapus).setVisible(true);

                    readMode();
                }

                return true;*/
            case R.id.menu_hapus:

                AlertDialog.Builder dialog = new AlertDialog.Builder(eventneedsList.this);
                dialog.setMessage("Hapus data ini?");
                dialog.setPositiveButton("Yes" ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteKegiatan("delete", id_kegiatan);
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

    private void saveKegiatan(int id_kegiatan){
        if (id_kegiatan == 0) {

            if (TextUtils.isEmpty(nama_kegiatan_ed.getText().toString()) ||
                    TextUtils.isEmpty(tgl_kegiatan_ed.getText().toString()) ){
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

                postKegiatan("insert");
                action.findItem(R.id.menu_edit).setVisible(true);
                action.findItem(R.id.menu_save).setVisible(false);
                action.findItem(R.id.menu_hapus).setVisible(true);

                readMode();

            }

        } else {

            updateKegiatan("update", id_kegiatan);
            action.findItem(R.id.menu_edit).setVisible(true);
            action.findItem(R.id.menu_save).setVisible(false);
            action.findItem(R.id.menu_hapus).setVisible(true);

            readMode();
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
            setTgl_keg();
        }

    };

    private void setTgl_keg() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        tgl_kegiatan_ed.setText(sdf.format(myCalendar.getTime()));
    }

    private void postKegiatan(final String key){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_kegiatan = nama_kegiatan_ed.getText().toString().trim();
        String tgl_kegiatan = tgl_kegiatan_ed.getText().toString().trim();


        apiInterface = UtilsApi.getAPIService();

        Call<kegiatan> call = apiInterface.add_kegiatan(key, nama_kegiatan, tgl_kegiatan);

        call.enqueue(new Callback<kegiatan>() {
            @Override
            public void onResponse(Call<kegiatan> call, Response<kegiatan> response) {

                progressDialog.dismiss();

                Log.i(eventneedsList.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(eventneedsList.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kegiatan> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(eventneedsList.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateKegiatan(final String key, final int id_kegiatan){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_kegiatan = nama_kegiatan_ed.getText().toString().trim();
        String tgl_kegiatan = tgl_kegiatan_ed.getText().toString().trim();


        apiInterface = UtilsApi.getAPIService();

        Call<kegiatan> call = apiInterface.update_kegiatan(key, id_kegiatan, nama_kegiatan, tgl_kegiatan);

        call.enqueue(new Callback<kegiatan>() {
            @Override
            public void onResponse(Call<kegiatan> call, Response<kegiatan> response) {

                progressDialog.dismiss();

                Log.i(eventneedsList.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(eventneedsList.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kegiatan> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(eventneedsList.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteKegiatan(final String key, final int id_kegiatan){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        readMode();

        apiInterface = UtilsApi.getAPIService();

        Call<kegiatan> call = apiInterface.delete_kegiatan(key, id_kegiatan);

        call.enqueue(new Callback<kegiatan>() {
            @Override
            public void onResponse(Call<kegiatan> call, Response<kegiatan> response) {

                progressDialog.dismiss();

                Log.i(eventneedsList.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    Toast.makeText(eventneedsList.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(eventneedsList.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<kegiatan> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(eventneedsList.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getPengeluaran(final int id_kegiatan){
        Call<List<eventneeds>> call = apiInterface.get_eventneeds(id_kegiatan);
        call.enqueue(new Callback<List<eventneeds>>() {
            @Override
            public void onResponse(Call<List<eventneeds>> call, Response<List<eventneeds>> response) {
                progressBar.setVisibility(View.GONE);
                eventneedslist = response.body();
                Log.i(eventneedsList.class.getSimpleName(), response.body().toString());
                adapter = new eventneedsAdapter(eventneedslist, eventneedsList.this, listener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<eventneeds>> call, Throwable t) {
                Toast.makeText(eventneedsList.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPengeluaran(id_kegiatan);
        setDataFromIntentExtra();
    }

    private void readMode(){
        nama_kegiatan_ed.setFocusableInTouchMode(false);
        tgl_kegiatan_ed.setFocusableInTouchMode(false);
        saveButton.setVisibility(View.GONE);

    }

    private void editMode(){
        nama_kegiatan_ed.setFocusableInTouchMode(true);
        tgl_kegiatan_ed.setFocusableInTouchMode(true);
        saveButton.setVisibility(View.VISIBLE);
    }



}