package com.tu.tuchati.Activities;

import android.widget.Filter;

import com.tu.tuchati.Adapters.ProductAdapter;
import com.tu.tuchati.Models.ProductsModel;

import java.util.ArrayList;

public class FilterProducts extends Filter {
    private ProductAdapter adapter;
    private ArrayList<ProductsModel> filterList;

    public FilterProducts(ProductAdapter adapter, ArrayList<ProductsModel> filterList) {
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
            ArrayList<ProductsModel> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check, search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
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
        adapter.productList = (ArrayList<ProductsModel>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
