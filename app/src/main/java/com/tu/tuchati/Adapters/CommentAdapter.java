package com.tu.tuchati.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Models.CommentsModel;
import com.tu.tuchati.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder>{

    Context context;
    List<CommentsModel> commentList;
    String currentUserID,PostKey;

    public CommentAdapter(Context context, List<CommentsModel> commentList, String currentUserID, String postKey) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserID = currentUserID;
        PostKey = postKey;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_comments_item_layout,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String date = commentList.get(position).getDate();
        String comment = commentList.get(position).getComment();
        String username = commentList.get(position).getUsername();
        String time = commentList.get(position).getTime();
        String image = commentList.get(position).getProfileimage();
        String uid = commentList.get(position).getUid();
        String cid = commentList.get(position).getCid();

        //set data
        holder.comment.setText(comment);
        holder.username.setText(username);
        holder.date.setText(date);
        holder.time.setText(time);

        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_icon).into(holder.personImage);
        }
        catch (Exception e){

        }
        holder.itemView.setOnClickListener(v -> {
            //check if this comment is by currently signed in user
            if (currentUserID.equals(uid)){
                //my comment
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this comment?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete comment
                        deleteComment(cid);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismis dialog
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            else{
                Toast.makeText(context, "Cant delete other people comment", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteComment(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(PostKey);
        ref.child("Comments").child(cid).removeValue();

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        public CircleImageView personImage;
        public TextView username,date,comment,time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            personImage = itemView.findViewById(R.id.personImage);
            date = itemView.findViewById(R.id.date);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
        }
    }
}
