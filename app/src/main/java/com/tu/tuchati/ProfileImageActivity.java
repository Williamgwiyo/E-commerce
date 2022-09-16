package com.tu.tuchati;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.tu.tuchati.common.Common;
import com.tu.tuchati.databinding.ActivityMainScreenBinding;
import com.tu.tuchati.databinding.ActivityProfileImageBinding;

public class ProfileImageActivity extends AppCompatActivity {
    ActivityProfileImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.profileImageView.setImageBitmap(Common.IMAGE_BITMAP);

    }
}