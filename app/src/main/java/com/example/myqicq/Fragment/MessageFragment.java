package com.example.myqicq.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.Activity.ChatActivity;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Chat;
import com.example.myqicq.Object.Message;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MessageFragment extends Fragment {
    private View view; // Root view of the fragment
    private RecyclerView recycler_view; // RecyclerView to display messages
    private String current_user; // Username of the current user
    private List<Message> messages = new ArrayList<>(); // List of messages
    private List<User> users; // List of users fetched from the server

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.message_fragment, container, false);
        initializeView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData(); // Refresh data when the fragment is resumed
    }

    /**
     * Initialize UI components and set up the RecyclerView.
     */
    private void initializeView() {
        recycler_view = view.findViewById(R.id.Recycler_ViewMessage);
        current_user = MyApplication.getUser().getUsername();
        recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_view.setAdapter(new MessageAdapter());
    }

    /**
     * Fetch users and messages from the server and update the RecyclerView.
     */
    private void fetchData() {
        BmobQuery<User> userQuery = new BmobQuery<>();
        userQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> userList, BmobException e) {
                if (e == null) {
                    users = new ArrayList<>(userList);
                    fetchMessages();
                }
                else {
                    Log.e("MessageFragment", "Failed to fetch users", e);
                }
            }
        });
    }

    /**
     * Fetch chat data and process it into message objects.
     */
    private void fetchMessages() {
        BmobQuery<Chat> chatQuery = new BmobQuery<>();
        chatQuery.findObjects(new FindListener<Chat>() {
            @Override
            public void done(List<Chat> chatList, BmobException e) {
                if (e != null) {
                    Log.e("MessageFragment", "Failed to fetch chats", e);
                    return;
                }

                List<Message> previousMessages = new ArrayList<>(messages);
                messages.clear();

                if (chatList != null) {
                    processChats(chatList);
                }

                // Refresh adapter only if there is a change in the message list
                if (!areListsEqual(previousMessages, messages)) {
                    recycler_view.setAdapter(new MessageAdapter());
                }
            }
        });
    }

    /**
     * Process chat data and transform it into message objects.
     *
     * @param chatList List of chat objects retrieved from the server
     */
    private void processChats(List<Chat> chatList) {
        for (Chat chat : chatList) {
            if (chat.getSender().equals(current_user)) {
                handleOutgoingChat(chat);
            }
            else if (chat.getReceiver().equals(current_user)) {
                handleIncomingChat(chat);
            }
        }
    }

    private void handleOutgoingChat(Chat chat) {
        boolean isUpdated = false;
        for (Message message : messages) {
            if (message.getTitle().equals(chat.getReceiver())) {
                if (chat.getOrder() > message.getOrder()) {
                    message.setContent(chat.getContent());
                }
                isUpdated = true;
                break;
            }
        }
        if (!isUpdated) {
            messages.add(new Message(chat.getReceiver(), chat.getContent(), 0, chat.getOrder(), getAvatar(chat.getReceiver())));
        }
    }

    private void handleIncomingChat(Chat chat) {
        boolean isUpdated = false;
        for (Message message : messages) {
            if (message.getTitle().equals(chat.getSender())) {
                if (!chat.isRead()) {
                    message.increasePrompt();
                    Collections.swap(messages, messages.indexOf(message), 0);
                }
                if (chat.getOrder() > message.getOrder()) {
                    message.setContent(chat.getContent());
                }
                isUpdated = true;
                break;
            }
        }
        if (!isUpdated) {
            if (chat.isRead()) {
                messages.add(new Message(chat.getSender(), chat.getContent(), 0, chat.getOrder(), getAvatar(chat.getSender())));
            }
            else {
                messages.add(0, new Message(chat.getSender(), chat.getContent(), 1, chat.getOrder(), getAvatar(chat.getSender())));
            }
        }
    }

    /**
     * Retrieve the avatar URL for a given username.
     *
     * @param username The username whose avatar is needed
     * @return The URL of the avatar
     */
    private String getAvatar(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.getAvatar();
            }
        }
        return "123"; // Default avatar ID
    }

    /**
     * Compare two lists of messages for equality.
     *
     * @param list1 First message list
     * @param list2 Second message list
     * @return True if the lists are equal, false otherwise
     */
    private boolean areListsEqual(List<Message> list1, List<Message> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            Message m1 = list1.get(i);
            Message m2 = list2.get(i);
            if (!m1.getTitle().equals(m2.getTitle()) || !m1.getContent().equals(m2.getContent()) || m1.getPrompt() != m2.getPrompt()) {
                return false;
            }
        }
        return true;
    }

    /**
     * RecyclerView Adapter for displaying messages.
     */
    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImage;
            TextView titleText;
            TextView contentText;
            TextView promptText;

            public ViewHolder(View view) {
                super(view);
                avatarImage = view.findViewById(R.id.Message_imageview);
                titleText = view.findViewById(R.id.MessageTitle_textview);
                contentText = view.findViewById(R.id.MessageContent_textview);
                promptText = view.findViewById(R.id.Prompt_textview);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(v -> {
                int position = viewHolder.getAdapterPosition();
                openChatActivity(position);
                updateMessagePrompt(position); // 更新消息的提示
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Message message = messages.get(position);
            holder.titleText.setText(message.getTitle());
            holder.contentText.setText(message.getContent());
            loadImage(holder.avatarImage, message.getAvatar());

            if (message.getPrompt() > 0) {
                holder.promptText.setVisibility(View.VISIBLE);
                holder.promptText.setText(String.valueOf(message.getPrompt()));
            }
            else {
                holder.promptText.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        /**
         * Open the ChatActivity for the selected message.
         *
         * @param position Position of the selected message
         */
        private void openChatActivity(int position) {
            Message message = messages.get(position);
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("username", message.getTitle());
            intent.putExtra("avatar", message.getAvatar());
            startActivity(intent);
        }

        /**
         * Update the message prompt to 0 and make it invisible.
         *
         * @param position The position of the selected message
         */
        private void updateMessagePrompt(int position) {
            Message message = messages.get(position);

            // 将 prompt 设置为 0
            message.setPrompt(0);

            // 通知适配器更新数据
            notifyItemChanged(position);
        }
    }

    /**
     * Load the user's avatar into an ImageView.
     *
     * @param imageView The ImageView to load the avatar into
     * @param avatarUrl The URL of the avatar image
     */
    private void loadImage(ImageView imageView, String avatarUrl) {
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(30));
        int placeholder = R.mipmap.user;

        Glide.with(requireContext())
                .load(avatarUrl != null ? avatarUrl : placeholder)
                .placeholder(placeholder)
                .error(placeholder)
                .apply(options)
                .into(imageView);
    }

}
