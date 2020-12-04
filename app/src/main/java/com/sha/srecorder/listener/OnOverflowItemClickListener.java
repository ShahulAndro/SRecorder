package com.sha.srecorder.listener;

import android.content.Context;

import com.sha.srecorder.database.RecordedItem;

/**
 * Created by Shahul Hameed Shaik on 03/12/2020.
 */
public interface OnOverflowItemClickListener {
    void onPlay(Context context, RecordedItem recordedItem);
    void onShare(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem);
    void onRename(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem, final int position);
    void onDelete(OnRenameDeleteDBChangedListener adapterListener, RecordedItem recordedItem,final int position);
}
