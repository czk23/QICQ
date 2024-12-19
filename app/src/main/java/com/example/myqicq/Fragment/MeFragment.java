package com.example.myqicq.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Moment;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class MeFragment extends Fragment {
    // 常量定义
    private static final int CHOOSE_PHOTO = 2;  // 相册选择照片的请求码
    private static final String TAG = "MeFragment"; // 日志标识符

    // UI 控件
    private View view;
    private TextView Username_textview;
    private ImageView Avatar_imageview;
    private Button Logout_button;
    private ActivityResultLauncher<Intent> choosePhotoLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化图片选择返回结果处理器
        choosePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            handleImageOnKitKat(data); // 处理 KitKat 及以上版本
                        }
                        else {
                            handleImageBeforeKitKat(data); // 处理 KitKat 以下版本
                        }
                    }
                    else {
                        Util.showSnackBar("red", Logout_button, "取消选择图片", getContext());
                    }
                });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openAlbum();
                    }
                    else {
                        Util.showSnackBar("yellow", Logout_button, "您拒绝了图片访问权限", getContext());
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.me_fragment, container, false);
        initView(); // 初始化控件
        loadImage(); // 加载头像
        setEventListeners(); // 设置事件监听器
        return view;
    }

    /**
     * 初始化UI控件
     */
    private void initView() {
        Username_textview = view.findViewById(R.id.textview);
        Avatar_imageview = view.findViewById(R.id.Avatar_imageview);
        Logout_button = view.findViewById(R.id.Logout_button);

        // 设置当前用户名
        Username_textview.setText(MyApplication.getUser().getUsername());
    }

    /**
     * 加载用户头像
     */
    private void loadImage() {
        String avatarUrl = MyApplication.getUser().getAvatar();
        RequestOptions options = new RequestOptions().transform(new RoundedCorners(40)); // 设置圆角样式

        // 根据头像地址加载图片
        if ("123".equals(avatarUrl)) {
            Glide.with(requireContext()).load(R.mipmap.user).apply(options).into(Avatar_imageview);
        }
        else {
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.mipmap.user) // 加载中的默认头像
                    .apply(options)
                    .into(Avatar_imageview);
        }
    }

    /**
     * 设置控件事件监听器
     */
    private void setEventListeners() {
        // 设置登出按钮点击事件
        Logout_button.setOnClickListener(v -> {
            navigateToLogin(); // 切换至登录界面
            Util.showSnackBar("blue", Logout_button, "退出成功", getContext());
        });

        // 设置头像点击事件
        Avatar_imageview.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
            else {
                openAlbum();
            }
        });
//        Avatar_imageview.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(requireActivity(),
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            }
//            else {
//                openAlbum();
//            }
//        });
    }

    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.commit();
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // 对图片进行筛选
        choosePhotoLauncher.launch(intent);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openAlbum();
//            }
//            else {
//                Util.showSnackBar("yellow", Logout_button, "您拒绝了权限申请", getContext());
//            }
//        }
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == CHOOSE_PHOTO && resultCode == Activity.RESULT_OK) {
//            if (Build.VERSION.SDK_INT >= 19) {
//                assert data != null;
//                handleImageOnKitKat(data);
//            }
//            else {
//                assert data != null;
//                handleImageBeforeKitKat(data);
//            }
//        }
//    }

    /**
     * 处理Android 4.4以上版本的图片选择
     */
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            String docId = DocumentsContract.getDocumentId(uri);

            assert uri != null;
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
        }
        else {
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath);
    }

    /**
     * 处理Android 4.4以下版本的图片选择
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    /**
     * 获取图片的真实路径
     */
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }
        return path;
    }

    /**
     * 显示图片到ImageView并上传
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // 图片压缩
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            Avatar_imageview.setImageBitmap(bitmap);
            updateAvatar(imagePath);
        }
        else {
            Util.showSnackBar("red", Logout_button, "无法获取照片路径", getContext());
        }
    }

    /**
     * 更新用户头像链接
     */
    private void updateAvatar(String url) {
        // 获取当前用户
        User user = MyApplication.getUser();
        String userId = user.getObjectId();
        String username = user.getUsername();

        // 更新用户自身的头像
        user.setAvatar(url);
        user.update(userId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    // 用户头像更新成功，继续更新 Moment 表中的头像
                    updateMomentAvatar(username, url);
                    Util.showSnackBar("blue", Logout_button, "头像更新成功", getContext());
                } else {
                    Util.showSnackBar("red", Logout_button, "头像更新失败", getContext());
                }
            }
        });
    }

    private void updateMomentAvatar(String username, String avatarUrl) {
        // 查询 Moment 表中所有与 userId 相同的记录
        BmobQuery<Moment> query = new BmobQuery<>();
        query.addWhereEqualTo("user", username);

        query.findObjects(new FindListener<Moment>() {
            @Override
            public void done(List<Moment> moments, BmobException e) {
                if (e == null) {
                    if (moments != null && !moments.isEmpty()) {
                        Log.d("updateMomentAvatar", "Found " + moments.size() + " moments to update.");

                        // 用于存储需要批量更新的 Moment 对象
                        List<BmobObject> updatedMoments = new ArrayList<>();

                        // 遍历所有匹配的记录并设置新的头像
                        for (Moment moment : moments) {
                            Moment moment_t = new Moment(moment);
                            moment_t.setAvatar(avatarUrl);
                            moment_t.setObjectId(moment.getObjectId());
                            updatedMoments.add(moment_t);  // 将修改后的 moment 放入列表中
                        }

                        // 使用 updateBatch 进行批量更新
                        new BmobBatch().updateBatch(updatedMoments).doBatch(new QueryListListener<BatchResult>() {
                            @Override
                            public void done(List<BatchResult> results, BmobException e) {
                                if (e == null) {
                                    // 所有 Moment 更新成功
                                    Log.d("updateMomentAvatar", "Successfully updated all moments.");
                                } else {
                                        Log.e("updateMomentAvatar", "Failed to update moment: " + e.getMessage());
                                    Util.showSnackBar("red", Logout_button, "批量更新动态头像失败", getContext());
                                }
                            }
                        });
                    } else {
                        Log.d("updateMomentAvatar", "No moments found for username: " + username);
                    }
                } else {
                    Log.e("updateMomentAvatar", "Query failed: " + e.getMessage());
                    Util.showSnackBar("red", Logout_button, "查询动态信息失败：" + e.getMessage(), getContext());
                }
            }
        });
    }
}
