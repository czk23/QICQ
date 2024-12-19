package com.example.myqicq.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.util.List;
import java.util.Objects;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class LoginFragment extends Fragment {
    private View rootView;
    private Button loginButton;
    private TextView registerText;
    private TextView forgotPasswordText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loginProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.login_fragment, container, false);
        // Initialize the views
        initializeViews();
        // Set up event listeners for buttons and text views
        setupEventListeners();
        return rootView;
    }

    // onResume is called when the fragment comes into the foreground
    @Override
    public void onResume() {
        super.onResume();
        clearInputFields(); // Clear the username and password fields when the fragment is resumed
    }

    // Initializes all the views by finding them in the layout
    private void initializeViews() {
        loginButton = rootView.findViewById(R.id.Login_button); // Login button
        registerText = rootView.findViewById(R.id.Register_textview); // Register TextView
        forgotPasswordText = rootView.findViewById(R.id.Forget_textview); // Forgot password TextView
        usernameEditText = rootView.findViewById(R.id.Username_edittext); // Username EditText
        passwordEditText = rootView.findViewById(R.id.Password_edittext); // Password EditText
        loginProgressBar = rootView.findViewById(R.id.Login_progressBar); // Progress bar for login
    }

    // Sets up event listeners for the buttons and text views
    private void setupEventListeners() {
        // When login button is clicked, handle the login process
        loginButton.setOnClickListener(view -> handleLogin());

        // When the register text is clicked, navigate to the RegisterFragment
        registerText.setOnClickListener(view -> navigateToFragment(new RegisterFragment()));

        // When the forgot password text is clicked, navigate to the VerifyFragment
        forgotPasswordText.setOnClickListener(view -> navigateToFragment(new VerifyFragment()));
    }

    // Clears the username and password input fields
    private void clearInputFields() {
        usernameEditText.setText(""); // Clear the username field
        passwordEditText.setText(""); // Clear the password field
    }

    // Handles the login logic
    private void handleLogin() {
        hideKeyboard(); // Hide the keyboard when login is triggered
        showProgressBar(true); // Show the progress bar during the login process

        String username = usernameEditText.getText().toString().trim(); // Get the username input
        String password = passwordEditText.getText().toString().trim(); // Get the password input

        // Check if the username or password is empty
        if (username.isEmpty()) {
            showSnackBar("red", "用户名不能为空!"); // Show an error message
            showProgressBar(false); // Hide the progress bar
            return;
        }

        if (password.isEmpty()) {
            showSnackBar("red", "密码不能为空!"); // Show an error message
            showProgressBar(false); // Hide the progress bar
            return;
        }

        // Query the user data from the Bmob database
        new BmobQuery<User>().findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> userList, BmobException e) {
                showProgressBar(false); // Hide the progress bar after the query is done

                // If there is an error during the query, show a Snackbar with the error message
                if (e != null) {
                    showSnackBar("red", "错误: " + e.getMessage());
                    return;
                }

                // Search for the user matching the entered username and password
                User matchedUser = userList.stream()
                        .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                        .findFirst()
                        .orElse(null); // If no match is found, return null

                // If a matching user is found, navigate to the main screen
                if (matchedUser != null) {
                    navigateToMainFragment(matchedUser);
                    showSnackBar("blue", "登录成功!"); // Show success message
                }
                else {
                    showSnackBar("red", "用户名或密码不正确!"); // Show error message
                }
            }
        });
    }

    // Navigates to the main screen after a successful login
    private void navigateToMainFragment(User user) {
        MainFragment mainFragment = new MainFragment(); // Create a new instance of MainFragment
        Bundle args = new Bundle();
        args.putSerializable("user", user); // Pass the user object as an argument
        mainFragment.setArguments(args); // Set the arguments for the fragment
        replaceFragment(mainFragment, false); // Replace the current fragment with the main fragment
    }

    // Navigates to a specified fragment
    private void navigateToFragment(Fragment fragment) {
        replaceFragment(fragment, true); // Replace the current fragment with the specified fragment
    }

    // Replaces the current fragment with a new one and optionally adds it to the back stack
    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction(); // Start a fragment transaction
        transaction.replace(R.id.fragment_container, fragment); // Replace the current fragment
        if (addToBackStack) {
            transaction.addToBackStack(null); // Add the transaction to the back stack if specified
        }
        transaction.commit(); // Commit the transaction
    }

    // Shows a Snackbar with a specified color and message
    private void showSnackBar(String color, String message) {
        Util.showSnackBar(color, loginButton, message, getContext()); // Call the Util method to show the Snackbar
    }

    // Hides the soft keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0); // Hide the keyboard if it's visible
        }
    }

    // Shows or hides the progress bar based on the visibility flag
    private void showProgressBar(boolean isVisible) {
        loginProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE); // Set the progress bar visibility
    }
}

