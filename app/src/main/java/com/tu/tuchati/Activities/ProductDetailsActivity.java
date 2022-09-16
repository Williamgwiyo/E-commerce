package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityMyShopBinding;
import com.tu.tuchati.databinding.ActivityProductDetailsBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProductDetailsActivity extends AppCompatActivity {
private String title,pPrice,dPrice,pImage,discountAvailable,morethanone,productcondition,productcategory,productdescription
        ,productid;
ActivityProductDetailsBinding binding;
    Boolean LikeChecker = false;
    DatabaseReference LikesRef;
    FirebaseAuth auth;
    String currentUserID,uid,shopUid;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        title = getIntent().getStringExtra("product_title");
        pPrice = getIntent().getStringExtra("product_price");
        dPrice = getIntent().getStringExtra("discount_price");
        pImage = getIntent().getStringExtra("product_image");
        morethanone = getIntent().getStringExtra("morethanone");
        productcondition = getIntent().getStringExtra("productcondition");
        discountAvailable=getIntent().getStringExtra("discountAvailable");
        productcategory=getIntent().getStringExtra("productcategory");
        productdescription=getIntent().getStringExtra("productdescription");
        productid= getIntent().getStringExtra("productid");
        uid= getIntent().getStringExtra("uid");

     //   currentUserID = auth.getCurrentUser().getUid();

        initAdmob();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products").child(productid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                           String databasUserID = snapshot.child("uid").getValue().toString();

                           if (currentUserID.equals(databasUserID)){
                               binding.deleteBtn.setVisibility(View.VISIBLE);
                               binding.editBtn.setVisibility(View.VISIBLE);
                           }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        LikesRef = FirebaseDatabase.getInstance().getReference().child("ProductsLikes");
        LikesRef.keepSynced(true);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        if (morethanone.equals("true")){
            binding.tvCodIndicator.setText("In stock");
        }
        else if(morethanone.equals("false")){
            binding.tvCodIndicator.setText("Order only 1");
        }

        if (discountAvailable.equals("true")){
            binding.dPrice.setVisibility(View.VISIBLE);
            binding.oPrice.setPaintFlags(binding.oPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.oPrice.setTextColor(Color.GRAY);
        }
        else {
            binding.dPrice.setVisibility(View.GONE);
          //  binding.oPrice.setVisibility(View.VISIBLE);
        }


        try {
            Picasso.get().load(pImage).into(binding.productImage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        binding.productTitle.setText(title);
        binding.oPrice.setText("Ksh. "+pPrice);
        binding.dPrice.setText("Ksh."+dPrice);
        binding.Status.setText(productcondition);
        binding.pCategory.setText(productcategory);
        binding.productDescription.setText(productdescription);

        binding.deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder =  new AlertDialog.Builder(this);
            builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete product "+title+" ?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        deleteProduct(); // id is the id of the product
                    }).setNegativeButton("NO", (dialog, which) -> {
                dialog.dismiss();
            }).show();
        });
        binding.readMoreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, SafetyTipsActivity.class);
            startActivity(intent);
        });
        binding.backbtn.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.like.setOnClickListener(v -> {

            LikeChecker = true;
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (LikeChecker.equals(true))
                    {
                        if (snapshot.child(productid).hasChild(currentUserID))
                        {
                            //check if like already exist
                            LikesRef.child(productid).child(currentUserID).removeValue();
                            LikeChecker = false;
                        }
                        else
                        {
                            LikesRef.child(productid).child(currentUserID).setValue(true);
                            LikeChecker = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            setLikeButtonStatus(productid);
        });
        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductDetailsActivity.this,EditProductActivity.class);
                intent.putExtra("productId", productid);
                startActivity(intent);
            }
        });

    }

    private void initAdmob() {
        //ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setLikeButtonStatus(String productid) {
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(productid).hasChild(currentUserID))
                {
                    //count no of likes
                    int countLikes = (int) snapshot.child(productid).getChildrenCount();
                    binding.like.setImageResource(R.drawable.like_red);
                    binding.noOfLikes.setText((countLikes +(" Likes")));
                    binding.likeIcon.setImageResource(R.drawable.like_border_red);
                }
                else
                {
                    //count no of likes
                    int countLikes = (int) snapshot.child(productid).getChildrenCount();
                    binding.like.setImageResource(R.drawable.ic_love_grey_24);
                    binding.noOfLikes.setText((countLikes +(" Likes")));
                    binding.likeIcon.setImageResource(R.drawable.love_border_black);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteProduct() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(firebaseAuth.getUid()).child("Products").child(productid).removeValue()
                .addOnSuccessListener(sVoid -> {
                    Toast.makeText(ProductDetailsActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProductDetailsActivity.this, MyShopActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(ProductDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onStart() {
        super.onStart();
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(productid).hasChild(currentUserID))
                {
                    //count no of likes
                    int countLikes = (int) snapshot.child(productid).getChildrenCount();
                    binding.like.setImageResource(R.drawable.like_red);
                    binding.noOfLikes.setText((countLikes +(" Likes")));
                    binding.likeIcon.setImageResource(R.drawable.like_border_red);
                }
                else
                {
                    //count no of likes
                    int countLikes = (int) snapshot.child(productid).getChildrenCount();
                    binding.like.setImageResource(R.drawable.ic_love_grey_24);
                    binding.noOfLikes.setText((countLikes +(" Likes")));
                    binding.likeIcon.setImageResource(R.drawable.love_border_black);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}