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

import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterParticipant extends RecyclerView.Adapter<AdapterParticipant.HolderParticipantAdd> {

    private Context context;
    private ArrayList<User> userList;
    private String groupId,myGroupRole;

    public AdapterParticipant(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_addparticipant,parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get the data
        User modelUser = userList.get(position);
        String name = modelUser.getUsername();
       // String email = modelUser.getEmail();
        String city = modelUser.getCity();
        String country = modelUser.getCountry();
        String uid = modelUser.getUid();
        String image = modelUser.getProfileImage();

        //set the data
        holder.personName.setText(name);
        holder.countryp.setText(country+",");
        holder.cityp.setText(city);

        try {
            Picasso.get().load(image).placeholder(R.drawable.profile_icon).into(holder.personProfile);
        }
        catch (Exception e){
            holder.personProfile.setImageResource(R.drawable.profile_icon);
        }

        checkIfUserAlreadyExistInGroup(modelUser,holder);
        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if user already added show remove options
                //if not added then add option

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    // participant exists
                                    String hisPreviousRole = ""+snapshot.child("role").getValue();
                                   //options
                                    String [] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("Admin")){
                                        //im main admin he is adminAssistant
                                        if (hisPreviousRole.equals("adminAssistant")){
                                            options = new String[]{"Remove Assistant", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                //handle options clicks
                                                if (which == 0){
                                                    removeAssistant(modelUser);
                                                }else{
                                                    removeParticipant(modelUser);
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Assistant", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        makeAssistant(modelUser);
                                                    }
                                                    else{
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("adminAssistant")){
                                        if (hisPreviousRole.equals("Admin")){
                                            Toast.makeText(context, "Group owner", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hisPreviousRole.equals("adminAssistant")){
                                            options = new String[]{"Remove Assistant", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                //handle options clicks
                                                if (which == 0){
                                                    removeAssistant(modelUser);
                                                }else{
                                                    removeParticipant(modelUser);
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Assistant", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        makeAssistant(modelUser);
                                                    }
                                                    else{
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else{
                                    // participant do not exists
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", (dialog, which) -> addParticipant(modelUser))
                                            .setNegativeButton("CANCEL",  (dialog, which) -> dialog.dismiss()).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });
    }

    private void addParticipant(User modelUser) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUser.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);

        //add the user to the group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void makeAssistant(User modelUser) {

        HashMap<String, Object> hashMaps = new HashMap<>();
        hashMaps.put("role", "adminAssistant");

        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMaps)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "role changed to Assistant", Toast.LENGTH_SHORT)
                        .show())
                .addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void removeParticipant(User modelUser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void removeAssistant(User modelUser) {

        HashMap<String, Object> hashMaps = new HashMap<>();
        hashMaps.put("role", "participant");

        //update role in db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMaps)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "no longer admin assistant", Toast.LENGTH_SHORT)
                        .show())
                .addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void checkIfUserAlreadyExistInGroup(User modelUser, HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            // participant exists
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.statusp.setText(hisRole);
                        }
                        else{
                            // participant do not exists
                            holder.statusp.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        CircleImageView personProfile;
        TextView personName,cityp,countryp,statusp;
        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);
            cityp = itemView.findViewById(R.id.cityp);
            personProfile = itemView.findViewById(R.id.personProfilep);
            personName = itemView.findViewById(R.id.personNamep);
            countryp = itemView.findViewById(R.id.countryp);
            statusp = itemView.findViewById(R.id.statusp);

        }
    }
}
