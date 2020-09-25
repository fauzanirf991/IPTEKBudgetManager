package com.fauzan.ukmbudgetmanager.divaset;

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
import com.fauzan.ukmbudgetmanager.helper.BaseApiService;
import com.fauzan.ukmbudgetmanager.helper.UtilsApi;

import java.util.List;

public class asetList2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private asetAdapter adapter;
    private List<aset> asetlist;
    BaseApiService apiInterface;
    asetAdapter.RecyclerViewClickListener listener;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aset_list2);

        apiInterface = UtilsApi.getAPIService();
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listener = new asetAdapter.RecyclerViewClickListener(){

            @Override
            public void onRowClick(View view, final int position){
                Intent intent = new Intent(asetList2.this, asetDetail.class);
                intent.putExtra("id_aset", asetlist.get(position).getId_aset());
                intent.putExtra("nama_aset", asetlist.get(position).getNama_aset());
                intent.putExtra("harga_aset", asetlist.get(position).getHarga_aset());
                intent.putExtra("tgl_aset", asetlist.get(position).getTgl_aset());
                intent.putExtra("bukti", asetlist.get(position).getBukti());
                startActivity(intent);
            }
        };




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_aset_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        assert searchManager != null;
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName())
        );
        searchView.setQueryHint("Search aset...");
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

    public void getAset(){
        Call<List<aset>> call = apiInterface.get_aset();
        call.enqueue(new Callback<List<aset>>() {
            @Override
            public void onResponse(Call<List<aset>> call, Response<List<aset>> response) {
                progressBar.setVisibility(View.GONE);
                asetlist = response.body();
                Log.i(asetList.class.getSimpleName(), response.body().toString());
                adapter = new asetAdapter(asetlist, asetList2.this, listener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<aset>> call, Throwable t) {
                Toast.makeText(asetList2.this, "error :"+
                                t.getMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAset();
    }
}
