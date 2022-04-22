package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mygrocery.OtherClasses.Constants;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt;
    private SwitchCompat discountSwitch;
    private TextView categoryIv, quantityEt,priceEt,discountedPriceEt,discountNoteEt;
    private Button addProductBtn;

    //permission constant
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picker
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        discountSwitch = findViewById(R.id.discountSwitch);
        categoryIv = findViewById(R.id.categoryIv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountNoteEt = findViewById(R.id.discountNoteEt);;
        addProductBtn = findViewById(R.id.addProductBtn);

        discountedPriceEt.setVisibility(View.GONE);
        discountNoteEt.setVisibility(View.GONE);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        categoryIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);
                }
                else{
                    discountedPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                }
            }
        });
    }
    private String productTitle,productDescription,productCategory,originalPrice,productQuantity,discountPrice,discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryIv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Product Category required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "discount Price is required...", Toast.LENGTH_SHORT).show();
                return;
            }
    }
        else{
            discountPrice = "0";
            discountNote = "";
        }
        addProduct();
        }

    private void addProduct() {
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();
        String timestamp = ""+System.currentTimeMillis();
        if (image_uri == null){

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", ""+timestamp);
            hashMap.put("productTitle", ""+productTitle);
            hashMap.put("productDescription", ""+productDescription);
            hashMap.put("productCategory", ""+productCategory);
            hashMap.put("productQuantity", ""+productQuantity);
            hashMap.put("productIcon", "");
            hashMap.put("originalPrice", ""+originalPrice);
            hashMap.put("discountPrice", ""+discountPrice);
            hashMap.put("discountNote", ""+discountNote);
            hashMap.put("discountAvailable", ""+discountAvailable);
            hashMap.put("timestamp", ""+timestamp);
            hashMap.put("uid", ""+firebaseAuth.getUid());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(firebaseAuth.getUid()).child("products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product Added...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            String filePathAndName = "Product_images/"+""+timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId", ""+timestamp);
                                hashMap.put("productTitle", ""+productTitle);
                                hashMap.put("productDescription", ""+productDescription);
                                hashMap.put("productCategory", ""+productCategory);
                                hashMap.put("productQuantity", ""+productQuantity);
                                hashMap.put("productIcon", ""+downloadImageUri);
                                hashMap.put("originalPrice", ""+originalPrice);
                                hashMap.put("discountPrice", ""+discountPrice);
                                hashMap.put("discountNote", ""+discountNote);
                                hashMap.put("discountAvailable", ""+discountAvailable);
                                hashMap.put("timestamp", ""+timestamp);
                                hashMap.put("uid", ""+firebaseAuth.getUid());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                                reference.child(firebaseAuth.getUid()).child("products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product Added...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void categoryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String category = Constants.productCategories1[i];
                        categoryIv.setText(category);
                    }
                })
        .show();
    }

    private void clearData() {
        //clear data after uploading
        titleEt.setText("");
        descriptionEt.setText("");
        categoryIv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri = null;
    }
    private void showImagePickDialog()  {
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle clicks
                        if (i==0){
                            //camera clicked
                            if (checkCameraPermissions()){
                                //allowed
                                pickFromCamera();
                            }
                            else{
                                //not allowed
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermissions()){
                                //allowed
                                pickFromGallery();
                            }
                            else{
                                //not allowed
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)  ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)  ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults .length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
//permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera Permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
//permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage Permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                //profileIv.setImageURI(image_uri);
                Glide.with(this)
                        .load(image_uri)
                        .centerCrop()
                        .into(productIconIv);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to image view
                //profileIv.setImageURI(image_uri);
                Glide.with(this)
                        .load(image_uri)
                        .centerCrop()
                        .into(productIconIv);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}