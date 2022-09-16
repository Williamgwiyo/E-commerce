package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.PersonProfileActivity;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyHolder> {

    Context context;
    List<User> userList;


    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users,parent,false);

        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String username = userList.get(position).getUsername();
        String status = userList.get(position).getStatus();
        String image=userList.get(position).getProfileImage();
        String uid =userList.get(position).getUid();


        holder.userName.setText(username);
        holder.xstatus.setText(status);

        try{
            Picasso.get().load(image).into(holder.profileImage);
        }catch (Exception e){

        }
        holder.itemView.setOnClickListener(v -> {
            //get the key of the person

            //sent user to profile activity
            Intent profileIntent = new Intent(context, PersonProfileActivity.class);
            profileIntent.putExtra("visit", uid);
            context.startActivity(profileIntent);
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        public CircleImageView profileImage;
        public TextView userName,xstatus;
        public ImageView blocked;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.personprofile);
            xstatus = itemView.findViewById(R.id.statuss);
            userName = itemView.findViewById(R.id.personName);
            blocked = itemView.findViewById(R.id.blocked);
        }
    }

}
