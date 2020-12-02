package com.sha.srecorder.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sha.srecorder.R;
import com.sha.srecorder.fragment.RecordFragment;
import com.sha.srecorder.fragment.SavedRecordingFragment;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */

public class ShaViewPagerAdapter extends FragmentPagerAdapter {

    private String[] titles;

    public ShaViewPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);

        titles = new String[]{context.getString(R.string.record),
                context.getString(R.string.saved_recordings)};
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:{
                return RecordFragment.newInstance(position);
            }
            case 1:{
                return SavedRecordingFragment.newInstance(position);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
