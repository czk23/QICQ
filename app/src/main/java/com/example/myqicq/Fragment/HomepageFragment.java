package com.example.myqicq.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.Activity.MomentActivity;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.util.List;

public class HomepageFragment extends Fragment {

    private View view;
    private ImageView Avatar_imageview;
    private TextView Username_textview;
    private ImageButton Close_imagebutton;
    private ConstraintLayout QQ_show_layout;
    private ConstraintLayout Money_layout;
    private ConstraintLayout Decorate_layout;
    private ConstraintLayout Photo_layout;
    private ConstraintLayout Collect_layout;
    private ConstraintLayout File_layout;
    private ConstraintLayout Love_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.homepage_fragment, container, false);

        // Initialize views and setup events
        initViews(view);
        loadImage();
        setupEventListeners();

        return view;
    }

    /**
     * Initialize the views used in this fragment.
     *
     * @param view Root view of the fragment
     */
    private void initViews(View view) {
        Avatar_imageview = view.findViewById(R.id.Avatar_imageview);
        Username_textview = view.findViewById(R.id.Username_textview);
        Close_imagebutton = view.findViewById(R.id.Close_image_button);
        QQ_show_layout = view.findViewById(R.id.QQ_show_layout);
        Money_layout = view.findViewById(R.id.Money_layout);
        Decorate_layout = view.findViewById(R.id.Decorate_layout);
        Photo_layout = view.findViewById(R.id.Photo_layout);
        Collect_layout = view.findViewById(R.id.Collect_layout);
        File_layout = view.findViewById(R.id.File_layout);
        Love_layout = view.findViewById(R.id.Love_layout);

        // 设置当前用户名
        Username_textview.setText(MyApplication.getUser().getUsername());
    }

    private void loadImage() {
        User user = MyApplication.getUser();
        String avatarUrl = user.getAvatar();
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(40)); // 设置圆角样式

        // 根据头像地址加载图片
        if ("123".equals(avatarUrl)) {
            Glide.with(this).load(R.mipmap.user).apply(options).into(Avatar_imageview);
        }
        else {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.user) // 加载中的默认头像
                    .apply(options)
                    .into(Avatar_imageview);
        }
    }

    private void setupEventListeners() {
        Close_imagebutton.setOnClickListener(v -> closeFragment());

        QQ_show_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        Money_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        Decorate_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        Photo_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        Collect_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        File_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

        Love_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar("尚在开发中!");
            }
        });

    }

    /**
     * 关闭当前 Fragment 并执行左滑出的动画
     */
    private void closeFragment() {
        // 获取 FragmentManager
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        // 开始一个新的 FragmentTransaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 设置自定义动画
        transaction.setCustomAnimations(
                R.anim.slide_in_left,    // enter：新 Fragment 从左侧滑入
                R.anim.slide_out_left,   // exit：当前 Fragment 从左侧滑出
                R.anim.slide_in_left,   // popEnter：从回退栈弹出时，新 Fragment 从右侧滑入
                R.anim.slide_out_left   // popExit：从回退栈弹出时，当前 Fragment 从右侧滑出
        );

        // 弹出栈顶的 Fragment（移除栈顶 Fragment）
        fragmentManager.popBackStack();  // 弹出栈顶 Fragment，应用动画

        // 提交事务
        transaction.commit();
    }


    private void showSnackBar(String message) {
        Util.showSnackBar("yellow", view, message, getContext()); // Call the Util method to show the Snackbar
    }

}
