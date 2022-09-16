package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tu.tuchati.Adapters.CommentAdapter;
import com.tu.tuchati.Models.CommentsModel;
import com.tu.tuchati.R;
import com.tu.tuchati.common.Common;
import com.tu.tuchati.databinding.ActivityPostDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {
    ActivityPostDetailsBinding binding;


    private DatabaseReference clickPostRef,LikesRef,UserRef,cRef,ref;
    private FirebaseAuth mAuth;
    private String PostKey,currentUserID,databasUserID,image;
    Boolean LikeChecker = false;
   ProgressDialog progressDialog;
    RecyclerView recyclerView;

    List<CommentsModel> commentList;
    CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //for comments
       // Intent postNotfication = getIntent();
       // String postId = postNotfication.getStringExtra("postId");
        PostKey = getIntent().getExtras().get("PostKey").toString();
       // comment_key = getIntent().getExtras().get("PostKey").toString();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
         cRef = FirebaseDatabase.getInstance().getReference("Posts").child(PostKey).child("Comments");
        LikesRef.keepSynced(true);

        recyclerView = findViewById(R.id.commentsRecyclerView);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);

        binding.likeBtn.setOnClickListener(v -> {
            LikeChecker = true;
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (LikeChecker.equals(true))
                    {
                        if(snapshot.child(PostKey).hasChild(currentUserID))
                        {
                            //check if like salready exist
                            LikesRef.child(PostKey).child(currentUserID).removeValue();
                            LikeChecker = false;
                        }
                        else
                        {
                            LikesRef.child(PostKey).child(currentUserID).setValue(true);
                            LikeChecker = false;
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.sendBtn.setOnClickListener(v -> {
            postComment();
            loadComments();

        });
        binding.numberOfLikes.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailsActivity.this, PostLikedByActivity.class);
            intent.putExtra("postId",PostKey);
            startActivity(intent);
        });
        binding.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.postImage.invalidate();
                Drawable dr = binding.postImage.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(PostDetailsActivity.this,binding.postImage,"image");
                Intent intent = new Intent(PostDetailsActivity.this, PostImageActivity.class);
                startActivity(intent,activityOptions.toBundle());
            }
        });

        setLikeButton();
        setComment();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        //get id of use who is online
        currentUserID = mAuth.getCurrentUser().getUid();

        //init firebase storage

        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        clickPostRef.keepSynced(true);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {

                    image = snapshot.child("postimage").getValue().toString();
                    String personimage = snapshot.child("userprofile").getValue().toString();
                    String personName= snapshot.child("username").getValue().toString();
                    String date= snapshot.child("posttime").getValue().toString();
                    String pdescription= snapshot.child("description").getValue().toString();

                    //convert timestamp to dd/mm/yyy
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(date));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


                    if (image.equals("noImage")){
                        binding.postImage.setVisibility(View.GONE);
                    }
                    else{
                        //show imageview
                        binding.postImage.setVisibility(View.VISIBLE);
                        try {
                            Glide.with(binding.postImage
                                    .getContext())
                                    .load(image)
                                    .into(binding.postImage);
                        }
                        catch(Exception e) {
                            binding.postImage.setImageResource(R.drawable.bigplaceholder);
                        }
                    }

                    try {
                        Glide.with(binding.profileImage
                                .getContext())
                                .load(personimage)
                                .into(binding.profileImage);
                    }
                    catch(Exception e) {
                        binding.profileImage.setImageResource(R.drawable.bigplaceholder);
                    }


                    databasUserID = snapshot.child("uid").getValue().toString();
                    binding.description.setText(pdescription);
                    binding.personName.setText(personName);
                    binding.date.setText(pTime);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init comments list
        commentList = new ArrayList<>();

        //path of the post, to get its comments
         ref = FirebaseDatabase.getInstance().getReference("Posts").child(PostKey).child("Comments");
         ref.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    CommentsModel commentsModel = ds.getValue(CommentsModel.class);

                    commentList.add(commentsModel);

                    //setup adapter
                    commentAdapter = new CommentAdapter(getApplicationContext(),commentList,currentUserID,PostKey);
                    //set adapter
                    recyclerView.setAdapter(commentAdapter);
                }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    @Override
    protected void onStart() {
        loadComments();
        super.onStart();
    }

    private void setComment() {
        cRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //count no of likes on a single post and set thumb to blue
                    int countComments = (int) snapshot.getChildrenCount();
                    binding.NoOfComments.setText((countComments +(" Comments")));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String userName = snapshot.child("username")
                            .getValue().toString();

                    String image = snapshot.child("profileImage").getValue().toString();

                    //must type thing before click send
                    validateComment(userName,image);

                    binding.writeCommentTextBox.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validateComment(String userName, String image) {
        String commentText = binding.writeCommentTextBox.getText().toString();

        if (commentText.isEmpty()) {
            binding.writeCommentTextBox.setError("Comment cannot be empty");
            return;
        }
        //store the data
        else{
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(PostKey).child("Comments");
            ref.keepSynced(true);
            String timeStamp = String.valueOf(System.currentTimeMillis());

            //current date
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            //current time
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);
            commentsMap.put("cid", timeStamp);
            commentsMap.put("profileimage",image );

            ref.child(timeStamp).setValue(commentsMap)
                   .addOnSuccessListener(aVoid -> {
                       Toast.makeText(PostDetailsActivity.this, "comment Added", Toast.LENGTH_SHORT).show();
                       binding.writeCommentTextBox.setText("");

                   }).addOnFailureListener(e -> {
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                   });

        }
    }
   // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(PostKey).child("Comments");

    private void setLikeButton() {
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(PostKey).hasChild(currentUserID)){
                    //count no of likes on a single post and set thumb to blue
                   int countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                    binding.likeBtn.setImageResource(R.drawable.thumb_up_blue);
                    binding.numberOfLikes.setText((countLikes +(" Likes")));
                }
                else {
                    //count no of likes on a single post and set thumb to blue
                    int countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                    binding.likeBtn.setImageResource(R.drawable.thumb_up_outline);
                    binding.numberOfLikes.setText((countLikes +(" Likes")));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}