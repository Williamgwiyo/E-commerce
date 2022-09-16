package com.tu.tuchati;

import android.widget.Filter;

import com.tu.tuchati.Adapters.AdapterOrderSeller;
import com.tu.tuchati.Models.ModelOrderSeller;

import java.util.ArrayList;

public class FilterOrderSeller extends Filter {

    private AdapterOrderSeller adapter;
    private ArrayList<ModelOrderSeller> filterList;

    public FilterOrderSeller(AdapterOrderSeller adapter, ArrayList<ModelOrderSeller> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search querry
        if (constraint != null && constraint.length() >0){

            //change to upper case,  insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderSeller> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check, search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.modelOrderSellerArrayList = (ArrayList<ModelOrderSeller>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
