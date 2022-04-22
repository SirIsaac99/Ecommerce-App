package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.adapters.AdapterOrderSeller;
import com.example.mygrocery.adapters.AdapterProductSeller;
import com.example.mygrocery.OtherClasses.Constants;
import com.example.mygrocery.models.ModelOrderSeller;
import com.example.mygrocery.models.ModelProduct;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameIv,shopnameEt,emailEt, filteredOrdersIv,tabProductsIv,tabOrdersIv,filteredProductsIv;
    private EditText searchProductEt;
    private ImageButton logoutBtn, filterOrdersBtn, editProfileBtn,addProductBtn,filterProductBtn,reviewsBtn,settingsBtn;
    private ImageView profileIv;
    private RelativeLayout ordersRl,productsRl;
    private RecyclerView productsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderSeller> orderSellerArrayList;
    private AdapterOrderSeller adapterOrderSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameIv = findViewById(R.id.nameIv);
        tabProductsIv = findViewById(R.id.tabProductsIv);
        tabOrdersIv = findViewById(R.id.tabOrdersIv);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsIv = findViewById(R.id.filteredProductsIv);
        shopnameEt = findViewById(R.id.shopnameEt);
        productsRv = findViewById(R.id.productsRv);
        emailEt = findViewById(R.id.emailEt);
        ordersRl = findViewById(R.id.ordersRl);
        productsRl = findViewById(R.id.productsRl);
        addProductBtn = findViewById(R.id.addProductBtn);
        profileIv = findViewById(R.id.profileIv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        filteredOrdersIv = findViewById(R.id.filteredOrdersIv);
        filterOrdersBtn = findViewById(R.id.filterOrdersBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        ordersRv = findViewById(R.id.ordersRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();
        
        showProductUI();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductSeller.getFilter().filter(charSequence);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, SellerProfileEditActivity.class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open & edit products activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOffLine();
            }
        });
        tabProductsIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProductUI();
            }
        });
        tabOrdersIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder  builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category")
                .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selected = Constants.productCategories1[i];
                        filteredProductsIv.setText(selected);
                        if (selected.equals("All")){
                            loadAllProducts();
                        }
                        else {
                            loadFilteredProducts(selected);
                        }
                    }
                })
                .show();
            }
        });

        filterOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    filteredOrdersIv.setText("Showing All");
                                    adapterOrderSeller.getFilter().filter("");
                                }
                                else {
                                    String optionClicked = options[i];
                                    filteredOrdersIv.setText("Showing"+optionClicked+"Orders");
                                    adapterOrderSeller.getFilter().filter(optionClicked);
                                }
                            }
                        });
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
        });
    }

    private void loadAllOrders() {
        orderSellerArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderSellerArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderSeller modelOrderSeller = ds.getValue(ModelOrderSeller.class);
                            orderSellerArrayList.add(modelOrderSeller);
                        }
                        adapterOrderSeller = new AdapterOrderSeller(MainSellerActivity.this, orderSellerArrayList);
                        ordersRv.setAdapter(adapterOrderSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String productCategory = ""+ds.child("productCategory").getValue();
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProductUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsIv.setTextColor(getResources().getColor(R.color.black));
        tabProductsIv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersIv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersIv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsIv.setTextColor(getResources().getColor(R.color.white));
        tabProductsIv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersIv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersIv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeOffLine() {
        //making user online after success login
        progressDialog.setMessage("Logging Out...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update db value
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user==null){
                startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));

            }
            else {
                loadMyInfo();
            }
        }

        private void loadMyInfo() {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                String accountType = ""+ds.child("accountType").getValue();
                                String email = ""+ds.child("email").getValue();
                                String shopName = ""+ds.child("shopName").getValue();
                                String profileImage = ""+ds.child("profileImage").getValue();

                                nameIv.setText(name);
                                shopnameEt.setText(shopName);
                                emailEt.setText(email);
                                try {
                                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                                }
                                catch (Exception e){
                                    profileIv.setImageResource(R.drawable.ic_store_gray);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
