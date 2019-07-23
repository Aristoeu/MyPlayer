package com.learn.lister.pagerslide.fragment;

import android.content.Context;
import android.os.Bundle;
import 	androidx.annotation.Nullable;
import 	androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lister on 2017-05-18.
 * 所有 Fragment 的基类
 */

public abstract class BaseFragment extends Fragment {

    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 继承此类的子类必须重写此方法加载布局
     */
    public abstract View initView();

    /**
     * 加载数据的方法
     */
    public void initData() { }
}
