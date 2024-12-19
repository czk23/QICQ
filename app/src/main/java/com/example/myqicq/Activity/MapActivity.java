package com.example.myqicq.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ZoomControls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.share.LocationShareURLOption;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.example.myqicq.MyApplication;
import com.example.myqicq.R;
import com.example.myqicq.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private MapView mapview; // 地图视图
    private BaiduMap baiduMap; // 百度地图对象
    private LocationClient Client_location; // 定位客户端
    private Button SaveLocation_button; // 保存位置按钮
    private ImageButton MapBack_imagebutton;
    private BDLocation location; // 当前定位信息
    private boolean isFirstLocate = true; // 是否是第一次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // 初始化视图、事件、地图、定位及权限
        initView();
        setEvent();
        initMap();
        initLocation();
        initPermission();
    }

    /**
     * 初始化视图组件
     */
    private void initView() {
        mapview = findViewById(R.id.mapview); // 获取地图视图
        SaveLocation_button = findViewById(R.id.SaveLocation_button); // 获取保存位置按钮
        MapBack_imagebutton = findViewById(R.id.MapBack_image_button);

        // 隐藏地图视图中的ZoomControls和定位按钮
        View child = mapview.getChildAt(1);
        if ((child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置事件监听器
     */
    private void setEvent() {
        MapBack_imagebutton.setOnClickListener(view -> finish());
        // 保存位置按钮点击事件
        SaveLocation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建ShareUrlSearch对象，用于生成位置分享链接
                ShareUrlSearch urlSearch = ShareUrlSearch.newInstance();
                // 设置结果回调监听器
                OnGetShareUrlResultListener listener = new MyOnGetShareUrlResultListener();
                urlSearch.setOnGetShareUrlResultListener(listener);

                // 请求生成位置分享URL
                urlSearch.requestLocationShareUrl(new LocationShareURLOption()
                        .location(new LatLng(location.getLatitude(), location.getLongitude())) // 定位信息
                        .name(MyApplication.getUser().getUsername()) // 用户名
                        .snippet("The location of " + MyApplication.getUser().getUsername())); // 附加信息

                // 显示提示信息
                Util.showSnackBar("blue", SaveLocation_button, "位置信息已保存", MapActivity.this);

                // 销毁urlSearch对象
                urlSearch.destroy();
            }
        });
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        baiduMap = mapview.getMap(); // 获取地图对象
        baiduMap.setMyLocationEnabled(true); // 启用定位图层
    }

    /**
     * 初始化定位功能
     */
    private void initLocation() {
        Client_location = new LocationClient(getApplicationContext()); // 创建定位客户端
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 开启GPS定位
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000); // 设置定位请求间隔时间

        Client_location.setLocOption(option); // 设置定位参数
        Client_location.registerLocationListener(new MyLocationListener()); // 注册定位监听器
    }

    /**
     * 初始化权限检查和请求
     */
    private void initPermission() {
        List<String> permissionList = new ArrayList<>(); // 权限列表

        // 检查定位权限
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // 检查读取电话状态权限
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
        }

        // 如果有权限未授予，申请权限
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(MapActivity.this, permissions, 1);
        }
        else {
            Client_location.start(); // 启动定位
        }
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Util.showSnackBar("red", SaveLocation_button, "你必须同意这些权限才能使用定位功能", MapActivity.this);
                        return;
                    }
                }
                Client_location.start(); // 启动定位
            } else {
                Util.showSnackBar("red", SaveLocation_button, "出现错误", MapActivity.this);
            }
        }
    }

    /**
     * 定位成功后，更新地图视图
     */
    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude()); // 获取当前定位的经纬度
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll); // 设置地图中心点
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f); // 设置地图缩放级别
            baiduMap.animateMapStatus(update);
            isFirstLocate = false; // 只进行一次首次定位
        }
    }

    static class MyOnGetShareUrlResultListener implements OnGetShareUrlResultListener {
        @Override
        public void onGetPoiDetailShareUrlResult(ShareUrlResult shareUrlResult) {

        }

        @Override
        public void onGetLocationShareUrlResult(ShareUrlResult shareUrlResult) {
            String shareUrl = shareUrlResult.getUrl();
            MyApplication.setShareUrl(shareUrl);
        }

        @Override
        public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {

        }
    }

    /**
     * 自定义的定位回调监听器
     */
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // 如果地图视图已经被销毁，不再处理新的定位
            if (location == null || mapview == null) {
                return;
            }

            MapActivity.this.location = location; // 更新定位信息
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()) // 定位精度
                    .direction(location.getDirection()) // 定位方向
                    .latitude(location.getLatitude()) // 经度
                    .longitude(location.getLongitude()) // 纬度
                    .build();
            baiduMap.setMyLocationData(locData); // 更新定位数据

            // 如果是GPS定位或网络定位，更新地图
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location); // 调整地图视角
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume(); // 恢复地图
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause(); // 暂停地图
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Client_location.stop(); // 停止定位
        baiduMap.setMyLocationEnabled(false); // 禁用定位图层
        mapview.onDestroy(); // 销毁地图视图
        mapview = null; // 清空mapView引用
    }

}
