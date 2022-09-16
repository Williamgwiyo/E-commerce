package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.tu.tuchati.Fragments.AllFriendsFragment;
import com.tu.tuchati.Fragments.FriendRequestFragment;
import com.tu.tuchati.databinding.ActivityFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    ActivityFriendsBinding binding;
    private DatabaseReference FriendsRef;
    private FirebaseAuth auth;
    private String currentUserId;

    private int countFriends=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpWithViewPager(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

         binding.backBtn.setOnClickListener(v -> {
            onBackPressed();
        });
         auth = FirebaseAuth.getInstance();
         currentUserId = auth.getCurrentUser().getUid();
         FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
         FriendsRef.keepSynced(true);

         //count no of friends for the current signed in user
         FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //user have friends
                    countFriends = (int) snapshot.getChildrenCount();
                    binding.nameP.setText("Friends ("+Integer.toString(countFriends)+")");
                }else{
                    //no friends
                    binding.nameP.setText("Friends (no friends)");
                }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }
    private void setUpWithViewPager(ViewPager viewPager){
        FriendsActivity.SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AllFriendsFragment(),"All");
        adapter.addFragment(new FriendRequestFragment(),"Requests");
        viewPager.setAdapter(adapter);
    }
    private static class SectionPagerAdapter extends FragmentPagerAdapter{

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position){return mFragmentTitleList.get(position);}
    }
}