package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.devansh.talkative.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class DirectReplyReceiver extends BroadcastReceiver {
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
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        int count = Integer.parseInt(intent.getStringExtra("count"))+1;
        if(remoteInput!=null) {
            String message = remoteInput.getString("key_text_reply");
            if (message.trim().length() == 0) return;
            while (message.startsWith(" ")) message = message.substring(1);
            while (message.endsWith(" ")) message = message.substring(0, message.length() - 1);
            String time = (System.currentTimeMillis() + Long.parseLong(intent.getStringExtra("offset"))) + "";
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            DatabaseReference senderReference = databaseReference.child(user_id)
                    .child("messages").child(intent.getStringExtra("receiver_id"));
            DatabaseReference receiverReference = databaseReference.child(intent.getStringExtra("receiver_id"))
                    .child("messages").child(user_id);
            senderReference.child("message" + count).setValue("outgoing " + message + " " + time);
            receiverReference.child("message" + count).setValue("incoming " + message + " " + time);
            senderReference.child("count").setValue(count + "");
            receiverReference.child("count").setValue(count + "");
        }
    }
}
