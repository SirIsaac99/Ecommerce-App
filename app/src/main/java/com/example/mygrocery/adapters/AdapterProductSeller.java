package com.example.mygrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.OtherClasses.FilterProducts;
import com.example.mygrocery.R;
import com.example.mygrocery.activities.EditProductActivity;
import com.example.mygrocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProducts filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        //get data
        ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String title = modelProduct.getProductTitle();
        String quantity = modelProduct.getProductQuantity();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        holder.titleEt.setText(title);
        holder.quantityEt.setText(quantity);
        holder.discountNoteEt.setText(discountNote);
        holder.discountedPriceEt.setText(discountPrice);
        holder.originalPriceEt.setText(originalPrice);
        if (discountAvailable.equals("true")){
            holder.discountedPriceEt.setVisibility(View.VISIBLE);
            holder.discountNoteEt.setVisibility(View.VISIBLE);
            holder.originalPriceEt.setPaintFlags(holder.originalPriceEt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            holder.discountedPriceEt.setVisibility(View.GONE);
            holder.discountNoteEt.setVisibility(View.GONE);
            holder.originalPriceEt.setPaintFlags(0);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailBottomSheet(modelProduct);
            }
        });
    }

    private void detailBottomSheet(ModelProduct modelProduct) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
        bottomSheetDialog.setContentView(view);

        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteEt = view.findViewById(R.id.discountNoteEt);
        TextView titleEt = view.findViewById(R.id.titleEt);
        TextView descriptionEt = view.findViewById(R.id.descriptionEt);
        TextView categoryIv = view.findViewById(R.id.categoryIv);
        TextView quantityEt = view.findViewById(R.id.quantityEt);
        TextView discountedPriceEt = view.findViewById(R.id.discountedPriceEt);
        TextView originalPriceEt = view.findViewById(R.id.originalPriceEt);

        //get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String productDescription = modelProduct.getProductDescription();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String icon = modelProduct.getProductIcon();
        String title = modelProduct.getProductTitle();
        String quantity = modelProduct.getProductQuantity();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        titleEt.setText(title);
        descriptionEt.setText(productDescription);
        categoryIv.setText(productCategory);
        quantityEt.setText(quantity);
        discountNoteEt.setText(discountNote);
        discountedPriceEt.setText("$"+discountPrice);
        originalPriceEt.setText("$"+originalPrice);
        if (discountAvailable.equals("true")){
            discountedPriceEt.setVisibility(View.VISIBLE);
            discountNoteEt.setVisibility(View.VISIBLE);
            originalPriceEt.setPaintFlags(originalPriceEt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            discountedPriceEt.setVisibility(View.GONE);
            discountNoteEt.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIv);
        }
        catch (Exception e){
            productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        //edit click
        bottomSheetDialog.show();
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete product"+title+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteProduct(id);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Product Deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter= new FilterProducts(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productIconIv;
        private TextView discountNoteEt,titleEt,quantityEt,originalPriceEt,discountedPriceEt;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteEt = itemView.findViewById(R.id.discountNoteEt);
            titleEt = itemView.findViewById(R.id.titleEt);
            quantityEt = itemView.findViewById(R.id.quantityEt);
            originalPriceEt = itemView.findViewById(R.id.originalPriceEt);
            discountedPriceEt = itemView.findViewById(R.id.discountedPriceEt);
        }
    }
}
