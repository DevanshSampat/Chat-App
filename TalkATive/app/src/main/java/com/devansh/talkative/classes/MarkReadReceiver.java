package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MarkReadReceiver extends BroadcastReceiver {
    private String user_id;

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            FileInputStream fis = context.openFileInput("UserId.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            user_id = br.readLine();
            if(user_id==null) return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference senderReference = databaseReference.child(user_id)
                .child("messages").child(intent.getStringExtra("receiver_id"));
        DatabaseReference receiverReference = databaseReference.child(intent.getStringExtra("receiver_id"))
                .child("messages").child(user_id);
        senderReference.child("last_active").setValue((System.currentTimeMillis()+Long.parseLong(intent.getStringExtra("offset")))+"");
        receiverReference.child("last_active_other").setValue((System.currentTimeMillis()+Long.parseLong(intent.getStringExtra("offset")))+"");
        NotificationManagerCompat.from(context).cancel(Integer.parseInt(intent.getStringExtra("notification_id")));
        NotificationManagerCompat.from(context).cancel(360);
    }
}
