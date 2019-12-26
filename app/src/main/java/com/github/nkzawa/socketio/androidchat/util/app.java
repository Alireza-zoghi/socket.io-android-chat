package com.github.nkzawa.socketio.androidchat.util;

import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class app {

    public static class main {
        private static final String TAG = "SocketChatAppTag";
        public static final String CHAT_SERVER_URL = "https://socket-io-chat.now.sh/";

    }

    public static void l(String massage) {
        Log.e(main.TAG, "" + massage);
    }

    public static void t(String massage) {
        Toast.makeText(ChatApplication.getContext(), "" + massage, Toast.LENGTH_LONG).show();
    }

    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}
