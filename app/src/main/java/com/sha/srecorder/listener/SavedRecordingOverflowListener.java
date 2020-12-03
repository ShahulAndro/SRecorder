package com.sha.srecorder.listener;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.sha.srecorder.database.RecordedItem;
import com.sha.srecorder.fragment.PlaybackFragment;

/**
 * Created by Shahul Hameed Shaik on 03/12/2020.
 */
public class SavedRecordingOverflowListener implements OnOverflowItemClickListener{

    @Override
    public void onPlay(Context context, RecordedItem recordedItem) {
        FragmentTransaction transaction = ((FragmentActivity) context)
                .getSupportFragmentManager()
                .beginTransaction();

        PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recordedItem);
        playbackFragment.show(transaction, "playback_fragment");
    }

    @Override
    public void onRename(RecordedItem recordedItem) {

    }

    @Override
    public void onDelete(RecordedItem recordedItem) {

    }
}
