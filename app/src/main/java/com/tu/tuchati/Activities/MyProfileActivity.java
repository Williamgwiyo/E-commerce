package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.Adapters.PostsAdapter;
import com.tu.tuchati.Models.PostsModel;
import com.tu.tuchati.ProfileImageActivity;
import com.tu.tuchati.R;
import com.tu.tuchati.common.Common;
import com.tu.tuchati.databinding.ActivityMyProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity {
 ActivityMyProfileBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference userRef,postRef,LikesRef;
    StorageReference storageReference;
    String currentUserID,editImage,description;
    ProgressDialog progressDialog;
   // Boolean LikeChecker = false;
    FirebaseUser firebaseUser;

    List<PostsModel> postsList;
    PostsAdapter adapter;


    String storagePath = "profile_images_coverphoto/" + "" +currentUserID;

    //check if profile photo or cover photo
    String profileOrCoverPhoto;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.Myorders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, MyOrderActivity.class);
                startActivity(intent);
            }
        });

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postRef.keepSynced(true);
        //for the posts
        binding.postsRecylerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.postsRecylerview.setLayoutManager(linearLayoutManager);

        loadMyPosts();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(MyProfileActivity.this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        currentUserID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userRef.keepSynced(true);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);

        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.edit.setOnClickListener(v -> {
            showEditProfileDialog();
        });
        binding.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(MyProfileActivity.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this account, your account will no longer be available after this action")
                        .setPositiveButton("DELETE", (dialog, which) -> {
                            //delete all posts
                            beginDelete();
                            deleteAccount(currentUserID); // id is the id of the product
                        }).setNegativeButton("NO", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
            }
        });
        binding.accountMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, ProfileMoreInfoActivity.class);
                startActivity(intent);
            }
        });
        binding.storeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             binding.storeProfileImage.invalidate();
                Drawable dr = binding.storeProfileImage.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(MyProfileActivity.this,binding.storeProfileImage,"image");
                Intent intent = new Intent(MyProfileActivity.this, ProfileImageActivity.class);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        binding.coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.coverPhoto.invalidate();
                Drawable dr = binding.coverPhoto.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(MyProfileActivity.this,binding.coverPhoto,"image");
                Intent intent = new Intent(MyProfileActivity.this, CoverPhotoActivity.class);
                startActivity(intent,activityOptions.toBundle());
            }
        });
    }
