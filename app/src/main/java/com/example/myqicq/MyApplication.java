package com.example.myqicq;

import android.app.Application;
import android.content.Context;

import com.example.myqicq.Object.User;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import cn.bmob.v3.Bmob;

public class MyApplication extends Application {

    private static Context context;
    private static MyApplication instance;
    private static User user;
    private static String shareUrl = "";
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        instance = this;
        Bmob.initialize(this, "f61b81e77c9510c1ac93256a48bbc8cd");
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        MyApplication.context = context;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static void setInstance(MyApplication instance) {
        MyApplication.instance = instance;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        MyApplication.user = user;
    }

    public static String getShareUrl() {
        return shareUrl;
    }

    public static void setShareUrl(String shareUrl) {
        MyApplication.shareUrl = shareUrl;
    }
}
