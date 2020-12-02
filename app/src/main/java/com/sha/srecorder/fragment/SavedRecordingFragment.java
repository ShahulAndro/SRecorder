package com.sha.srecorder.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */
public class SavedRecordingFragment extends Fragment {

    private static final String POSITION = "position";

    public static SavedRecordingFragment newInstance(int position) {
        SavedRecordingFragment savedRecordingFragment = new SavedRecordingFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        savedRecordingFragment.setArguments(b);

        return savedRecordingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
