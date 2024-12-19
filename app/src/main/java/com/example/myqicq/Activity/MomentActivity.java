package com.example.myqicq.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.myqicq.Object.Friend;
import com.example.myqicq.Object.Moment;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class MomentActivity extends AppCompatActivity {
    private static final String TAG = "MomentActivity";

    // 界面元素
    private ImageButton MomentBack_imagebutton;
    private RecyclerView Moment_recyclerview;
    private ImageButton Camera_imagebutton;

    // 当前用户和好友列表
    private User me;
    private List<String> friends = new ArrayList<>();
    private List<Moment> moments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moment_activity);
        initView();  // 初始化视图组件
        setEvent();  // 设置事件监听
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMoments();  // 更新动态内容
    }

    /**
     * 初始化界面元素
     * 获取当前用户信息，并为控件设置引用
     */
    private void initView() {
        me = MyApplication.getUser();  // 获取当前用户
        MomentBack_imagebutton = findViewById(R.id.MomentBack_image_button);
        Moment_recyclerview = findViewById(R.id.Moment_recyclerview);
        Camera_imagebutton = findViewById(R.id.Camera_image_button);

        Moment_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        Moment_recyclerview.setAdapter(new MomentAdapter());
    }

    /**
     * 设置界面元素的事件监听
     * 主要包括返回按钮和相机按钮的点击事件
     */
    private void setEvent() {
        // 返回按钮：结束当前 Activity
        MomentBack_imagebutton.setOnClickListener(view -> finish());

        // 相机按钮：跳转到 EditActivity，进行动态发布
        Camera_imagebutton.setOnClickListener(v -> {
            Intent intent = new Intent(MomentActivity.this, EditActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 更新动态内容
     * 通过 Bmob API 获取好友列表和动态信息，并根据好友筛选出相应的动态进行展示
     */
    private void updateMoments() {
        BmobQuery<Friend> friendQuery = new BmobQuery<>();
        friendQuery.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> friendList, BmobException e) {
                friends.clear();  // 清空好友列表

                if (friendList != null) {
                    List<String> ids = new ArrayList<>();
                    for (Friend friend : friendList) {
                        // 根据当前用户的 objectId 找到所有好友的 objectId
                        if (friend.getUser1().contentEquals(me.getObjectId())) {
                            ids.add(friend.getUser2());
                        }
                        else if (friend.getUser2().contentEquals(me.getObjectId())) {
                            ids.add(friend.getUser1());
                        }
                    }

                    // 获取好友的用户名
                    BmobQuery<User> userQuery = new BmobQuery<>();
                    userQuery.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> userList, BmobException e) {
                            if (userList != null) {
                                for (User user : userList) {
                                    if (ids.contains(user.getObjectId())) {
                                        friends.add(user.getUsername());
                                    }
                                }
                            }

                            // 获取动态信息
                            BmobQuery<Moment> momentQuery = new BmobQuery<>();
                            momentQuery.findObjects(new FindListener<Moment>() {
                                @Override
                                public void done(List<Moment> momentList, BmobException e) {
                                    moments.clear();  // 清空动态列表
                                    if (momentList != null) {
                                        // 根据好友列表筛选出相应的动态
                                        for (Moment moment : momentList) {
                                            if (friends.contains(moment.getUser()) || moment.getUser().contentEquals(me.getUsername())) {
                                                moments.add(0, moment);  // 将符合条件的动态添加到列表中
                                            }
                                        }
                                    }
                                    Moment_recyclerview.setAdapter(new MomentAdapter());  // 更新 RecyclerView
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * 自定义 RecyclerView 的 Adapter，用于显示动态列表
     */
    class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView MomentUser_imageview;
            TextView MomentUser_textview;
            TextView MomentContent_textview;
            ImageView MomentPhoto_imageview;
            TextView MomentTime_textview;
            ImageView Like_imageview;
            TextView Like_textview;

            public ViewHolder(View view) {
                super(view);
                MomentUser_imageview = view.findViewById(R.id.MomentUser_imageview);
                MomentUser_textview = view.findViewById(R.id.MomentUser_textview);
                MomentContent_textview = view.findViewById(R.id.MomentContent_textview);
                MomentPhoto_imageview = view.findViewById(R.id.MomentPhoto_imageview);
                MomentTime_textview = view.findViewById(R.id.MomentTime_textview);
                Like_imageview = view.findViewById(R.id.Like_imageview);
                Like_textview = view.findViewById(R.id.Like_textview);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.moment_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Moment moment = moments.get(position);
            holder.MomentUser_textview.setText(moment.getUser());
            holder.MomentContent_textview.setText(moment.getContent());
            holder.MomentTime_textview.setText(moment.getCreatedAt());

            // 设置点赞数量
            int like = moment.getLike();
            if (like != 0) {
                holder.Like_textview.setVisibility(View.VISIBLE);
                holder.Like_textview.setText(String.valueOf(like));
            }

            // 加载用户头像和动态照片
            loadAvatar(holder.MomentUser_imageview, moment.getAvatar());
            loadPhoto(holder.MomentPhoto_imageview, moment.getPhoto());

            // 点赞功能
            holder.Like_imageview.setOnClickListener(v -> {
                if (holder.Like_textview.getCurrentTextColor() != -1979711488)
                    return;

                // 更新点赞状态
                moment.increaseLike();
                holder.Like_textview.setVisibility(View.VISIBLE);
                holder.Like_textview.setTextColor(getResources().getColor(R.color.Pink));
                holder.Like_textview.setText(String.valueOf(moment.getLike()));

                // 更新数据库中的动态点赞数
                Moment updatedMoment = new Moment(moment.getAvatar(), moment.getUser(), moment.getContent(), moment.getPhoto(), moment.getLike());
                updatedMoment.update(moment.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            Log.d(TAG, "Update successfully");
                        }
                        else {
                            Log.d(TAG, "Failed to update");
                            e.printStackTrace();
                        }
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return moments == null ? 0 : moments.size();
        }
    }

    /**
     * 加载用户头像
     * @param imageView 显示头像的 ImageView
     * @param avatar 头像 URL
     */
    private void loadAvatar(ImageView imageView, String avatar) {
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(20));
        if (avatar.equals("123")) {
            Glide.with(MomentActivity.this)
                    .load(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
        else {
            Glide.with(MomentActivity.this)
                    .load(avatar)
                    .placeholder(R.mipmap.user)
                    .error(R.mipmap.user)
                    .apply(options)
                    .into(imageView);
        }
    }

    /**
     * 加载动态照片
     * @param imageView 显示照片的 ImageView
     * @param photo 照片 URL
     */
    private void loadPhoto(ImageView imageView, String photo) {
        if (!photo.equals("123")) {
            Glide.with(MomentActivity.this)
                    .load(photo)
                    .into(imageView);
        }
    }
}
