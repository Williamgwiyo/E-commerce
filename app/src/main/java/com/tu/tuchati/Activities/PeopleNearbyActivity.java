package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.tu.tuchati.Adapters.PeopleNearbyAdapter;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityPeopleNearbyBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PeopleNearbyActivity extends AppCompatActivity {
    ActivityPeopleNearbyBinding binding;
    FirebaseAuth auth;
    RecyclerView peoplenearby;
    private ArrayList<User>userList;
    private PeopleNearbyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeopleNearbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        peoplenearby=findViewById(R.id.people_nearby);

        binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
        });
        checkUser();

    }

    private void checkUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user!=null){
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String city = ""+ds.child("city").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            binding.peoplenearbyTv.setText("Displaying People near you, Location: "+city);
                            loadPeople(city);
                            //load shops to people in same city of user

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPeople(String city) {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("email")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            String userCity = ""+ds.child("city").getValue();

                            //show only user city
                            if (userCity.equals(city)){
                                userList.add(user);
                            }
                        }
                        adapter = new PeopleNearbyAdapter(PeopleNearbyActivity.this,userList);
                        peoplenearby.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}