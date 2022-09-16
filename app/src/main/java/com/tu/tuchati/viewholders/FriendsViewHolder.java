package com.tu.tuchati.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView profileImage;
    public TextView userName,xstatus;
    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);
        profileImage = itemView.findViewById(R.id.allUserProfileImage);
        xstatus = itemView.findViewById(R.id.allStatus);
        userName = itemView.findViewById(R.id.allUsername);
    }
}
