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
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * ResetFragment
 * Fragment for resetting user passwords. Users input and confirm a new password.
 * If successful, the password is updated in the backend.
 */
public class ResetFragment extends Fragment {

    // UI Components
    private View root_view;
    private ImageButton back_button;
    private EditText password_edittext;
    private EditText confirmPassword_edittext;
    private Button reset_button;

    // User object passed as argument
    private User verifyUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.reset_fragment, container, false);
        initView(); // Initialize UI components
        setListeners(); // Set up event listeners
        return root_view;
    }

    /**
     * Initialize the UI components and retrieve passed arguments.
     */
    private void initView() {
        back_button = root_view.findViewById(R.id.Back_image_button);
        password_edittext = root_view.findViewById(R.id.Password_edittext);
        confirmPassword_edittext = root_view.findViewById(R.id.ConfirmPassword_edittext);
        reset_button = root_view.findViewById(R.id.Reset_button);

        // Retrieve the User object from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            verifyUser = (User) bundle.getSerializable("user");
        }
    }

    /**
     * Set up event listeners for UI components.
     */
    private void setListeners() {
        // Handle back button click
        back_button.setOnClickListener(view -> {
            if (getActivity() != null) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle reset button click
        reset_button.setOnClickListener(view -> {
            hideKeyboard(); // Hide the soft keyboard

            String password = password_edittext.getText().toString().trim();
            String confirmPassword = confirmPassword_edittext.getText().toString().trim();

            // Validate user inputs
            if (!validateInputs(password, confirmPassword)) {
                return;
            }

            // Update the password in the backend
            resetPassword(password);
        });
    }

    /**
     * Hide the soft keyboard.
     */
    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(root_view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Validate password and confirmation inputs.
     *
     * @param password        New password input
     * @param confirmPassword Confirmation password input
     * @return True if inputs are valid, false otherwise
     */
    private boolean validateInputs(String password, String confirmPassword) {
        if (password.isEmpty()) {
            Util.showSnackBar("red", reset_button, "密码不能为空!", getContext());
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Util.showSnackBar("red", reset_button, "密码不一致!", getContext());
            return false;
        }
        return true;
    }

    /**
     * Reset the user's password in the backend.
     *
     * @param newPassword The new password to set
     */
    private void resetPassword(String newPassword) {
        if (verifyUser == null) {
            Util.showSnackBar("red", reset_button, "暂无当前用户!", getContext());
            return;
        }

        verifyUser.setPassword(newPassword);
        verifyUser.update(verifyUser.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Util.showSnackBar("blue", reset_button, "重置密码成功!", getContext());
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                    LoginFragment loginFragment = new LoginFragment();
                    transaction.replace(R.id.fragment_container, loginFragment);
                    transaction.commit();  // 提交事务
                }
                else {
                    Util.showSnackBar("red", reset_button, "重置密码失败, 请检查网络配置!", getContext());
                    Log.e("ResetFragment", "Error updating password", e);
                }
            }
        });
    }



}
