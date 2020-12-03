package com.sha.srecorder.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by Shahul Hameed Shaik on 02/12/2020.
 */

@Entity(tableName = "recorded_item")
public class RecordedItem implements Serializable {

    @PrimaryKey (autoGenerate = true)
    public int rid;

    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "recorded_length")
    public long recordedLength;

    @ColumnInfo(name = "recorded_time")
    public long recordedTime;
}
