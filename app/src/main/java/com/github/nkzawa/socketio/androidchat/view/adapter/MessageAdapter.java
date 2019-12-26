package com.github.nkzawa.socketio.androidchat.view.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.androidchat.model.Message;
import com.github.nkzawa.socketio.androidchat.util.app;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> mMessages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(List<Message> messages) {
        mMessages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == Message.TYPE_MESSAGE || viewType == Message.TYPE_My_MESSAGE) {
            return new MessageViewHolder(layoutInflater.inflate(R.layout.item_chat, parent, false));
        } else {
            return new LogViewHolder(layoutInflater.inflate(R.layout.item_log, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        if (holder instanceof LogViewHolder) {
            ((LogViewHolder) holder).bindLog(message.getMessage());
        } else if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages != null ? mMessages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView otherChatUsernameView;
        private TextView otherChatMessageView;

        private TextView myChatMessageView;

        private TextView myTextViewTime;
        private TextView otherTextViewTime;

        private LinearLayout myChat, otherChat;

        private MessageViewHolder(View itemView) {
            super(itemView);
            otherChatUsernameView = itemView.findViewById(R.id.tv_username);
            otherChatMessageView = itemView.findViewById(R.id.text_content);

            myChatMessageView = itemView.findViewById(R.id.text_my_content);

            myChat = itemView.findViewById(R.id.layout_my_chat);
            otherChat = itemView.findViewById(R.id.layout_other_chat);

            myTextViewTime = itemView.findViewById(R.id.text_my_time);
            otherTextViewTime = itemView.findViewById(R.id.text_time);
        }

        private void bind(Message message) {
            if (message.getType() == Message.TYPE_My_MESSAGE) {
                myChat.setVisibility(View.VISIBLE);
                otherChat.setVisibility(View.GONE);
                myTextViewTime.setText(app.getTime());
                myChatMessageView.setText(message.getMessage());
            } else {
                myChat.setVisibility(View.GONE);
                otherChat.setVisibility(View.VISIBLE);
                otherChatUsernameView.setText(message.getUsername());
                otherChatMessageView.setText(message.getMessage());
                otherTextViewTime.setText(app.getTime());
            }
        }
    }

    private class LogViewHolder extends RecyclerView.ViewHolder {
        private TextView logTextView;

        private LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTextView = itemView.findViewById(R.id.message);
        }

        private void bindLog(String text) {
            logTextView.setText(text);
        }
    }
}
