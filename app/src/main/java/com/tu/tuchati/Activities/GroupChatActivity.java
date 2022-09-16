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

import com.tu.tuchati.Adapters.AdapterGroupChat;
import com.tu.tuchati.Models.ModelGroupChat;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
ActivityGroupChatBinding binding;
private String groupId, myGroupRole;
private FirebaseAuth firebaseAuth;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    ProgressDialog progressDialog;

    //image picked uri
    private Uri image_uri;

private ArrayList<ModelGroupChat> groupChatList;
private AdapterGroupChat adapterGroups;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        progressDialog = new ProgressDialog(this);
        //get id of the group
        Intent intent = getIntent();
        groupId=intent.getStringExtra("groupId");
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.sendBtn.setOnClickListener(v -> {
            String message = binding.messageBox.getText().toString().trim();
            //validate
            if (TextUtils.isEmpty(message)){
                //if empty then do not send
                Toast.makeText(this, "can't send empty message....", Toast.LENGTH_SHORT).show();
            }
            else {
                sendMessage(message);
            }
        });
        binding.attach.setOnClickListener(v -> {
            showImagePickedDialog();
        });
        binding.groupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this,GroupInfoActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });
        binding.addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addgroupparticipant = new Intent(GroupChatActivity.this, AddGroupParticipantActivity.class);
                startActivity(addgroupparticipant);
            }
        });

    }

    private void loadMyGroupRole() {
     DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
     ref.keepSynced(true);
     ref.child(groupId).child("Participants")
             .orderByChild("uid").equalTo(firebaseAuth.getUid())
             .addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                     for (DataSnapshot ds: snapshot.getChildren()){
                          myGroupRole = ""+ds.child("role").getValue();
                          if (myGroupRole.equals("Admin") || myGroupRole.equals("adminAssistant")){
                              binding.addParticipant.setVisibility(View.VISIBLE);

                              //handle button click
                              binding.addParticipant.setOnClickListener(v -> {
                                  Intent intent = new Intent(GroupChatActivity.this,AddGroupParticipantActivity.class);
                                  intent.putExtra("groupId",groupId);
                                  startActivity(intent);
                              });

                          }
                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });
    }

    private void loadGroupMessages() {
        //init list
        groupChatList= new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       groupChatList.clear();
                       for (DataSnapshot ds:snapshot.getChildren()){
                           ModelGroupChat modelGroupChat = ds.getValue(ModelGroupChat.class);
                           groupChatList.add(modelGroupChat);

                       }
                       //adapter
                        adapterGroups = new AdapterGroupChat(GroupChatActivity.this,groupChatList);
                       //set to recyclerview
                        binding.groupChatRecycler.setAdapter(adapterGroups);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message) {
        //timestamp
        String timestamp = ""+System.currentTimeMillis();

        //setup message data
        HashMap<String, Object>hashMap = new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type",""+"text");

        //add data to the database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //message is sent
                    binding.messageBox.setText("");
                    Toast.makeText(GroupChatActivity.this, "sent", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String groupTitle=""+ds.child("groupTitle").getValue();
                            String groupDescription=""+ds.child("groupDescription").getValue();
                            String groupIcon=""+ds.child("groupIcon").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String createdBy=""+ds.child("createdBy").getValue();

                            binding.nameP.setText(groupTitle);

                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.profile_icon).into(binding.imageView2);
                            }
                            catch (Exception e){
                                binding.imageView2.setImageResource(R.drawable.profile_icon);
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
                sendImageMessage();

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                sendImageMessage();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage() {
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending Image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String filenamePath = "ChatImages/" + ""+System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        //upload image
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //upload image and get the image url
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();

                        //save image into the database
                        //timestamp
                        String timestamp = ""+System.currentTimeMillis();

                        //setup message data
                        HashMap<String, Object>hashMap = new HashMap<>();
                        hashMap.put("sender",""+firebaseAuth.getUid());
                        hashMap.put("message",""+p_downloadUri);
                        hashMap.put("timestamp",""+timestamp);
                        hashMap.put("type",""+"image");

                        //add data to the database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).child("Messages").child(timestamp)
                                .setValue(hashMap)
                                .addOnSuccessListener(aVoid -> {
                                    //message is sent
                                    binding.messageBox.setText("");
                                    Toast.makeText(GroupChatActivity.this, "sent", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }).addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

                        if (p_uriTask.isSuccessful()){

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

}