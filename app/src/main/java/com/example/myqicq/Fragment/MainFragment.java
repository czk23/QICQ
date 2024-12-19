package com.example.myqicq.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myqicq.Activity.AddFriendActivity;
import com.example.myqicq.Object.User;
import com.example.myqicq.MyApplication;
import com.example.myqicq.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

/**
 * MainFragment is the central fragment managing multiple sub-fragments (Message, Contacts, Discover, Me)
 * and provides a TabLayout to switch between them.
 */
public class MainFragment extends Fragment {

    // UI components
    private View view;
    private TabLayout tabLayout;
    private TextView titleTextView;
    private ImageView plusImageView;

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
        setupTabLayout();
        setupPlusButton();
        loadUserData();

        return view;
    }

    /**
     * Initialize the views used in this fragment.
     *
     * @param view Root view of the fragment
     */
    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.Tab_Layout);
        titleTextView = view.findViewById(R.id.Title_textview);
        plusImageView = view.findViewById(R.id.Plus_image_view);
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
            tabLayout.addTab(tabLayout.newTab().setCustomView(createTabView(i)));
        }

        // Handle tab selection events
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        ImageView imageView = Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(position)).getCustomView()).findViewById(R.id.Icon_imageview);
        switch (position) {
            case 0:
                imageView.setImageResource(R.mipmap.message_select);
                titleTextView.setText("消息");
                replaceFragment(messageFragment);
                currentIndex = 0;
                plusImageView.setVisibility(View.INVISIBLE);
                break;
            case 1:
                imageView.setImageResource(R.mipmap.contact_select);
                titleTextView.setText("联系人");
                replaceFragment(contactFragment);
                currentIndex = 1;
                plusImageView.setVisibility(View.INVISIBLE);
//                plusImageView.setImageResource(R.mipmap.add_friends);
                break;
            case 2:
                imageView.setImageResource(R.mipmap.discover_select);
                titleTextView.setText("我的空间");
                replaceFragment(discoverFragment);
                currentIndex = 2;
                plusImageView.setVisibility(View.INVISIBLE);
                break;
            case 3:
                imageView.setImageResource(R.mipmap.my_select);
                titleTextView.setText("我");
                replaceFragment(meFragment);
                currentIndex = 3;
                plusImageView.setVisibility(View.INVISIBLE);
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
        ImageView imageView = Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(position)).getCustomView()).findViewById(R.id.Icon_imageview);
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

    /**
     * Set up the plus button to handle click events based on the current tab.
     */
    private void setupPlusButton() {
        plusImageView.setOnClickListener(view -> {
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
}
