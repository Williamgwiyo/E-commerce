package com.tu.tuchati.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityEmailLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EmailLoginActivity extends AppCompatActivity {

    private  ActivityEmailLoginBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private ProgressDialog progressDialog;
    private  static final int RC_SIGN_IN=100;
    GoogleSignInClient mGoogleSignInClient;
    private Boolean emailAddresschecker;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //////status bar start////
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //////status bar start////

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        binding.Loginbtn.setOnClickListener(v -> Login());
        binding.DontHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(EmailLoginActivity.this, EmailRegisterActivity.class);
            startActivity(intent);
            Animatoo.animateSwipeLeft(EmailLoginActivity.this);
        });
        binding.forgotBtn.setOnClickListener(v -> {
            showRecoveryPasswordDialog();
        });
        binding.GoogleloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    private void showRecoveryPasswordDialog() {
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //linear layout
        LinearLayout linearLayout =  new LinearLayout(this);
        //view to set in dialog
        EditText email = new EditText(this);
        email.setHint("Email");
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //text size
        email.setMinEms(10);

        linearLayout.addView(email);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);
        //buttons
        builder.setPositiveButton("Recover", (dialog, which) -> {
            String emaill = email.getText().toString().trim();
            startRecovery(emaill);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        //show dialog
        builder.create().show();
    }

    private void startRecovery(String emaill) {
        progressDialog.setMessage("Sending Email..");
        progressDialog.show();
        auth.sendPasswordResetEmail(emaill)
           .addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                   progressDialog.dismiss();
                    if (task.isSuccessful()){
                        Toast.makeText(EmailLoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(EmailLoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
               }
           }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EmailLoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Login() {
        progressDialog.setTitle("We are checking your credentials");
        progressDialog.setMessage("Please wait.....");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        String email = binding.txtEmail.getText().toString();
        String password = binding.txtPassword.getText().toString();

        if (email.isEmpty()) {
            binding.txtEmail.setError("Email cannot be empty");
            return;
        }
        else if (password.isEmpty()) {
            binding.txtPassword.setError("Password cannot be empty");
            return;
        }

        else {

            //login
            auth.signInWithEmailAndPassword(email, password)
                   .addOnSuccessListener(authResult -> {
                       HashMap loginMap = new HashMap();
                       loginMap.put("onlineStatus", "online");
                       loginMap.put("typingTo", "noOne");
                       userRef.child(auth.getUid()).updateChildren(loginMap).addOnSuccessListener(new OnSuccessListener() {
                           @Override
                           public void onSuccess(Object o) {
                               progressDialog.dismiss();
                               verifyEmailAddress();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               progressDialog.dismiss();
                               Toast.makeText(EmailLoginActivity.this, "Got error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                           }
                       });

                   });
        }

    }

    private void verifyEmailAddress() {
        FirebaseUser user = auth.getCurrentUser();
        emailAddresschecker = user.isEmailVerified();
        //if email is verified
        if (emailAddresschecker){
            //send user to home Activity
            Intent intent = new Intent(EmailLoginActivity.this,HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Animatoo.animateSwipeLeft(EmailLoginActivity.this);
            finish();
            progressDialog.dismiss();
        }
        //email not verified
        else
        {
            Toast.makeText(this, "Please verify the email", Toast.LENGTH_SHORT).show();
            auth.signOut();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();

                            Toast.makeText(EmailLoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            //send user to home Activity
                            Intent intent = new Intent(EmailLoginActivity.this,HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Animatoo.animateSwipeLeft(EmailLoginActivity.this);
                            finish();
                            progressDialog.dismiss();
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(EmailLoginActivity.this, "Login Failure", Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EmailLoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
