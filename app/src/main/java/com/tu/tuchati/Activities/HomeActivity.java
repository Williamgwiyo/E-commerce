package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.Fragments.GroupsFragment;
import com.tu.tuchati.Fragments.ShopFragment;
import com.tu.tuchati.Fragments.ChatsFragment;
import com.tu.tuchati.Fragments.TuchatFragment;
import com.tu.tuchati.R;

import com.tu.tuchati.databinding.ActivityHomeBinding;
import com.tu.tuchati.notifications.Token;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ActivityHomeBinding binding;
    private DatabaseReference UsersRef;

    //  FrameLayout frameLayout;
    private AdView mAdView;
    FirebaseAuth firebaseAuth;
    String mUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView=findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigation);

        firebaseAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        checkUserStatus();
        initAdmob();

        ////////repplacing by default fragment on Home activity////
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TuchatFragment()).commit();

        ////////repplacing by default fragment on Home activity////

        //update token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void initAdmob() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void checkUserStatus() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user!=null) {
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            checkUserExistance();
        }
    }

    private void checkUserExistance() {
        final String current_user_id = firebaseAuth.getCurrentUser().getUid();

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild("username"))
                {
                    Intent intent = new Intent(HomeActivity.this,SetupProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Animatoo.animateSwipeLeft(HomeActivity.this);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        int seletedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.navigation_tuchat != seletedItemId) {
            setHomeItem(HomeActivity.this);
        } else {
            super.onBackPressed();
        }

    }
    private void setHomeItem(Activity activity) {
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                activity.findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_tuchat);

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigation=
            item -> {
                Fragment selectedFragment = null;
                switch (item.getItemId())
                {
                    case R.id.navigation_chat:
                        selectedFragment = new ChatsFragment();
                        break;
                    case R.id.navigation_tuchat:
                        selectedFragment = new TuchatFragment();
                        break;
                    case R.id.navigation_myshop:
                        selectedFragment = new GroupsFragment();
                        break;
                    case R.id.navigation_shop:
                        selectedFragment = new ShopFragment();
                        break;
                    default:
                        // selectedFragment = new ChatsFragment();


                }
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,selectedFragment).commit();
                return true;
            };

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExistance();
    }
}