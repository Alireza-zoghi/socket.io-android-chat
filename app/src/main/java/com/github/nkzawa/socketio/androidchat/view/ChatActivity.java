package com.github.nkzawa.socketio.androidchat.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.androidchat.model.Message;
import com.github.nkzawa.socketio.androidchat.util.ChatApplication;
import com.github.nkzawa.socketio.androidchat.util.app;
import com.github.nkzawa.socketio.androidchat.view.LoginActivity;
import com.github.nkzawa.socketio.androidchat.view.adapter.MessageAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_LOGIN = 0;
    private static final int TYPING_TIMER_LENGTH = 1000;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private TextView isTyping;

    private MessageAdapter mAdapter;

    private Handler mTypingHandler = new Handler();

    private Socket mSocket;

    private List<Message> mMessages = new ArrayList<>();
    private boolean mTyping = false;
    private String mUsername;
    private int userCount;
    private Boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        onSocketConnect();
        startSignIn();
        init();
    }

    private void init() {
        mMessagesView = findViewById(R.id.recyclerView);
        mMessagesView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MessageAdapter(mMessages);
        mMessagesView.setAdapter(mAdapter);

        isTyping = findViewById(R.id.tv_isTyping);

        ImageButton leaveButton = findViewById(R.id.lyt_back);
        leaveButton.setOnClickListener(view1 -> exitDialog());

        mInputMessageView = findViewById(R.id.text_content);
        mInputMessageView.setOnEditorActionListener((v, id, event) -> {
            if (id == R.id.btn_send || id == EditorInfo.IME_NULL) {
                attemptSend();
                return true;
            }
            return false;
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageView sendButton = findViewById(R.id.btn_send);
        sendButton.setOnClickListener(v -> attemptSend());
    }

    private void onSocketConnect() {
        ChatApplication application = (ChatApplication) getApplication();
        mSocket = application.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("If you close all your progress would not be saved... Do you wish to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> leave())
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        userCount = numUsers;
        isTyping.setText(userCount + " online");
    }

    private void addMessage(String username, String message) {
        if (username.equals(mUsername)) {
            mMessages.add(new Message.Builder(Message.TYPE_My_MESSAGE).username(username).message(message).build());
        } else {
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE).username(username).message(message).build());
        }
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        isTyping.setText(username + " is typing");
        scrollToBottom();
    }

    private void removeTyping() {
        isTyping.setText(userCount + " online");
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        // perform the sending message attempt.
        mSocket.emit("new message", message);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
        mSocket.connect();
        startSignIn();
    }

    private void startSignIn() {
        mUsername = null;
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            finish();
            return;
        }

        mUsername = data.getStringExtra("username");
        int numUsers = data.getIntExtra("numUsers", 1);

        addLog(getResources().getString(R.string.message_welcome) + " " + mUsername);
        addParticipantsLog(numUsers);
    }


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                if (!isConnected) {
                    if (null != mUsername)
                        mSocket.emit("add user", mUsername);
                    app.t("Connected");
                    isConnected = true;
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {
        isConnected = false;
        app.t("Disconnected, Please check your internet connection");
    });

    private Emitter.Listener onConnectError = args -> runOnUiThread(() -> {
        app.t("Failed to connect");
    });

    private Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        String message;
        try {
            username = data.getString("username");
            message = data.getString("message");
        } catch (JSONException e) {
            app.l(e.getMessage());
            return;
        }

        removeTyping();
        addMessage(username, message);
    });

    private Emitter.Listener onUserJoined = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        int numUsers;
        try {
            username = data.getString("username");
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            app.l(e.getMessage());
            return;
        }

        addLog(getResources().getString(R.string.message_user_joined, username));
        addParticipantsLog(numUsers);
    });

    private Emitter.Listener onUserLeft = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        int numUsers;
        try {
            username = data.getString("username");
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            app.l(e.getMessage());
            return;
        }

        addLog(getResources().getString(R.string.message_user_left, username));
        addParticipantsLog(numUsers);
        removeTyping();
    });

    private Emitter.Listener onTyping = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        try {
            username = data.getString("username");
        } catch (JSONException e) {
            app.l(e.getMessage());
            return;
        }
        addTyping(username);
    });

    private Emitter.Listener onStopTyping = args -> runOnUiThread(() -> removeTyping());

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
    }
}