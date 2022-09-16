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

import com.tu.tuchati.Activities.NearbyProfileActivity;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleNearbyAdapter extends RecyclerView.Adapter<PeopleNearbyAdapter.HolderPeople> {
    private Context context;
    public ArrayList<User> usersList;

    public PeopleNearbyAdapter(Context context, ArrayList<User> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public HolderPeople onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.nearby_user_item,parent,false);

        return new HolderPeople(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPeople holder, int position) {
        User user = usersList.get(position);
        Long latitude = user.getLatitude();
        Long longitude = user.getLongitude();
        String city = user.getCity();
        String username = user.getUsername();
        String status = user.getStatus();
        String uid = user.getUid();
        String userImage = user.getProfileImage();
        //set data
        holder.city.setText(city);
        holder.userName.setText(username);
        holder.xstatus.setText(status);

        if (usersList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatus.setVisibility(View.VISIBLE);
        }


        try {
            Picasso.get().load(userImage).placeholder(R.drawable.profile_icon).into(holder.pImage);
        }
        catch (Exception e){
            holder.pImage.setImageResource(R.drawable.profile_icon);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NearbyProfileActivity.class);
            intent.putExtra("visit", uid);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class HolderPeople extends RecyclerView.ViewHolder{
        public CircleImageView pImage;
        public TextView userName,xstatus,city;
        public ImageView onlineStatus;

        public HolderPeople(@NonNull View itemView) {
            super(itemView);
            pImage = itemView.findViewById(R.id.allUserProfileImage);
            xstatus = itemView.findViewById(R.id.allStatus);
            userName = itemView.findViewById(R.id.allUsername);
            city = itemView.findViewById(R.id.cityp);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);

        }
    }
}
