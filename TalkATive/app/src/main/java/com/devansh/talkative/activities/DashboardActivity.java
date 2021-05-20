package com.devansh.talkative.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.devansh.talkative.BuildConfig;
import com.devansh.talkative.R;
import com.devansh.talkative.adapters.ChatAdapter;
import com.devansh.talkative.classes.ChatData;
import com.devansh.talkative.classes.ChatNotificationService;
import com.devansh.talkative.classes.ServiceStartingReceiver;
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

import java.io.File;
import java.io.IOException;

public class DashboardActivity extends AppCompatActivity {

    private String userId;
    private RecyclerView recyclerView;
    private int chat_count;
    private int temp_count;
    private ChatData[] chatData;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean pause;
    private boolean goingForSignIn;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        checkForUpdates();
        pause = false;
        goingForSignIn = false;
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("general",
                    "General", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("All chats here");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("service",
                    "Service Notifications", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
            channel.setShowBadge(false);
            channel.setSound(null,null);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setDescription("Services show up here");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
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
        if(!new File(getFilesDir(),"Service.txt").exists()){
            try{
                new File(getFilesDir(),"Service.txt").createNewFile();
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        PendingIntent.getBroadcast(this,108,new Intent(this, ServiceStartingReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    chat_count = (int) snapshot.getChildrenCount();
                    temp_count = 0;
                } catch (Exception exception) {
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ChatNotificationService.class);
                intent.putExtra("user_id",userId);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) startForegroundService(intent);
                else startService(intent);
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
                        try{
                            if(dataSnapshot.child("status_other").getValue().toString().equals("typing")){
                                message = "incoming Typing..."+message.substring(message.lastIndexOf(' '));
                                chatData[temp_count].setMessage(message);
                            }
                        } catch (Exception exception) {

                        }
                        try{
                            long t1 = Long.parseLong(dataSnapshot.child("last_active").getValue().toString());
                            long t2 = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
                            if(t2>t1&&message.startsWith("incoming")) chatData[temp_count].setUnread(true);
                            if(dataSnapshot.child("last_active_other").getValue().toString().equals("online")&&message.startsWith("outgoing")) chatData[temp_count].setSeen(true);
                            t1 = Long.parseLong(dataSnapshot.child("last_active_other").getValue().toString());
                            if(t1>=t2&&message.startsWith("outgoing")) chatData[temp_count].setSeen(true);
                        } catch (Exception exception) {

                        }
                        temp_count++;
                    } catch (Exception exception) {

                    }
                }
                for(int i=0;i< chatData.length-1;i++) {
                    for (int j = 0; j < chatData.length - 1 - i; j++) {
                        long t1, t2;
                        try {
                            t1 = Long.parseLong(chatData[j].getMessage().substring(chatData[j].getMessage().lastIndexOf(' ') + 1));
                            t2 = Long.parseLong(chatData[j + 1].getMessage().substring(chatData[j + 1].getMessage().lastIndexOf(' ') + 1));
                            if (t2 > t1) {
                                ChatData tempData = chatData[j];
                                chatData[j] = chatData[j + 1];
                                chatData[j + 1] = tempData;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(recyclerView.getAdapter()==null) recyclerView.setAdapter(new ChatAdapter(chatData,getIntent().getStringExtra("user_id")));
                else ((ChatAdapter)recyclerView.getAdapter()).setChatData(chatData);
                findViewById(R.id.progress).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("info").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        if(goingForSignIn) return;
                        Picasso.with(DashboardActivity.this).load(snapshot.child("image").getValue().toString()).into((ImageView)findViewById(R.id.profile_image));
                        long time = Long.parseLong(snapshot.child("sign_in_time").getValue().toString());
                        if(time+ AlarmManager.INTERVAL_DAY<System.currentTimeMillis()){
                            goingForSignIn = true;
                            new File(getApplicationContext().getFilesDir(),"UserId.txt").delete();
                            finish();
                            stopService(new Intent(getApplicationContext(),ChatNotificationService.class));
                            NotificationManagerCompat.from(getApplicationContext()).cancel(360);
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
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

    private void checkForUpdates() {
        FirebaseDatabase.getInstance().getReference("app_info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                        new File(getApplicationContext().getExternalFilesDir(null),"update.apk"));
                getContentResolver().delete(updateFileUri,null,null);
                if(BuildConfig.VERSION_CODE<Integer.parseInt(snapshot.child("version_code").getValue().toString())){
                    Intent intent = new Intent(getApplicationContext(),UpdaterActivity.class);
                    intent.putExtra("name",snapshot.child("version_name").getValue().toString());
                    intent.putExtra("link",snapshot.child("link").getValue().toString());
                    intent.putExtra("info",snapshot.child("info").getValue().toString());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void startSearch(View view) {
        startActivity(new Intent(this,SearchUserActivity.class).putExtra("user_id",getIntent().getStringExtra("user_id")));
    }
    @Override
    protected void onPause() {
        pause = true;
        super.onPause();
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!pause) return;
                pause = false;
                long time = System.currentTimeMillis() + Long.parseLong(snapshot.getValue().toString());
                FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("info").child("status").setValue(time+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void signOut(View view) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()) return;
                        Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                        new File(getApplicationContext().getFilesDir(),"UserId.txt").delete();
                        finish();
                        stopService(new Intent(getApplicationContext(),ChatNotificationService.class));
                        NotificationManagerCompat.from(getApplicationContext()).cancel(360);
                    }
                });
    }
}