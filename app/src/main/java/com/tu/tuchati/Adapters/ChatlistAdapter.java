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

import com.tu.tuchati.Activities.privateChatActivity;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder> {
   Context context;
   List<User> userList;
   private HashMap<String, String> lastMessageMap;

    public ChatlistAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getProfileImage();
        String userName = userList.get(position).getUsername();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.name.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            holder.message.setVisibility(View.GONE);
        }
        else{
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(lastMessage);


        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.profile_icon).into(holder.profileImage);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.profile_icon).into(holder.profileImage);
        }

        //set online status of other users
        if (userList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatus.setImageResource(R.drawable.circle_online);
        }
         else {
             //offline
            holder.onlineStatus.setImageResource(R.drawable.circle_offline);
        }
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //start chat activity with that user
                 Intent intent = new Intent(context, privateChatActivity.class);
                 intent.putExtra("hisUid",hisUid);
                 context.startActivity(intent);

             }
         });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        ImageView onlineStatus;
        TextView name,message,new1;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.cname);
            message = itemView.findViewById(R.id.cmessage);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);


        }
    }
}
