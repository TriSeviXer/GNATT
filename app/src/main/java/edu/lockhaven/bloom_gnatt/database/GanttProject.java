package edu.lockhaven.bloom_gnatt.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import io.reactivex.rxjava3.annotations.NonNull;

@Entity(tableName = "GANTT_PROJECTS")
public class GanttProject {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "PROJECT_ID")
    public int id;

    @ColumnInfo(name = "PROJECT_NAME")
    public String name;

    @ColumnInfo(name = "PROJECT_CREATED")
    public String created;

    @ColumnInfo(name = "PROJECT_MODIFIED")
    public String modified;

}
