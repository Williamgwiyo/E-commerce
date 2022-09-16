package com.tu.tuchati.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.Activities.CreateGroupActivity;
import com.tu.tuchati.Activities.CreatePostActivity;
import com.tu.tuchati.Activities.EmailLoginActivity;
import com.tu.tuchati.Activities.FindFriendsActivity;
import com.tu.tuchati.Activities.FriendsActivity;
import com.tu.tuchati.Activities.MyOrderActivity;
import com.tu.tuchati.Activities.MyProfileActivity;
import com.tu.tuchati.Activities.MyShopActivity;
import com.tu.tuchati.Activities.PaymentActivity;
import com.tu.tuchati.Activities.PeopleNearbyActivity;
import com.tu.tuchati.Activities.SettingsActivity;
import com.tu.tuchati.Activities.ShopSetupActivity;
import com.tu.tuchati.Adapters.FeatureAdapter;
import com.tu.tuchati.Adapters.ShopAdapter;
import com.tu.tuchati.Models.FeatureModel;
import com.tu.tuchati.Models.Shops;
import com.tu.tuchati.R;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShopFragment() {
        // Required empty public constructor
    }
    private View view;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView navigationBar;
    private Button logout_btn;
    FirebaseAuth auth;
    RecyclerView shopsRv,featureRv;
    String currentUserID;
    private ProgressDialog progressDialog;
    private  ArrayList<Shops> shopList;
    private ArrayList<FeatureModel> featureModellist;
    private ShopAdapter shopAdapter;
    private FeatureAdapter featureAdapter;
    private Button feature_Shop;

    //private RelativeLayout loginlogout;
    private TextView languages,your_orders,wishlist,address,help,my_orders,mywallet,rate_us,settings,start_selling,people_nearby,local,create_group;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShopFragment newInstance(String param1, String param2) {
        ShopFragment fragment = new ShopFragment();
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
        view= inflater.inflate(R.layout.fragment_shop, container, false);

        onSetNavigationDrawerEvents();

        checkUser();
        return view;
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

                            local.setText("Nearby Shops, Location: "+city);
                            loadShops(city);
                            //load shops to people in same city of user

                            //for the featured shops
                            loadFeaturedShops(city);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFeaturedShops(String city) {
        featureModellist = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.keepSynced(true);

            ref.orderByChild("approve").equalTo("true").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        featureModellist.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            FeatureModel featureModel = ds.getValue(FeatureModel.class);

                            String shopCity =""+ds.child("city").getValue();
                            String shopuid =""+ds.child("uid").getValue();

                            //show the shop for the time that it has been selected to show only
                            long timecurrent = System.currentTimeMillis();
                            if (timecurrent>featureModel.getTimestart() && timecurrent <featureModel.getTimeend()){
                                //show only user city shops
                                if (shopCity.equals(city)){
                                    featureModellist.add(featureModel);
                                }
                            }

                        }
                        featureAdapter = new FeatureAdapter(getContext(),featureModellist);
                        featureRv.setAdapter(featureAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShops(String city) {
        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            Shops shops = ds.getValue(Shops.class);

                            String shopCity =""+ds.child("city").getValue();
                            String country =""+ds.child("country").getValue();
                            String shopimage =""+ds.child("shopimage").getValue();

                            //show only user city shops
                            if (shopCity.equals(city)){
                                shopList.add(shops);
                            }

                        }
                        shopAdapter = new ShopAdapter(getContext(),shopList);
                        shopsRv.setAdapter(shopAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void onSetNavigationDrawerEvents() {
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) view.findViewById(R.id.navView);

        //firebase
        auth = FirebaseAuth.getInstance();
        navigationBar = view.findViewById(R.id.navigationBar);
        currentUserID = auth.getCurrentUser().getUid();

        settings = view.findViewById(R.id.settings);
        featureRv=view.findViewById(R.id.feauredShopRv);
        create_group=view.findViewById(R.id.create_group);
        people_nearby = view.findViewById(R.id.people_nearby);
        logout_btn=view.findViewById(R.id.logout_btn);
        start_selling=view.findViewById(R.id.start_selling);
        logout_btn=view.findViewById(R.id.logout_btn);
        your_orders=view.findViewById(R.id.add_new_post);
        wishlist=view.findViewById(R.id.profile);
        languages = view.findViewById(R.id.languages);
        address=view.findViewById(R.id.my_friends);
        help=view.findViewById(R.id.search_people);
        mywallet = view.findViewById(R.id.myWallet);
        my_orders =view.findViewById(R.id.my_orders);

        rate_us=view.findViewById(R.id.rate_us);
        shopsRv = view.findViewById(R.id.shopsRv);
        local = view.findViewById(R.id.local);
        feature_Shop=view.findViewById(R.id.feature_shop_btn);

        progressDialog = new ProgressDialog(getContext());

        navigationBar.setOnClickListener(this);
        create_group.setOnClickListener(this);
        people_nearby.setOnClickListener(this);
        languages.setOnClickListener(this);
        start_selling.setOnClickListener(this);
        your_orders.setOnClickListener(this);
        my_orders.setOnClickListener(this);
        mywallet.setOnClickListener(this);
        help.setOnClickListener(this);
        address.setOnClickListener(this);
        rate_us.setOnClickListener(this);
        wishlist.setOnClickListener(this);
        settings.setOnClickListener(this);
        logout_btn.setOnClickListener(v -> {
            LogOutUser();
        });
    }

    private void LogOutUser() {

        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with timestamp
        checkOnlineStatus(timestamp);
        auth.signOut();
        Intent intent = new Intent(getActivity(), EmailLoginActivity.class);
        startActivity(intent);
    }
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        dbRef.keepSynced(true);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update status of online user
        dbRef.updateChildren(hashMap);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.navigationBar:
                drawerLayout.openDrawer(navigationView,true);
                break;

            case R.id.my_orders:
                Intent myorders = new Intent(getActivity(), MyOrderActivity.class);
                startActivity(myorders);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.add_new_post:
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.search_people:
                Intent frindfriends = new Intent(getActivity(), FindFriendsActivity.class);
                startActivity(frindfriends);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.my_friends:
                Intent friends = new Intent(getActivity(), FriendsActivity.class);
                startActivity(friends);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.myWallet:
                Intent wallet = new Intent(getActivity(), PaymentActivity.class);
                startActivity(wallet);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.rate_us:
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.languages:
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.profile:
                Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                startActivity(profile);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.settings:
                Intent profileSetting = new Intent(getActivity(), SettingsActivity.class);
                startActivity(profileSetting);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.start_selling:
                CheckIfShopCreated();
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.people_nearby:
                Intent peoplenearby = new Intent(getActivity(), PeopleNearbyActivity.class);
                startActivity(peoplenearby);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.create_group:
                Intent create_group = new Intent(getActivity(), CreateGroupActivity.class);
                startActivity(create_group);
                drawerLayout.closeDrawer(navigationView,true);
                break;

        }

    }

    private void CheckIfShopCreated() {
        progressDialog.setTitle("Please wait....");
        progressDialog.setMessage("checking if shop is created");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren()){
                            String accountType = ""+s.child("accountType").getValue();
                            if (accountType.equals("User"))
                            {

                                progressDialog.dismiss();

                                start_selling.setText("Start Selling");
                                feature_Shop.setText("Create Shop");

                                feature_Shop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), ShopSetupActivity.class);
                                        startActivity(intent);
                                    }
                                });

                                Animatoo.animateSwipeRight(getActivity());

                            }
                            else if(accountType.equals("Seller")) {

                                progressDialog.dismiss();
                                Intent intent = new Intent(getActivity(), MyShopActivity.class);
                                startActivity(intent);
                                start_selling.setText("My Shop");

                                feature_Shop.setText("Feature My Shop");

                                Animatoo.animateSwipeRight(getActivity());

                                feature_Shop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), PaymentActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void changeTextofshop(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren()){
                            String accountType = ""+s.child("accountType").getValue();
                            if (accountType.equals("User"))
                            {

                                progressDialog.dismiss();

                                start_selling.setText("Start Selling");
                                my_orders.setVisibility(View.VISIBLE);

                                feature_Shop.setText("Create Shop");

                                feature_Shop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), ShopSetupActivity.class);
                                        startActivity(intent);
                                    }
                                });

                                Animatoo.animateSwipeRight(getActivity());

                            }
                            else if(accountType.equals("Seller")) {

                                progressDialog.dismiss();

                                start_selling.setText("My Shop");
                                my_orders.setVisibility(View.GONE);

                                feature_Shop.setText("Feature My Shop");

                                feature_Shop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), PaymentActivity.class);
                                        startActivity(intent);
                                    }
                                });

                                Animatoo.animateSwipeRight(getActivity());
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        changeTextofshop();
       // CheckIfShopCreated();
    }
}