package com.example.mygrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.R;
import com.example.mygrocery.activities.ShopDetailsActivity;
import com.example.mygrocery.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitems, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String pid = modelCartItem.getPid();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String title = modelCartItem.getName();
        String quantity = modelCartItem.getQuantity();
        //set data
        holder.itemTitleIv.setText(""+title);
        holder.itemPriceEt.setText("$"+cost);
        holder.itemQuantityEt.setText("["+quantity+"]");
        holder.itemPriceEachEt.setText(""+price);
        //remove handler
        holder.itemRemoveEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyDB easyDB = EasyDB.init(context, "CART_ITEMS_DB")
                        .setTableName("CART_ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context,"Item removed from cart...",Toast.LENGTH_SHORT).show();

                //refresh db
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double isa = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceIv.getText().toString().trim().replace("$", "")));
                double totalPrice = isa-Double.parseDouble(cost.replace("$", ""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("$", "")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalIv.setText("$"+String.format("%.2f", sTotalPrice));
                ((ShopDetailsActivity)context).allTotalPriceIv.setText("$"+String.format("%.2f", Double.parseDouble(String.format("%.2f",totalPrice))));

                //update after item is removed from cart
                ((ShopDetailsActivity)context).countCart();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();//return no. of records
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView itemTitleIv,itemPriceEt,itemPriceEachEt,itemQuantityEt,itemRemoveEt;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleIv = itemView.findViewById(R.id.itemTitleIv);
            itemPriceEt = itemView.findViewById(R.id.itemPriceEt);
            itemPriceEachEt = itemView.findViewById(R.id.itemPriceEachEt);
            itemQuantityEt = itemView.findViewById(R.id.itemQuantityEt);
            itemRemoveEt = itemView.findViewById(R.id.itemRemoveEt);
        }
    }
}
