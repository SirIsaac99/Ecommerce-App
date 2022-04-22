package com.example.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegistersellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, shopnameEt, phoneEt, emailEt, countryEt, stateEt,
            passwordEt, cPasswordEt, addressEt, cityEt, deliveryFeeEt;
    private Button registerBtn;

    //permission constant
    public static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    public static final int IMAGE_PICK_GALLERY_CODE = 400;
    public static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picker
    private Uri image_uri;

    private LocationManager locationManager;
    private double latitude, longitude;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerseller);

        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        shopnameEt = findViewById(R.id.shopnameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cpasswordEt);
        addressEt = findViewById(R.id.addressEt);
        registerBtn = findViewById(R.id.registerBtn);
        deliveryFeeEt = findViewById(R.id.deliveryFeeEt);

        //init permissions
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //user location
                if (checkLocationPermission()){
                    detectLocation();
                }
                else{
                    requestLocationPermission();
                }
            }
        });
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image picker
                ShowImagePickDialog();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //registers user
                inputData();
            }
        });
    }
private String fullName, shopName, phoneNumber, deliveryFee, country, city, state,password,confirmPassword,address,
    email;
    private void inputData() {
        fullName = nameEt.getText().toString().trim();
        shopName = shopnameEt.getText().toString().trim();
        phoneNumber = phoneEt.getText().toString().trim();
        deliveryFee = deliveryFeeEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confirmPassword = cPasswordEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Please Fill Empty Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shopName)){
            Toast.makeText(this, "Please Fill Empty Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please Fill Empty Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(deliveryFee)){
            Toast.makeText(this, "Please Fill Empty Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0.0 || longitude == 0.0){
            Toast.makeText(this, "Please turn GPS On To Detect location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Fill Password Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Password does not match...d", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saverFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(RegistersellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");
        String timestamp = ""+System.currentTimeMillis();
        if (image_uri == null){
            //save without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email", ""+email);
            hashMap.put("name", ""+fullName);
            hashMap.put("shopName", ""+shopName);
            hashMap.put("phone", ""+phoneNumber);
            hashMap.put("deliveryFee", ""+deliveryFee);
            hashMap.put("country", ""+country);
            hashMap.put("state", ""+state);
            hashMap.put("city", ""+city);
            hashMap.put("address", ""+address);
            hashMap.put("latitude", ""+latitude);
            hashMap.put("longitude", ""+longitude);
            hashMap.put("timestamp", ""+timestamp);
            hashMap.put("accountType", "seller");
            hashMap.put("online", "true");
            hashMap.put("shopOpen", "true");
            hashMap.put("profileImage", "");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegistersellerActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegistersellerActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    });
        }
        else {
            //save with image

            //name & path of image
            String filePathAndName = "profileImage/"+firebaseAuth.getUid();
            //image upload
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                //save without image
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email", ""+email);
                                hashMap.put("name", ""+fullName);
                                hashMap.put("shopName", ""+shopName);
                                hashMap.put("phone", ""+phoneNumber);
                                hashMap.put("deliveryFee", ""+deliveryFee);
                                hashMap.put("country", ""+country);
                                hashMap.put("state", ""+state);
                                hashMap.put("city", ""+city);
                                hashMap.put("address", ""+address);
                                hashMap.put("latitude", ""+latitude);
                                hashMap.put("longitude", ""+longitude);
                                hashMap.put("timestamp", ""+timestamp);
                                hashMap.put("accountType", "seller");
                                hashMap.put("online", "true");
                                hashMap.put("shopOpen", "true");
                                hashMap.put("profileImage", ""+downloadImageUri);

                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegistersellerActivity.this, MainSellerActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegistersellerActivity.this, MainSellerActivity.class));
                                                finish();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegistersellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void ShowImagePickDialog() {
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

    private void pickFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, IMAGE_PICK_GALLERY_CODE);
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

    public void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

        locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location detected
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        findAddress();
    }
    private void findAddress(){
        //find address country code, state,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(latitude, longitude, 1);
            String address=addresses.get(0).getAddressLine(0); //complte address
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();

            //set addresses
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)  ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)  ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)  ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
//permission allowed
                        detectLocation();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Permissions necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
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
            break;
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
            break;
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
                        .into(profileIv);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to image view
                //profileIv.setImageURI(image_uri);
                Glide.with(this)
                        .load(image_uri)
                        .centerCrop()
                        .into(profileIv);
               }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}