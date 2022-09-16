package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.tu.tuchati.databinding.ActivityGroupInfoBinding;
import com.tu.tuchati.databinding.ActivityProfileMoreInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileMoreInfoActivity extends AppCompatActivity {
    ActivityProfileMoreInfoBinding binding;
    private DatabaseReference userRef;
    String currentUserID;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileMoreInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userRef.keepSynced(true);

        binding.backBtn.setOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadaboutInfo();
    }

    private void loadaboutInfo() {
        userRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("city")) {
                        String city = snapshot.child("city").getValue().toString();
                        binding.CityTv.setText(city);
                    }
                    if (snapshot.hasChild("country")) {
                        String country = snapshot.child("country").getValue().toString();
                        binding.CountryTv.setText(country);
                    }
                    if (snapshot.hasChild("gender")) {
                        String gender = snapshot.child("gender").getValue().toString();
                        binding.GenderTv.setText(gender);
                    }
                    if (snapshot.hasChild("email")) {
                        String email = snapshot.child("email").getValue().toString();
                        binding.EmailTv.setText(email);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}