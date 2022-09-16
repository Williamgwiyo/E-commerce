package com.tu.tuchati.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.Activities.Constants;
import com.tu.tuchati.Activities.EditShopActivity;
import com.tu.tuchati.Activities.HomeActivity;
import com.tu.tuchati.Activities.PaymentActivity;
import com.tu.tuchati.Activities.ShopAddProductActivity;
import com.tu.tuchati.Adapters.ProductAdapter;
import com.tu.tuchati.Models.ProductsModel;
import com.tu.tuchati.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyShopFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyShopFragment() {
        // Required empty public constructor
    }

    FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private String currentUsedId;
    private ImageView filterBtn;
    private TextView filtreredProducttv,personName,shopEmail,shopName;
    private Button featureShopBtn;
    private EditText search;
    private ImageButton editshop,addProduct,deleteshop;
    private CircleImageView storeProfileImage;

    private RecyclerView productRecyclerview;

    private ArrayList<ProductsModel> productList;
    private ProductAdapter productAdapter;
    private AdView mAdView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyShopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyShopFragment newInstance(String param1, String param2) {
        MyShopFragment fragment = new MyShopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_my_shop, container, false);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
        currentUsedId = auth.getCurrentUser().getUid();

        addProduct = view.findViewById(R.id.add_product);
        filterBtn = view.findViewById(R.id.filterBtn);
        filtreredProducttv = view.findViewById(R.id.filtreredProducttv);
        featureShopBtn = view.findViewById(R.id.feature_shop_btn);
        search = view.findViewById(R.id.search);
        editshop = view.findViewById(R.id.edit_shop);
        deleteshop = view.findViewById(R.id.delete_shop);
        productRecyclerview = view.findViewById(R.id.product_recyclerview);
        personName = view.findViewById(R.id.personName);
        shopEmail = view.findViewById(R.id.shop_email);
        shopName = view.findViewById(R.id.shopName);
        storeProfileImage = view.findViewById(R.id.store_profile_image);


        MobileAds.initialize(getContext(), initializationStatus -> {
        });

        mAdView = view.findViewById(R.id.myshoppdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        addProduct.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShopAddProductActivity.class);
            startActivity(intent);
            Animatoo.animateFade(getContext());
        });
        filterBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Choose Category:")
                    .setItems(Constants.productCategories1, (dialog, which) -> {
                        //get selected item
                        String selected = Constants.productCategories1[which];
                        filtreredProducttv.setText(selected);
                        if (selected.equals("All")){
                            loadAllProducts();
                        }
                        else {
                            //load filtered products
                            loadFilteredProducts(selected);
                        }
                    }).show();
        });
        featureShopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                startActivity(intent);
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    productAdapter.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editshop.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditShopActivity.class);
            startActivity(intent);

        });
        deleteshop.setOnClickListener(v -> {
            //update the account type to user
            AlertDialog.Builder builder =  new AlertDialog.Builder(getContext());
            builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete Shop?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        deleteTheProducts();
                        ChangeTypeToUser();
                    }).setNegativeButton("NO", (dialog, which) -> {
                dialog.dismiss();
            }).show();
        });
        loadAllProducts();

        FirebaseUser user = auth.getCurrentUser();
        if (user!=null)
        {
            loadInfo();
        }

        return view;
    }

    private void ChangeTypeToUser() {
        //get shop info first
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String profileImage = ""+ds.child("shopimage").getValue();

                            deleteShopImageFromStorage(profileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void deleteShopImageFromStorage(String profileImage) {

        //first delete image from storage
        StorageReference coverRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImage);

        coverRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //deleting the shop
                progressDialog.setTitle("Please wait....");
                progressDialog.setMessage("Deleting ");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);
                //save info without pic
                HashMap userMap = new HashMap();
                userMap.put("accountType", "User");
                userMap.put("shopName", "");
                userMap.put("shopphone", "");
                userMap.put("shopemail", "");
                userMap.put("timestart","");
                userMap.put("timeend","");
                userMap.put("packageType","");
                userMap.put("approve","false");
                DatabaseReference refs= FirebaseDatabase.getInstance().getReference("Users");
                refs.keepSynced(true);
                refs.child(auth.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(), "Shop is deleted", Toast.LENGTH_SHORT).show();
                            //now delete all the products of that shop
                            //progressDialog.dismiss();
                        }
                        else {
                            String message = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteTheProducts() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(firebaseAuth.getUid()).child("Products").removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Shop Products Deleted", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    startActivity(intent);
                }).addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(auth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String produCategory = ""+ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(produCategory)){
                                ProductsModel productsModel = ds.getValue(ProductsModel.class);
                                productList.add(productsModel);
                            }
                        }
                        //setup adapter
                        productAdapter = new ProductAdapter(getContext(), productList);
                        productRecyclerview.setAdapter(productAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.child(auth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ProductsModel productsModel = ds.getValue(ProductsModel.class);
                            productList.add(productsModel);
                        }
                        //setup adapter
                        productAdapter = new ProductAdapter(getContext(), productList);
                        productRecyclerview.setAdapter(productAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name=""+ds.child("username").getValue();
                            String email=""+ds.child("shopemail").getValue();
                            String shopname=""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("shopimage").getValue();



                            personName.setText(name);
                            shopEmail.setText(email);
                            shopName.setText(shopname);

                            Picasso.get()
                                    .load(profileImage)
                                    .placeholder(R.drawable.store_gray)
                                    .error(R.drawable.store_gray)
                                    .into(storeProfileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}