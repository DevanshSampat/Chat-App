package com.devansh.talkative.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devansh.talkative.R;
import com.devansh.talkative.adapters.MessageAdapter;
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
    private boolean send;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        send = false;
        sender_id = getIntent().getStringExtra("sender_id");
        receiver_id = getIntent().getStringExtra("receiver_id");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        senderReference = reference.child(sender_id);
        receiverReference = reference.child(receiver_id);
        recyclerView = findViewById(R.id.all_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        senderReference.child("messages").child(receiver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count;
                try{
                    count = Integer.parseInt(snapshot.child("count").getValue().toString());
                } catch (Exception e) {
                    count=0;
                    e.printStackTrace();
                }
                int i;
                String[] message = new String[count];
                for(i=1;i<=count;i++){
                    message[i-1] = snapshot.child("message"+i).getValue().toString();
                }
                MessageData[] messageData = new MessageData[count];
                if(count>0) messageData[0] = new MessageData(message[0],true);
                for(i=1;i<count;i++){
                    long t1 = Long.parseLong(message[i-1].substring(message[i-1].lastIndexOf(' ')+1));
                    long t2 = Long.parseLong(message[i].substring(message[i].lastIndexOf(' ')+1));
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();
                    c1.setTimeInMillis(t1);
                    c2.setTimeInMillis(t2);
                    messageData[i] = new MessageData(message[i],!(c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR)
                            && c1.get(Calendar.DAY_OF_YEAR)==c2.get(Calendar.DAY_OF_YEAR)));
                }
                recyclerView.setAdapter(new MessageAdapter(messageData));
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);
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
        receiverReference.child("info").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderReference.child("messages").child(receiver_id).child("name").setValue(snapshot.child("name").getValue().toString());
                senderReference.child("messages").child(receiver_id).child("image").setValue(snapshot.child("image").getValue().toString());
                Picasso.with(ChatActivity.this).load(snapshot.child("image").getValue().toString()).into((ImageView)findViewById(R.id.profile_image));
                ((TextView)findViewById(R.id.receiver_name)).setText(snapshot.child("name").getValue().toString());
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
                    else{
                        lastSeen = lastSeen + " " + month[lastSeenCalendar.get(Calendar.MONTH)] + " " + lastSeenCalendar.get(Calendar.DAY_OF_MONTH);
                        if(actualCalendar.get(Calendar.YEAR)!=lastSeenCalendar.get(Calendar.YEAR)) lastSeen = lastSeen + "," + lastSeenCalendar.get(Calendar.YEAR);
                        lastSeen = lastSeen + " ";
                    }
                    lastSeen = lastSeen + " at ";
                    if(lastSeenCalendar.get(Calendar.HOUR_OF_DAY)<10) lastSeen = lastSeen + "0";
                    lastSeen = lastSeen + lastSeenCalendar.get(Calendar.HOUR_OF_DAY) + ":";
                    if(lastSeenCalendar.get(Calendar.MINUTE)<10) lastSeen = lastSeen + "0";
                    lastSeen = lastSeen + lastSeenCalendar.get(Calendar.MINUTE);
                    ((TextView)findViewById(R.id.status)).setText(lastSeen);
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
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long time = System.currentTimeMillis() + Long.parseLong(snapshot.getValue().toString());
                FirebaseDatabase.getInstance().getReference("users").child(sender_id).child("info").child("status").setValue(time+"");
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
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void sendMessage(View view) {
        send = true;
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