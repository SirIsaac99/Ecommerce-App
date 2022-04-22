package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mygrocery.OtherClasses.Constants;
import com.example.mygrocery.R;
import com.example.mygrocery.adapters.AdapterOrderedItems;
import com.example.mygrocery.models.ModelOrderedItems;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private ImageButton backBtn,editBtn,mapBtn;
    private TextView orderIdEt,dateEt,statusEt,addressEt,emailEt,amountEt,phoneEt,totalItemsEt;
    private RecyclerView itemsRv;

    String orderId, orderBy;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItems> orderedItemsArrayList;
    private AdapterOrderedItems adapterOrderedItems;

    String sourceLatitude, sourceLongitude,destinationLatitude, destinationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdEt = findViewById(R.id.orderIdEt);
        dateEt = findViewById(R.id.dateEt);
        statusEt = findViewById(R.id.statusEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        amountEt = findViewById(R.id.amountEt);
        phoneEt = findViewById(R.id.phoneEt);
        totalItemsEt = findViewById(R.id.totalItemsEt);
        itemsRv = findViewById(R.id.itemsRv);

        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        final String[] options = {"In Progress", "Completed", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selectedOptions = options[i];
                        editOrderStatus(selectedOptions);
                    }
                })
        .show();
    }

    private void editOrderStatus(String selectedOptions) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOptions);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String message = "Order is Now"+selectedOptions;
                        Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" +sourceLatitude+ ","+sourceLatitude+ "&daddr=" +destinationLatitude+","+destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = ""+snapshot.child("latitude").getValue();
                        sourceLongitude = ""+snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationLatitude = ""+snapshot.child("latitude").getValue();
                        destinationLongitude = ""+snapshot.child("longitude").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String phone = ""+snapshot.child("phone").getValue();

                        emailEt.setText(email);
                        phoneEt.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

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
                        amountEt.setText("$ "+orderCost+"[Including Delivery Fee $ "+deliveryFee+"]");
                        dateEt.setText(formatDate);

                        findBuyerAddress(latitude, longitude); //find buyer's address to deliver
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findBuyerAddress(String latitude, String longitude) {
        Double lat = Double.parseDouble(latitude);
        Double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);
            addressEt.setText(address);
        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderedItems(){
        orderedItemsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemsArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            //orderedItemsArrayList.add(modelOrderedItems);
                        }
                        adapterOrderedItems = new AdapterOrderedItems(OrderDetailsSellerActivity.this, orderedItemsArrayList);
                        itemsRv.setAdapter(adapterOrderedItems);

                        totalItemsEt.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private  void prepareNotificationMessage(String orderId, String message){
        String NOTIFICATION_TOPIC = "/topics/"+ Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJb = new JSONObject();

        try {
            notificationBodyJb.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJb.put("buyerUid", orderBy);
            notificationBodyJb.put("sellerUid", firebaseAuth.getUid());
            notificationBodyJb.put("orderId", orderId);
            notificationBodyJb.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJb.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJb);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key" + Constants.FCM_KEY);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}