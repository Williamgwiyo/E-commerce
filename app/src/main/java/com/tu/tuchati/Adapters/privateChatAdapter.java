package com.tu.tuchati.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.ChatImage;
import com.tu.tuchati.Models.ChatModel;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class privateChatAdapter extends RecyclerView.Adapter<privateChatAdapter.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    List<ChatModel> chatModelList;
    String imageUri,currentUserID;

    FirebaseAuth auth;

    public privateChatAdapter(Context context, List<ChatModel> chatModelList, String imageUri) {
        this.context = context;
        this.chatModelList = chatModelList;
        this.imageUri = imageUri;

        auth=FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message = chatModelList.get(position).getMessage();
        String timestamp = chatModelList.get(position).getTimestamp();
        String uid= chatModelList.get(position).getReceiver();
        String type = chatModelList.get(position).getType();

        //convert time to dd/mm/yy
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("h:mm a", cal).toString();

        if (type.equals("text")){
            holder.message.setVisibility(View.VISIBLE);
            holder.leftImage1.setVisibility(View.GONE);
            holder.message.setText(message);
        }
        else{
            holder.message.setVisibility(View.GONE);
            holder.leftImage1.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.GONE);

            holder.leftImage1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //long click on image
                    //show delete message confirm dialog
                    AlertDialog.Builder builder =  new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure you want to delete this image?");

                    //delete button
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                        }
                    });
                    //  builder.setPositiveButton("Delete", (dialog, which) -> deleteMessage(position));
                    //cancel delete button
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });
                    //create and show dialog
                    builder.create().show();

                    return false;
                }
            });

            holder.leftImage1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.leftImage1.invalidate();
                    Drawable dr = holder.leftImage1.getDrawable();
                    Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context,holder.leftImage1,"image");
                    Intent intent = new Intent(context, ChatImage.class);
                    context.startActivity(intent,activityOptions.toBundle());

                }
            });
            Picasso.get().load(message).placeholder(R.drawable.profile_icon).into(holder.leftImage1);
        }

        //set data
        holder.message.setText(message);
        holder.time.setText(dateTime);

        try {
            Picasso.get().load(imageUri).into(holder.profileImage);
        } catch (Exception e) {

        }
        //set/delivered status of message
        if (position==chatModelList.size()-1){
            if (chatModelList.get(position).isSeen()){
                holder.isSeen.setText("Seen");
            }
            else {
                holder.isSeen.setText("Delivered");
            }
        }
        else{
            holder.isSeen.setVisibility(View.GONE);
        }
        //long click to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show delete message confirm dialog
                AlertDialog.Builder builder =  new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this message?");

                //delete button
                builder.setPositiveButton("Delete", (dialog, which) -> deleteMessage(position));
                //cancel delete button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();

                return false;
            }
        });

    }

    private void deleteMessage(int position) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get timestamp of clicked message and with all messages in chats
        String msgTimeStamp = chatModelList.get(position).getTimestamp();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.keepSynced(true);
        Query query = ref.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //allow sender to delete only his message
                    if (ds.child("sender").getValue().equals(myUID)){
                        //remove message from chats from both sides
                       // ds.getRef().removeValue();

                        //set value of this message to this messag was deleted
                       HashMap<String, Object> hashMap = new HashMap<>();
                       hashMap.put("message", "This message was deleted....");
                       hashMap.put("type","text");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can delete only your message", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }
    @Override
    public int getItemViewType(int position) {

        if (chatModelList.get(position).getSender().equals(currentUserID)){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        ImageView leftImage1;
        TextView message, time, isSeen;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.receiverImage);
            message = itemView.findViewById(R.id.rmessage);
            time = itemView.findViewById(R.id.dateTime);
            isSeen = itemView.findViewById(R.id.rdelivered);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            leftImage1 = itemView.findViewById(R.id.leftImage1);
        }
    }
}
