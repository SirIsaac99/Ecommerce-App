package com.example.mygrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.R;
import com.example.mygrocery.activities.ShopDetailsActivity;
import com.example.mygrocery.activities.ShopReviewsActivity;
import com.example.mygrocery.models.ModelReviews;
import com.example.mygrocery.models.ModelShops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    private ArrayList<ModelShops> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShops> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shops, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModelShops modelShops = shopsList.get(position);
        String accountType = modelShops.getAccountType();
        String address = modelShops.getAddress();
        String city = modelShops.getCity();
        String state = modelShops.getState();
        String country = modelShops.getCountry();
        String email = modelShops.getEmail();
        String deliveryFee = modelShops.getDeliveryFee();
        String latitude = modelShops.getLatitude();
        String longitude = modelShops.getLongitude();
        String name = modelShops.getName();
        String online = modelShops.getOnline();
        String timestamp = modelShops.getTimestamp();
        String shopOpen = modelShops.getShopOpen();
        String phone = modelShops.getPhone();
        String uid = modelShops.getUid();
        String profileImage = modelShops.getProfileImage();
        String shopName = modelShops.getShopName();

        loadReviews(modelShops, holder);

        //set data
        holder.shopnameEt.setText(shopName);
        holder.phoneEt.setText(phone);
        holder.addressEt.setText(address);
        if (online.equals("true")){
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            holder.onlineIv.setVisibility(View.GONE);
        }
        if (shopOpen.equals("true")){
            holder.shopClosedIv.setVisibility(View.GONE);
        }
        else{
            holder.shopClosedIv.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        }
        catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(ModelShops modelShops, HolderShop holder) {
        String shopUid = modelShops.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float ratings = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+ratings;
                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    class HolderShop extends RecyclerView.ViewHolder {

        private ImageView shopIv,onlineIv;
        public TextView shopClosedIv, shopnameEt,phoneEt,addressEt;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedIv = itemView.findViewById(R.id.shopClosedIv);
            shopnameEt = itemView.findViewById(R.id.shopnameEt);
            phoneEt = itemView.findViewById(R.id.phoneEt);
            addressEt = itemView.findViewById(R.id.addressEt);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
