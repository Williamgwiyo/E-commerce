package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityEditProductBinding;
import com.tu.tuchati.databinding.ActivityShopAddProductBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {
ActivityEditProductBinding binding;
private String productId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    FirebaseAuth firebaseAuth;

    //progressdialog
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get the id of the product from intent
        productId = getIntent().getStringExtra("productId");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);

        firebaseAuth= FirebaseAuth.getInstance();
        binding.backbtn.setOnClickListener(v -> onBackPressed());
        binding.productImage1.setOnClickListener(v -> {
            showImagePickedDialog();
        });
        binding.productCategory.setOnClickListener(v -> {
            categoryDialog();
        });
        binding.addProductBtn.setOnClickListener(v -> {
            inputData();
        });
        loadProductDetails();

        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.onDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                binding.discountPrice.setVisibility(View.VISIBLE);
            }
            else{
                binding.discountPrice.setVisibility(View.GONE);
            }
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productId = ""+snapshot.child("productId").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String moreThanOneOrder = ""+snapshot.child("moreThanOneOrder").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productCondition = ""+snapshot.child("productCondition").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productPrice = ""+snapshot.child("productPrice").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String productImage = ""+snapshot.child("productImage").getValue();

                        //set data to views
                        if (discountAvailable.equals("true")){
                            binding.onDiscount.setChecked(true);
                            binding.discountPrice.setVisibility(View.VISIBLE);

                        }
                        else{
                            binding.onDiscount.setChecked(false);
                            binding.discountPrice.setVisibility(View.GONE);
                        }
                        if (moreThanOneOrder.equals("true")){
                            binding.onStock.setChecked(true);
                           // binding.discountPrice.setVisibility(View.VISIBLE);

                        }
                        else{
                            binding.onStock.setChecked(false);
                           // binding.discountPrice.setVisibility(View.GONE);
                        }
                        try {
                            Picasso.get().load(productImage).placeholder(R.drawable.add_image).into(binding.productImage1);
                        }
                        catch (Exception e){
                            binding.productImage1.setImageResource(R.drawable.add_image);
                        }

                        binding.discountPrice.setText(discountPrice);
                        binding.productCategory.setText(productCategory);
                        binding.productCondition.setText(productCondition);
                        binding.productTitle.setText(productTitle);
                        binding.productDescription.setText(productDescription);
                        binding.productPrice.setText(productPrice);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle,productDescription,productCategory,productCondition,productPrice,discountPrice;
    private boolean discountAvailable = false, moreThanOneOrder = false;
    private void inputData() {
        //input data
        productTitle = binding.productTitle.getText().toString().trim();
        productDescription = binding.productDescription.getText().toString().trim();
        productCategory = binding.productCategory.getText().toString().trim();
        productCondition = binding.productCondition.getText().toString().trim();
        productPrice = binding.productPrice.getText().toString().trim();
        discountAvailable = binding.onDiscount.isChecked();
        moreThanOneOrder = binding.onStock.isChecked();

        //validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "description is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCondition)){
            Toast.makeText(this, "Condition is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productPrice)){
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            discountPrice = binding.discountPrice.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else{
            discountPrice ="0";
        }

        updateProduct();

    }

    private void updateProduct() {
        //add product to database
        progressDialog.setMessage("Updating Product");
        progressDialog.show();

        if (image_uri==null){
         Toast.makeText(this, "Product image is required", Toast.LENGTH_SHORT).show();
         progressDialog.dismiss();
         return;
        }
        else{
            String timestamp =""+System.currentTimeMillis();

            String filePathAndName = "product_images/" + "" +timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        //image uploaded successful
                        //get url of uploaded image

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadImageUri = uriTask.getResult();

                        if (uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("productTitle", ""+productTitle);
                            hashMap.put("productDescription", ""+productDescription);
                            hashMap.put("productCategory", ""+productCategory);
                            hashMap.put("productCondition", ""+productCondition);
                            hashMap.put("productPrice", ""+productPrice);
                            hashMap.put("discountPrice", ""+discountPrice);
                            hashMap.put("moreThanOneOrder", ""+moreThanOneOrder);
                            hashMap.put("discountAvailable", ""+discountAvailable);
                            hashMap.put("productImage", ""+downloadImageUri);
                            hashMap.put("uid", ""+firebaseAuth.getUid());

                            //add to db
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.keepSynced(true);
                            reference.child(firebaseAuth.getUid()).child("Products").child(productId).updateChildren(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProductActivity.this, "Product updated..", Toast.LENGTH_SHORT).show();
                                        // clearData();
                                        Intent intent = new Intent(EditProductActivity.this, MyShopActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }

    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = Constants.productCategories[which];
                        binding.productCategory.setText(category);
                    }
                }).show();
    }
    private void showImagePickedDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                pickedFromGallery();
                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }
    private void pickedFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickedFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is permission is needed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is needed", Toast.LENGTH_SHORT).show();
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
                //get picked image
                image_uri = data.getData();
                //set to imageview
                binding.productImage1.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                binding.productImage1.setImageURI(image_uri);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}