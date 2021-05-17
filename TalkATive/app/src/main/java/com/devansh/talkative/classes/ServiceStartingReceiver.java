package com.devansh.talkative.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServiceStartingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            FileInputStream fis = context.openFileInput("UserId.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            Intent serviceIntent = new Intent(context,ChatNotificationService.class);
            serviceIntent.putExtra("user_id",br.readLine());
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) context.startForegroundService(serviceIntent);
            else context.startService(serviceIntent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}