package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TurnOffChatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(intent.getStringExtra("sender_id"))
                        .child(intent.getStringExtra("receiver_id"))
                        .child("last_active").setValue(String.valueOf(System.currentTimeMillis()+Long.parseLong(snapshot.getValue().toString())));

                FirebaseDatabase.getInstance().getReference("users")
                        .child(intent.getStringExtra("receiver_id"))
                        .child(intent.getStringExtra("sender_id"))
                        .child("last_active_other").setValue(String.valueOf(System.currentTimeMillis()+Long.parseLong(snapshot.getValue().toString())));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}