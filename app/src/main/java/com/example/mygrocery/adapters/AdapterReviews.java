package com.example.mygrocery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.R;
import com.example.mygrocery.models.ModelReviews;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReviews extends RecyclerView.Adapter<AdapterReviews.HolderReviews> {

    private Context context;
    private ArrayList<ModelReviews> reviewsArrayList;

    public AdapterReviews(Context context, ArrayList<ModelReviews> reviewsArrayList) {
        this.context = context;
        this.reviewsArrayList = reviewsArrayList;
    }

    @NonNull
    @Override
    public HolderReviews onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_reviews, parent, false);
        return new HolderReviews(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReviews holder, int position) {
        ModelReviews modelReviews = reviewsArrayList.get(position);
        String uid = modelReviews.getUid();
        String ratings = modelReviews.getRatings();
        String timestamp = modelReviews.getTimestamp();
        String reviews = modelReviews.getReview();

        loadUserDetails(modelReviews, holder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewEt.setText(reviews);
        holder.dateEt.setText(dateFormat);
    }

    private void loadUserDetails(ModelReviews modelReviews, HolderReviews holder) {
        String uid = modelReviews.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        holder.nameEt.setText(name);

                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileIv);
                        }
                        catch (Exception e){
                            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewsArrayList.size();
    }

    class HolderReviews extends RecyclerView.ViewHolder{

        private ImageView profileIv;
        private TextView nameEt,dateEt,reviewEt;
        private RatingBar ratingBar;

        public HolderReviews(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            nameEt = itemView.findViewById(R.id.nameEt);
            dateEt = itemView.findViewById(R.id.dateEt);
            reviewEt = itemView.findViewById(R.id.reviewEt);
            ratingBar = itemView.findViewById(R.id.ratingBar);

        }
    }
}
