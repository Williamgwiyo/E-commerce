package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityProductDetailsBinding;
import com.tu.tuchati.databinding.ActivityWriteReviewBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {
ActivityWriteReviewBinding binding;
private String shopUid;
private FirebaseAuth firebaseAuth;
    private AdView rdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityWriteReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        shopUid = getIntent().getStringExtra("shopUid");

        binding.backBtn.setOnClickListener(v -> onBackPressed());
        //input the data
        binding.SubmitBtn.setOnClickListener(v -> {
            inputData();
        });
        firebaseAuth=FirebaseAuth.getInstance();
        //if someone write review then open load it
        loadMyReview();
        //load the shop information
        loadShopInfo();
        initAdmob();

    }

    private void initAdmob() {
        //ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        rdView = findViewById(R.id.rdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        rdView.loadAd(adRequest);
    }

    private void loadShopInfo() {
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop info
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("shopimage").getValue();

                //set the info
                binding.shopNameTv.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.store_gray).into(binding.profileIv);
                }catch (Exception e)
                {
                  binding.profileIv.setImageResource(R.drawable.store_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            float myRating = Float.parseFloat(ratings);
                            binding.ratingBar.setRating(myRating);
                            binding.reviewEt.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+binding.ratingBar.getRating();
        String review = ""+binding.reviewEt.getText().toString().trim();

        //time of review
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WriteReviewActivity.this, "Review added", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WriteReviewActivity.this,HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}