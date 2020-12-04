package com.sha.srecorder.listener;

import com.sha.srecorder.database.RecordedItem;

/**
 * Created by Shahul Hameed Shaik on 04/12/2020.
 */
public interface OnSavedRecordedItemClickListener {
    void onItemClick(RecordedItem recordedItem);
}
