package com.example.myqicq.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myqicq.MyApplication;
import com.example.myqicq.Object.Moment;
import com.example.myqicq.Object.User;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;


import java.io.File;
import java.util.Objects;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class EditActivity extends AppCompatActivity {
    private static final int CHOOSE_PHOTO = 2;  // Request code for opening the photo album

    private ImageButton EditBack_imagebutton;  // Back button to exit the activity
    private EditText Content_edittext;        // Text field for the user to write the moment's content
    private Button ChooseImage_button;        // Button to open the image picker
    private ImageView Photo_imageview;        // Image view to display the selected photo
    private TextView Post_textview;           // Text view to post the moment
    private String photoPath = null;         // Path to the selected photo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        initView();  // Initialize the UI components
        setEvent();  // Set up event listeners
    }

    /**
     * Initializes the views for the EditActivity.
     */
    private void initView() {
        EditBack_imagebutton = findViewById(R.id.EditBack_image_button);
        Content_edittext = findViewById(R.id.Content_edittext);
        ChooseImage_button = findViewById(R.id.MomentPhoto_button);
        Photo_imageview = findViewById(R.id.ChoosePhoto_imageView);
        Post_textview = findViewById(R.id.Post_textview);
    }

    /**
     * Sets up event listeners for buttons and other UI components.
     */
    private void setEvent() {
        // Back button click listener: Finish the activity
        EditBack_imagebutton.setOnClickListener(v -> finish());

        // Choose image button click listener: Request permissions and open album
        ChooseImage_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // Request permission to access external storage
                ActivityCompat.requestPermissions(EditActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
            else {
                // Permission granted, open album
                openAlbum();
            }
        });

        // Post button click listener: Post the moment with text and/or photo
        Post_textview.setOnClickListener(view -> {
            // Hide keyboard when posting
            InputMethodManager imm = (InputMethodManager) EditActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            String content = Content_edittext.getText().toString().trim();

            // Validate input: Ensure content is not empty
            if (content.isEmpty()) {
                Util.showSnackBar("red", Post_textview, "文本不能为空", EditActivity.this);
                return;
            }

            // Get the current user
            User me = MyApplication.getUser();

            // Create a new Moment object based on user input
            final Moment[] moment = new Moment[1];
            // No photo selected
            // Upload photo to Bmob
            moment[0] = new Moment(me.getAvatar(), me.getUsername(), content, Objects.requireNonNullElse(photoPath, "123"), 0);

            // Save the moment to the server
            saveMoment(moment[0]);
        });
    }

    /**
     * Saves the moment to the web server using Bmob.
     *
     * @param moment The moment object to be saved
     */
    private void saveMoment(Moment moment) {
        moment.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    Util.showSnackBar("blue", Post_textview, "动态发布成功", EditActivity.this);
                    Intent intent = new Intent(EditActivity.this, MomentActivity.class);
                    startActivity(intent);
                }
                else {
                    Util.showSnackBar("red", Post_textview, "动态发布失败", EditActivity.this);
                }
            }
        });
    }

    /**
     * Opens the photo album for the user to select an image.
     */
    @SuppressWarnings("deprecation")
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");  // Set MIME type to image
        startActivityForResult(intent, CHOOSE_PHOTO);  // Start activity for result
    }

    /**
     * Handles the result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open album
                openAlbum();
            }
            else {
                // Permission denied, show a message
                Util.showSnackBar("yellow", ChooseImage_button, "没有权限访问相册", EditActivity.this);
            }
        }
    }

    /**
     * Handles the result of the photo album activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PHOTO && resultCode == RESULT_OK && data != null) {
            // Handle photo selection based on Android version
            if (Build.VERSION.SDK_INT >= 19) {
                handleImageOnKitKat(data);
            }
            else {
                handleImageBeforeKitKat(data);
            }
        }
    }

    /**
     * Handles image selection for Android KitKat and above.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(EditActivity.this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            assert uri != null;
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // Handle image from Media Provider
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
        }
        else {
            assert uri != null;
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Handle image from content URI
                imagePath = getImagePath(uri, null);
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                // Handle image from file URI
                imagePath = uri.getPath();
            }
        }
        displayImage(imagePath);
    }

    /**
     * Handles image selection for Android versions before KitKat.
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    /**
     * Retrieves the image file path from the given URI.
     *
     * @param uri The URI to retrieve the image path from
     * @param selection The selection query for specific results
     * @return The image file path
     */
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }

    /**
     * Displays the selected image in the ImageView.
     *
     * @param imagePath The path to the image
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            photoPath = imagePath;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Photo_imageview.setImageBitmap(bitmap);
        }
        else {
            Util.showSnackBar("yellow", ChooseImage_button, "获取图像失败", EditActivity.this);
        }
    }

}
