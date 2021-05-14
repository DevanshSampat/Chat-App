package com.devansh.talkative.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devansh.talkative.R;
import com.devansh.talkative.activities.ChatActivity;
import com.devansh.talkative.classes.UserData;
import com.squareup.picasso.Picasso;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private UserData[] userData;
    private Context context;
    private String sender_uid;
    public UserAdapter(UserData[] userData,String sender_uid) {
        this.userData = userData;
        this.sender_uid = sender_uid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_item,parent,false));
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(userData[position].getName());
        holder.email.setText(userData[position].getEmail());
        Picasso.with(context).load(userData[position].getImage()).into(holder.imageView);
        holder.itemView.findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("sender_id",sender_uid);
                intent.putExtra("receiver_id",userData[position].getUid());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userData.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView email,name;
        private ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.email);
            name = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.profile_image);
        }
    }
}
