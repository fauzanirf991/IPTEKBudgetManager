package com.fauzan.ukmbudgetmanager.divkegiatan;

import android.widget.Filter;

import java.util.ArrayList;

public class CustomFilter extends Filter {

    kegiatanAdapter adapter;
    ArrayList<kegiatan> filterList;

    public CustomFilter(ArrayList<kegiatan> filterList, kegiatanAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<kegiatan> filteredkegiatan=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getNama_kegiatan().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredkegiatan.add(filterList.get(i));
                }
            }

            results.count=filteredkegiatan.size();
            results.values=filteredkegiatan;

        }else
        {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.kegiatan= (ArrayList<kegiatan>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();

    }
}