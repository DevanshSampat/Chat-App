package com.devansh.talkative.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devansh.talkative.R;
import com.devansh.talkative.adapters.MessageAdapter;
import com.devansh.talkative.classes.CurrentUserName;
import com.devansh.talkative.classes.MessageData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference senderReference, receiverReference;
    private String sender_id, receiver_id;
    private EditText message;
    private String name,image;
    private String typingStatus;
    private MessageData[] messageData;
    private String lastActiveStatus;
    private boolean send;
    private boolean toBeSend;
    private boolean pause;
    private int message_count;
    private String receiverName;
    private RecyclerView recyclerView;
    private boolean detailsLoaded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        send = false;
        toBeSend = false;
        pause = false;
        detailsLoaded = false;
        message_count = 0;
        sender_id = getIntent().getStringExtra("sender_id");
        receiver_id = getIntent().getStringExtra("receiver_id");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        senderReference = reference.child(sender_id);
        receiverReference = reference.child(receiver_id);
        recyclerView = findViewById(R.id.all_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        senderReference.child("messages").child(receiver_id).child("last_active").setValue("online");
        receiverReference.child("messages").child(sender_id).child("last_active_other").setValue("online");
        senderReference.child("messages").child(receiver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count;
                try{
                    count = Integer.parseInt(snapshot.child("count").getValue().toString());
                } catch (Exception e) {
                    count=0;
                    message_count = 0;
                    e.printStackTrace();
                }
                if(count!=message_count) {
                    message_count = count;
                    int i;
                    String[] message = new String[count];
                    for (i = 1; i <= count; i++) {
                        message[i - 1] = snapshot.child("message" + i).getValue().toString();
                    }
                    messageData = new MessageData[count];
                    if (count > 0) messageData[0] = new MessageData(message[0], true);
                    for (i = 1; i < count; i++) {
                        long t1 = Long.parseLong(message[i - 1].substring(message[i - 1].lastIndexOf(' ') + 1));
                        long t2 = Long.parseLong(message[i].substring(message[i].lastIndexOf(' ') + 1));
                        Calendar c1 = Calendar.getInstance();
                        Calendar c2 = Calendar.getInstance();
                        c1.setTimeInMillis(t1);
                        c2.setTimeInMillis(t2);
                        messageData[i] = new MessageData(message[i], !(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)));
                    }
                    recyclerView.setAdapter(new MessageAdapter(messageData));
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        message = findViewById(R.id.message);
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ((EditText)findViewById(R.id.message)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()>0) {
                    senderReference.child("messages").child(receiver_id).child("status").setValue("typing");
                    receiverReference.child("messages").child(sender_id).child("status_other").setValue("typing");
                }
                else {
                    senderReference.child("messages").child(receiver_id).child("status").setValue("idle");
                    receiverReference.child("messages").child(sender_id).child("status_other").setValue("idle");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        receiverReference.child("info").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverName = snapshot.child("name").getValue().toString();
                if(!detailsLoaded) CurrentUserName.setName(receiverName);
                detailsLoaded = true;
                senderReference.child("messages").child(receiver_id).child("name").setValue(snapshot.child("name").getValue().toString());
                senderReference.child("messages").child(receiver_id).child("image").setValue(snapshot.child("image").getValue().toString());
                Picasso.with(ChatActivity.this).load(snapshot.child("image").getValue().toString()).into((ImageView)findViewById(R.id.profile_image));
                ((TextView)findViewById(R.id.receiver_name)).setText(snapshot.child("name").getValue().toString());
                typingStatus = "Online";
                String status = snapshot.child("status").getValue().toString();
                String[] month = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                if(status.toLowerCase().equals("online")) ((TextView)findViewById(R.id.status)).setText("Online");
                else{
                    String lastSeen = "Last seen ";
                    Calendar lastSeenCalendar = Calendar.getInstance();
                    lastSeenCalendar.setTimeInMillis(Long.parseLong(status));
                    Calendar actualCalendar = Calendar.getInstance();
                    actualCalendar.setTimeInMillis(System.currentTimeMillis());
                    if(actualCalendar.get(Calendar.YEAR)==lastSeenCalendar.get(Calendar.YEAR)
                            && actualCalendar.get(Calendar.DAY_OF_YEAR)==lastSeenCalendar.get(Calendar.DAY_OF_YEAR)){
                        lastSeen = lastSeen + "today";
                    }
                    else if(actualCalendar.get(Calendar.YEAR)==lastSeenCalendar.get(Calendar.YEAR)
                            && actualCalendar.get(Calendar.DAY_OF_YEAR)==lastSeenCalendar.get(Calendar.DAY_OF_YEAR)+1){
                        lastSeen = lastSeen + "yesterday";
                    }
                    else{
                        lastSeen = lastSeen + month[lastSeenCalendar.get(Calendar.MONTH)] + " " + lastSeenCalendar.get(Calendar.DAY_OF_MONTH);
                        if(actualCalendar.get(Calendar.YEAR)!=lastSeenCalendar.get(Calendar.YEAR)) lastSeen = lastSeen + "," + lastSeenCalendar.get(Calendar.YEAR);
                    }
                    lastSeen = lastSeen + " at ";
                    if(lastSeenCalendar.get(Calendar.HOUR_OF_DAY)<10) lastSeen = lastSeen + "0";
                    lastSeen = lastSeen + lastSeenCalendar.get(Calendar.HOUR_OF_DAY) + ":";
                    if(lastSeenCalendar.get(Calendar.MINUTE)<10) lastSeen = lastSeen + "0";
                    lastSeen = lastSeen + lastSeenCalendar.get(Calendar.MINUTE);
                    typingStatus = lastSeen;
                    ((TextView)findViewById(R.id.status)).setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        receiverReference.child("messages").child(sender_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    try {
                        if (!messageData[messageData.length - 1].isSeen()&&messageData[messageData.length-1].getMessage().startsWith("outgoing")){
                            String message = messageData[messageData.length-1].getMessage();
                            Long time = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
                            if(snapshot.child("last_active").getValue().toString().equals("online")) messageData[messageData.length-1].setSeen(true);
                            else if(time<Long.parseLong(snapshot.child("last_active").getValue().toString())) messageData[messageData.length-1].setSeen(true);
                            ((MessageAdapter)recyclerView.getAdapter()).setMessageData(messageData);
                        }
                        lastActiveStatus = snapshot.child("last_active").getValue().toString();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    if(snapshot.child("status").getValue().toString().equals("typing")) ((TextView)findViewById(R.id.status)).setText("Typing...");
                    else ((TextView)findViewById(R.id.status)).setText(typingStatus);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        senderReference.child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverReference.child("messages").child(sender_id).child("name").setValue(snapshot.child("name").getValue().toString());
                receiverReference.child("messages").child(sender_id).child("image").setValue(snapshot.child("image").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        pause = true;
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!pause) return;
                pause = false;
                long time = System.currentTimeMillis() + Long.parseLong(snapshot.getValue().toString());
                FirebaseDatabase.getInstance().getReference("users").child(sender_id).child("info").child("status").setValue(time+"");
                senderReference.child("messages").child(receiver_id).child("last_active").setValue(time+"");
                receiverReference.child("messages").child(sender_id).child("last_active_other").setValue(time+"");
                senderReference.child("messages").child(receiver_id).child("status").setValue("idle");
                receiverReference.child("messages").child(sender_id).child("status_other").setValue("idle");
                CurrentUserName.setName(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            FirebaseDatabase.getInstance().getReference("users").child(sender_id).child("info").child("status").setValue("Online");
            senderReference.child("messages").child(receiver_id).child("last_active").setValue("online");
            receiverReference.child("messages").child(sender_id).child("last_active_other").setValue("online");
            CurrentUserName.setName(receiverName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void sendMessage(View view) {
        send = true;
        toBeSend = true;
        senderReference.child("messages").child(receiver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!send) return;
                send = false;
                String message = ((EditText)findViewById(R.id.message)).getText().toString();
                ((EditText)findViewById(R.id.message)).setText("");
                if(message.trim().length()==0) return;
                int count;
                try{
                    count = Integer.parseInt(snapshot.child("count").getValue().toString());
                } catch (Exception e) {
                    count = 0;
                    e.printStackTrace();
                }
                int finalCount = count;
                FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!toBeSend) return;
                        toBeSend = false;
                        int count = finalCount + 1;
                        senderReference.child("messages").child(receiver_id).child("message"+ count).setValue("outgoing "+message+ " "+
                                (System.currentTimeMillis()+Long.parseLong(snapshot.getValue().toString()))+"");
                        senderReference.child("messages").child(receiver_id).child("count").setValue((finalCount+1)+"");
                        receiverReference.child("messages").child(sender_id).child("message"+ count).setValue("incoming "+message+ " "+
                                (System.currentTimeMillis()+Long.parseLong(snapshot.getValue().toString()))+"");
                        receiverReference.child("messages").child(sender_id).child("count").setValue((finalCount+1)+"");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}