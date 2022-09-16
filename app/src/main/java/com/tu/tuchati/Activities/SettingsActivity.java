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
import android.provider.Settings;
import android.widget.Toast;

import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivitySettingsBinding;
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
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements LocationListener {
    ActivitySettingsBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;
    String currentUserID;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;
    private LocationManager locationManager;
    private double latitude, longitude;
    ProgressDialog progressDialog;

    String storagePath = "profile_images_coverphoto/" + "" +currentUserID;

    //check if profile photo or cover photo
    String profileOrCoverPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userRef.keepSynced(true);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        binding.settingsUserImage.setOnClickListener(v -> showImagePickedDialog());
        binding.UpdateLocation.setOnClickListener(v -> {
            //detect current location
            if (checkLocationPermission()){
                //already allowed
                detectlocation();
            }
            else{
                //not allowed
                requestLocationPermission();
            }
        });
        binding.backBtn.setOnClickListener(v -> onBackPressed());

        binding.updateProfile.setOnClickListener(v -> {
            String name = binding.personName.getText().toString();
            String status = binding.profileStatus.getText().toString();
            String country = binding.personCountry.getText().toString();
            String city = binding.personCity.getText().toString();
            String gender = binding.perosnGender.getText().toString();
            String address = binding.personAddress.getText().toString();
            String relationship = binding.perosnRelationship.getText().toString();

            if (name.isEmpty())
            {
                binding.personName.setError("Please type a name");
                return;
            }
            if (status.isEmpty())
            {
                binding.profileStatus.setError("Please type a status");
                return;
            }
            if (country.isEmpty())
            {
                binding.personCountry.setError("Please type a country");
                return;
            }

            if (city.isEmpty())
            {
                binding.personCity.setError("Please type a city");
                return;
            }
            if (gender.isEmpty())
            {
                binding.perosnGender.setError("Please type a gender");
                return;
            }
            if (relationship.isEmpty())
            {
                binding.perosnRelationship.setError("Please type a gender");
                return;
            }



            else
                {
                    progressDialog.setTitle("Please wait....");
                    progressDialog.setMessage("Updating details");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    //setup profile with no image
                    if (image_uri==null){
                        //save info without pic
                        HashMap userMap = new HashMap();
                        userMap.put("username", name);
                        userMap.put("status", status);
                        userMap.put("gender", gender);
                        userMap.put("relationshipstatus", relationship);
                        userMap.put("country", country);
                        userMap.put("city", city);
                        userMap.put("address",address);
                        userMap.put("uid", auth.getUid());
                        userMap.put("latitude", latitude);
                        userMap.put("longitude", longitude);
                        userMap.put("profileImage", String.valueOf(R.drawable.profile_icon));
                        userRef.updateChildren(userMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this, "Updated successful", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                // saveImage();
                                Intent intent = new Intent(SettingsActivity.this, MyProfileActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();



                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });

                    }
                    //with image
                    else{

                        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ currentUserID;
                        //upload image
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                        storageReference.putFile(image_uri)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        //get url of uploaded image
                                        Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());
                                        Uri downloadImageUri = uriTask.getResult();
                                        if (uriTask.isSuccessful()){
                                            //save info with pic
                                            HashMap userMap = new HashMap();
                                            userMap.put("username", name);
                                            userMap.put("status", status);
                                            userMap.put("gender", gender);
                                            userMap.put("relationshipstatus", relationship);
                                            userMap.put("country", country);
                                            userMap.put("city", city);
                                            userMap.put("address", address);
                                            userMap.put("latitude", latitude);
                                            userMap.put("longitude", longitude);
                                            userMap.put("profileImage", ""+downloadImageUri);
                                            userRef.updateChildren(userMap).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful())
                                                {
                                                    Toast.makeText(SettingsActivity.this, "Updated successful", Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                    // saveImage();
                                                    Intent intent = new Intent(SettingsActivity.this, MyProfileActivity.class);
                                                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();



                                                }
                                                else{
                                                    String message = task1.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            });


                                        }
                                        else{
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
        });
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
    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
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
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        detectlocation();
                    } else {
                        Toast.makeText(this, "Location permission is needed", Toast.LENGTH_SHORT).show();

                    }
                }
            }
            break;

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
                binding.settingsUserImage.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                binding.settingsUserImage.setImageURI(image_uri);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void detectlocation() {
        Toast.makeText(this, "Please wait detecting your current location....", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //detect current location
        if (checkLocationPermission()){
            //already allowed
            detectlocation();
        }
        else{
            //not allowed
            requestLocationPermission();
        }

        //get infomation of user
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                   String name = snapshot.child("username").getValue().toString();
                    binding.personName.setText(name);

                    String image = snapshot.child("profileImage").getValue().toString();

                    String realtionship = snapshot.child("relationshipstatus").getValue().toString();
                    binding.perosnRelationship.setText(realtionship);

                    String status = snapshot.child("status").getValue().toString();
                    binding.profileStatus.setText(status);

                    String country = snapshot.child("country").getValue().toString();
                    binding.personCountry.setText(country);

                    String city = snapshot.child("city").getValue().toString();
                    binding.personCity.setText(city);

                    String address = snapshot.child("address").getValue().toString();
                    binding.personAddress.setText(address);

                    String gender = snapshot.child("gender").getValue().toString();
                    binding.perosnGender.setText(gender);

                    Picasso.get()
                            .load(image)
                            .into(binding.settingsUserImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude,1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String country= addresses.get(0).getCountryName();

            //set addresses
            binding.personCountry.setText(country);
            binding.personCity.setText(city);
            binding.personAddress.setText(address);

        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show();

        //when location service is not enabled
        //open location setting
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}