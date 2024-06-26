package edu.lockhaven.bloom_gnatt.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.reactivex.rxjava3.annotations.NonNull;

@Entity(tableName = "GANTT_TASK", primaryKeys = {"TASK_ID", "PROJECT_ID"})
public class GanttTask {

    @NonNull
    @ColumnInfo(name = "TASK_ID")
    public int id;

    @NonNull
    @ColumnInfo(name = "PROJECT_ID")
    public int project;

    @ColumnInfo(name = "TASK_NAME")
    public String name;

    @ColumnInfo(name = "TASK_DESCRIPTION")
    public String description;

    @ColumnInfo(name = "TASK_START")
    public String start;

    @ColumnInfo(name = "TASK_END")
    public String end;

}