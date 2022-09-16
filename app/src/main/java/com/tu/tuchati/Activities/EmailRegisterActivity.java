package com.tu.tuchati.Activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.databinding.ActivityEmailRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EmailRegisterActivity extends AppCompatActivity {
    ActivityEmailRegisterBinding binding;
    FirebaseAuth auth;
    private DatabaseReference userRef;
    ProgressDialog progressDialog;
   // String currentUserID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //currentUserID = auth.getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);

        binding.RegisterInbtn.setOnClickListener(v -> CreateAccount());
    }

    private void CreateAccount() {
        String email = binding.signUpEmail.getText().toString();
        String password = binding.signUpPassword.getText().toString();
        String confirmPassword = binding.ConfirmPassword.getText().toString();

        if (email.isEmpty()) {
            binding.signUpEmail.setError("Email cannot be empty");
            return;
        }
        else if (password.isEmpty()) {
            binding.signUpPassword.setError("Password cannot be empty");
            return;
        }
        else if (confirmPassword.isEmpty()) {
            binding.ConfirmPassword.setError("Confirm password");
            return;
        }

        else if (!password.equals(confirmPassword))
        {
            Toast.makeText(this, "Password do not Match", Toast.LENGTH_SHORT).show();
        }

        else {

            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please wait.....");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        HashMap userMap = new HashMap();
                        userMap.put("email", email);
                        userMap.put("accountType", "User");
                        userMap.put("typingTo", "noOne");
                        userMap.put("onlineStatus", "online");
                        userMap.put("coverphoto", "noImage");
                        userMap.put("uid", auth.getUid());
                        userRef.child(auth.getUid()).setValue(userMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful())
                            {
                                sendEmailVerificationMessage();
                                progressDialog.dismiss();
                            }
                            else{

                                String message = task1.getException().getMessage();
                                Toast.makeText(EmailRegisterActivity.this, "error " +message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }).addOnFailureListener(e -> {

                        Toast.makeText(EmailRegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });

        }

    }

    public void goToLogin(View view) {
        Intent intent = new Intent(EmailRegisterActivity.this, EmailLoginActivity.class);
        startActivity(intent);

        Animatoo.animateSwipeRight(EmailRegisterActivity.this);
    }
    private void sendEmailVerificationMessage(){
        FirebaseUser user =auth.getCurrentUser();
        if (user !=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(EmailRegisterActivity.this, "Please check your email and verify your account", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(EmailRegisterActivity.this, EmailLoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        auth.signOut();
                    }
                    else{
                          String error = task.getException().getMessage();
                        Toast.makeText(EmailRegisterActivity.this, "Error" +error, Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    }
                }
            });
        }
    }

}
