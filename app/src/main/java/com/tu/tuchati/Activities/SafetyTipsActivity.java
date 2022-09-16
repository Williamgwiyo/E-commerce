package com.tu.tuchati.Activities;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tu.tuchati.databinding.ActivityEmailLoginBinding;
import com.tu.tuchati.databinding.ActivitySafetyTipsBinding;

public class SafetyTipsActivity extends AppCompatActivity {
   ActivitySafetyTipsBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySafetyTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backbtn.setOnClickListener(v -> onBackPressed());

    }
}
