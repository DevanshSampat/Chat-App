package com.devansh.talkative.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devansh.talkative.BuildConfig;
import com.devansh.talkative.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1008;
    private FirebaseAuth mAuth;
    private Dialog dialog;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.sign_in_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        try {
            FileInputStream fis = openFileInput("UserId.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            Intent intent = new Intent(getApplicationContext(),DashboardActivity.class);
            intent.putExtra("user_id",bufferedReader.readLine());
            if(getIntent().hasExtra("email_id")) intent.putExtra("email_id",getIntent().getStringExtra("email_id"));
            startActivity(intent);
                finish();
                return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        signInWithGoogle(new View(this));
    }

    public void signInWithGoogle(View view) {
        dialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                Log.d(BuildConfig.APPLICATION_ID, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                dialog.dismiss();
                Toast.makeText(getApplicationContext(),"Failed to sign in",Toast.LENGTH_SHORT).show();
                Log.w(BuildConfig.APPLICATION_ID, "Google sign in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            finish();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(BuildConfig.APPLICATION_ID, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("info");
                            reference.child("sign_in_time").setValue(System.currentTimeMillis()+"");
                            reference.child("status").setValue("Online");
                            reference.child("name").setValue(user.getDisplayName());
                            reference.child("email").setValue(user.getEmail());
                            try{
                                reference.child("image").setValue(user.getPhotoUrl().toString());
                            } catch (Exception exception) {
                            }
                            Intent intent = new Intent(MainActivity.this,DashboardActivity.class);
                            intent.putExtra("user_id",user.getUid());
                            if(getIntent().hasExtra("email_id")) intent.putExtra("email_id",getIntent().getStringExtra("email_id"));
                            startActivity(intent);
                            finish();
                            if(!new File(getApplicationContext().getFilesDir(),"UserId.txt").exists()) {
                                try {
                                    new File(getApplicationContext().getFilesDir(),"UserId.txt").createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                FileOutputStream fos = getApplicationContext().openFileOutput("UserId.txt",MODE_PRIVATE);
                                fos.write((user.getUid()+"\n").getBytes());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(),"Failed to sign in",Toast.LENGTH_SHORT).show();
                            Log.w(BuildConfig.APPLICATION_ID, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
}
