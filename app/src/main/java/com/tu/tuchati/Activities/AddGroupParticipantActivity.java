package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.tu.tuchati.Adapters.AdapterParticipant;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityAddGroupParticipantBinding;
import com.tu.tuchati.databinding.ActivityGroupChatBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddGroupParticipantActivity extends AppCompatActivity {
    ActivityAddGroupParticipantBinding binding;
    FirebaseAuth firebaseAuth;
    private String groupId,myGroupRole;
    private AdView mxdView;

    private ArrayList<User> userlist;
    private AdapterParticipant adapterParticipant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityAddGroupParticipantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        groupId=getIntent().getStringExtra("groupId");

        loadGroupInfo();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        admobInit();

    }

    private void admobInit() {
        //ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        mxdView = findViewById(R.id.xdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mxdView.loadAd(adRequest);
    }

    private void getAllUsers() {
        //init list
        userlist = new ArrayList<>();
        //get user from the database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    User modelUser = ds.getValue(User.class);

                    //get all users except current uid
                    if (!firebaseAuth.getUid().equals(modelUser.getUid())){
                        userlist.add(modelUser);
                    }
                }
                //adapter
                adapterParticipant = new AdapterParticipant(AddGroupParticipantActivity.this,userlist,""+groupId,""+myGroupRole);
                binding.addParticipantRecycler.setAdapter(adapterParticipant);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String groupIds=""+ds.child("groupId").getValue();
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String createdBy=""+ds.child("createdBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                    reference.keepSynced(true);
                    reference.child(groupIds).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        myGroupRole = ""+snapshot.child("role").getValue();
                                        getAllUsers();
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
}