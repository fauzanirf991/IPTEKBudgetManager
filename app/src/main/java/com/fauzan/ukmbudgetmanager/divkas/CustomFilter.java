package com.fauzan.ukmbudgetmanager.divkas;

import android.widget.Filter;
import java.util.ArrayList;

public class CustomFilter extends Filter {

    kasAdapter adapter;
    ArrayList<kas> filterList;

    public CustomFilter(ArrayList<kas> filterList,kasAdapter adapter)
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
            ArrayList<kas> filteredkas=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getNama_pembayar().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredkas.add(filterList.get(i));
                }
            }

            results.count=filteredkas.size();
            results.values=filteredkas;

        }else
        {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.kas= (ArrayList<kas>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();

    }
}