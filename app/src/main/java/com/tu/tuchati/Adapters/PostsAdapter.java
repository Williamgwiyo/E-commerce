package com.tu.tuchati.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tu.tuchati.Activities.CreatePostActivity;
import com.tu.tuchati.Activities.PersonProfileActivity;
import com.tu.tuchati.Activities.PostDetailsActivity;
import com.tu.tuchati.Activities.PostImageActivity;
import com.tu.tuchati.Activities.PostLikedByActivity;
import com.tu.tuchati.Models.PostsModel;
import com.tu.tuchati.R;
import com.tu.tuchati.common.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {

    private Context context;
    public List<PostsModel> postsList;
    Boolean LikeChecker = false;
    DatabaseReference LikesRef;
    String currentUserID;
    FirebaseAuth auth;

    public PostsAdapter(Context context, List<PostsModel> postsList) {
        this.context = context;
        this.postsList = postsList;
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_posts,parent,false);

        return new PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
        PostsModel postsModel = postsList.get(position);
        String description = postsModel.getDescription();
        String date = postsModel.getPosttime();
        String profileimage = postsModel.getUserprofile();
        String uid = postsModel.getUid();
        String pid = postsModel.getPid();
        String postimage = postsModel.getPostimage();
        String username = postsModel.getUsername();

        //convert timestamp to dd/mm/yyy
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(date));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //display the no of likes
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(pid).hasChild(currentUserID))
                {
                    //count no of likes on a single post and set thumb to blue
                    holder.countLikes = (int) snapshot.child(pid).getChildrenCount();
                    // likeBtn.setImageResource(R.drawable.thumb_up_blue);

                    holder.numberOfLikes.setText((holder.countLikes +(" Likes")));

                }
                else
                {
                    //count no of likes on a single post
                    holder.countLikes = (int) snapshot.child(pid).getChildrenCount();
                    // likeBtn.setDrawa(R.drawable.thumb_up_outline);
                    holder.numberOfLikes.setText((holder.countLikes +(" Likes")));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //  holder.Time.setText(model.getTime());

        holder.post_description.setText(description);
        holder.Date.setText(pTime);
        holder.post_profile_name.setText(username);

        if (postimage.equals("noImage")){
            holder.post_image.setVisibility(View.GONE);
        }
        else{
            //show imageview
            holder.post_image.setVisibility(View.VISIBLE);
            try {
                Glide.with(holder.post_image
                        .getContext())
                        .load(postimage)
                        .into(holder.post_image);
            }
            catch(Exception e) {
                holder.post_image.setImageResource(R.drawable.bigplaceholder);
            }
        }
        try {
            Glide.with(holder.post_profile_pic
                    .getContext())
                    .load(profileimage)
                    .into(holder.post_profile_pic);
        }
        catch (Exception e){
            holder.post_profile_pic.setImageResource(R.drawable.profile_icon);
        }
        holder.likeBtn.setOnClickListener(v -> {
            LikeChecker = true;
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (LikeChecker.equals(true))
                    {
                        if(snapshot.child(pid).hasChild(currentUserID))
                        {
                            //check if like salready exist
                            LikesRef.child(pid).child(currentUserID).removeValue();
                            LikeChecker = false;
                        }
                        else
                        {
                            LikesRef.child(pid).child(currentUserID).setValue(true);
                            LikeChecker = false;

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        //get uid of the owner of the post
        DatabaseReference postref = FirebaseDatabase.getInstance().getReference().child("Posts");
        postref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String uid = snapshot.child("uid").getValue().toString();

                    holder.post_profile_pic.setOnClickListener(v -> {
                        //sent user to profile activity
                        Intent profileIntent = new Intent(context, PersonProfileActivity.class);
                        profileIntent.putExtra("visit",uid);
                        context.startActivity(profileIntent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.comentBtn.setOnClickListener(v -> {
            Intent commentIntent = new Intent(context, PostDetailsActivity.class);
            commentIntent.putExtra("PostKey", pid);
            context.startActivity(commentIntent);
        });
        holder.post_image.setOnClickListener(v -> {
            holder.post_image.invalidate();
            Drawable dr = holder.post_image.getDrawable();
            Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context,holder.post_image,"image");
            Intent intent = new Intent(context, PostImageActivity.class);
            context.startActivity(intent,activityOptions.toBundle());
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showMoreOptions(holder.morebtn,uid,currentUserID,pid,postimage);
            }
        });
        holder.post_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(context,PersonProfileActivity.class);
                profileIntent.putExtra("visit",uid);
                context.startActivity(profileIntent);
            }
        });
        holder.numberOfLikes.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostLikedByActivity.class);
            intent.putExtra("postId",pid);
            context.startActivity(intent);
        });
    }

    private void showMoreOptions(ImageButton morebtn, String uid, String currentUserID, String pid, String postimage) {
        PopupMenu popupMenu = new PopupMenu(context, morebtn, Gravity.END);

        //show delete if is the user owner of the post
        if (uid.equals(currentUserID)){
            //additem in menu
            morebtn.setVisibility(View.VISIBLE);
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");

        }
        else{
            morebtn.setVisibility(View.GONE);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete(pid,postimage);
                }
                else if (id==1){
                    Intent intent = new Intent(context, CreatePostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pid);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pid, String postimage) {
        if (postimage.equals("noImage")){
            deleteWithoutImage(pid);
        }else{
            deleteWithImage(pid,postimage);
        }
    }

    private void deleteWithImage(String pid, String postimage) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postimage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                //already deleted in storage
                //delete from firebase database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        //deleted
                        Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pid) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                //deleted
                Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postsList.size();
    }

    class PostsViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView post_profile_pic;
        public TextView post_profile_name,Date,post_description,numberOfLikes;
        public Button comentBtn,likeBtn,editBtn,shareBtn;
        public ImageView post_image;
        public ImageButton morebtn;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;

        public PostsViewHolder(@NonNull View itemView) {
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
            shareBtn = itemView.findViewById(R.id.shareBtn);
            morebtn = itemView.findViewById(R.id.moreBtn);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            likesRef.keepSynced(true);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


    }
}
