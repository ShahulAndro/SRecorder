package com.sha.srecorder.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */

@Dao
public interface RecordedItemDao {
    @Query("SELECT * FROM recorded_item")
    List<RecordedItem> getAll();

    @Insert
    void insert(RecordedItem ... recordedItems);

    @Update
    void update(RecordedItem ... recordedItems);

    @Delete
    void delete(RecordedItem recordedItem);
}
