package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.devansh.talkative.activities.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OpenChatReceiver extends BroadcastReceiver {
    private boolean isLoaded;
    @Override
    public void onReceive(Context context, Intent intent) {
        String userId = intent.getStringExtra("user_id");
        String name = intent.getStringExtra("name");
        Log.println(Log.ASSERT,"user id",userId+"");
        isLoaded = false;
        if(userId==null) return;
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isLoaded) return;
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    try {
                        if (dataSnapshot.child("name").getValue().toString().equals(name)){
                            Intent chatIntent = new Intent(context, ChatActivity.class);
                            intent.putExtra("sender_id",userId);
                            intent.putExtra("receiver_id",dataSnapshot.getKey());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            isLoaded = true;
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
