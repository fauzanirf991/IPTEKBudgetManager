package com.fauzan.ukmbudgetmanager.divlaporan;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import org.json.*;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.divkas.kastotal;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class laporanActivity extends AppCompatActivity {
    private TextView ttlDana_txt, ttlPengeluaran_txt, ttlKas_txt, sisaDana_txt,
            ttlAset_txt, ttlKegiatan_txt;
    private int ttlDana, ttlPengeluaran, ttlKas, sisaDana, ttlAset, ttlKegiatan, total_aset, total_kegiatan;
    private List<danaTotal> danaTotalList;
    BaseApiService apiInterface;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        apiInterface = UtilsApi.getAPIService();

        ttlDana_txt = findViewById(R.id.ttl_dana_txt);
        ttlPengeluaran_txt = findViewById(R.id.ttl_pengeluaran_txt);
        ttlKas_txt =  findViewById(R.id.ttl_kas_txt);
        ttlAset_txt = findViewById(R.id.ttlaset_txt);
        ttlKegiatan_txt = findViewById(R.id.ttlkegiatan_txt);
        sisaDana_txt = findViewById(R.id.sisadana_txt);

        totalDana();
        totalKas();
        totalAset();
        totalKegiatan();
        totalPengeluaran();
        sisaDana();
        setData(ttlAset, ttlKegiatan, sisaDana);





    }

    private void totalDana(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<danaTotal>> call = apiInterface.get_danaTotal();
        call.enqueue(new Callback<List<danaTotal>>() {
            @Override
            public void onResponse(Call<List<danaTotal>> call, Response<List<danaTotal>> response) {
                List<danaTotal> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    ttlDana = addlist.get(0).getJumlahdana();
                    ttlDana_txt.setText(formatRupiah.format((double)ttlDana));

                }else{
                    ttlDana_txt.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<danaTotal>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void totalKas(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<kastotal>> call = apiInterface.getTotal_kas();
        call.enqueue(new Callback<List<kastotal>>() {
            @Override
            public void onResponse(Call<List<kastotal>> call, Response<List<kastotal>> response) {
                List<kastotal> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    ttlKas = addlist.get(0).getTotal_kas();
                    ttlKas_txt.setText(formatRupiah.format((double)ttlKas));

                }else{
                    ttlKas_txt.setText('0');
                }
            }

            @Override
            public void onFailure(Call<List<kastotal>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void totalAset(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<asetTotal>> call = apiInterface.get_Totalaset();
        call.enqueue(new Callback<List<asetTotal>>() {
            @Override
            public void onResponse(Call<List<asetTotal>> call, Response<List<asetTotal>> response) {
                List<asetTotal> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    ttlAset = addlist.get(0).getTotal_aset();
                    ttlAset_txt.setText(formatRupiah.format((double)ttlAset));
                    setData(ttlAset,ttlKegiatan,sisaDana);

                }else{
                    ttlAset_txt.setText('0');
                }
            }

            @Override
            public void onFailure(Call<List<asetTotal>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void totalKegiatan(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<kegiatanTotal>> call = apiInterface.get_Totalkegiatan();
        call.enqueue(new Callback<List<kegiatanTotal>>() {
            @Override
            public void onResponse(Call<List<kegiatanTotal>> call, Response<List<kegiatanTotal>> response) {
                List<kegiatanTotal> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    ttlKegiatan = addlist.get(0).getTotal_kegiatan();
                    ttlKegiatan_txt.setText(formatRupiah.format((double)ttlKegiatan));
                    setData(ttlAset,ttlKegiatan,sisaDana);

                }else{
                    ttlKegiatan_txt.setText('0');
                }
            }

            @Override
            public void onFailure(Call<List<kegiatanTotal>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void totalPengeluaran(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<ttlPengeluaran>> call = apiInterface.get_ttlPengeluaran();
        call.enqueue(new Callback<List<ttlPengeluaran>>() {
            @Override
            public void onResponse(Call<List<ttlPengeluaran>> call, Response<List<ttlPengeluaran>> response) {
                List<ttlPengeluaran> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    ttlPengeluaran = addlist.get(0).getTtl_pengeluaran();
                    ttlPengeluaran_txt.setText(formatRupiah.format((double)ttlPengeluaran));

                }else{
                    ttlPengeluaran_txt.setText(String.valueOf(0));
                }
            }

            @Override
            public void onFailure(Call<List<ttlPengeluaran>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sisaDana(){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.ENGLISH);
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        Call<List<sisaDana>> call = apiInterface.get_sisaDana();
        call.enqueue(new Callback<List<sisaDana>>() {
            @Override
            public void onResponse(Call<List<sisaDana>> call, Response<List<sisaDana>> response) {
                List<sisaDana> addlist = response.body();
                if (addlist != null && addlist.size() !=0) {
                    sisaDana = addlist.get(0).getSisa_dana();
                    sisaDana_txt.setText(formatRupiah.format((double)sisaDana));
                    setData(ttlAset,ttlKegiatan,sisaDana);

                }else{
                    sisaDana_txt.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<sisaDana>> call, Throwable t) {
                Toast.makeText(laporanActivity.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setData(final int ttlAset, final int ttlKegiatan, final int sisaDana) {

        PieChart pieChartList = findViewById(R.id.chart);

        ArrayList<Entry> pieChartData = new ArrayList<Entry>();
        pieChartData.add(new Entry(ttlAset, 0));
        pieChartData.add(new Entry(ttlKegiatan, 1));
        pieChartData.add(new Entry(sisaDana, 2));



        PieDataSet dataSet = new PieDataSet(pieChartData, "");
        dataSet.setValueTextSize(8);


        ArrayList<String> pieChartSectionName = new ArrayList<String>();
        pieChartSectionName.add(0, "Aset");
        pieChartSectionName.add(1, "Kegiatan");
        pieChartSectionName.add(2, "Sisa Pagudana");

        PieData data = new PieData(pieChartSectionName, dataSet);
        pieChartList.setData(data);


        dataSet.setColors(new int[]{Color.parseColor("#C71585"),
                Color.parseColor("#90EE90"),
                Color.parseColor("#F08080")});

        pieChartList.animateXY(500, 500);

    }

    private void cetakTabel(){
        String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyCsvFile.csv"); // Here csv file name is MyCsvFile.csv


    }


}
