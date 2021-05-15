package com.devansh.talkative.adapters;

import android.annotation.SuppressLint;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.recyclerview.widget.RecyclerView;

import com.devansh.talkative.R;
import com.devansh.talkative.classes.MessageData;

import java.util.Calendar;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private MessageData[] messageData;
    public MessageAdapter(MessageData[] messageData){this.messageData = messageData;}
    String[] month = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_message_item,parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String message = messageData[position].getMessage();
        holder.date.setVisibility(View.GONE);
        holder.sent_layout.setVisibility(View.GONE);
        holder.received_layout.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.seen).setVisibility(View.GONE);
        if(messageData[position].isDateChanged()){
            long time = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            holder.date.setText(month[calendar.get(Calendar.MONTH)]+" "+calendar.get(Calendar.DAY_OF_MONTH)+", "+calendar.get(Calendar.YEAR));
            holder.date.setVisibility(View.VISIBLE);
        }
        if(message.startsWith("incoming")){
            long time = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            message = message.substring(message.indexOf(' ')+1);
            message = message.substring(0,message.lastIndexOf(' '));
            while (message.startsWith(" ")||message.startsWith("\n")) message = message.substring(1);
            while (message.endsWith(" ")||message.endsWith("\n")) message = message.substring(0,message.length()-1);
            holder.received_message.setText(message);
            holder.received_message.setMovementMethod(LinkMovementMethod.getInstance());
            String timeString = "";
            if(calendar.get(Calendar.HOUR_OF_DAY)<10) timeString = timeString + "0";
            timeString = timeString + calendar.get(Calendar.HOUR_OF_DAY) + ":";
            if(calendar.get(Calendar.MINUTE)<10) timeString = timeString + "0";
            timeString = timeString + calendar.get(Calendar.MINUTE);
            holder.received_time.setText(timeString);
            holder.received_layout.setVisibility(View.VISIBLE);
        }
        else {
            long time = Long.parseLong(message.substring(message.lastIndexOf(' ')+1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            message = message.substring(message.indexOf(' ')+1);
            message = message.substring(0,message.lastIndexOf(' '));
            while (message.startsWith(" ")||message.startsWith("\n")) message = message.substring(1);
            while (message.endsWith(" ")||message.endsWith("\n")) message = message.substring(0,message.length()-1);
            holder.sent_message.setText(message);
            holder.sent_message.setMovementMethod(LinkMovementMethod.getInstance());
            String timeString = "";
            if(calendar.get(Calendar.HOUR_OF_DAY)<10) timeString = timeString + "0";
            timeString = timeString + calendar.get(Calendar.HOUR_OF_DAY) + ":";
            if(calendar.get(Calendar.MINUTE)<10) timeString = timeString + "0";
            timeString = timeString + calendar.get(Calendar.MINUTE);
            holder.sent_time.setText(timeString);
            if(position== messageData.length-1&&messageData[position].isSeen()) holder.itemView.findViewById(R.id.seen).setVisibility(View.VISIBLE);
            holder.sent_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageData.length;
    }

    public void setMessageData(MessageData[] messageData){
        this.messageData = messageData;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView date,sent_message,received_message,sent_time,received_time;
        private LinearLayout sent_layout,received_layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            sent_message = itemView.findViewById(R.id.sent_message);
            received_message = itemView.findViewById(R.id.received_message);
            sent_layout = itemView.findViewById(R.id.sent_layout);
            received_layout = itemView.findViewById(R.id.received_layout);
            sent_time = itemView.findViewById(R.id.sent_time);
            received_time = itemView.findViewById(R.id.received_time);
        }
    }
}
