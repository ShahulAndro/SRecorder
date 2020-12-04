package com.sha.srecorder.listener;

import android.content.Context;

/**
 * Created by Shahul Hameed Shaik on 04/12/2020.
 */
public interface OnRenameDeleteDBChangedListener {
    Context getContext();
    void onRenameRecordedItem(int position);
    void onDeleteRecordedItem(int position);
}
