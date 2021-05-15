package com.devansh.talkative.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.devansh.talkative.R;
import com.devansh.talkative.adapters.UserAdapter;
import com.devansh.talkative.classes.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SearchUserActivity extends AppCompatActivity {
    private UserData[] userData;
    private RecyclerView recyclerView;
    private EditText searchText;
    private boolean isListLoaded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_TalkATive);
        setContentView(R.layout.activity_search_user);
        searchText = findViewById(R.id.search_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                String query = charSequence.toString().toLowerCase().trim();
                int i,j=0;
                if(userData==null) return;
                for(i=0;i< userData.length;i++){
                    if(userData[i].getName().toLowerCase().trim().contains(query)
                            || userData[i].getEmail().toLowerCase().trim().contains(query)) j++;
                }
                UserData[] tempData = new UserData[j];
                j=0;
                for(i=0;i<userData.length;i++){
                    if(userData[i].getName().toLowerCase().trim().contains(query)
                            || userData[i].getEmail().toLowerCase().trim().contains(query)) {
                        tempData[j] = userData[i];
                        j++;
                    }
                }
                recyclerView.setAdapter(new UserAdapter(tempData,getIntent().getStringExtra("user_id")));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        isListLoaded = false;
        recyclerView = findViewById(R.id.all_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        loadList();
    }

    private void loadList() {
        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isListLoaded) return;
                userData = new UserData[(int) snapshot.getChildrenCount()-1];
                int i=0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals(getIntent().getStringExtra("user_id"))) {
                        userData[i] = new UserData(dataSnapshot.child("info").child("name").getValue().toString(),
                                dataSnapshot.child("info").child("email").getValue().toString(),
                                dataSnapshot.child("info").child("image").getValue().toString(),
                                dataSnapshot.getKey());
                        i++;
                    }
                }
                recyclerView.setAdapter(new UserAdapter(userData,getIntent().getStringExtra("user_id")));
                isListLoaded = true;
                findViewById(R.id.progress).setVisibility(View.GONE);
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
                FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("info").child("status").setValue(time+"");
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
            FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("user_id")).child("info").child("status").setValue("Online");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}