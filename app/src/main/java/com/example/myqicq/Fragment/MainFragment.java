package com.example.myqicq.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.Activity.AddFriendActivity;
import com.example.myqicq.Object.User;
import com.example.myqicq.MyApplication;
import com.example.myqicq.R;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;

/**
 * MainFragment is the central fragment managing multiple sub-fragments (Message, Contacts, Discover, Me)
 * and provides a TabLayout to switch between them.
 */
public class MainFragment extends Fragment {

    // UI components
    private View view;
    private TabLayout Tab_Layout;
    private TextView Title_Textview;
    private ImageView Plus_Imageview;
    private ImageButton Avatar_imagebutton;

    // Fragments managed by this class
    private Fragment messageFragment;
    private Fragment contactFragment;
    private Fragment discoverFragment;
    private Fragment meFragment;

    // Index to track the current tab
    private int currentIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment, container, false);

        // Initialize fragments and default view
        initFragments();
        replaceFragment(messageFragment);
        currentIndex = 0;

        // Initialize views and setup events
        initViews(view);
        loadUserData();
        setupTabLayout();
        setupPlusButton();
        setupAvatarImageButton();

        // 取消当前选中状态
        Tab_Layout.selectTab(null);
        // 重新选中
        Objects.requireNonNull(Tab_Layout.getTabAt(0)).select();

        return view;
    }

    /**
     * Initialize the views used in this fragment.
     *
     * @param view Root view of the fragment
     */
    private void initViews(View view) {
        Tab_Layout = view.findViewById(R.id.Tab_Layout);
        Title_Textview = view.findViewById(R.id.Title_textview);
        Plus_Imageview = view.findViewById(R.id.Plus_image_view);
        Avatar_imagebutton = view.findViewById(R.id.Avatar_image_button);
    }

    /**
     * Initialize the fragments managed by this class.
     */
    private void initFragments() {
        messageFragment = new MessageFragment();
        contactFragment = new ContactFragment();
        discoverFragment = new DiscoverFragment();
        meFragment = new MeFragment();
    }

    /**
     * Set up the TabLayout with custom tabs and handle tab selection events.
     */
    private void setupTabLayout() {
        // Add four tabs with custom views
        for (int i = 0; i < 4; i++) {
            Tab_Layout.addTab(Tab_Layout.newTab().setCustomView(createTabView(i)));
        }

        // Handle tab selection events
        Tab_Layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabSelection(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                handleTabUnselection(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed for reselection
            }
        });
    }

    /**
     * Create a custom view for each tab.
     *
     * @param position Position of the tab
     * @return The custom view for the tab
     */
    private View createTabView(int position) {
//        View tabView = LayoutInflater.from(getContext()).inflate(R.layout.tab_view, null);
        @SuppressLint("InflateParams") View tabView = LayoutInflater.from(getContext()).inflate(R.layout.tab_view, null);
        ImageView imageView = tabView.findViewById(R.id.Icon_imageview);

        // Set the default icon for each tab
        switch (position) {
            case 0:
                imageView.setImageResource(R.mipmap.message1);
                break;
            case 1:
                imageView.setImageResource(R.mipmap.contact1);
                break;
            case 2:
                imageView.setImageResource(R.mipmap.discover1);
                break;
            case 3:
                imageView.setImageResource(R.mipmap.my1);
                break;
        }

        return tabView;
    }

    /**
     * Handle tab selection to update the UI and display the corresponding fragment.
     *
     * @param position Position of the selected tab
     */
    private void handleTabSelection(int position) {
        // ImageView imageView = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.Icon_imageview);
        ImageView imageView = Objects.requireNonNull(Objects.requireNonNull(Tab_Layout.getTabAt(position)).getCustomView()).findViewById(R.id.Icon_imageview);
        switch (position) {
            case 0:
                imageView.setImageResource(R.mipmap.message_select);
                Title_Textview.setText("消息");
                replaceFragment(messageFragment);
                currentIndex = 0;
                Avatar_imagebutton.setVisibility(View.VISIBLE);
                loadImage();
                Plus_Imageview.setVisibility(View.INVISIBLE);
                break;
            case 1:
                imageView.setImageResource(R.mipmap.contact_select);
                Title_Textview.setText("联系人");
                replaceFragment(contactFragment);
                currentIndex = 1;
                Avatar_imagebutton.setVisibility(View.VISIBLE);
                loadImage();
                Plus_Imageview.setVisibility(View.INVISIBLE);
                break;
            case 2:
                imageView.setImageResource(R.mipmap.discover_select);
                Title_Textview.setText("我的空间");
                replaceFragment(discoverFragment);
                currentIndex = 2;
                Avatar_imagebutton.setVisibility(View.VISIBLE);
                loadImage();
                Plus_Imageview.setVisibility(View.INVISIBLE);
                break;
            case 3:
                imageView.setImageResource(R.mipmap.my_select);
                Title_Textview.setText("我");
                replaceFragment(meFragment);
                currentIndex = 3;
                Avatar_imagebutton.setVisibility(View.INVISIBLE);
                Plus_Imageview.setVisibility(View.INVISIBLE);
                break;
        }
    }

    /**
     * Handle tab unselection to reset the tab's icon.
     *
     * @param position Position of the unselected tab
     */
    private void handleTabUnselection(int position) {
        // ImageView imageView = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.Icon_imageview);
        ImageView imageView = Objects.requireNonNull(Objects.requireNonNull(Tab_Layout.getTabAt(position)).getCustomView()).findViewById(R.id.Icon_imageview);
        switch (position) {
            case 0:
                imageView.setImageResource(R.mipmap.message1);
                break;
            case 1:
                imageView.setImageResource(R.mipmap.contact1);
                break;
            case 2:
                imageView.setImageResource(R.mipmap.discover1);
                break;
            case 3:
                imageView.setImageResource(R.mipmap.my1);
                break;
        }
    }

    private void setupAvatarImageButton() {
        Avatar_imagebutton.setOnClickListener(view ->  {
            // 创建下一个 Fragment
            HomepageFragment Homepage_fragment = new HomepageFragment();

            // 开始 FragmentTransaction
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

            // 设置动画：从右滑入左滑出，创建左滑切换效果
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,  // 进入动画：从右侧滑入
                    R.anim.slide_out_right // 离开动画：向左滑出
            );

            // 替换当前 Fragment
            transaction.replace(R.id.fragment_container, Homepage_fragment);

            // 添加到回退栈（如果需要回退到前一个 Fragment）
            transaction.addToBackStack(null);



            // 提交事务
            transaction.commit();
        });
    }

    /**
     * Set up the plus button to handle click events based on the current tab.
     */
    private void setupPlusButton() {
        Plus_Imageview.setOnClickListener(view -> {
            if (currentIndex == 1) {
                // Open AddFriendActivity when "Contacts" tab is selected
                Intent intent = new Intent(getContext(), AddFriendActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(getContext(), "No action for this tab", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Replace the current fragment with the specified fragment.
     *
     * @param fragment Fragment to be displayed
     */
    private void replaceFragment(Fragment fragment) {
        // FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.Center_layout, fragment);
        transaction.commit();
    }

    /**
     * Load user data from the arguments and store it in the application context.
     */
    private void loadUserData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            User user = (User) bundle.getSerializable("user");
            MyApplication.setUser(user);
        }
    }

    private void loadImage() {
        User user = MyApplication.getUser();
        String avatarUrl = user.getAvatar();
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(40)); // 设置圆角样式

        // 根据头像地址加载图片
        if ("123".equals(avatarUrl)) {
            Glide.with(this).load(R.mipmap.user).apply(options).into(Avatar_imagebutton);
        }
        else {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.mipmap.user) // 加载中的默认头像
                    .apply(options)
                    .into(Avatar_imagebutton);
        }
    }

}
