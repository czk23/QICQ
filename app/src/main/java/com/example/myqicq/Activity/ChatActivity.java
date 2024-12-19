package com.example.myqicq.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Chat;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.SaveListener;

public class ChatActivity extends AppCompatActivity {
    // UI Components
    private TextView ChatUser_textview;
    private ImageButton ChatBack_image_button;
    private Button Send_button;
    private ImageView GetLocation_imageview;
    private EditText Chat_edittext;
    private RecyclerView Recycler_view;

    // Data
    private List<Chat> chats = new ArrayList<>();
    private User currentUser;
    private String friendName;
    private String friendAvatar;

    // Thread Management
    private Thread updateThread;
    private boolean isThreadRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        initView(); // Initialize UI components
        setEventListeners(); // Set up event handlers
        updateChatList(); // Load initial chats
        startUpdateThread(); // Start background chat updater
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isThreadRunning = false; // Stop the update thread when the activity is destroyed
    }

    /**
     * Initializes the UI components and chat information.
     */
    private void initView() {
        currentUser = MyApplication.getUser();
        friendName = getIntent().getStringExtra("username");
        friendAvatar = getIntent().getStringExtra("avatar");

        ChatUser_textview = findViewById(R.id.ChatUser_textview);
        Chat_edittext = findViewById(R.id.Chat_edittext);
        ChatBack_image_button = findViewById(R.id.ChatBack_image_button);
        Send_button = findViewById(R.id.Send_button);
        GetLocation_imageview = findViewById(R.id.Location_imageview);
        Recycler_view = findViewById(R.id.recyclerViewChat);

        ChatUser_textview.setText(friendName);

        // Set up RecyclerView
        Recycler_view.setLayoutManager(new LinearLayoutManager(this));
        Recycler_view.setAdapter(new ChatAdapter());
    }

    /**
     * Sets event listeners for various UI actions.
     */
    private void setEventListeners() {
        // Handle back button click
        ChatBack_image_button.setOnClickListener(v -> finish());

        // Handle send button click
        Send_button.setOnClickListener(v -> {
            String messageContent = Chat_edittext.getText().toString().trim();

            if (messageContent.isEmpty()) return; // Do nothing if input is empty

            // Create new chat message
            Chat newChat = new Chat(currentUser.getUsername(), friendName, messageContent, chats.size() + 1, false);
            chats.add(newChat);

            // Notify adapter and scroll to the latest message
            Objects.requireNonNull(Recycler_view.getAdapter()).notifyItemInserted(chats.size() - 1);
            Recycler_view.scrollToPosition(chats.size() - 1);

            Chat_edittext.setText(""); // Clear input field

            // Save the message to the server
            newChat.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e != null) {
                        showSnackbar(Send_button, "消息发送失败", R.color.Red);
                    }
                }
            });
        });

        // Handle location sharing button click
        GetLocation_imageview.setOnClickListener(v -> {
            String shareUrl = MyApplication.getShareUrl();

            if (shareUrl.contentEquals("")) {
                Util.showSnackBar("yellow", Send_button, "还未保存当前位置!", this);
            }
            else if (shareUrl == null) {
                Util.showSnackBar("yellow", Send_button, "获取位置失败!", this);
            }
            else {
                Chat_edittext.setText(shareUrl);
            }
        });
    }

    /**
     * Updates the chat list by fetching messages from the server.
     */
    private void updateChatList() {
        int previousSize = chats.size();
        String currentUserName = currentUser.getUsername();

        BmobQuery<Chat> query = new BmobQuery<>();
        query.findObjects(new FindListener<Chat>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void done(List<Chat> result, BmobException e) {
                if (e == null && result != null) {
                    chats.clear();
                    List<BmobObject> updatedChats = new ArrayList<>();

                    for (Chat chat : result) {
                        if (isRelevantChat(chat, currentUserName)) {
                            chats.add(chat);

                            // Mark messages sent to the user as read
                            if (isUnreadChatFromFriend(chat, currentUserName)) {
                                chat.setRead(true);
                                Chat chat_t = new Chat(chat);
                                chat_t.setObjectId(chat.getObjectId());
                                updatedChats.add(chat_t);
                            }
                        }
                    }

                    // Sort chats by order
                    chats.sort(Comparator.comparingInt(Chat::getOrder));

                    // Update RecyclerView if chat count has changed
                    if (chats.size() != previousSize) {
                        Objects.requireNonNull(Recycler_view.getAdapter()).notifyDataSetChanged();
                        Recycler_view.scrollToPosition(chats.size() - 1);
                    }

                    // Batch update read status on the server
                    if (!updatedChats.isEmpty()) {
                        new BmobBatch().updateBatch(updatedChats).doBatch(new QueryListListener<BatchResult>() {
                            @Override
                            public void done(List<BatchResult> results, BmobException e) {
                                if (e != null) {
                                    showSnackbar(ChatUser_textview, "更新读状态失败: " + e.getMessage(), R.color.Red);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Starts a thread that periodically refreshes the chat list.
     */
    private void startUpdateThread() {
        isThreadRunning = true;
        updateThread = new Thread(() -> {
            try {
                while (isThreadRunning) {
                    Thread.sleep(1000);
                    runOnUiThread(this::updateChatList);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        updateThread.start();
    }

    /**
     * Determines if a chat is relevant (belongs to the current conversation).
     */
    private boolean isRelevantChat(Chat chat, String currentUserName) {
        return (currentUserName.equals(chat.getSender()) && friendName.equals(chat.getReceiver())) ||
                (currentUserName.equals(chat.getReceiver()) && friendName.equals(chat.getSender()));
    }

    /**
     * Determines if a chat is an unread message from the friend.
     */
    private boolean isUnreadChatFromFriend(Chat chat, String currentUserName) {
        return friendName.equals(chat.getSender()) && currentUserName.equals(chat.getReceiver()) && !chat.isRead();
    }

    /**
     * Displays a Snackbar with the given message.
     */
    private void showSnackbar(View view, String message, int colorResource) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(colorResource));
        snackbar.show();
    }

    /**
     * RecyclerView Adapter for chat messages.
     */
    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textview;
            private ImageView imageview;

            public ViewHolder(View view) {
                super(view);
                textview = view.findViewById(R.id.textview);
                imageview = view.findViewById(R.id.imageview);
            }
        }

        @NonNull
        @Override
        public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Chat chat = chats.get(position);
            holder.textview.setText(chat.getContent());

            if (chat.getSender().contentEquals(currentUser.getUsername())) {
                loadImage(holder.imageview, currentUser.getAvatar());
            } else if (chat.getSender().contentEquals(friendName)) {
                loadImage(holder.imageview, friendAvatar);
            }
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        @Override
        public int getItemViewType(int position) {
            Chat chat = chats.get(position);
            if (chat.getSender().contentEquals(currentUser.getUsername())) {
                return R.layout.chat_right_item;
            }
            else {
                return R.layout.chat_left_item;
            }
        }
    }

    private void loadImage(ImageView imageView, String avatar) {
        if (avatar == null)
            return;
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20));
        if (avatar.contentEquals("123")) {
            Glide.with(this)
                    .load(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
        else {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.mipmap.user)
                    .error(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
    }
}