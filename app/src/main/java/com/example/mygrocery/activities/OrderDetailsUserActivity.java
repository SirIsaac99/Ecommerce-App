package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mygrocery.R;
import com.example.mygrocery.adapters.AdapterOrderedItems;
import com.example.mygrocery.models.ModelOrderedItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {

    private String orderTo, orderId;

    //ui items
    private ImageButton backBtn,writeReviewBtn;
    private TextView orderIdEt, dateEt, statusEt, shopnameEt, totalItemsEt, amountEt, addressEt;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelOrderedItems> orderedItemsArrayList;
    private AdapterOrderedItems adapterOrderedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_user);

        orderIdEt = findViewById(R.id.orderIdEt);
        dateEt = findViewById(R.id.dateEt);
        statusEt = findViewById(R.id.statusEt);
        shopnameEt = findViewById(R.id.shopnameEt);
        totalItemsEt = findViewById(R.id.totalItemsEt);
        amountEt = findViewById(R.id.amountEt);
        addressEt = findViewById(R.id.addressEt);
        itemsRv = findViewById(R.id.itemsRv);
        backBtn = findViewById(R.id.backBtn);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(OrderDetailsUserActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo);
                startActivity(intent1);
            }
        });
    }

    private void loadOrderedItems() {
        orderedItemsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemsArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            //orderedItemsArrayList.add(modelOrderedItems);
                        }
                        adapterOrderedItems = new AdapterOrderedItems(OrderDetailsUserActivity.this, orderedItemsArrayList);
                        itemsRv.setAdapter(adapterOrderedItems);

                        totalItemsEt.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderTime = "" + snapshot.child("orderTime").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String latitude = "" + snapshot.child("latitude").getValue();
                        String longitude = "" + snapshot.child("longitude").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if (orderStatus.equals("In Progress")) {
                            statusEt.setTextColor(getResources().getColor(R.color.purple_700));
                        } else if (orderStatus.equals("Completed")) {
                            statusEt.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else if (orderStatus.equals("Cancelled")) {
                            statusEt.setTextColor(getResources().getColor(R.color.colorRed));
                        }
                        orderIdEt.setText(orderId);
                        statusEt.setText(orderStatus);
                        amountEt.setText("$ " + orderCost + "[including Delivery Fee: $ " + deliveryFee + "]");
                        dateEt.setText(formatDate);

                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);
            addressEt.setText(address);
        } catch (Exception e) {

        }
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        shopnameEt.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}