//FirebaseAuth.getInstance().signOut();
    private void deleteAccount(String currentUserID) {
        progressDialog.setTitle("Please wait.....");
        progressDialog.setMessage("deleting the account");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        loadUserInfo(currentUserID);
    }

    private void loadUserInfo(String currentUserID) {
        //load user info
        DatabaseReference userref = FirebaseDatabase.getInstance().getReference("Users");
        userref.keepSynced(true);
        //get datails of post using id of post
        Query query = userref.orderByChild("uid").equalTo(currentUserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String userimage = "" + ds.child("profileImage").getValue();
                    String coverphoto = "" +ds.child("coverphoto").getValue();

                    if (userimage.equals("noImage")){
                        deleteUserWithoutImage();
                    }if(!userimage.equals("noImage")){
                        deleteUserWithImage(userimage);
                    }
                    if (coverphoto.equals("noImage")){
                        deleteUserWithoutCover();
                    }
                    if (!coverphoto.equals("noImage")){
                        deleteUserWithCover(coverphoto);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteUserWithCover(String coverphoto) {
        StorageReference coverRef = FirebaseStorage.getInstance().getReferenceFromUrl(coverphoto);
        coverRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                //already deleted in storage
                //delete from firebase database

                deleteUserWithCoverInDatabase();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserWithCoverInDatabase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(currentUserID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    //delete all posts of current user
                                    Toast.makeText(MyProfileActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MyProfileActivity.this, MainScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(MyProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUserWithoutCover() {
        progressDialog.dismiss();
        //already deleted in storage
        //delete from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(currentUserID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    //delete all posts of current user
                                    Toast.makeText(MyProfileActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MyProfileActivity.this, MainScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(MyProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUserWithoutImage() {
        progressDialog.dismiss();
        //already deleted in storage
        //delete from firebase database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(currentUserID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    //delete all posts of current user
                                    Toast.makeText(MyProfileActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MyProfileActivity.this, MainScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(MyProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteUserWithImage(String userimage) {
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(userimage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                //already deleted in storage
                //delete from firebase database
                deleteUserWithImageInDatabase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserWithImageInDatabase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(currentUserID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    //delete all posts of current user
                                    Toast.makeText(MyProfileActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MyProfileActivity.this, MainScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }
                                else{
                                    progressDialog.dismiss();
                                    Toast.makeText(MyProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void beginDelete() {
        //load post data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        //get datails of post using id of post
        Query query = reference.orderByChild("uid").equalTo(currentUserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    description = ""+ds.child("description").getValue();
                    editImage = ""+ds.child("postimage").getValue();

                    if (editImage.equals("noImage")){
                        deleteWithoutImage();
                    }else{
                        deleteWithImage(editImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });        //

    }

    private void deleteWithImage(String editImage) {
        ProgressDialog progressDialog = new ProgressDialog(MyProfileActivity.this);
        progressDialog.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                //already deleted in storage
                //delete from firebase database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(currentUserID);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        //deleted
                        Toast.makeText(MyProfileActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        ProgressDialog progressDialog = new ProgressDialog(MyProfileActivity.this);
        progressDialog.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(currentUserID);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                //deleted
                Toast.makeText(MyProfileActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyPosts() {
        postsList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        binding.postsRecylerview.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.keepSynced(true);
        Query query = ref.orderByChild("uid").equalTo(currentUserID);

        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    PostsModel myposts = ds.getValue(PostsModel.class);

                    //add to list
                    postsList.add(myposts);
                    //adapter
                    adapter = new PostsAdapter(MyProfileActivity.this,  postsList);
                    binding.postsRecylerview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        loadMyPosts();
        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("username")) {
                        String name = snapshot.child("username").getValue().toString();
                        binding.username.setText(name);
                    }
                    if (snapshot.hasChild("profileImage")) {
                        String image = snapshot.child("profileImage").getValue().toString();
                        Picasso.get()
                                .load(image)
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person)
                                .into(binding.storeProfileImage);
                    }
                if (snapshot.hasChild("coverphoto")) {
                    String cover = snapshot.child("coverphoto").getValue().toString();
                    //for the cover photo
                    Picasso.get()
                            .load(cover)
                            .placeholder(R.drawable.bigplaceholder)
                            .error(R.drawable.bigplaceholder)
                            .into(binding.coverPhoto);
                }
                else{
                    Toast.makeText(MyProfileActivity.this, "username do not exist ", Toast.LENGTH_SHORT).show();
                }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        CheckUser();
    }
    @Override
    public void onStop() {
        super.onStop();

    }
    private void showEditProfileDialog() {
        //options to show on dialog
        String options[] = {"Edit profile picture","Edit Cover Photo","Edit Name",
        "Edit Status","Edit City", "Edit Country","Edit Relationship","Edit Gender"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);

        //set title
        builder.setItems(options, (dialog, which) -> {
        //handle dialog item clicks
            if (which==0){
                progressDialog.setMessage("Updating profile picture");
                profileOrCoverPhoto = "profileImage";
                showImagePicDialog();
            }
            else if (which == 1){
                progressDialog.setMessage("Updating Cover Photo");
                profileOrCoverPhoto = "coverphoto";
                showImagePicDialog();
            }
            else if (which == 2){
                progressDialog.setMessage("Updating Name");
                updateDetails("username");
            }
            else if (which == 3){
                progressDialog.setMessage("Updating Status");
                updateDetails("status");
            }
            else if (which == 4){
                progressDialog.setMessage("Updating city");
                updateDetails("city");
            }
            else if (which == 5){
                progressDialog.setMessage("Updating country");
                updateDetails("country");
            }
            else if (which == 6){
                progressDialog.setMessage("Updating relationship status");
                updateDetails("relationshipstatus");
            }
            else if (which == 7){
                progressDialog.setMessage("Updating Gender");
                updateDetails("gender");
            }
        });
        //create and show dialog
        builder.create().show();
    }
    private void updateDetails(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setTitle("Update "+ key);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(MyProfileActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        //add edit text
        EditText editText = new EditText(MyProfileActivity.this);
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog
        builder.setPositiveButton("Update", (dialog, which) -> {
            //input text
            String value = editText.getText().toString().trim();
            //validate edittext
            if (!TextUtils.isEmpty(value)){
                progressDialog.show();
                HashMap<String, Object> result = new HashMap<>();
                result.put(key, value);

                userRef.updateChildren(result)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
            else {
                Toast.makeText(this, "Please enter "+key, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

        });
        //create and show dialog
        builder.create().show();
    }
    public static class AllPostsViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView post_profile_pic;
        public TextView post_profile_name,Date,post_description,numberOfLikes;
        public ImageView post_image,comentBtn,likeBtn,editBtn;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;
        public AllPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            post_profile_pic = itemView.findViewById(R.id.post_profile_pic);
            post_profile_name = itemView.findViewById(R.id.post_profile_name);
            Date = itemView.findViewById(R.id.datePosted);
            post_description = itemView.findViewById(R.id.post_description);
            post_image = itemView.findViewById(R.id.post_image);
            comentBtn = itemView.findViewById(R.id.comentBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            editBtn = itemView.findViewById(R.id.deleteBtn);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            likesRef.keepSynced(true);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

    public void setLikeButtonStatus(String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(postKey).hasChild(currentUserId))
                    {
                        //count no of likes on a single post and set thumb to blue
                        countLikes = (int) snapshot.child(postKey).getChildrenCount();
                        likeBtn.setImageResource(R.drawable.thumb_up_blue);
                        numberOfLikes.setText((countLikes +(" Likes")));

                    }
                    else
                    {
                        //count no of likes on a single post
                        countLikes = (int) snapshot.child(postKey).getChildrenCount();
                        likeBtn.setImageResource(R.drawable.thumb_up_outline);
                        numberOfLikes.setText((countLikes +(" Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void showImagePicDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
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
                uploadProfileCoverPhoto(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void uploadProfileCoverPhoto(Uri image_uri) {

        //if no image...add image but dont delete from storage
        progressDialog.show();
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ currentUserID;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(image_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            Uri downloadImageUri = uriTask.getResult();

            //check if image is uploaded or not
            if (uriTask.isSuccessful()){
                HashMap<String, Object> results = new HashMap<>();
                results.put(profileOrCoverPhoto,downloadImageUri.toString());

                userRef.updateChildren(results)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(MyProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, "Error Updating image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                progressDialog.dismiss();
                Toast.makeText(MyProfileActivity.this, "Error: some error occured", Toast.LENGTH_SHORT).show();
            }


        }).addOnFailureListener(e -> {
            Toast.makeText(MyProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void CheckUser() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren()){
                            String accountType = ""+s.child("accountType").getValue();
                            if (accountType.equals("User"))
                            {
                                binding.myShop.setVisibility(View.GONE);

                            }
                            if(accountType.equals("Seller")){

                                binding.myShop.setVisibility(View.VISIBLE);
                                binding.myShop.setOnClickListener(v -> {
                                    Intent intent = new Intent(MyProfileActivity.this, MyShopActivity.class);
                                    startActivity(intent);
                                    Animatoo.animateSwipeLeft(MyProfileActivity.this);
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}