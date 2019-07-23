package com.learn.lister.pagerslide.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import 	androidx.fragment.app.FragmentPagerAdapter;

import com.learn.lister.pagerslide.fragment.BaseFragment;

import java.util.List;

/**
 * Created by Lister on 2017-05-18.
 * Fragment 滑动适配器
 */

public class PagerSlideAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> mFragmentList;

    public PagerSlideAdapter(FragmentManager fm, List<BaseFragment> fragmentList) {
        super(fm);
        this.mFragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
