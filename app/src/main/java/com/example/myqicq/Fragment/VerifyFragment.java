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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class VerifyFragment extends Fragment {
    // UI Components
    private View view;
    private ImageButton Back_image_button;
    private EditText Username_edittext;
    private EditText Email_edittext;
    private EditText Code_edittext;
    private Button Send_button;
    private Button Verify_button;

    // User object to be verified
    private User verifyUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.verify_fragment, container, false);
        initView();
        setEventListeners();
        return view;
    }

    /**
     * Initializes the UI components.
     */
    private void initView() {
        Back_image_button = view.findViewById(R.id.Back_image_button);
        Username_edittext = view.findViewById(R.id.Username_edittext);
        Email_edittext = view.findViewById(R.id.Email_edittext);
        Code_edittext = view.findViewById(R.id.Code_edittext);
        Send_button = view.findViewById(R.id.Send_button);
        Verify_button = view.findViewById(R.id.Verify_button);
    }

    /**
     * Sets event listeners for UI components.
     */
    private void setEventListeners() {
        // Back button event listener
        Back_image_button.setOnClickListener(view -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Send verification code button event listener
        Send_button.setOnClickListener(view -> sendVerificationCode());

        // Verify code button event listener
        Verify_button.setOnClickListener(view -> verifyCode());
    }

    /**
     * Handles sending the verification code to the user's email.
     */
    private void sendVerificationCode() {
        // Hide the soft keyboard
        hideKeyboard();

        String username = Username_edittext.getText().toString().trim();
        String email = Email_edittext.getText().toString().trim();

        if (username.isEmpty()) {
            Util.showSnackBar("red", Send_button, "用户名不能为空!", getContext());
            return;
        }

        if (email.isEmpty()) {
            Util.showSnackBar("red", Send_button, "邮箱不能为空!", getContext());
            return;
        }

        // Query the user from the database
        BmobQuery<User> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> users, BmobException e) {
                if (users != null && !users.isEmpty()) {
                    for (User user : users) {
                        if (user.getUsername().equals(username)) {
                            if (!user.getEmail().equals(email)) {
                                Util.showSnackBar("red", Send_button, "这不是你的邮箱, 请重试!", getContext());
                                return;
                            }
                            Util.generateCode();
                            Util.sendEmail(email, Send_button, getContext());
                            verifyUser = user; // Save the user object for later use
                            return;
                        }
                    }
                }
                Util.showSnackBar("red", Send_button, "用户名不存在!", getContext());
            }
        });
    }

    /**
     * Verifies the code entered by the user.
     */
    private void verifyCode() {
        // Hide the soft keyboard
        hideKeyboard();

        String code = Code_edittext.getText().toString().trim();

        if (code.isEmpty()) {
            Util.showSnackBar("red", Send_button, "验证码不能为空!", getContext());
            return;
        }

        if (code.equals(Util.getVerificationCode())) {
            Util.showSnackBar("blue", Send_button, "请重置密码!", getContext());
            navigateToFragment(new ResetFragment(), true);
        }
        else {
            Util.showSnackBar("red", Send_button, "验证码错误!", getContext());
        }
    }

    /**
     * Hides the soft keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Navigates to a new fragment.
     *
     * @param fragment  The fragment to navigate to.
     * @param hasBundle Whether to pass data to the new fragment.
     */
    private void navigateToFragment(Fragment fragment, boolean hasBundle) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (hasBundle && verifyUser != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", verifyUser);
            fragment.setArguments(bundle);
        }

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("verify");
        transaction.commit();
    }

}
