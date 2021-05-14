package com.devansh.talkative.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devansh.talkative.R;
import com.devansh.talkative.activities.ChatActivity;
import com.devansh.talkative.classes.ChatData;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ChatData[] chatData;
    private Context context;
    private String userId;

    public ChatAdapter(ChatData[] chatData, String userId) {
        this.chatData = chatData;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_chat_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Picasso.with(context).load(chatData[position].getImage()).into(holder.imageView);
        holder.name.setText(chatData[position].getName());
        String message = chatData[position].getMessage();
        if(!message.startsWith("outgoing")) holder.itemView.findViewById(R.id.tick).setVisibility(View.GONE);
        message = message.substring(message.indexOf(' ')+1);
        holder.message.setText(message.substring(0,message.lastIndexOf(' ')).replace('\n',' '));
        message = message.substring(message.lastIndexOf(' ')+1);
        Calendar calendar = Calendar.getInstance();
        String time = "";
        calendar.setTimeInMillis(Long.parseLong(message));
        Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTimeInMillis(System.currentTimeMillis());
        if(calendar.get(Calendar.DAY_OF_YEAR)==actualCalendar.get(Calendar.DAY_OF_YEAR)&&calendar.get(Calendar.YEAR)==actualCalendar.get(Calendar.YEAR)){
            if(calendar.get(Calendar.HOUR_OF_DAY)<10) time = time+"0";
            time = time + calendar.get(Calendar.HOUR_OF_DAY)+":";
            if(calendar.get(Calendar.MINUTE)<10) time = time+"0";
            time = time + calendar.get(Calendar.MINUTE);
        }
        else{
            if(calendar.get(Calendar.DAY_OF_MONTH)<10) time = time+"0";
            time = time + calendar.get(Calendar.DAY_OF_MONTH)+"/";
            if(calendar.get(Calendar.MONTH)<9) time = time+"0";
            time = time + (calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
        }
        holder.time.setText(time);
        holder.itemView.findViewById(R.id.linear_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("sender_id",userId);
                intent.putExtra("receiver_id",chatData[position].getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatData.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,message,time;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            imageView = itemView.findViewById(R.id.profile_image);
            time = itemView.findViewById(R.id.time);
        }
    }
}
