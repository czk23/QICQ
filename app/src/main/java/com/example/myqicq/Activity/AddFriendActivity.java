package com.example.myqicq.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Friend;
import com.example.myqicq.Object.Request;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class AddFriendActivity extends AppCompatActivity {
    private ImageButton AddBack_imagebutton;      // 返回按钮
    private EditText AddFriend_edittext;          // 输入好友用户名
    private ImageButton Search_imagebutton;       // 搜索按钮
    private ImageView SearchUser_imageview;       // 显示查找到的用户图标
    private TextView SearchUser_textview;         // 显示查找到的用户名
    private Button AddFriend_button;              // 添加好友按钮
    private String receiverId;                    // 目标用户的 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend_activity);
        initView();
        setEvent();
    }

    /**
     * 初始化视图元素
     */
    private void initView() {
        AddBack_imagebutton = findViewById(R.id.AddBack_image_button);
        AddFriend_edittext = findViewById(R.id.AddFriend_edittext);
        Search_imagebutton = findViewById(R.id.Search_image_button);
        SearchUser_imageview = findViewById(R.id.SearchUser_imageview);
        SearchUser_textview = findViewById(R.id.SearchUser_textview);
        AddFriend_button = findViewById(R.id.AddFriend_button);
    }

    /**
     * 设置点击事件
     */
    private void setEvent() {
        AddBack_imagebutton.setOnClickListener(v -> finish());

        Search_imagebutton.setOnClickListener(v -> searchUser());

        AddFriend_button.setOnClickListener(v -> sendFriendRequest());
    }

    /**
     * 搜索用户并显示结果
     */
    private void searchUser() {
        hideKeyboard();
        String username = AddFriend_edittext.getText().toString().trim();
        if (username.isEmpty()) {
            showSnackBar("yellow", "用户名不能为空");
            return;
        }

        BmobQuery<User> query = new BmobQuery<>();
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> users, BmobException e) {
                if (e == null) {
                    User foundUser = findUserByName(users, username);
                    if (foundUser != null) {
                        showUserInfo(foundUser);
                    }
                    else {
                        showSnackBar("red", "该用户不存在");
                    }
                }
                else {
                    showSnackBar("red", "请检查网络连接");
                }
            }
        });
    }

    /**
     * 发送好友请求
     */
    private void sendFriendRequest() {
        String requesterId = MyApplication.getUser().getObjectId();
        if (requesterId.equals(receiverId)) {
            showSnackBar("red", "不能添加自己为好友");
            return;
        }

        Request request = createFriendRequest();

        // 检查是否已经是好友
        BmobQuery<Friend> query = new BmobQuery<>();
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> friends, BmobException e) {
                if (e == null && isAlreadyFriends(friends, request)) {
                    showSnackBar("yellow", "你们已经是好友了");
                }
                else {
                    saveFriendRequest(request);
                }
            }
        });
    }

    /**
     * 保存好友请求到服务器
     */
    private void saveFriendRequest(Request request) {
        request.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    showSnackBar("blue", "请求已发送");
                }
                else {
                    showSnackBar("gold", "不能重复发送请求");
                }
            }
        });
    }

    /**
     * 创建 Request 对象
     */
    private Request createFriendRequest() {
        return new Request(
                MyApplication.getUser().getObjectId(),
                receiverId,
                MyApplication.getUser().getUsername(),
                MyApplication.getUser().getAvatar()
        );
    }

    /**
     * 查找指定用户名的用户
     */
    private User findUserByName(List<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 显示查找到的用户信息
     */
    private void showUserInfo(User user) {
        SearchUser_textview.setText(user.getUsername());
        loadImage(user);
        SearchUser_imageview.setVisibility(View.VISIBLE);
        AddFriend_button.setVisibility(View.VISIBLE);
        receiverId = user.getObjectId();
    }

    /**
     * 加载用户头像
     */
    private void loadImage(User user) {
        String avatarUrl = user.getAvatar();
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(40)); // 设置圆角样式

        // 根据头像地址加载图片
        if ("123".equals(avatarUrl)) {
            Glide.with(this).load(R.mipmap.user).apply(options).into(SearchUser_imageview);
        }
        else {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.user) // 加载中的默认头像
                    .apply(options)
                    .into(SearchUser_imageview);
        }
    }

    /**
     * 判断是否已经是好友
     */
    private boolean isAlreadyFriends(List<Friend> friends, Request request) {
        for (Friend friend : friends) {
            if ((request.getRequesterId().equals(friend.getUser1()) && request.getReceiverId().equals(friend.getUser2()))
                    || (request.getRequesterId().equals(friend.getUser2()) && request.getReceiverId().equals(friend.getUser1()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    /**
     * 展示 SnackBar 消息
     */
    private void showSnackBar(String color, String message) {
        Util.showSnackBar(color, AddFriend_button, message, AddFriendActivity.this);
    }

}
