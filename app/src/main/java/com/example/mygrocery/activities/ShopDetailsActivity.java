package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mygrocery.OtherClasses.Constants;
import com.example.mygrocery.R;
import com.example.mygrocery.adapters.AdapterCartItem;
import com.example.mygrocery.adapters.AdapterProductUser;
import com.example.mygrocery.models.ModelCartItem;
import com.example.mygrocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIv;
    private TextView shopnameEt,phoneEt,emailEt,addressEt,deliveryFeeEt,openCloseIv,filteredProductsIv,cartItemCountEt;
    private ImageButton callBtn,mapBtn,cartBtn,filterProductBtn,backBtn,reviewsBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;

    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private  String myLatitude, myLongitude,myPhone;
    private  String shopName,shopPhone,shopEmail,shopAddress,shopLatitude, shopLongitude;
    public String deliveryFee;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    private ArrayList<ModelCartItem> cartItemsList;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv = findViewById(R.id.shopIv);
        shopnameEt = findViewById(R.id.shopnameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        addressEt = findViewById(R.id.addressEt);
        deliveryFeeEt = findViewById(R.id.deliveryFeeEt);
        openCloseIv = findViewById(R.id.openCloseIv);
        filteredProductsIv = findViewById(R.id.filteredProductsIv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartItemCountEt = findViewById(R.id.cartItemCountEt);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        ratingBar = findViewById(R.id.ratingBar);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();

        easyDB = EasyDB.init(this, "CART_ITEMS_DB")
                .setTableName("CART_ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        deleteCartData();
        countCart();



        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductUser.getFilter().filter(charSequence);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCartDialog();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder  builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.productCategories1[i];
                                filteredProductsIv.setText(selected);
                                if (selected.equals("All")){
                                    loadShopProducts();
                                }
                                else {
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {

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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable(); //deletes all data record from table
    }
    public void countCart(){
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //if no items in cart hide cart count textview
            cartItemCountEt.setVisibility(View.GONE);
        }
        else{
            cartItemCountEt.setVisibility(View.VISIBLE);
            cartItemCountEt.setText(""+count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView sTotalIv,dFeeIv,allTotalPriceIv;

    private void showCartDialog() {
        cartItemsList = new ArrayList<>();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        TextView shopnameEt = view.findViewById(R.id.shopnameEt);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalIv = view.findViewById(R.id.sTotalIv);
        dFeeIv = view.findViewById(R.id.dFeeIv);
        allTotalPriceIv = view.findViewById(R.id.totalIv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopnameEt.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "CART_ITEMS_DB")
                .setTableName("CART_ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pid = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice+Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pid,
                    ""+name,
                    ""+price,
                    ""+quantity,
                    ""+cost
            );
            cartItemsList.add(modelCartItem);
        }
        adapterCartItem = new AdapterCartItem(this, cartItemsList);
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeIv.setText("$"+deliveryFee);
        sTotalIv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceIv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$", ""))));

        AlertDialog dialog = builder.create();
        dialog.show();



        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing an order",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (myPhone.equals("") || myPhone.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter Phone number in your profile before placing an order",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemsList.size() == 0){
                    Toast.makeText(ShopDetailsActivity.this, "No items in Cart",Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });
    }

    private void submitOrder() {
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        String cost = allTotalPriceIv.getText().toString().trim().replace("$", "");

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("deliveryFee", ""+deliveryFee);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i=0; i<cartItemsList.size(); i++){
                            String pid = cartItemsList.get(i).getPid();
                            String id = cartItemsList.get(i).getId();
                            String cost = cartItemsList.get(i).getCost();
                            String name = cartItemsList.get(i).getName();
                            String price = cartItemsList.get(i).getPrice();
                            String quantity = cartItemsList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pid);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order placed successfully...",Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" +myLatitude+ ","+myLongitude+ "&daddr=" +shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String email = ""+ds.child("email").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String city = ""+ds.child("city").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get data
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress = ""+snapshot.child("address").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();

                //set data
                shopnameEt.setText(shopName);
                emailEt.setText(shopEmail);
                deliveryFeeEt.setText("Delivery Fee: $"+deliveryFee);
                addressEt.setText(shopAddress);
                shopnameEt.setText(shopName);
                addressEt.setText(shopAddress);
                phoneEt.setText(shopPhone);
                if (shopOpen.equals("true")){
                    openCloseIv.setText("Open");
                }
                else{
                    openCloseIv.setText("Closed");
                }
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        productsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(shopUid).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private  void prepareNotificationMessage(String orderId){
        String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order "+orderId;
        String NOTIFICATION_MESSAGE = "Alert!!! You have a new order";
        String NOTIFICATION_TYPE = "NewOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJb = new JSONObject();

        try {
            notificationBodyJb.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJb.put("buyerUid", firebaseAuth.getUid());
            notificationBodyJb.put("sellerUid", shopUid);
            notificationBodyJb.put("orderId", orderId);
            notificationBodyJb.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJb.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJb);
        }
        catch (Exception e){
           Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //open order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //open order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
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