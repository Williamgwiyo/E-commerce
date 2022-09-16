package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tu.tuchati.Adapters.PostsAdapter;
import com.tu.tuchati.Models.PostsModel;
import com.tu.tuchati.ProfileImageActivity;
import com.tu.tuchati.R;
import com.tu.tuchati.common.Common;
import com.tu.tuchati.databinding.ActivityNearbyProfileBinding;
import com.tu.tuchati.databinding.ActivityPersonProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NearbyProfileActivity extends AppCompatActivity {
ActivityNearbyProfileBinding binding;
    private DatabaseReference FriendRequestRef,usersRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserID,receiverUserID, CURRENT_STATE,saveCurrentDate;
    //RequestQueue requestQueue;

    List<PostsModel> postsList;
    PostsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityNearbyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

      //  requestQueue = Volley.newRequestQueue(this);
        CURRENT_STATE = "not_friends";
        mAuth = FirebaseAuth.getInstance();
        //current user
        senderUserID = mAuth.getCurrentUser().getUid();

        receiverUserID = getIntent().getExtras().get("visit").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserID);
        usersRef.keepSynced(true);
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendRequestRef.keepSynced(true);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);
        //specific user on which it is cliked

        //for the posts
        binding.postsRecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.postsRecyclerview.setLayoutManager(linearLayoutManager);

        binding.storeProfileImage.setOnClickListener(v -> {
            binding.storeProfileImage.invalidate();
            Drawable dr = binding.storeProfileImage.getDrawable();
            Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(NearbyProfileActivity.this,binding.storeProfileImage,"image");
            Intent intent = new Intent(NearbyProfileActivity.this, ProfileImageActivity.class);
            startActivity(intent,activityOptions.toBundle());
        });

        binding.coverPhoto.setOnClickListener(v -> {
            binding.coverPhoto.invalidate();
            Drawable dr = binding.coverPhoto.getDrawable();
            Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(NearbyProfileActivity.this,binding.coverPhoto,"image");
            Intent intent = new Intent(NearbyProfileActivity.this, CoverPhotoActivity.class);
            startActivity(intent,activityOptions.toBundle());
        });

        binding.accountMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NearbyProfileActivity.this, ProfileMoreInfoActivity.class);
                startActivity(intent);
            }
        });

        loadPosts();

        binding.backBtn.setOnClickListener(v -> onBackPressed());

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String name = snapshot.child("username").getValue().toString();
                    binding.pUsername.setText(name);

                    String image = snapshot.child("profileImage").getValue().toString();

                    String cover = snapshot.child("coverphoto").getValue().toString();

                    //for the profile photo
                    Picasso.get()
                            .load(image)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(binding.storeProfileImage);

                    //for the cover photo
                    Picasso.get()
                            .load(cover)
                            .placeholder(R.drawable.bigplaceholder)
                            .error(R.drawable.bigplaceholder)
                            .into(binding.coverPhoto);

                    MaintainanceOfBtn();

                    //send message to the profile user
                    binding.sendMessage.setOnClickListener(v -> {
                        Intent chatintent =  new Intent(NearbyProfileActivity.this, privateChatActivity.class);
                        chatintent.putExtra("hisUid",receiverUserID);
                        chatintent.putExtra("username",name);
                        startActivity(chatintent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!senderUserID.equals(receiverUserID)){
            binding.addFriendBtn.setOnClickListener(v -> {
                binding.addFriendBtn.setEnabled(false);

                //not friends then send friend request
                if (CURRENT_STATE.equals("not_friends"))
                {
                    SendFriendRequest();
                }
                if (CURRENT_STATE.equals("request_sent"))
                {
                    CancelFriendRequest();
                }
                if (CURRENT_STATE.equals("request_received"))
                {
                    AcceptFriendrequest();
                }
                if (CURRENT_STATE.equals("friends"))
                {
                    UnFriendAnExistingFriend();
                }

            });
        }
        else{

            binding.addFriendBtn.setVisibility(View.INVISIBLE);

        }
    }
    private void loadPosts() {
        postsList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(NearbyProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        binding.postsRecyclerview.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.keepSynced(true);
        Query query = ref.orderByChild("uid").equalTo(receiverUserID);

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
                    adapter = new PostsAdapter(NearbyProfileActivity.this,  postsList);
                    binding.postsRecyclerview.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NearbyProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPosts();
    }
    private void UnFriendAnExistingFriend() {
        FriendsRef.child(senderUserID).child(receiverUserID)
                //for the reciever
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        //for the sender
                        FriendsRef.child(receiverUserID).child(senderUserID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful())
                                    {
                                        binding.addFriendBtn.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        binding.addFriendBtn.setText("Add Friend");
                                        binding.sendMessage.setVisibility(View.INVISIBLE);
                                    }

                                });

                    }
                });
    }

    private void AcceptFriendrequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(aVoid -> {
                    FriendsRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                            .addOnSuccessListener(bVoid -> {

                                FriendRequestRef.child(senderUserID).child(receiverUserID)
                                        //for the reciever
                                        .removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful())
                                            {
                                                //for the sender
                                                FriendRequestRef.child(receiverUserID).child(senderUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task.isSuccessful())
                                                            {
                                                                binding.addFriendBtn.setEnabled(true);
                                                                CURRENT_STATE = "friends";
                                                                binding.addFriendBtn.setText("Unfriend");
                                                                binding.declineRequest.setVisibility(View.INVISIBLE);
                                                                binding.declineRequest.setEnabled(false);
                                                                binding.sendMessage.setVisibility(View.VISIBLE);



                                                            }

                                                        });

                                            }
                                        });

                            })
                            .addOnFailureListener(e -> Toast.makeText(NearbyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(NearbyProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void CancelFriendRequest() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                //for the reciever
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        //for the sender
                        FriendRequestRef.child(receiverUserID).child(senderUserID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful())
                                    {
                                        binding.addFriendBtn.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        binding.addFriendBtn.setText("Add Friend");
                                    }

                                });

                    }
                });
    }

    private void MaintainanceOfBtn() {
        FriendRequestRef.child(senderUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)){
                            String request_type = snapshot.child(receiverUserID).child("request_type")
                                    .getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                binding.addFriendBtn.setText("Cancel Request");
                                binding.declineRequest.setVisibility(View.INVISIBLE);
                                binding.sendMessage.setVisibility(View.INVISIBLE);
                            }
                            else if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                binding.addFriendBtn.setText("Accept Request");
                                binding.declineRequest.setVisibility(View.VISIBLE);
                                binding.sendMessage.setVisibility(View.INVISIBLE);
                                // binding.declineRequest.setEnabled(true);
                                binding.declineRequest.setOnClickListener(v -> {
                                    CancelFriendRequest();
                                    binding.declineRequest.setVisibility(View.INVISIBLE);
                                });
                            }
                        }
                        else{
                            FriendsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserID))
                                            {
                                                CURRENT_STATE = "friends";
                                                binding.addFriendBtn.setText("Unfriend");

                                                binding.declineRequest.setVisibility(View.INVISIBLE);
                                                binding.sendMessage.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendFriendRequest() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                //for the reciever
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        //for the sender
                        FriendRequestRef.child(receiverUserID).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task.isSuccessful())
                                    {
                                        binding.addFriendBtn.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        binding.addFriendBtn.setText("Cancel Request");

                                    }

                                });

                    }
                });
    }


}