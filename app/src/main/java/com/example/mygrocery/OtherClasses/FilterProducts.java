package com.example.mygrocery.OtherClasses;

import android.widget.Filter;

import com.example.mygrocery.adapters.AdapterProductSeller;
import com.example.mygrocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterProducts extends Filter {
    private AdapterProductSeller adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProducts(AdapterProductSeller adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelProduct> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint)
                ){
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.productList = (ArrayList<ModelProduct>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
