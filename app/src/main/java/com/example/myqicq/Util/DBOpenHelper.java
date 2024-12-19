package com.example.myqicq.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myqicq.Object.Request;
import com.example.myqicq.Object.User;

import java.util.ArrayList;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "QICQ_db"; // 数据库名称
    private static final int DATABASE_VERSION = 4;         // 数据库版本

    // 用户表和请求表的 SQL 语句
    private static final String TABLE_USERS = "users";
    private static final String TABLE_REQUESTS = "requests";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "(" +
                    "userId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username VARCHAR," +
                    "password VARCHAR" +
                    ")";

    private static final String SQL_CREATE_REQUESTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_REQUESTS + "(" +
                    "requestId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "requesterId VARCHAR," +
                    "receiverId VARCHAR," +
                    "requesterName VARCHAR," +
                    "requesterAvatar VARCHAR" +
                    ")";

    private SQLiteDatabase db;

    /**
     * 构造方法：初始化数据库。
     * @param context 上下文
     */
    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase(); // 打开可写数据库
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 初始化用户表和请求表
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_REQUESTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库时删除旧表，重新创建
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS);
        onCreate(db);
    }

    /**
     * 插入用户数据。
     * @param username 用户名
     * @param password 密码
     */
    public void insertUser(String username, String password) {
        db.execSQL("INSERT INTO " + TABLE_USERS + "(username, password) VALUES (?, ?)",
                new Object[]{username, password});
    }

    /**
     * 插入好友请求数据。
     * @param requesterId    请求者ID
     * @param receiverId     接收者ID
     * @param requesterName  请求者名字
     * @param requesterAvatar 请求者头像
     */
    public void insertRequest(String requesterId, String receiverId, String requesterName, String requesterAvatar) {
        db.execSQL("INSERT INTO " + TABLE_REQUESTS + "(requesterId, receiverId, requesterName, requesterAvatar) VALUES (?, ?, ?, ?)",
                new Object[]{requesterId, receiverId, requesterName, requesterAvatar});
    }

    /**
     * 获取用户数据表的所有用户信息。
     * @return 包含所有用户的 ArrayList
     */
    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();

        try (Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex("username"));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                users.add(new User(username, password, "123", ""));
            }
        }
        // 确保Cursor关闭

        return users;
    }

    /**
     * 删除指定的好友请求记录。
     * @param requesterId 请求者ID
     * @param receiverId  接收者ID
     */
    public void deleteRequest(String requesterId, String receiverId) {
        db.execSQL("DELETE FROM " + TABLE_REQUESTS + " WHERE requesterId = ? AND receiverId = ?",
                new Object[]{requesterId, receiverId});
    }

    /**
     * 清空好友请求表的数据。
     */
    public void clearAllRequests() {
        db.execSQL("DELETE FROM " + TABLE_REQUESTS);
    }

    /**
     * 获取所有好友请求的数据。
     * @return 包含所有好友请求的 ArrayList
     */
    public ArrayList<Request> getAllRequests() {
        ArrayList<Request> requests = new ArrayList<>();

        try (Cursor cursor = db.query(TABLE_REQUESTS, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String requesterId = cursor.getString(cursor.getColumnIndex("requesterId"));
                @SuppressLint("Range") String receiverId = cursor.getString(cursor.getColumnIndex("receiverId"));
                @SuppressLint("Range") String requesterName = cursor.getString(cursor.getColumnIndex("requesterName"));
                @SuppressLint("Range") String requesterAvatar = cursor.getString(cursor.getColumnIndex("requesterAvatar"));
                requests.add(new Request(requesterId, receiverId, requesterName, requesterAvatar));
            }
        }
        // 确保Cursor关闭

        return requests;
    }
}
