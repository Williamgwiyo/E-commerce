package com.tu.tuchati.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.GroupChatImageActivity;
import com.tu.tuchati.Models.ModelGroupChat;
import com.tu.tuchati.R;
import com.tu.tuchati.common.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat>{
    private static final int MSG_TYPE_LEFT =0;
    private static final int MSG_TYPE_RIGHT=1;

    private Context context;
    private ArrayList<ModelGroupChat>modelGroupChatList;

    private FirebaseAuth firebaseAuth;
   // String currentUser;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;


        firebaseAuth = FirebaseAuth.getInstance();
       // currentUser =firebaseAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        //get the data
        ModelGroupChat model = modelGroupChatList.get(position);
        String message = model.getMessage();
        String senderUid = model.getSender();
        String timestamp = model.getTimestamp();
        String messageType = model.getType();

       // String name = model.get


        //convert time to dd/mm/yy
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("h:mm a", cal).toString();
        //set data
        //check message type
        if (messageType.equals("text")){
          holder.leftImage.setVisibility(View.GONE);
          holder.messageTv.setVisibility(View.VISIBLE);
          holder.messageTv.setText(message);

        }
        else if (messageType.equals("image")){
            holder.leftImage.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);

            holder.leftImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.leftImage.invalidate();
                    Drawable dr = holder.leftImage.getDrawable();
                    Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context,holder.leftImage,"image");
                    Intent intent = new Intent(context, GroupChatImageActivity.class);
                    context.startActivity(intent,activityOptions.toBundle());

                }
            });


            try{
                Picasso.get().load(message).placeholder(R.drawable.bigplaceholder).into(holder.leftImage);
            }
            catch (Exception e){
                holder.leftImage.setImageResource(R.drawable.bigplaceholder);
            }
        }
        holder.timeTv.setText(dateTime);

        //search to get the username
        DatabaseReference usersRef=FirebaseDatabase.getInstance().getReference("Users");
        Query userQuery = usersRef.orderByChild("uid").equalTo(senderUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until data is found
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name = ""+ ds.child("username").getValue();
                    holder.GroupPersonName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    static class HolderGroupChat extends RecyclerView.ViewHolder{
        private TextView GroupPersonName,messageTv,timeTv;
        private ImageView leftImage;
        LinearLayout messageLayout1;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            GroupPersonName = itemView.findViewById(R.id.GroupPersonName);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            leftImage=itemView.findViewById(R.id.leftImage);
            messageLayout1=itemView.findViewById(R.id.messageLayout1);

        }
    }
}
