package com.fauzan.ukmbudgetmanager.divaset;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class asetEditor extends AppCompatActivity {
    private EditText nama_aset_ed, harga_aset_ed, tgl_aset_ed;
    private ImageView bukti_img;
    private Button ChoosePic;

    Calendar myCalendar = Calendar.getInstance();


    private String nama_aset, tgl_aset, bukti;
    private int id_aset, harga_aset;

    private Menu action;
    private Bitmap bitmap;

    private BaseApiService apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aset_editor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        nama_aset_ed = findViewById(R.id.namaaset_et);
        harga_aset_ed = findViewById(R.id.hargaaset_et);
        tgl_aset_ed = findViewById(R.id.tglaset_et);
        ChoosePic = findViewById(R.id.button_upload);
        bukti_img = findViewById(R.id.buktishow_img);

        tgl_aset_ed.setFocusableInTouchMode(false);
        tgl_aset_ed.setFocusable(false);
        tgl_aset_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(asetEditor.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        ChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });


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

            readMode();
            getSupportActionBar().setTitle("Edit Aset".toString());

            nama_aset_ed.setText(nama_aset);
            harga_aset_ed.setText(String.valueOf(harga_aset));
            tgl_aset_ed.setText(tgl_aset);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.skipMemoryCache(true);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.placeholder(R.drawable.ic_menu_gallery);
            requestOptions.error(R.drawable.ic_menu_gallery);

            Glide.with(asetEditor.this)
                    .load(bukti)
                    .apply(requestOptions)
                    .into(bukti_img);


        } else {
            getSupportActionBar().setTitle("Tambah Aset");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_aset_editor, menu);
        action = menu;
        action.findItem(R.id.menu_save).setVisible(false);

        if (id_aset == 0){

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
                imm.showSoftInput(nama_aset_ed, InputMethodManager.SHOW_IMPLICIT);

                action.findItem(R.id.menu_edit).setVisible(false);
                action.findItem(R.id.menu_hapus).setVisible(false);
                action.findItem(R.id.menu_save).setVisible(true);

                return true;
            case R.id.menu_save:
                //Save

                if (id_aset == 0) {

                    if (TextUtils.isEmpty(nama_aset_ed.getText().toString()) ||
                            TextUtils.isEmpty(harga_aset_ed.getText().toString()) ||
                            TextUtils.isEmpty(tgl_aset_ed.getText().toString()) ){
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

                    updateData("update", id_aset);
                    action.findItem(R.id.menu_edit).setVisible(true);
                    action.findItem(R.id.menu_save).setVisible(false);
                    action.findItem(R.id.menu_hapus).setVisible(true);

                    readMode();
                }

                return true;
            case R.id.menu_hapus:

                AlertDialog.Builder dialog = new AlertDialog.Builder(asetEditor.this);
                dialog.setMessage("Hapus data ini?");
                dialog.setPositiveButton("Yes" ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteData("delete", id_aset, bukti);
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

        tgl_aset_ed.setText(sdf.format(myCalendar.getTime()));
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                bukti_img.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void postData(final String key) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_aset = nama_aset_ed.getText().toString().trim();
        String harga_asetx = harga_aset_ed.getText().toString().trim();
        int harga_aset = Integer.parseInt(harga_asetx);
        String tgl_aset = tgl_aset_ed.getText().toString().trim();
        String bukti = null;
        if (bitmap == null) {
            bukti = "";
        } else {
            bukti = getStringImage(bitmap);
        }

        apiInterface = UtilsApi.getAPIService();

        Call<aset> call = apiInterface.add_aset(key, nama_aset, harga_aset, tgl_aset, bukti);

        call.enqueue(new Callback<aset>() {
            @Override
            public void onResponse(Call<aset> call, Response<aset> response) {

                progressDialog.dismiss();

                Log.i(asetEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(asetEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<aset> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(asetEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateData(final String key, final int id_aset) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Menyimpan....");
        progressDialog.show();

        readMode();

        String nama_aset = nama_aset_ed.getText().toString().trim();
        String harga_asetx = harga_aset_ed.getText().toString().trim();
        int harga_aset = Integer.parseInt(harga_asetx);
        String tgl_aset = tgl_aset_ed.getText().toString().trim();
        String bukti = null;
        if (bitmap == null) {
            bukti = "";
        } else {
            bukti = getStringImage(bitmap);
        }

        apiInterface = UtilsApi.getAPIService();

        Call<aset> call = apiInterface.update_aset(key, id_aset, nama_aset, harga_aset, tgl_aset, bukti);

        call.enqueue(new Callback<aset>() {
            @Override
            public void onResponse(Call<aset> call, Response<aset> response) {

                progressDialog.dismiss();

                Log.i(asetEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    finish();
                } else {
                    Toast.makeText(asetEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<aset> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(asetEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteData(final String key, final int id_aset, final String bukti) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        readMode();

        apiInterface = UtilsApi.getAPIService();

        Call<aset> call = apiInterface.delete_aset(key, id_aset, bukti);

        call.enqueue(new Callback<aset>() {
            @Override
            public void onResponse(Call<aset> call, Response<aset> response) {

                progressDialog.dismiss();

                Log.i(asetEditor.class.getSimpleName(), response.toString());

                String value = response.body().getValue();
                String message = response.body().getMessage();

                if (value.equals("1")){
                    Toast.makeText(asetEditor.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(asetEditor.this, message, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<aset> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(asetEditor.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void readMode(){

        nama_aset_ed.setFocusableInTouchMode(false);
        harga_aset_ed.setFocusableInTouchMode(false);
        tgl_aset_ed.setEnabled(false);

        ChoosePic.setVisibility(View.INVISIBLE);

    }

    private void editMode(){

        nama_aset_ed.setFocusableInTouchMode(true);
        harga_aset_ed.setFocusableInTouchMode(true);
        tgl_aset_ed.setFocusableInTouchMode(true);
        tgl_aset_ed.setEnabled(true);

        ChoosePic.setVisibility(View.VISIBLE);
    }

}
