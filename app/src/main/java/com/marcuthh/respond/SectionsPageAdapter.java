package com.marcuthh.respond;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SectionsPageAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment frag, String title) {
        mFragmentList.add(frag);
        mFragmentTitleList.add(title);
    }

    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public Fragment getFragmentByTag(String fragTag) {

        for (int i = 0; i < mFragmentList.size(); i++) {
            if (mFragmentList.get(i).getTag().equals(fragTag)) {
                return mFragmentList.get(i);
            }
        }

        return null;
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
