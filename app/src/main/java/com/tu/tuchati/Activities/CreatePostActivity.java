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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tu.tuchati.databinding.ActivityCreatePostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {
    ActivityCreatePostBinding binding;
    ProgressDialog progressDialog;
    private StorageReference postReference;
    private DatabaseReference userRef, postref;
    private FirebaseAuth auth;
    String currentUserID,email,dp,name;
    String Description;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
     Uri image_uri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        //generate random number

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        FirebaseUser user = auth.getCurrentUser();
        email = user.getEmail();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        //get data using intent from the other activity
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");

        //check if we came here to update post
        if (isUpdateKey.equals("editPost")){
            binding.nameP.setText("Update Post");
            binding.postbtn2.setText("Update");
            binding.postBtn.setText("Update");
            loadPostData(editPostId);
            binding.postBtn.setOnClickListener(v -> {
                beginUpdate(editPostId);
            });
            binding.postbtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beginUpdate(editPostId);
                }
            });
        }
        else{
            binding.nameP.setText("Create Post");
            binding.postbtn2.setText("Post");
            binding.postBtn.setText("Post");

            binding.postBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Description = binding.postText.getText().toString();

                    if (TextUtils.isEmpty(Description)){
                        Toast.makeText(CreatePostActivity.this, "Enter Description", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (image_uri==null){
                        //post without image
                        progressDialog.setMessage("Posting....");
                        progressDialog.show();

                        String timestamp = String.valueOf(System.currentTimeMillis());


                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", currentUserID);
                        hashMap.put("username", name);
                        hashMap.put("email", email);
                        hashMap.put("userprofile", dp);
                        hashMap.put("pid", timestamp);
                        hashMap.put("description", Description);
                        hashMap.put("postimage", "noImage");
                        hashMap.put("posttime", timestamp);

                        postref.child(timestamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        binding.postImage.setImageURI(null);
                                        binding.postText.setText("");
                                        image_uri = null;
                                        Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        //post with image
                        progressDialog.setMessage("Posting....");
                        progressDialog.show();

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timestamp;

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putFile(Uri.parse(String.valueOf(image_uri))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()){
                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    hashMap.put("uid", currentUserID);
                                    hashMap.put("username", name);
                                    hashMap.put("email", email);
                                    hashMap.put("userprofile", dp);
                                    hashMap.put("pid", timestamp);
                                    hashMap.put("description", Description);
                                    hashMap.put("postimage", downloadUri);
                                    hashMap.put("posttime", timestamp);

                                    postref.child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                    binding.postImage.setImageURI(null);
                                                    binding.postText.setText("");
                                                    image_uri = null;
                                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                }
            });
            binding.postbtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Description = binding.postText.getText().toString();

                    if (TextUtils.isEmpty(Description)){
                        Toast.makeText(CreatePostActivity.this, "Enter Description", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (image_uri==null){
                        //post without image
                        progressDialog.setMessage("Posting....");
                        progressDialog.show();

                        String timestamp = String.valueOf(System.currentTimeMillis());


                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", currentUserID);
                        hashMap.put("username", name);
                        hashMap.put("email", email);
                        hashMap.put("userprofile", dp);
                        hashMap.put("pid", timestamp);
                        hashMap.put("description", Description);
                        hashMap.put("postimage", "noImage");
                        hashMap.put("posttime", timestamp);

                        postref.child(timestamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        binding.postImage.setImageURI(null);
                                        binding.postText.setText("");
                                        image_uri = null;
                                        Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        //post with image
                        progressDialog.setMessage("Posting....");
                        progressDialog.show();

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timestamp;

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putFile(Uri.parse(String.valueOf(image_uri))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()){
                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    hashMap.put("uid", currentUserID);
                                    hashMap.put("username", name);
                                    hashMap.put("email", email);
                                    hashMap.put("userprofile", dp);
                                    hashMap.put("pid", timestamp);
                                    hashMap.put("description", Description);
                                    hashMap.put("postimage", downloadUri);
                                    hashMap.put("posttime", timestamp);

                                    postref.child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                    binding.postImage.setImageURI(null);
                                                    binding.postText.setText("");
                                                    image_uri = null;
                                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                }
            });
        }

        Query query = userRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    name = ""+ds.child("username").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("profileImage").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        postref = FirebaseDatabase.getInstance().getReference().child("Posts");
        postref.keepSynced(true);
        //postReference = FirebaseStorage.getInstance().getReference().child("Post_images/");


        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.postImage.setOnClickListener(v -> showImagePickedDialog());
    }

    private void beginUpdate(String editPostId) {

        if (image_uri==null){
            //post without image
            if (editImage.equals("noImage")){
                //if no image in database then dont delete from the storage
                progressDialog.setMessage("Updating....");
                progressDialog.show();

                //  String timestamp = String.valueOf(System.currentTimeMillis());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("uid", currentUserID);
                hashMap.put("username", name);
                hashMap.put("email", email);
                hashMap.put("userprofile", dp);
                hashMap.put("description", Description);
                hashMap.put("postimage", "noImage");

                postref.child(editPostId).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                binding.postImage.setImageURI(null);
                                binding.postText.setText("");
                                image_uri = null;
                                Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                //if got image in database then delete from storage
                //first delete previous image from storage
                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
                mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.setMessage("Updating....");
                        progressDialog.show();

                        //  String timestamp = String.valueOf(System.currentTimeMillis());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", currentUserID);
                        hashMap.put("username", name);
                        hashMap.put("email", email);
                        hashMap.put("userprofile", dp);
                        hashMap.put("description", Description);
                        hashMap.put("postimage", "noImage");

                        postref.child(editPostId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreatePostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                        binding.postImage.setImageURI(null);
                                        binding.postText.setText("");
                                        image_uri = null;
                                        Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        else{
            //update with image
            if (editImage.equals("noImage")){
                //if no image in database then dont delete from storage
                //post with image
                progressDialog.setMessage("Updating....");
                progressDialog.show();

                String timestamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/" + "post_" + timestamp;

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putFile(Uri.parse(String.valueOf(image_uri))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", currentUserID);
                            hashMap.put("username", name);
                            hashMap.put("email", email);
                            hashMap.put("userprofile", dp);
                            hashMap.put("description", Description);
                            hashMap.put("postimage", downloadUri);

                            postref.child(editPostId).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                                            binding.postImage.setImageURI(null);
                                            binding.postText.setText("");
                                            image_uri = null;
                                            Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
            else{
                //delete from storage
                //delete the previous first from the storage
                StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
                mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted upload new image
                        //post with image
                        progressDialog.setMessage("Updating....");
                        progressDialog.show();

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timestamp;

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putFile(Uri.parse(String.valueOf(image_uri))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()){
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", currentUserID);
                                    hashMap.put("username", name);
                                    hashMap.put("email", email);
                                    hashMap.put("userprofile", dp);
                                    hashMap.put("description", Description);
                                    hashMap.put("postimage", downloadUri);

                                    postref.child(editPostId).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreatePostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                                                    binding.postImage.setImageURI(null);
                                                    binding.postText.setText("");
                                                    image_uri = null;
                                                    Intent intent = new Intent(CreatePostActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreatePostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }

        }
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        //get datails of post using id of post
        Query query = reference.orderByChild("pid").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    editDescription = ""+ds.child("description").getValue();
                    editImage = ""+ds.child("postimage").getValue();

                    //set the data to view
                    binding.postText.setText(editDescription);
                    if (!editImage.equals("noImage")){
                        try {
                            Picasso.get().load(editImage).into(binding.postImage);
                        }
                        catch (Exception e){

                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic){
        String NOTIFICATION_TOPIC ="/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = title; //william added new post
        String NOTIFICATION_MESSAGE =description; // content of post
        String NOTIFICATION_TYPE = notificationType;

        //prepare json what to send and wheer to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender",currentUserID);//uid of current user
            notificationBodyJo.put("pId",pId); //post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription",NOTIFICATION_MESSAGE);

            //where to send
            notificationBodyJo.put("to", NOTIFICATION_TOPIC);
            notificationBodyJo.put("data",notificationBodyJo);//combine data to be sent

        }
        catch (JSONException e){
            Toast.makeText(this, ""+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
       // sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        //send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                response -> {
                    Log.d("FCM_RESPONSE", "onResponse: "+response.toString());

                },
                error -> {
                    Toast.makeText(this, ""+error.toString(), Toast.LENGTH_SHORT).show();

                })
        {
            //put requirres headers

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "key=AAAAaVRXRMU:APA91bEAsaveA3vVocHM1wBKj5Pp66_HZdzk4etMjvGGCDVgco1KtD3WrxZY0SAq-ZnviLp9oNXmYAeYc-_nuJAJfvbaMckaJfyaCJpVO69IM85D0BPWCLZl3TqLNjdzpkf2CaKvWH2v"); //paste your fcm key here
                return params;
            }


        };
        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                image_uri = data.getData();
                //set to imageview
                binding.postImage.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                binding.postImage.setImageURI(image_uri);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}