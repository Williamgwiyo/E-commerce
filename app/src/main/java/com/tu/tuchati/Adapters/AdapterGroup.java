package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.GroupChatActivity;
import com.tu.tuchati.Models.ModelGroups;
import com.tu.tuchati.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroup extends RecyclerView.Adapter<AdapterGroup.HolderGroupChatList> {
    private Context context;
    private ArrayList<ModelGroups> groupChatLists;

    public AdapterGroup(Context context, ArrayList<ModelGroups> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        //get data
        ModelGroups model = groupChatLists.get(position);
        final String groupId= model.getGroupId();
        String groupImage= model.getGroupIcon();
        String groupTitle= model.getGroupTitle();
        //String date =model.getTimestamp();

        holder.sendername.setText("");
        holder.datetv.setText("");
        holder.messagetv.setText("");

        //show last message and the time
        loadLastMessage(model,holder);


        //set data
        holder.groupname.setText(groupTitle);
        //holder.date.setText(date);
        try {
            Picasso.get().load(groupImage).placeholder(R.drawable.profile_icon).into(holder.groupimage);
        }
        catch (Exception e){
            holder.groupimage.setImageResource(R.drawable.profile_icon);
        }
        holder.itemView.setOnClickListener(v -> {
            //open group chat
            Intent intent = new Intent(context, GroupChatActivity.class);
            intent.putExtra("groupId", groupId);
            context.startActivity(intent);
        });

    }

    private void loadLastMessage(ModelGroups model, HolderGroupChatList holder) {
        //get and display the last message froom the group chat
        DatabaseReference refs = FirebaseDatabase.getInstance().getReference("Groups");
        refs.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType=""+ds.child("type").getValue();

                            //convert time to dd/mm/yy
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("h:mm a", cal).toString();

                            if (messageType.equals("image")){
                                holder.messagetv.setText("Sent Photo");
                            }
                            else{
                                holder.messagetv.setText(message);
                            }

                            holder.datetv.setText(dateTime);

                            //get the infomation of the sender of the last image
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()){
                                                String name = ""+ds.child("username").getValue();
                                                holder.sendername.setText(name);
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

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{
        ImageView groupimage;
        TextView groupname,sendername,messagetv,datetv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupimage =itemView.findViewById(R.id.GroupImage);
            groupname =itemView.findViewById(R.id.groupName);
            sendername =itemView.findViewById(R.id.sendername);
            messagetv =itemView.findViewById(R.id.message);
            datetv =itemView.findViewById(R.id.createdDate);

        }
    }
}
