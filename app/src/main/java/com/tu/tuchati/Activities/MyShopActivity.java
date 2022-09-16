package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.tu.tuchati.Fragments.FeaturedShopFragment;
import com.tu.tuchati.Fragments.MyShopFragment;
import com.tu.tuchati.Fragments.OrdersFragment;
import com.tu.tuchati.databinding.ActivityMyShopBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyShopActivity extends AppCompatActivity {
ActivityMyShopBinding binding;
    FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private String currentUsedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityMyShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpWithViewPager(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        currentUsedId = auth.getCurrentUser().getUid();
        binding.backBtn.setOnClickListener(v -> onBackPressed());
        binding.shopratingss.setOnClickListener(v -> {
            Intent intent = new Intent(MyShopActivity.this, ShopReviewsActivity.class);
            intent.putExtra("shopUid", currentUsedId);
            startActivity(intent);
        });

    }

    private void setUpWithViewPager(ViewPager viewPager){
        MyShopActivity.SectionPagerAdapter adapter = new MyShopActivity.SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MyShopFragment(),"My Shop");
        adapter.addFragment(new FeaturedShopFragment(),"Featured");
        adapter.addFragment(new OrdersFragment(),"Orders");
        viewPager.setAdapter(adapter);
    }
    private static class SectionPagerAdapter extends FragmentPagerAdapter {

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