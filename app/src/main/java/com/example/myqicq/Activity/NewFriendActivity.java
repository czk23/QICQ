package com.example.myqicq.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myqicq.Object.Friend;
import com.example.myqicq.Object.Request;
import com.example.myqicq.R;
import com.example.myqicq.Util.DBOpenHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.function.Predicate;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class NewFriendActivity extends AppCompatActivity {
    private ImageButton Back_button;                  // Button to return to the previous activity
    private RecyclerView Recycle_view;            // RecyclerView to display friend requests
    private ImageView AddFriend_button;               // Button to navigate to AddFriendActivity
    private List<Request> requestList;            // List holding friend request objects
    private DBOpenHelper dbHelper;                // Helper for database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_friend_activity);

        initData(); // Load friend request data
        initView(); // Initialize views
        setEvents(); // Set click listeners for UI components
    }

    /**
     * Fetch friend request data from the database.
     */
    private void initData() {
        dbHelper = new DBOpenHelper(this);
        requestList = dbHelper.getAllRequests();
    }

    /**
     * Initialize views and RecyclerView.
     */
    private void initView() {
        Back_button = findViewById(R.id.FriendBack_image_button);
        Recycle_view = findViewById(R.id.NewFriend_recyclerview);
        AddFriend_button = findViewById(R.id.AddFriend_imageview);

        Recycle_view.setLayoutManager(new LinearLayoutManager(this));
        Recycle_view.setAdapter(new NewFriendAdapter(requestList));
    }

    /**
     * Set button click event listeners.
     */
    private void setEvents() {
        // Handle back button click: simply close this activity
        Back_button.setOnClickListener(v -> finish());

        // Handle "Add Friend" button click: navigate to AddFriendActivity
        AddFriend_button.setOnClickListener(v -> {
            Intent intent = new Intent(NewFriendActivity.this, AddFriendActivity.class);
            startActivity(intent);
        });
    }

    /**
     * RecyclerView Adapter for displaying friend requests.
     */
    class NewFriendAdapter extends RecyclerView.Adapter<NewFriendAdapter.ViewHolder> {

        private final List<Request> requests; // Local list of requests

        // Constructor to initialize the request list
        public NewFriendAdapter(List<Request> requests) {
            this.requests = requests;
        }

        /**
         * ViewHolder class for holding views of each item.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView Profile_imageview;         // Profile picture
            TextView Name_textview;              // Requester name
            ImageButton Accept_button;              // Accept request button
            ImageButton Refuse_button;              // Refuse request button

            public ViewHolder(View view) {
                super(view);
                Profile_imageview = view.findViewById(R.id.NewFriend_imageview);
                Name_textview = view.findViewById(R.id.NewFriend_textview);
                Accept_button = view.findViewById(R.id.Accept_image_button);
                Refuse_button = view.findViewById(R.id.Refuse_image_button);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_friend_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            // Accept request click handler
            viewHolder.Accept_button.setOnClickListener(v -> handleFriendRequest(true, viewHolder.getAdapterPosition()));

            // Refuse request click handler
            viewHolder.Refuse_button.setOnClickListener(v -> handleFriendRequest(false, viewHolder.getAdapterPosition()));

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Request request = requests.get(position);
            holder.Name_textview.setText(request.getRequesterName());
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        /**
         * Handles acceptance or refusal of a friend request.
         *
         * @param isAccepted True if the request is accepted, false otherwise.
         * @param position   Position of the request in the list.
         */
        private void handleFriendRequest(boolean isAccepted, int position) {
            Request request = requests.get(position);

            BmobQuery<Request> query = new BmobQuery<>();
            query.findObjects(new FindListener<Request>() {
                @Override
                public void done(List<Request> resultList, BmobException e) {
                    if (e == null) {
                        for (Request r : resultList) {
                            if (r.getRequesterId().equals(request.getRequesterId())
                                    && r.getReceiverId().equals(request.getReceiverId())) {
                                request.setObjectId(r.getObjectId());
                                processRequestDeletion(request, isAccepted, position);
                                break;
                            }
                        }
                    }
                    else {
                        showSnackbar("处理请求失败，请检查网络设置", R.color.Red);
                    }
                }
            });
        }

        /**
         * Processes request deletion and handles friend addition if accepted.
         */
        private void processRequestDeletion(Request request, boolean isAccepted, int position) {
            request.delete(new UpdateListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        requests.remove(position); // Remove request from local list
                        notifyDataSetChanged();
                        dbHelper.deleteRequest(request.getRequesterId(), request.getReceiverId());

                        if (isAccepted) {
                            addFriend(request);
                        }
                        else {
                            showSnackbar("你拒绝了" + request.getRequesterName() + "的好友申请", R.color.Blue);
                        }
                    }
                    else {
                        showSnackbar("好友请求删除失败", R.color.Red);
                    }
                }
            });
        }

        /**
         * Adds a new friend when a request is accepted.
         */
        private void addFriend(Request request) {
            Friend friend = new Friend(request.getRequesterId(), request.getReceiverId());
            friend.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        showSnackbar("你已经添加" + request.getRequesterName() + "为你的好友", R.color.Blue);
                    }
                    else {
                        showSnackbar("添加好友失败，请检查网络设置", R.color.Red);
                    }
                }
            });
        }

        /**
         * Displays a Snackbar with a message and color.
         */
        private void showSnackbar(String message, int color) {
            Snackbar snackbar = Snackbar.make(Back_button, message, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(getResources().getColor(color));
            snackbar.show();
        }
    }
}
