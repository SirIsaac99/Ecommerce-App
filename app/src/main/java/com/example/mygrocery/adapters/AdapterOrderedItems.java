package com.example.mygrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.R;
import com.example.mygrocery.models.ModelOrderedItems;

import java.util.ArrayList;

public class AdapterOrderedItems extends RecyclerView.Adapter<AdapterOrderedItems.HolderorderedItems> {

    private Context context;
    private ArrayList<ModelOrderedItems> orderedItemsArrayList;

    public AdapterOrderedItems(Context context, ArrayList<ModelOrderedItems> orderedItemsArrayList) {
        this.context = context;
        this.orderedItemsArrayList = orderedItemsArrayList;
    }

    @NonNull
    @Override
    public HolderorderedItems onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditems,parent,false);
        return new HolderorderedItems(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderorderedItems holder, int position) {
        ModelOrderedItems modelOrderedItems = orderedItemsArrayList.get(position);
        String getpId = modelOrderedItems.getpId();
        String name = modelOrderedItems.getName();
        String cost = modelOrderedItems.getCost();
        String price = modelOrderedItems.getPrice();
        String quantity = modelOrderedItems.getQuantity();

        holder.itemTitleEt.setText(name);
        holder.itemPriceEachEt.setText("$ "+price);
        holder.itemPriceEt.setText("$ "+cost);
        holder.totalItemsEt.setText("["+quantity+"]");
    }

    @Override
    public int getItemCount() {
        return orderedItemsArrayList.size();
    }

    class HolderorderedItems extends RecyclerView.ViewHolder{

        private TextView itemTitleEt,itemPriceEt,itemPriceEachEt,totalItemsEt;

        public HolderorderedItems(@NonNull View itemView) {
            super(itemView);

            itemTitleEt = itemView.findViewById(R.id.itemTitleEt);
            itemPriceEt = itemView.findViewById(R.id.itemPriceEt);
            itemPriceEachEt = itemView.findViewById(R.id.itemPriceEachEt);
            totalItemsEt = itemView.findViewById(R.id.totalItemsEt);
        }
    }
}
