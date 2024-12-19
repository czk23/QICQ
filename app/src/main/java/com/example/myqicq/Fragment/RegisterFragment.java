package com.example.myqicq.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class RegisterFragment extends Fragment {

    /**
     * Fragment for user registration.
     * Handles input validation, user data submission, and interaction with the backend.
     */
    private View view;
    private ImageButton back_imagebutton;
    private EditText username_edittext, password_edittext, email_edittext;
    private Button register_button;
    private ProgressBar register_progressbar;

    // Called when the fragment is created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflates the fragment layout and initializes the views
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.register_fragment, container, false);
        initializeViews();
        setupEventListeners();
        return view;
    }

    /**
     * Initializes views by linking them with their XML counterparts.
     */
    private void initializeViews() {
        back_imagebutton = view.findViewById(R.id.Back_image_button);
        username_edittext = view.findViewById(R.id.Username_edittext);
        password_edittext = view.findViewById(R.id.Password_edittext);
        email_edittext = view.findViewById(R.id.Email_edittext);
        register_button = view.findViewById(R.id.Register_button);
        register_progressbar = view.findViewById(R.id.Register_progressBar);
    }

    /**
     * Sets up event listeners for user interaction.
     */
    private void setupEventListeners() {
        // Handles the back button click.
        back_imagebutton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Handles the register button click.
        register_button.setOnClickListener(this::register);
    }

    /**
     * Handles user registration logic.
     */
    private void register(View view) {
        showProgressBar(true);
        hideKeyboard();

        String username = username_edittext.getText().toString().trim();
        String password = password_edittext.getText().toString().trim();
        String email = email_edittext.getText().toString().trim();

        // Check if the username or password is empty
        if (username.isEmpty()) {
            displaySnackBar("red", "用户名不能为空!"); // Show an error message
            showProgressBar(false); // Hide the progress bar
            return;
        }

        if (password.isEmpty()) {
            displaySnackBar("red", "密码不能为空!"); // Show an error message
            showProgressBar(false); // Hide the progress bar
            return;
        }

        if (email.isEmpty()) {
            displaySnackBar("red", "邮箱不能为空!"); // Show an error message
            showProgressBar(false); // Hide the progress bar
            return;
        }

        // Check if the username already exists in the database.
        new BmobQuery<User>().findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> userList, BmobException e) {
                showProgressBar(false); // Hide the progress bar after the query is done

                // If there is an error during the query, show a Snackbar with the error message
                if (e != null) {
                    displaySnackBar("red", "错误: " + e.getMessage());
                    return;
                }

                if (userList != null) {
                    for (User existingUser : userList) {
                        if (existingUser.getUsername().equals(username)) {
                            displaySnackBar("red", "用户名已经存在!");
                            return;
                        }
                    }
                }

                // Save the new user data to the database.
                registerNewUser(username, password, email);
            }
        });
    }

    /**
     * Registers a new user in the database.
     */
    private void registerNewUser(String username, String password, String email) {
        User user = new User(username, password, "123", email); // Default value "123" for placeholder.

        user.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    displaySnackBar("blue", "注册成功!");
                    navigateBack();
                } else {
                    displaySnackBar("red", "网络错误, 请重试!");
                }
                showProgressBar(false);
            }
        });
    }

    /**
     * Displays a Snackbar message with a specified color and message.
     */
    private void displaySnackBar(String color, String message) {
        Util.showSnackBar(color, register_button, message, getContext());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // Hide the keyboard if it's visible
        }
    }

    // Shows or hides the progress bar based on the visibility flag
    private void showProgressBar(boolean isVisible) {
        register_progressbar.setVisibility(isVisible ? View.VISIBLE : View.GONE); // Set the progress bar visibility
    }

    /**
     * Navigates back to the previous screen.
     */
    private void navigateBack() {
        if (getActivity() != null) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }


}
