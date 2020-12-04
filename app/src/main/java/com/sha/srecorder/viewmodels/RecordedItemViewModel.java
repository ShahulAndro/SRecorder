package com.sha.srecorder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sha.srecorder.database.RecordedItem;

/**
 * Created by Shahul Hameed Shaik on 04/12/2020.
 */
public class RecordedItemViewModel extends ViewModel {
    private final MutableLiveData<RecordedItem> addedRecordedItem = new MutableLiveData<RecordedItem>();
    public void insertedRecordedItem(RecordedItem recordedItem) {
        addedRecordedItem.setValue(recordedItem);
    }
    public LiveData<RecordedItem> getAddedDBRecordedItem() {
        return addedRecordedItem;
    }
}
