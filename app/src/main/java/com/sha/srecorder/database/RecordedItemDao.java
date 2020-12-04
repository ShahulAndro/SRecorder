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
    @Query("SELECT * FROM recorded_item ORDER BY rid DESC")
    List<RecordedItem> getAll();

    @Query("SELECT * FROM recorded_item WHERE rid = :id")
    RecordedItem findByRecordedItemId(long id);

    @Insert
    long insert(RecordedItem recordedItem);

    @Update
    int update(RecordedItem recordedItem);

    @Delete
    int delete(RecordedItem recordedItem);
}
