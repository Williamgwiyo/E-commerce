package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tu.tuchati.Adapters.AdapterCartItem;
import com.tu.tuchati.Adapters.ProductAdapterUser;
import com.tu.tuchati.Models.ModelCartItem;
import com.tu.tuchati.Models.ProductsModel;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityShopDetailsBinding;
import com.tu.tuchati.databinding.ActivityShopSetupBinding;
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

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
ActivityShopDetailsBinding binding;


 private String shopUid;
 private FirebaseAuth firebaseAuth;
 private String myLatitude, myLongitude,shopphone;
 private String shopLatitude, shopLongitude;
 public String shopName,email;
 private EasyDB easyDB;
 private AdView mxdView;

 //progress dialog
 private ProgressDialog progressDialog;


 private RecyclerView productsRv;

 private ArrayList<ProductsModel> productsList;
 private ProductAdapterUser adapterUser;

 //cart
    private ArrayList<ModelCartItem> cartItemsList;
    private AdapterCartItem adapterCartItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productsRv = findViewById(R.id.productsRv);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();

        initAdmob();

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        deleteCartData();
        cartCount();

        //Search
        binding.searchProductEt.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            adapterUser.getFilter().filter(s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
});
        binding.Cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });

        binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.callBtn.setOnClickListener(v -> {
            dialPhone();
        });
        binding.mapBtn.setOnClickListener(v -> {
            openMap();
        });
        binding.shopReview.setOnClickListener(v -> {
            Intent intent = new Intent(ShopDetailsActivity.this,WriteReviewActivity.class);
            intent.putExtra("shopUid",shopUid);
            startActivity(intent);
        });
        binding.filterProductBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
            builder.setTitle("Choose Category:")
                    .setItems(Constants.productCategories1, (dialog, which) -> {
                        //get selected item
                        String selected = Constants.productCategories1[which];
                        binding.filterProductTv.setText(selected);
                        if (selected.equals("All")){
                            loadShopProducts();
                        }
                        else {
                            //load filtered products
                            adapterUser.getFilter().filter(selected);
                        }
                    }).show();
        });
        binding.shopratingss.setOnClickListener(v -> {
            Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
            intent.putExtra("shopUid", shopUid);
            startActivity(intent);
        });

    }

    private void initAdmob() {
        //ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        mxdView = findViewById(R.id.detailsdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mxdView.loadAd(adRequest);
    }


    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();
    }
    public void cartCount(){
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            binding.cartCount.setVisibility(View.GONE);
        }
        else {
            binding.cartCount.setVisibility(View.VISIBLE);
            binding.cartCount.setText(""+count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView sTotalTv,allTotalPriceTv,shopNameTv;
    private void showCartDialog() {
        cartItemsList = new ArrayList<>();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);

        shopNameTv = view.findViewById(R.id.shopNameTv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cartItemsList.size() == 0){
                    //if cartlist is empty
                    Toast.makeText(ShopDetailsActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
            }
        });

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItemsList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemsList);

        cartItemsRv.setAdapter(adapterCartItem);
        sTotalTv.setText("Ksh "+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("Ksh "+(allTotalPrice));

        //reset total proce on dialog dismiss
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });
    }

    private void submitOrder() {
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("Ksh ", "");

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);

        //add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //add order items
                        for (int i=0; i<cartItemsList.size(); i++){
                            String pId = cartItemsList.get(i).getpId();
                            String id = cartItemsList.get(i).getId();
                            String cost = cartItemsList.get(i).getCost();
                            String name = cartItemsList.get(i).getName();
                            String price = cartItemsList.get(i).getPrice();
                            String quantity = cartItemsList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();
                        //after place order empty the cart
                        deleteCartData();

                        //after placing order open order details page
                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId", timestamp);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private float ratingSum=0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear data before adding data
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+rating;

                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        binding.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void openMap() {
    //saddr source address
    //daddr destination address
        String address = "https://maps.google.com/maps?saddr=" +myLatitude + "," +myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }
    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopphone))));
        Toast.makeText(this, ""+shopphone, Toast.LENGTH_SHORT).show();
    }
    private void loadShopProducts() {
        //init list
        productsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      //clear list before adding items
                      productsList.clear();
                      for (DataSnapshot ds: snapshot.getChildren()){
                          ProductsModel productsModel = ds.getValue(ProductsModel.class);
                          productsList.add(productsModel);
                      }
                      //setup adapter

                        adapterUser = new ProductAdapterUser(ShopDetailsActivity.this,productsList);
                      //set adapter
                        productsRv.setAdapter(adapterUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String city = ""+ds.child("city").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String name = ""+ds.child("username").getValue();
                            email = ""+ds.child("email").getValue();
                            String shopimage = ""+ds.child("shopimage").getValue();
                            myLatitude= ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private  void loadShopDetails(){
        shopUid = getIntent().getStringExtra("shopUid");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String shopemail = ""+snapshot.child("shopemail").getValue();
                shopphone = ""+snapshot.child("shopphone").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                String shopimage = ""+snapshot.child("shopimage").getValue();
                String shopaddress = ""+snapshot.child("address").getValue();
                //set data
                binding.shopNameTv.setText(shopName);
                binding.emailTv.setText(shopemail);
                binding.phoneTv.setText(shopphone);
                binding.addressTv.setText(shopaddress);

                try {
                    Picasso.get().load(shopimage).into(binding.shopIv);
                }
                catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        deleteCartData();
    }
}