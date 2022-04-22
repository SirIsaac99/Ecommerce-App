package com.example.mygrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.OtherClasses.FilterOrdersSeller;
import com.example.mygrocery.R;
import com.example.mygrocery.activities.OrderDetailsSellerActivity;
import com.example.mygrocery.models.ModelOrderSeller;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderOrderSeller> implements Filterable {

    public ArrayList<ModelOrderSeller> orderSellerArrayList,filterList;
    private Context context;
    private FilterOrdersSeller filter;

    public AdapterOrderSeller(Context context, ArrayList<ModelOrderSeller> orderSellerArrayList) {
        this.context = context;
        this.orderSellerArrayList = orderSellerArrayList;
        this.filterList = orderSellerArrayList;
    }

    @NonNull
    @Override
    public HolderOrderSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_orders_seller, parent, false);
        return new HolderOrderSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderSeller holder, int position) {
        ModelOrderSeller modelOrderSeller = orderSellerArrayList.get(position);
        String orderId = modelOrderSeller.getOrderId();
        String orderBy = modelOrderSeller.getOrderBy();
        String orderCost = modelOrderSeller.getOrderCost();
        String orderStatus = modelOrderSeller.getOrderStatus();
        String orderTime = modelOrderSeller.getOrderTime();
        String orderTo = modelOrderSeller.getOrderTo();
        //load buyer Info
        loadBuyerOrders(modelOrderSeller,holder);

        holder.amountEt.setText("Amount: $ " + orderCost);
        holder.statusEt.setText(orderStatus);
        holder.orderIdEt.setText("OrderID: " + orderId);

        if (orderStatus.equals("In Progress")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.purple_700));
        } else if (orderStatus.equals("Completed")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();
        holder.orderIdEt.setText(formatDate);
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId",orderId);
                intent.putExtra("orderBy",orderBy);
                context.startActivity(intent);
            }
        });
    }

    private void loadBuyerOrders(ModelOrderSeller modelOrderSeller, HolderOrderSeller holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(modelOrderSeller.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        holder.emailEt.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderSellerArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter=new FilterOrdersSeller(this, filterList);
        }
        return filter;
    }

    class HolderOrderSeller extends RecyclerView.ViewHolder {

        private TextView orderIdEt, orderDateEt, emailEt, amountEt, statusEt;

        public HolderOrderSeller(@NonNull View itemView) {
            super(itemView);

            orderIdEt = itemView.findViewById(R.id.orderIdEt);
            orderDateEt = itemView.findViewById(R.id.orderDateEt);
            emailEt = itemView.findViewById(R.id.emailEt);
            amountEt = itemView.findViewById(R.id.amountEt);
            statusEt = itemView.findViewById(R.id.statusEt);
        }
    }
}
