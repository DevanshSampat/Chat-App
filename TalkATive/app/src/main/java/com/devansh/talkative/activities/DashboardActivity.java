package com.devansh.talkative.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.devansh.talkative.R;
import com.devansh.talkative.adapters.ChatAdapter;
import com.devansh.talkative.classes.ChatData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DashboardActivity extends AppCompatActivity {

    private String userId;
    private RecyclerView recyclerView;
    private int chat_count;
    private int temp_count;
    private ChatData[] chatData;
    private GoogleSignInClient mGoogleSignInClient;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        recyclerView = findViewById(R.id.all_chats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        userId = getIntent().getStringExtra("user_id");
        FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    chat_count = (int) snapshot.getChildrenCount();
                    temp_count = 0;
                } catch (Exception exception) {
                    return;
                }
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try{
                        int count = Integer.parseInt(dataSnapshot.child("count").getValue().toString());
                    } catch (Exception e) {
                        chat_count--;
                    }
                }
                chatData = new ChatData[chat_count];
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try{
                        int lastMessage = Integer.parseInt(dataSnapshot.child("count").getValue().toString());
                        String message = dataSnapshot.child("message"+lastMessage).getValue().toString();
                        chatData[temp_count] = new ChatData(dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("image").getValue().toString(),
                                dataSnapshot.getKey(), message);
                        temp_count++;
                    } catch (Exception exception) {

                    }
                }
                for(int i=0;i< chatData.length-1;i++){
                    for(int j=0;j< chatData.length-1-i;j++){
                        long t1,t2;
                        t1 = Long.parseLong(chatData[j].getMessage().substring(chatData[j].getMessage().lastIndexOf(' ')+1));
                        t2 = Long.parseLong(chatData[j+1].getMessage().substring(chatData[j+1].getMessage().lastIndexOf(' ')+1));
                        if(t2>t1){
                            ChatData tempData = chatData[j];
                            chatData[j] = chatData[j+1];
                            chatData[j+1] = tempData;
                        }
                    }
                }
                recyclerView.setAdapter(new ChatAdapter(chatData,getIntent().getStringExtra("user_id")));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("info").child("image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        Picasso.with(DashboardActivity.this).load(snapshot.getValue().toString()).into((ImageView)findViewById(R.id.profile_image));
                    } catch (Exception exception) {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception exception) {

        }
    }

    public void startSearch(View view) {
        startActivity(new Intent(this,SearchUserActivity.class).putExtra("user_id",getIntent().getStringExtra("user_id")));
    }
    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long time = System.currentTimeMillis() + Long.parseLong(snapshot.getValue().toString());
                FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("info").child("status").setValue(time+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("info").child("status").setValue("Online");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        },1000);
    }

    public void signOut(View view) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()) return;
                        Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}