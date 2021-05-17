package com.devansh.talkative.classes;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.devansh.talkative.BuildConfig;
import com.devansh.talkative.R;
import com.devansh.talkative.activities.ChatActivity;
import com.devansh.talkative.activities.DashboardActivity;
import com.devansh.talkative.activities.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatNotificationService extends Service {
    ArrayList<String> list= new ArrayList<>();
    HashMap<String,String> names_id = new HashMap<>();
    String[][] array = new String[2][1];
    private long offset;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intentForMain = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            intentForMain = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            intentForMain.putExtra(Settings.EXTRA_APP_PACKAGE,BuildConfig.APPLICATION_ID);
            intentForMain.putExtra(Settings.EXTRA_CHANNEL_ID,"service");
            NotificationCompat.Builder serviceBuilder = new NotificationCompat.Builder(getApplicationContext(), "service")
                    .setAutoCancel(true)
                    .setColor(Color.BLUE)
                    .setContentTitle("Service is running")
                    .setContentText("Tap to disable this notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intentForMain, PendingIntent.FLAG_UPDATE_CURRENT));
                super.startForeground(540, serviceBuilder.build());
        }
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                offset = Long.parseLong(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try{
            if(intent.getStringExtra("user_id")==null) return START_STICKY;
        } catch (Exception exception) {
            return START_STICKY;
        }
        FirebaseDatabase.getInstance().getReference("users").child(intent.getStringExtra("user_id")).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String str = "";
                try{
                    FileInputStream fis = getApplicationContext().openFileInput("UserId.txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    if(!br.readLine().equals(intent.getStringExtra("user_id"))) return;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                list.clear();
                names_id.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    try{
                        int count = Integer.parseInt(dataSnapshot.child("count").getValue().toString());
                        String message = dataSnapshot.child("message"+count).getValue().toString();
                        if(message.startsWith("incoming")){
                            long t1 = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
                            long t2 = Long.parseLong(dataSnapshot.child("last_active").getValue().toString());
                            if(t1>t2){
                                try{
                                    names_id.put(dataSnapshot.child("name").getValue().toString(),dataSnapshot.getKey());
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                            while(t1>t2&&count>0){
                                str = dataSnapshot.child("name").getValue().toString() + " : " + message.substring(message.indexOf(' ')+1);
                                list.add(str);
                                count--;
                                message = dataSnapshot.child("message"+count).getValue().toString();
                                t1 = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
                                t2 = Long.parseLong(dataSnapshot.child("last_active").getValue().toString());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(list.size()==0) NotificationManagerCompat.from(getApplicationContext()).cancel(360);
                if(list.size()>0){
                    int i,j;
                    boolean isSame = true;
                    if(array[0].length==list.size()) {
                        try {
                            for (i = 0; i < list.size(); i++) {
                                if (!isSame || !list.get(i).equals(array[0][i])) isSame = false;
                            }
                            if (isSame) return;
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                    NotificationManagerCompat.from(getApplicationContext()).cancel(360);
                    array[0] = new String[list.size()];
                    array[1] = new String[list.size()];
                    for(i=0;i<list.size();i++)
                    {
                        array[0][i] = list.get(i);
                        array[1][i] = list.get(i);
                    }
                    for(i=0; i< array[1].length-1; i++){
                        for(j=0; j< array[1].length-1-i; j++){
                            Long t1 = Long.parseLong(array[1][j].substring(array[1][j].lastIndexOf(' ')+1));
                            Long t2 = Long.parseLong(array[1][j+1].substring(array[1][j+1].lastIndexOf(' ')+1));
                            if(t1>t2){
                                String temp = array[1][j];
                                array[1][j] = array[1][j+1];
                                array[1][j+1] = temp;
                            }
                        }
                    }
                    str = "";
                    ArrayList<String> names = new ArrayList<>();
                    for(i=0;i<array[1].length;i++){
                        if(!names.contains(array[1][i].substring(0,array[1][i].indexOf(':')-1))) names.add(array[1][i].substring(0,array[1][i].indexOf(':')-1));
                    }
                    String[][] sortedString = new String[names.size()][1];
                    for(i=0;i<names.size();i++){
                        int count = 1;
                        for(j=0;j<array[1].length;j++){
                            if(names.get(i).equals(array[1][j].substring(0,array[1][j].indexOf(':')-1))) count++;
                        }
                        sortedString[i] = new String[count];
                        count = 0;
                        for(j=0;j<array[1].length;j++){
                            if(names.get(i).equals(array[1][j].substring(0,array[1][j].indexOf(':')-1))){
                                sortedString[i][count] = array[1][j];
                                sortedString[i][sortedString[i].length-1] = array[1][j].substring(array[1][j].lastIndexOf(' ')+1);
                                count++;
                            }
                        }
                    }
                    for(i=0;i<names.size()-1;i++){
                        for(j=0;j<names.size()-1-i;j++) {
                            Long t1 = Long.parseLong(sortedString[j][sortedString[j].length-1]);
                            Long t2 = Long.parseLong(sortedString[j+1][sortedString[j+1].length-1]);
                            if(t1<t2){
                                String[] temp = sortedString[j];
                                sortedString[j] = sortedString[j+1];
                                sortedString[j+1] = temp;
                            }
                        }
                    }
                    int id = 1;
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    for(i=names.size()-1;i>=0;i--) {
                        String title = sortedString[i][0].substring(0,sortedString[i][0].indexOf(':')-1);
                        str = "";
                        for(j=0;j<sortedString[i].length-1;j++) {
                            String temp = sortedString[i][j];
                            temp = temp.substring(temp.indexOf(':')+2);
                            temp = temp.substring(0,temp.lastIndexOf(' '));
                            str = str + temp.replace('\n', ' ') + "\n";
                        }
                        RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply").setLabel("Reply to "+title).build();
                        Intent replyIntent = new Intent(getApplicationContext(),DirectReplyReceiver.class);
                        replyIntent.putExtra("sender_id",intent.getStringExtra("user_id"));
                        replyIntent.putExtra("receiver_id",names_id.get(title));
                        replyIntent.putExtra("count",snapshot.child(names_id.get(title)).child("count").getValue().toString());
                        replyIntent.putExtra("notification_id",id+"");
                        replyIntent.putExtra("offset",offset+"");
                        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),65536-id,replyIntent,PendingIntent.FLAG_ONE_SHOT);
                        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,"Reply",replyPendingIntent)
                                .addRemoteInput(remoteInput).build();
                        Intent markReadIntent = new Intent(getApplicationContext(),MarkReadReceiver.class);
                        markReadIntent.putExtra("sender_id",intent.getStringExtra("user_id"));
                        markReadIntent.putExtra("receiver_id",names_id.get(title));
                        markReadIntent.putExtra("offset",offset+"");
                        markReadIntent.putExtra("notification_id",id+"");
                        PendingIntent pendingIntentRead = PendingIntent.getBroadcast(getApplicationContext(),32768-id,markReadIntent,0);
                        NotificationCompat.Action markReadAction = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,"Mark as read",pendingIntentRead).build();
                        Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                        chatIntent.putExtra("sender_id",intent.getStringExtra("user_id"));
                        chatIntent.putExtra("receiver_id",names_id.get(title));
                        PendingIntent pendingIntentForChat = PendingIntent.getActivity(getApplicationContext(),id,chatIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                        String lastMessage = sortedString[i][sortedString[i].length-2];
                        lastMessage = lastMessage.substring(lastMessage.indexOf(':')+2,lastMessage.lastIndexOf(' '));
                        lastMessage = lastMessage.replace('\n',' ');
                        NotificationCompat.Builder singleChatNotification = new NotificationCompat.Builder(getApplicationContext(),"general")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setContentTitle(title)
                                .setContentText(lastMessage)
                                .setWhen(Long.parseLong(sortedString[i][sortedString[i].length-1]))
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(str.substring(0,str.length()-1)))
                                .setColor(Color.BLUE)
                                .setGroup("chat_notifications")
                                .addAction(replyAction)
                                .addAction(markReadAction)
                                .setContentIntent(pendingIntentForChat);
                        NotificationManagerCompat.from(getApplicationContext()).notify(id,singleChatNotification.build());
                        inboxStyle.addLine(title+" "+str.substring(0,str.indexOf('\n')));
                        showNotification(id,names_id.get(title),singleChatNotification);
                        id++;
                    }
                    String messagesFrom = " messages from";
                    if(list.size()==1) messagesFrom = " message from";
                    String chats = " chats";
                    if(id==2) chats = " chat";
                    inboxStyle.setBigContentTitle("Talk-A-Tive");
                    inboxStyle.setSummaryText((list.size()+messagesFrom+(id-1)+chats));
                    str = str.substring(0,str.length()-1);
                    i = array[1].length -1;
                    String temp = array[1][i].substring(0, array[1][i].lastIndexOf(' ')).replace('\n',' ');
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"general")
                            .setAutoCancel(true)
                            .setColor(Color.BLUE)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setStyle(inboxStyle)
                            .setGroup("chat_notifications")
                            .setGroupSummary(true)
                            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(str))
                            .setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,
                                    new Intent(getApplicationContext(), DashboardActivity.class).putExtra("user_id",intent.getStringExtra("user_id")),
                                    PendingIntent.FLAG_UPDATE_CURRENT));
                    try{
                        if(CurrentUserName.getName().equals(temp.substring(0,temp.indexOf(':')-1))) return;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    NotificationManagerCompat.from(getApplicationContext()).notify(360,builder.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return START_STICKY;
    }

    private void showNotification(int id, String field, NotificationCompat.Builder singleChatNotification) {
        FirebaseDatabase.getInstance().getReference("users").child(field).child("info").child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Picasso.with(getApplicationContext()).load(snapshot.getValue().toString()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        singleChatNotification.setLargeIcon(circularBitmap(bitmap));
                        NotificationManagerCompat.from(getApplicationContext()).notify(id,singleChatNotification.build());
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private Bitmap circularBitmap(Bitmap bitmap){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(0xff424242);
        canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getWidth()/2,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        return output;
    }
}
