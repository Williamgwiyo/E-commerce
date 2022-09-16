package com.tu.tuchati.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.tu.tuchati.databinding.ActivityPhoneLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneLoginActivity extends AppCompatActivity {

    ActivityPhoneLoginBinding binding;
    FirebaseAuth auth;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() !=null){
            Intent intent = new Intent(PhoneLoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding.phoneBox.requestFocus();
        binding.buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = binding.phoneBox.getText().toString().trim();

                if (mobile.isEmpty() || mobile.length() < 10){
                    binding.phoneBox.setError("Enter a valid mobile");
                    binding.phoneBox.requestFocus();
                    return;
                }

                Intent intent = new Intent(PhoneLoginActivity.this, VerifyOtpActivity.class);
                intent.putExtra("phoneNumber", binding.phoneBox.getText().toString());
                startActivity(intent);
            }
        });
    }
}
