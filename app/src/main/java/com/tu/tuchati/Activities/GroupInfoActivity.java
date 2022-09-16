package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.tu.tuchati.Adapters.AdapterParticipant;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityGroupChatBinding;
import com.tu.tuchati.databinding.ActivityGroupInfoBinding;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
ActivityGroupInfoBinding binding;
private FirebaseAuth  firebaseAuth;
private String groupId;
private String myGroupRole = "";

private ArrayList<User> userList;
private AdapterParticipant adapterParticipant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("groupId");
        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.addParticipanttv.setOnClickListener(v -> {
            Intent intent = new Intent(GroupInfoActivity.this,AddGroupParticipantActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        });
        binding.leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";

                if (myGroupRole.equals("Admin")){
                    dialogTitle="Delete Group";
                    dialogDescription = "Are you sure you want to Delete group permanently";
                    positiveButtonTitle="DELETE";
                }
                else{
                    dialogTitle="Leave Group";
                    dialogDescription = "Are you sure you want to Leave group permanently";
                    positiveButtonTitle="LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("Admin")){
                                    deleteGeoup();
                                }
                                else{
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
        binding.editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this,EditGroupActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        firebaseAuth =FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.keepSynced(true);
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Left Group", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, HomeActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGeoup() {
        //get group info
        DatabaseReference groupref = FirebaseDatabase.getInstance().getReference("Groups");
        //get datails of post using id of post
        Query query = groupref.orderByChild("groupId").equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String groupIcon = "" + ds.child("groupIcon").getValue();

                    ////delete the group and also its image from the firesbase storage

                    StorageReference coverRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupIcon);
                    coverRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.keepSynced(true);
                            ref.child(groupId)
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(GroupInfoActivity.this, "Group deleted", Toast.LENGTH_SHORT).show();
                                          //  Toast.makeText(GroupInfoActivity.this, "Left Group", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(GroupInfoActivity.this, HomeActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String groupTitle=""+ds.child("groupTitle").getValue();
                            String groupDescription=""+ds.child("groupDescription").getValue();
                            String groupIcon=""+ds.child("groupIcon").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String createdBy=""+ds.child("createdBy").getValue();

                            //convert time to dd/mm/yy
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("h:mm a", cal).toString();

                            loadCreatorInfo(dateTime, createdBy);

                            binding.nameP.setText(groupTitle);
                            binding.groupDescriptiontv.setText(groupDescription);
                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.profile_icon).into(binding.groupIcon);
                            }
                            catch (Exception e){
                                binding.groupIcon.setImageResource(R.drawable.profile_icon);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("username").getValue();
                    binding.createdBy.setText("Created by "+name+" on"+dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            binding.nameP.setText("Group Details "+"("+myGroupRole+")");

                            if (myGroupRole.equals("participant")){
                                binding.editGroup.setVisibility(View.GONE);
                                binding.addParticipanttv.setVisibility(View.GONE);
                                binding.leaveGroup.setText("Leave Group");

                            }else if (myGroupRole.equals("adminAssistant")){
                                binding.editGroup.setVisibility(View.GONE);
                                binding.addParticipanttv.setVisibility(View.VISIBLE);
                                binding.leaveGroup.setText("Leave Group");

                            }else if (myGroupRole.equals("Admin")){
                                binding.editGroup.setVisibility(View.VISIBLE);
                                binding.addParticipanttv.setVisibility(View.VISIBLE);
                                binding.leaveGroup.setText("Delete Group");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get uid from group
                    String uid = ""+ds.child("uid").getValue();
                    //get info of user using uid
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()){
                                User modeluser = ds.getValue(User.class);

                                userList.add(modeluser);
                            }
                            //adapter
                            adapterParticipant = new AdapterParticipant(GroupInfoActivity.this,userList,groupId,myGroupRole);
                            binding.participants.setAdapter(adapterParticipant);
                            binding.participantstv.setText("Participants ("+userList.size()+")");
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
}