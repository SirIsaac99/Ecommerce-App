package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.R;
import com.example.mygrocery.adapters.AdapterOrderUser;
import com.example.mygrocery.adapters.AdapterShop;
import com.example.mygrocery.models.ModelOrderUser;
import com.example.mygrocery.models.ModelShops;
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

public class MainUserActivity extends AppCompatActivity {

    private TextView nameIv,emailEt,phoneEt,tabShopsIv,tabOrdersIv;
    private RelativeLayout shopsRl, ordersRl;
    private ImageButton logoutBtn, editProfileBtn,settingsBtn;
    private ImageView profileIv;
    private RecyclerView shopsRv,ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShops> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameIv=findViewById(R.id.nameIv);
        emailEt=findViewById(R.id.emailEt);
        phoneEt=findViewById(R.id.phoneEt);
        tabOrdersIv=findViewById(R.id.tabOrdersIv);
        tabShopsIv=findViewById(R.id.tabShopsIv);
        shopsRl=findViewById(R.id.shopsRl);
        ordersRl=findViewById(R.id.ordersRl);
        profileIv=findViewById(R.id.profileIv);
        logoutBtn= findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        shopsRv=findViewById(R.id.shopsRv);
        ordersRv=findViewById(R.id.ordersRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth= FirebaseAuth.getInstance();
        checkUser();

        //at start show shops Ui
        showShopsUi();


        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make open edit profile activity
                startActivity(new Intent(MainUserActivity.this, UserProfileEditActivity.class));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        tabShopsIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShopsUi();
            }
        });
        tabOrdersIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrdersUI();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });
    }

    private void showShopsUi() {
        //show shops hide orders
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsIv.setTextColor(getResources().getColor(R.color.black));
        tabShopsIv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersIv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersIv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsIv.setTextColor(getResources().getColor(R.color.white));
        tabShopsIv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                        Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            
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
                            String phone = ""+ds.child("phone").getValue();
                            String email = ""+ds.child("email").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String city = ""+ds.child("city").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            nameIv.setText(name);
                            emailEt.setText(email);
                            phoneEt.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            }
                            catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        ordersList = new ArrayList<>();
        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(uid).child("Orders");
                    reference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            ordersList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                 }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String myCity) {

        shopsList = new ArrayList<>();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                    reference.orderByChild("accountType").equalTo("seller")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    shopsList.clear();
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ModelShops modelShops = ds.getValue(ModelShops.class);
                                            String shopCity = "" + ds.child("city").getValue();
                                            shopsList.add(modelShops);
                                        }
                                        adapterShop = new AdapterShop(MainUserActivity.this, shopsList);
                                        shopsRv.setAdapter(adapterShop);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }
