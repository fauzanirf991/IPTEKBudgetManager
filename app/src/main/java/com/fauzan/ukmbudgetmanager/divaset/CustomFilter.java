package com.fauzan.ukmbudgetmanager.divaset;


import android.widget.Filter;

import java.util.ArrayList;

public class CustomFilter extends Filter {

    asetAdapter adapter;
    ArrayList<aset> filterList;

    public CustomFilter(ArrayList<aset> filterList,asetAdapter adapter)
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
            ArrayList<aset> filteredaset=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getNama_aset().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredaset.add(filterList.get(i));
                }/*else if(filterList.get(i).getTgl_aset().toUpperCase().contains(constraint)){
                    filteredaset.add(filterList.get(i));
                }*/
            }

            results.count=filteredaset.size();
            results.values=filteredaset;

        }else
        {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.aset= (ArrayList<aset>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();

    }
}