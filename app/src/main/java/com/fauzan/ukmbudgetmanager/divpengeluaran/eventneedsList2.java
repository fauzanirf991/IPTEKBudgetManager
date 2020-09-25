package com.fauzan.ukmbudgetmanager.divpengeluaran;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.util.Calendar;
import java.util.List;

public class eventneedsList2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private eventneedsAdapter adapter;
    private List<eventneeds> eventneedslist;
    BaseApiService apiInterface;
    eventneedsAdapter.RecyclerViewClickListener listener;
    ProgressBar progressBar;
    Calendar myCalendar = Calendar.getInstance();
    private TextView nama_kegiatan_ed, tgl_kegiatan_ed;
    private Button saveButton;
    private Menu action;
    private int id_kegiatan;
    private String nama_kegiatan, tgl_kegiatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventneeds_list2);

        apiInterface = UtilsApi.getAPIService();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        nama_kegiatan_ed = findViewById(R.id.namakegiatan_et);
        tgl_kegiatan_ed = findViewById(R.id.tglkegiatan_et);

        Intent intent2 = getIntent();
        id_kegiatan = intent2.getIntExtra("id_kegiatan",0);
        nama_kegiatan = intent2.getStringExtra("nama_kegiatan");
        tgl_kegiatan = intent2.getStringExtra("tgl_kegiatan");
        setDataFromIntentExtra();

        listener = new eventneedsAdapter.RecyclerViewClickListener(){

            @Override
            public void onRowClick(View view, final int position){
                Intent intent = new Intent(eventneedsList2.this, eventneedsDetail.class);
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
    }

    private void setDataFromIntentExtra() {

        if (id_kegiatan != 0) {
            getSupportActionBar().setTitle("Daftar pengeluaran".toString());

            nama_kegiatan_ed.setText(nama_kegiatan);
            tgl_kegiatan_ed.setText(tgl_kegiatan);


        } else {
            getSupportActionBar().setTitle("Daftar pengeluaran");
        }
    }

    public void getPengeluaran(final int id_kegiatan){
        Call<List<eventneeds>> call = apiInterface.get_eventneeds(id_kegiatan);
        call.enqueue(new Callback<List<eventneeds>>() {
            @Override
            public void onResponse(Call<List<eventneeds>> call, Response<List<eventneeds>> response) {
                progressBar.setVisibility(View.GONE);
                eventneedslist = response.body();
                Log.i(eventneedsList.class.getSimpleName(), response.body().toString());
                adapter = new eventneedsAdapter(eventneedslist, eventneedsList2.this, listener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<eventneeds>> call, Throwable t) {
                Toast.makeText(eventneedsList2.this, "error :"+
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
}
