package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.tu.tuchati.Adapters.AdapterReviews;
import com.tu.tuchati.Models.ModelReview;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityShopDetailsBinding;
import com.tu.tuchati.databinding.ActivityShopReviewsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {
    ActivityShopReviewsBinding binding;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private ArrayList <ModelReview> reviewsList;
    private AdapterReviews adapterReviews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails();
        loadReviews();
    }
    private float ratingSum=0;
    private void loadReviews() {
        reviewsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear data before adding data
                        reviewsList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+rating;

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewsList.add(modelReview);
                        }
                        //adapter
                        adapterReviews = new AdapterReviews(ShopReviewsActivity.this,reviewsList);
                        //set adapter
                        binding.reviewsRv.setAdapter(adapterReviews);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        binding.ratingsTv.setText(String.format("%.2f", avgRating) + "["+numberOfReviews+"]");
                        binding.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("shopimage").getValue();

                //set data
                binding.shopNameTv.setText(name);
                try{
                    Picasso.get().load(shopImage).placeholder(R.drawable.profile_icon).into(binding.shopIv);
                }catch (Exception e){
                    binding.shopIv.setImageResource(R.drawable.profile_icon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}