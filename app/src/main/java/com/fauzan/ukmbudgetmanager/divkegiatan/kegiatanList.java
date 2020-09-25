package com.fauzan.ukmbudgetmanager.divkegiatan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fauzan.ukmbudgetmanager.R;
import com.fauzan.ukmbudgetmanager.divpengeluaran.eventneedsList;
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class kegiatanList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private kegiatanAdapter adapter;
    private List<kegiatan> kegiatanlist;
    BaseApiService apiInterface;
    kegiatanAdapter.RecyclerViewClickListener listener;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kegiatan_list);

        apiInterface = UtilsApi.getAPIService();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listener = new kegiatanAdapter.RecyclerViewClickListener(){

            @Override
            public void onRowClick(View view, final int position){
                Intent intent = new Intent(kegiatanList.this, eventneedsList.class);
                intent.putExtra("id_kegiatan", kegiatanlist.get(position).getId_kegiatan());
                intent.putExtra("nama_kegiatan", kegiatanlist.get(position).getNama_kegiatan());
                intent.putExtra("tgl_kegiatan", kegiatanlist.get(position).getTgl_kegiatan());
                startActivity(intent);
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(kegiatanList.this, eventneedsList.class));
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_kegiatan_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        assert searchManager != null;
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName())
        );
        searchView.setQueryHint("Search kegiatan...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {

                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getkegiatan(){
        Call<List<kegiatan>> call = apiInterface.get_kegiatan();
        call.enqueue(new Callback<List<kegiatan>>() {
            @Override
            public void onResponse(Call<List<kegiatan>> call, Response<List<kegiatan>> response) {
                progressBar.setVisibility(View.GONE);
                kegiatanlist = response.body();
                Log.i(kegiatanList.class.getSimpleName(), response.body().toString());
                adapter = new kegiatanAdapter(kegiatanlist, kegiatanList.this, listener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<kegiatan>> call, Throwable t) {
                Toast.makeText(kegiatanList.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getkegiatan();
    }



}