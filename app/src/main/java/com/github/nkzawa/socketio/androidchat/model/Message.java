package com.github.nkzawa.socketio.androidchat.model;

public class Message {

    public static final int TYPE_My_MESSAGE = 0;
    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_LOG = 2;

    private int mType;
    private String mMessage;
    private String mUsername;

    public int getType() {
        return mType;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getUsername() {
        return mUsername;
    }


    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            return message;
        }
    }
}
