package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tu.tuchati.databinding.ActivityCreatePostBinding;
import com.tu.tuchati.databinding.ActivityPaymentBinding;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity  {
    ActivityPaymentBinding binding;
    private RewardedAd mRewardedAd;
    private FirebaseAuth firebaseAuth;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    private ProgressDialog progressDialog;
    private String currentUsedId;

    private final String TAG = "-->Admob";

    private String text;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        currentUsedId = firebaseAuth.getCurrentUser().getUid();


        MobileAds.initialize(this, initializationStatus -> loadRewardedAd());

        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.watchadsBtn.setOnClickListener(v -> showRewardAd());
        binding.pay100Btn.setOnClickListener(v -> {

           int reward = Integer.parseInt(binding.TotalCoins.getText().toString().trim());

            //if coins are less that 100 then cannot feature the shop
            if (reward <100){
                Toast.makeText(PaymentActivity.this, "You need 100 Tuchati coins, get more coins", Toast.LENGTH_LONG).show();
            }
            else{//feature the shop and minus the coins used
                binding.TotalCoins.setText(String.valueOf(reward - 100));
                Toast.makeText(PaymentActivity.this, "Your shop will be featured for one Week", Toast.LENGTH_LONG).show();
                //input the new coins to the firebase database

                //add the remaining coins to the database

                int remainingCoins = Integer.parseInt(binding.TotalCoins.getText().toString().trim());

                //add to realtime database
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("uid",""+firebaseAuth.getUid());
                hashMap.put("coins", ""+remainingCoins);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.keepSynced(true);
                ref.child(firebaseAuth.getUid()).child("Coins").child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(aVoid -> {

                        }).addOnFailureListener(e -> Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());


                //feature the shop for one week
                featureShopForOneWeek();
            }


        });
        binding.pay200Btn.setOnClickListener(v -> {

            int reward = Integer.parseInt(binding.TotalCoins.getText().toString().trim());

            //if coins are less that 100 then cannot feature the shop
            if (reward <200){
                Toast.makeText(PaymentActivity.this, "You need 200 Tuchati coins, get more coins", Toast.LENGTH_SHORT).show();
            }
            else{//feature the shop and minus the coins used
                binding.TotalCoins.setText(String.valueOf(reward - 200));
                Toast.makeText(PaymentActivity.this, "Your shop will be featured for Two Weeks", Toast.LENGTH_SHORT).show();
                //input the new coins to the firebase database

                //add the remaining coins to the database

                int remainingCoins = Integer.parseInt(binding.TotalCoins.getText().toString().trim());

                //add to realtime database
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("uid",""+firebaseAuth.getUid());
                hashMap.put("coins", ""+remainingCoins);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.keepSynced(true);
                ref.child(firebaseAuth.getUid()).child("Coins").child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(aVoid -> {

                        }).addOnFailureListener(e -> Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());


                //feature the shop for two weeks
                featureShopForTwoWeeks();
            }


        });

    }

    private void featureShopForTwoWeeks() {
        progressDialog.setTitle("Please wait....");
        progressDialog.setMessage("Requesting to add Shop to featured ");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsedId);
        usersRef.keepSynced(true);
        long timeend = System.currentTimeMillis()+1209600000;

        HashMap usermap = new HashMap();
        usermap.put("timestart", ServerValue.TIMESTAMP);
        usermap.put("timeend",timeend);
        usermap.put("packageType","Two Weeks");
        usermap.put("approve","true");
        usersRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(PaymentActivity.this, "Your shop will be featured within 5 hours", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PaymentActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void featureShopForOneWeek() {
        progressDialog.setTitle("Please wait....");
        progressDialog.setMessage("Requesting to add Shop to featured ");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsedId);
        long timeend = System.currentTimeMillis()+604800000;

        HashMap usermap = new HashMap();
        usermap.put("timestart", ServerValue.TIMESTAMP);
        usermap.put("timeend",timeend);
        usermap.put("packageType","One Week");
        usermap.put("approve","true");
        usersRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(PaymentActivity.this, "Your shop will be featured within 5 hours", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PaymentActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback(){
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                        Log.d(TAG, "Ad Failed to load.");
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                                mRewardedAd = null;

                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                loadRewardedAd();
                            }
                        });
                    }
                });
    }

    //Handle the reward event
    private void showRewardAd(){
        if (mRewardedAd != null) {
            mRewardedAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();

                    //getting the value from the textview
                   int reward = Integer.parseInt(binding.TotalCoins.getText().toString().trim());
                    //adding coins after the ads video
                    binding.TotalCoins.setText(String.valueOf(reward + 20));

                    int newReward = Integer.parseInt(binding.TotalCoins.getText().toString().trim());



                    //put the reward value into the realtime database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid",""+firebaseAuth.getUid());
                    hashMap.put("coins", ""+newReward);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.keepSynced(true);
                    ref.child(firebaseAuth.getUid()).child("Coins").child(firebaseAuth.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(PaymentActivity.this, "You Got Coins ", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }


    private void loadMyCoins(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(firebaseAuth.getUid()).child("Coins").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String Coins = ""+snapshot.child("coins").getValue();

                            binding.TotalCoins.setText(Coins);
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
        loadMyCoins();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMyCoins();
    }
}