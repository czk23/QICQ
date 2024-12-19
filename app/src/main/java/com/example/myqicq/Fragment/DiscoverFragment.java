package com.example.myqicq.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.myqicq.Activity.MapActivity;
import com.example.myqicq.Activity.MomentActivity;
import com.example.myqicq.R;

public class DiscoverFragment extends Fragment {
    // 界面视图
    private View view;

    // 功能模块：动态、位置布局
    private ConstraintLayout Moment_layout;
    private ConstraintLayout Location_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 使用 inflater 将 fragment_discover.xml 布局文件加载到视图中
        view = inflater.inflate(R.layout.discover_fragment, container, false);
        // 初始化界面元素
        initView();
        // 设置事件监听
        setEvent();

        return view;
    }

    /**
     * 初始化界面组件
     * 通过 findViewById 获取布局中的各个控件，方便后续使用
     */
    private void initView() {
        // 获取动态模块布局
        Moment_layout = view.findViewById(R.id.Moment_layout);
        // 获取位置模块布局
        Location_layout = view.findViewById(R.id.Location_layout);
    }

    /**
     * 设置各个模块的点击事件
     * 在点击不同的布局时，启动对应的 Activity
     */
    private void setEvent() {
        // 设置“动态”模块的点击事件
        Moment_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到 MomentActivity，展示动态内容
                Intent intent = new Intent(getContext(), MomentActivity.class);
                startActivity(intent);
            }
        });

        // 设置“位置”模块的点击事件
        Location_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MapActivity，展示位置相关内容
                Intent intent = new Intent(getContext(), MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
