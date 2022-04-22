package com.example.mygrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.OtherClasses.FilterProductsUser;
import com.example.mygrocery.R;
import com.example.mygrocery.activities.ShopDetailsActivity;
import com.example.mygrocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList,filterList;
    private FilterProductsUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        ModelProduct modelProduct = productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        holder.titleEt.setText(productTitle);
        holder.discountNoteEt.setText(discountNote);
        holder.originalPriceEt.setText("$"+originalPrice);
        holder.discountedPriceEt.setText("$"+discountPrice);
        holder.descriptionEt.setText(productDescription);

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
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.addToCartIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(modelProduct);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleEt = view.findViewById(R.id.titleEt);
        TextView pQuantityEt = view.findViewById(R.id.pQuantityEt);
        TextView descriptionEt = view.findViewById(R.id.descriptionEt);
        TextView discountNoteEt = view.findViewById(R.id.discountNoteEt);
        TextView originalPriceEt = view.findViewById(R.id.originalPriceEt);
        TextView discountedPriceEt = view.findViewById(R.id.discountedPriceEt);
        TextView quantityEt = view.findViewById(R.id.quantityEt);
        TextView finalPriceEt = view.findViewById(R.id.finalPriceEt);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //set data
        String productId = modelProduct.getProductId();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity= modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        final String price;
        if (modelProduct.getDiscountAvailable().equals("true")){
            price = modelProduct.getDiscountPrice();
            discountNoteEt.setVisibility(View.VISIBLE);
            originalPriceEt.setPaintFlags(originalPriceEt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            discountNoteEt.setVisibility(View.GONE);
            discountedPriceEt.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }
        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost = Double.parseDouble(price.replaceAll("$", ""));
        quantity = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(productIv);
        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.ic_cart_gray);

        }
        titleEt.setText(""+productTitle);
        pQuantityEt.setText(""+productQuantity);
        descriptionEt.setText(""+description);
        discountNoteEt.setText(""+discountNote);
        quantityEt.setText(""+quantity);
        originalPriceEt.setText("$"+modelProduct.getOriginalPrice());
        discountedPriceEt.setText("$"+modelProduct.getDiscountPrice());
        finalPriceEt.setText("$"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost+cost;
                quantity++;

                finalPriceEt.setText("$"+finalCost);
                quantityEt.setText(""+quantity);
            }
        });
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity>1){
                    finalCost = finalCost-cost;
                    quantity--;

                    finalPriceEt.setText("$"+finalCost);
                    quantityEt.setText(""+quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString().trim();
                String quantity = quantityEt.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceEt.getText().toString().trim().replace("$", "");

                addToCart(productId,title,quantity,priceEach,totalPrice);
                dialog.dismiss();
            }
        });
    }

    private int itemId = 1;
    private void addToCart(String title, String quantity,String productId, String priceEach, String price) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context, "CART_ITEMS_DB")
        .setTableName("CART_ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "   Added To Cart", Toast.LENGTH_SHORT).show();
        //count
        ((ShopDetailsActivity)context).countCart();
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductsUser(this, filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {

        private ImageView productIconIv;
        private TextView discountNoteEt, titleEt,descriptionEt,addToCartIv,discountedPriceEt,originalPriceEt;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteEt = itemView.findViewById(R.id.discountNoteEt);
            titleEt = itemView.findViewById(R.id.titleEt);
            descriptionEt = itemView.findViewById(R.id.descriptionEt);
            addToCartIv = itemView.findViewById(R.id.addToCartIv);
            discountedPriceEt = itemView.findViewById(R.id.discountedPriceEt);
            originalPriceEt = itemView.findViewById(R.id.originalPriceEt);
        }
    }
}
