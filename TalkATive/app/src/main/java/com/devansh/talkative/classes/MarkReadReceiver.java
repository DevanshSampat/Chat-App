package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MarkReadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference senderReference = databaseReference.child(intent.getStringExtra("sender_id"))
                .child("messages").child(intent.getStringExtra("receiver_id"));
        DatabaseReference receiverReference = databaseReference.child(intent.getStringExtra("receiver_id"))
                .child("messages").child(intent.getStringExtra("sender_id"));
        senderReference.child("last_active").setValue((System.currentTimeMillis()+Long.parseLong(intent.getStringExtra("offset")))+"");
        receiverReference.child("last_active_other").setValue((System.currentTimeMillis()+Long.parseLong(intent.getStringExtra("offset")))+"");
        NotificationManagerCompat.from(context).cancel(Integer.parseInt(intent.getStringExtra("notification_id")));
        NotificationManagerCompat.from(context).cancel(360);
    }
}
