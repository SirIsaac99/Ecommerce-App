package com.example.mygrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.R;
import com.example.mygrocery.activities.OrderDetailsUserActivity;
import com.example.mygrocery.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {

    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser = orderUserList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderCost = modelOrderUser.getOrderCost();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderTime = modelOrderUser.getOrderTime();
        String orderBy = modelOrderUser.getOrderBy();
        String orderTo = modelOrderUser.getOrderTo();

        //get shop info
        loadShopInfo(modelOrderUser, holder);
        //set data
        holder.amountEt.setText("Amount: $ " + orderCost);
        holder.orderIdEt.setText("OrderID: " + orderId);
        holder.statusEt.setText(orderStatus);
        if (orderStatus.equals("In Progress")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.purple_700));
        } else if (orderStatus.equals("Completed")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusEt.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.dateEt.setText(formatDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, final HolderOrderUser holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        holder.shopnameEt.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HolderOrderUser extends RecyclerView.ViewHolder {

        private TextView orderIdEt, dateEt, amountEt, shopnameEt, statusEt;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderIdEt = itemView.findViewById(R.id.orderIdEt);
            dateEt = itemView.findViewById(R.id.dateEt);
            amountEt = itemView.findViewById(R.id.amountEt);
            shopnameEt = itemView.findViewById(R.id.shopnameEt);
            statusEt = itemView.findViewById(R.id.statusEt);
        }
    }
}
