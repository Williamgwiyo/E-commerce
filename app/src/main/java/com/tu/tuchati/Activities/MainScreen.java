package com.tu.tuchati.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.databinding.ActivityMainScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainScreen extends AppCompatActivity {
    ActivityMainScreenBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //////status bar start////
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //////status bar start////

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
//check if user already logged in
      //  if (auth.getCurrentUser() !=null){
        //    Intent intent = new Intent(MainScreen.this, HomeActivity.class);
          //  startActivity(intent);
            //finish();
        //}

        binding.buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreen.this, EmailRegisterActivity.class);
                startActivity(intent);
                finish();
                Animatoo.animateSwipeLeft(MainScreen.this);
            }
        });

        }

}
