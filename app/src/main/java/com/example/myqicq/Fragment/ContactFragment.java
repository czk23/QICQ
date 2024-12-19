package com.example.myqicq.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.Activity.ChatActivity;
import com.example.myqicq.Activity.NewFriendActivity;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Friend;
import com.example.myqicq.Object.Request;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.DBOpenHelper;
import com.gjiazhe.wavesidebar.WaveSideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ContactFragment extends Fragment {
    private View view;
    private RecyclerView Recycler_view;
    private WaveSideBar WaveSideBar;
    private TextView AddPrompt_textview;
    private TextView ContactNumber_textview;
    private ConstraintLayout NewFriend_layout;
    private ContactAdapter Contact_adapter; // 使用单例 adapter 来提高性能
    private List<User> users = new ArrayList<>();
    private DBOpenHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_fragment, container, false);
        initView();
        initSideBar();
        setEvent();
        return view;
    }

    private void initView() {
        AddPrompt_textview = view.findViewById(R.id.AddPrompt_textview);
        NewFriend_layout = view.findViewById(R.id.NewFriend_layout);
        ContactNumber_textview = view.findViewById(R.id.ContactNumber_textview);
        Recycler_view = (RecyclerView) view.findViewById(R.id.Contact_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        Recycler_view.setLayoutManager(layoutManager);
        ContactAdapter contactAdapter = new ContactAdapter(users);
        Recycler_view.setAdapter(contactAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateContactPrompt();
        updateContactUser();
    }

    private void initSideBar() {
        WaveSideBar = (WaveSideBar) view.findViewById(R.id.side_bar);
        WaveSideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                int i;
                boolean find = false;
                for (i = 0; i < users.size(); i++) {
                    if (users.get(i).getUsername().toUpperCase().substring(0, 1).contentEquals(index)) {
                        find = true;
                        break;
                    }
                }
                if (find) {
                    Recycler_view.scrollToPosition(i);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) Recycler_view.getLayoutManager();
                    assert layoutManager != null;
                    layoutManager.scrollToPositionWithOffset(i, 0);
                }
            }
        });
    }

    private void setEvent() {
        NewFriend_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        List<User> list;

        public ContactAdapter(List<User> list) {
            this.list = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView Contact_imageview;
            TextView Contact_textview;
            TextView Blank_textview;
            View Contact_view;
            View ContactLeft_view;

            public ViewHolder(View view) {
                super(view);
                Contact_imageview = (ImageView) view.findViewById(R.id.Contact_imageview);
                Contact_textview = (TextView) view.findViewById(R.id.Contact_textview);
                Blank_textview = (TextView) view.findViewById(R.id.Blank_textview);
                Contact_view = (View) view.findViewById(R.id.Contact_view);
                ContactLeft_view = (View) view.findViewById(R.id.ContactLeft_view);
            }
        }

        @NonNull
        @Override
        public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
            ContactAdapter.ViewHolder viewHolder = new ContactAdapter.ViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = list.get(viewHolder.getAdapterPosition());
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("username", user.getUsername());
                    intent.putExtra("avatar", user.getAvatar());
                    startActivity(intent);
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
            User user = list.get(position);
            String username = user.getUsername();
            holder.Contact_textview.setText(username);
            loadImage(holder.Contact_imageview, user.getAvatar());

            if (position >= 1) {
                if (list.get(position - 1).getUsername().toLowerCase().charAt(0) == username.toLowerCase().charAt(0)) {
                    holder.Blank_textview.setVisibility(View.GONE);
                }
                else {
                    holder.Blank_textview.setText(username.toUpperCase().substring(0, 1));
                }
            }
            if (position == 0) {
                holder.Blank_textview.setText(username.toUpperCase().substring(0, 1));
            }

            if (position != getItemCount() - 1) {
                if (list.get(position + 1).getUsername().toLowerCase().charAt(0) != username.toLowerCase().charAt(0)) {
                    holder.Contact_view.setVisibility(View.GONE);
                }
            }
            else {
                holder.ContactLeft_view.setVisibility(View.VISIBLE);
                holder.Contact_view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    private void loadImage(ImageView imageView, String avatar) {
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20));
        if (avatar.contentEquals("123")) {
            Glide.with(requireContext())
                    .load(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
        else {
            Glide.with(requireContext())
                    .load(avatar)
                    .placeholder(R.mipmap.user)
                    .error(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
    }

    private void updateContactPrompt() {
        helper = new DBOpenHelper(getContext());
        helper.clearAllRequests();
        BmobQuery<Request> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(new FindListener<Request>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(List<Request> list, BmobException e) {
                String userId = MyApplication.getUser().getObjectId();
                if (list != null) {
                    int count = 0;
                    for (Request request : list) {
                        if (request.getReceiverId().contentEquals(userId)) {
                            String requesterId = request.getRequesterId();
                            String receiverId = request.getReceiverId();
                            String requesterName = request.getRequesterName();
                            String requesterAvatar = request.getRequesterAvatar();
                            helper.insertRequest(requesterId, receiverId, requesterName, requesterAvatar);
                            count++;
                        }
                    }
                    if (count == 0) {
                        AddPrompt_textview.setVisibility(View.INVISIBLE);
                    }
                    else {
                        AddPrompt_textview.setVisibility(View.VISIBLE);
                        AddPrompt_textview.setText(count + "");
                    }
                }
            }
        });
    }

    private void updateContactUser() {
        BmobQuery<User> bmobQuery = new BmobQuery<>();
        int previousSize = users.size();
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                BmobQuery<Friend> bmobQuery1 = new BmobQuery<>();
                bmobQuery1.findObjects(new FindListener<Friend>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void done(List<Friend> list1, BmobException e) {
                        List<User> temp = new ArrayList<>();
                        User me = MyApplication.getUser();
                        if (list != null && list1 != null) {
                            for (User user : list) {
                                for (Friend friend : list1) {
                                    if (user.getObjectId().contentEquals(friend.getUser1()) && me.getObjectId().contentEquals(friend.getUser2())) {
                                        temp.add(user);
                                    }
                                    if (user.getObjectId().contentEquals(friend.getUser2()) && me.getObjectId().contentEquals(friend.getUser1())) {
                                        temp.add(user);
                                    }
                                }
                            }
                        }
                        users = temp;

                        users.sort(new Comparator<User>() {
                            @Override
                            public int compare(User o1, User o2) {
                                String s1 = o1.getUsername().toLowerCase();
                                String s2 = o2.getUsername().toLowerCase();
                                return s1.compareTo(s2);
                            }
                        });

                        switch (users.size()) {
                            case 0:
                                ContactNumber_textview.setText("当前暂无好友");
                                break;
                            case 1:
                                ContactNumber_textview.setText("有一个好友");
                                break;
                            default:
                                ContactNumber_textview.setText("好友总数: " + users.size());
                                break;
                        }

                        if (users.size() != previousSize)
                            Recycler_view.setAdapter(new ContactAdapter(users));
                    }
                });
            }
        });
    }
}