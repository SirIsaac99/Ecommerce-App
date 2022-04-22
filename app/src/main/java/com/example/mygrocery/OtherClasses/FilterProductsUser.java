package com.example.mygrocery.OtherClasses;

import android.widget.Filter;

import com.example.mygrocery.adapters.AdapterProductSeller;
import com.example.mygrocery.adapters.AdapterProductUser;
import com.example.mygrocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterProductsUser extends Filter {
    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProductsUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelProduct> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size();i++){
                if (filterList.get(i).getProductTitle().toUpperCase().contains(charSequence) ||
                filterList.get(i).getProductCategory().toUpperCase().contains(charSequence)
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
        adapter.productsList = (ArrayList<ModelProduct>) filterResults.values;

        adapter.notifyDataSetChanged();
    }
}
