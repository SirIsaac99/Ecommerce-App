package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private TextView shopnameEt;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private ImageView profileIv;
    private FloatingActionButton submitBtn;
    private String shopUid;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        backBtn = findViewById(R.id.backBtn);
        shopnameEt = findViewById(R.id.shopnameEt);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        profileIv = findViewById(R.id.profileIv);
        submitBtn = findViewById(R.id.submitBtn);

        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        loadSopInfo();
        loadMyReview();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private void loadSopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String shopImage = ""+snapshot.child("profileImage").getValue();

                        shopnameEt.setText(shopName);
                        try {
                            Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
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

    private void loadMyReview() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            float myRatings = Float.parseFloat(ratings);
                            ratingBar.setRating(myRatings);
                            reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ratingBar.getRating();
        String review = ""+reviewEt.getText().toString().trim();
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" +firebaseAuth.getUid());
        hashMap.put("ratings", ""+ratings);
        hashMap.put("review", ""+review);
        hashMap.put("timestamp", ""+timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(WriteReviewActivity.this, "Review Published Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}