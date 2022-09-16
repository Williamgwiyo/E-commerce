package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.tu.tuchati.Adapters.AdapterOrderedItem;
import com.tu.tuchati.Models.ModelOrderedItem;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityOrderDetailsUsersBinding;
import com.tu.tuchati.databinding.ActivityShopDetailsBinding;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {
    ActivityOrderDetailsUsersBinding binding;
    private String orderTo, orderId;
    private FirebaseAuth firebaseAuth;
    private AdView mAdView;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");

        binding.backBtn.setOnClickListener(v -> onBackPressed());
        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();
        initAdmob();

    }

    private void initAdmob() {
        //ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.odetailsdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }
                      adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this, orderedItemArrayList);
                        //set adapter
                        binding.itemsRv.setAdapter(adapterOrderedItem);

                        //set item count
                        binding.totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
      DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
      ref.keepSynced(true);
      ref.child(orderTo).child("Orders").child(orderId)
              .addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                      String orderBy = ""+snapshot.child("orderBy").getValue();
                      String orderCost = ""+snapshot.child("orderCost").getValue();
                      String orderId = ""+snapshot.child("orderId").getValue();
                      String orderStatus = ""+snapshot.child("orderStatus").getValue();
                      String orderTime = ""+snapshot.child("orderTime").getValue();
                      String orderTo = ""+snapshot.child("orderTo").getValue();
                      String latitude = ""+snapshot.child("latitude").getValue();
                      String longitude = ""+snapshot.child("longitude").getValue();

                      //convert time
                      Calendar calendar = Calendar.getInstance();
                      calendar.setTimeInMillis(Long.parseLong(orderTime));
                      String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                      if (orderStatus.equals("In Progress")){
                          binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                      }
                      else if (orderStatus.equals("Completed")){
                          binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                      }
                      else if (orderStatus.equals("Cancelled")){
                          binding.orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                      }

                      //set data
                      binding.orderIdTv.setText(orderId);
                      binding.orderStatusTv.setText(orderStatus);
                      binding.amountTv.setText("Ksh "+orderCost);
                      binding.dateTv.setText(formatedDate);

                      findAddress(latitude, longitude);
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
              });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find add,country,state and city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon,1);
            String address = addresses.get(0).getAddressLine(0);
            binding.addressTv.setText(address);
        }catch (Exception e){

        }
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        binding.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}