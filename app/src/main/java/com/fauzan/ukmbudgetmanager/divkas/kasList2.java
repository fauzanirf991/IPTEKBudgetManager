package com.fauzan.ukmbudgetmanager.divkas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class kasList2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private kasAdapter adapter;
    private List<kas> kaslist;
    private TextView totalKas_txt;
    private List<kastotal> kastotalList;
    private int totalKas;
    BaseApiService apiInterface;
    kasAdapter.RecyclerViewClickListener listener;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kas_list2);

        apiInterface = UtilsApi.getAPIService();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);
        totalKas_txt = findViewById(R.id.totkas_txt);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listener = new kasAdapter.RecyclerViewClickListener(){

            @Override
            public void onRowClick(View view, final int position){
                Intent intent = new Intent(kasList2.this, kasDetail.class);
                intent.putExtra("id_kas", kaslist.get(position).getId_kas());
                intent.putExtra("nama_pembayar", kaslist.get(position).getNama_pembayar());
                intent.putExtra("jumlah_bayar", kaslist.get(position).getJumlah_bayar());
                intent.putExtra("tgl_bayar", kaslist.get(position).getTgl_bayar());
                startActivity(intent);
            }
        };
        getKasTotal();
    }

    public void getkas(){
        Call<List<kas>> call = apiInterface.get_kas();
        call.enqueue(new Callback<List<kas>>() {
            @Override
            public void onResponse(Call<List<kas>> call, Response<List<kas>> response) {
                progressBar.setVisibility(View.GONE);
                kaslist = response.body();
                Log.i(kasList.class.getSimpleName(), response.body().toString());
                adapter = new kasAdapter(kaslist, kasList2.this, listener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<kas>> call, Throwable t) {
                Toast.makeText(kasList2.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getKasTotal(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<kastotal>> call = apiInterface.getTotal_kas();
        call.enqueue(new Callback<List<kastotal>>() {
            @Override
            public void onResponse(Call<List<kastotal>> call, Response<List<kastotal>> response) {
                progressBar.setVisibility(View.GONE);
                List<kastotal> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    totalKas = addlist.get(0).getTotal_kas();
                    totalKas_txt.setText(formatRupiah.format((double)totalKas));

                }else{
                    totalKas_txt.setText('0');
                }
            }

            @Override
            public void onFailure(Call<List<kastotal>> call, Throwable t) {
                Toast.makeText(kasList2.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        getkas();
        getKasTotal();
    }
}
