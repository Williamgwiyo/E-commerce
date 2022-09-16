package com.tu.tuchati.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.tu.tuchati.common.Common;
import com.tu.tuchati.databinding.ActivityChatImageBinding;
import com.tu.tuchati.databinding.ActivityGroupChatBinding;
import com.tu.tuchati.databinding.ActivityGroupChatImageBinding;

public class GroupChatImageActivity extends AppCompatActivity {
    ActivityGroupChatImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //////status bar start////
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //////status bar start////

        binding.arrowBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.groupChatImage.setImageBitmap(Common.IMAGE_BITMAP);

    }
    }
