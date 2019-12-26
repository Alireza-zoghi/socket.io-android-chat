package com.github.nkzawa.socketio.androidchat.util;

import android.app.Application;
import android.content.Context;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {
    private static Context context;
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(app.main.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public Socket getSocket() {
        return mSocket;
    }

    public static Context getContext() {
        return context;
    }
}
