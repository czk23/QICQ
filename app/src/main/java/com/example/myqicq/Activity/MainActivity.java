package com.example.myqicq.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myqicq.Fragment.LoginFragment;
import com.example.myqicq.Fragment.StartFragment;
import com.example.myqicq.R;

public class MainActivity extends AppCompatActivity {

    public void setFullscreen(boolean isShowStatusBar, boolean isShowNavigationBar) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (!isShowStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (!isShowNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);


    }

    // Message type for showing the LoginFragment
    private static final int MSG_SHOW_LOGIN = 0;
    private final Handler handler = new Handler(Looper.getMainLooper(), this::handleMessage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.main_activity);

        // Load the initial StartFragment
        addFragment(new StartFragment());

        // Start the background task
        startBackgroundTask();
    }

    /**
     * Handles messages sent to the Handler.
     */
    private boolean handleMessage(@NonNull Message msg) {
        if (msg.what == MSG_SHOW_LOGIN) {
            addFragment(new LoginFragment());
            return true;
        }
        return false;
    }

    /**
     * Starts a background thread to simulate a delay before switching fragments.
     */
    private void startBackgroundTask() {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate a delay of 2 seconds
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }

            // Notify the handler to switch to the LoginFragment
            handler.sendEmptyMessage(MSG_SHOW_LOGIN);
        }).start();
    }

    /**
     * Adds or replaces a fragment in the activity's container.
     *
     * @param fragment The fragment to add or replace
     */
    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}