package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mygrocery.R;
import com.example.mygrocery.adapters.AdapterReviews;
import com.example.mygrocery.models.ModelReviews;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private String shopUid;
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopnameEt,ratingsEt;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReviews> reviewsArrayList;
    private AdapterReviews adapterReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopnameEt = findViewById(R.id.shopnameEt);
        ratingsEt = findViewById(R.id.ratingsEt);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsRv = findViewById(R.id.reviewsRv);

        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        loadshopDetails();
        loadReviews();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        reviewsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewsArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float ratings = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+ratings;
                            ModelReviews modelReviews = ds.getValue(ModelReviews.class);
                            reviewsArrayList.add(modelReviews);
                        }
                        adapterReviews = new AdapterReviews(ShopReviewsActivity.this, reviewsArrayList);
                        reviewsRv.setAdapter(adapterReviews);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingsEt.setText(String.format("%.2f", avgRating)+" ["+numberOfReviews+"]");
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadshopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        shopnameEt.setText(shopName);

                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                        }
                        catch (Exception e){
                            profileIv.setImageResource(R.drawable.ic_store_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}