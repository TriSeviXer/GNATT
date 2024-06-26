package edu.lockhaven.bloom_gnatt.database;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for managing save data.
 */
@Database(entities = {GanttProject.class, GanttTask.class}, version = 1, exportSchema = false)
public abstract class GanttDatabase extends RoomDatabase{

    public abstract GanttProjectDao project();
    public abstract GanttTaskDao task();

    private static volatile GanttDatabase INSTANCE;
    private static final int THREAD_COUNT = 4;
    public static final ExecutorService DB_WRITER = Executors.newFixedThreadPool(THREAD_COUNT);

    /**
     * Retrieves a database.
     * @param context Parent activity context.
     * @return The database.
     */
    public static GanttDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (GanttDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GanttDatabase.class, "GANTT_DATABASE").build();
                }
            }
        }
        return INSTANCE;
    }

    @Dao
    public interface GanttProjectDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public void insert(GanttProject... project);

        @Update
        public void update(GanttProject... project);

        @Delete
        public void delete(GanttProject... project);

        @Query("DELETE FROM GANTT_PROJECTS WHERE PROJECT_ID = :id")
        public void delete(int id);

        @Query("SELECT * FROM GANTT_PROJECTS WHERE PROJECT_ID = :id ORDER BY PROJECT_MODIFIED ASC")
        public GanttProject get(int id);

        @Query("SELECT * FROM GANTT_PROJECTS")
        public LiveData<List<GanttProject>> getAll();

        @Query("SELECT * FROM GANTT_PROJECTS WHERE EXISTS (SELECT * FROM GANTT_PROJECTS WHERE PROJECT_ID = :id)")
        public boolean isExisting(int id);
    }

    @Dao
    public interface GanttTaskDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public void insert(GanttTask... task);

        @Update
        public void update(GanttTask... task);

        @Delete
        public void delete(GanttTask... task);

        @Query("DELETE FROM GANTT_TASK WHERE PROJECT_ID = :pid")
        public void delete(int pid);

        @Query("DELETE FROM GANTT_TASK WHERE TASK_ID = :tid AND PROJECT_ID = :pid")
        public void delete(int tid, int pid);

        @Query("SELECT * FROM GANTT_TASK WHERE TASK_ID = :tid AND PROJECT_ID = :pid")
        public GanttTask get(int tid, int pid);

        @Query("SELECT * FROM GANTT_TASK")
        public LiveData<List<GanttTask>> getAll();

        @Query("SELECT * FROM GANTT_TASK WHERE PROJECT_ID = :id ORDER BY TASK_START ASC")
        public LiveData<List<GanttTask>> getAll(int id);

        @Query("SELECT * FROM GANTT_TASK WHERE EXISTS (SELECT * FROM GANTT_TASK WHERE TASK_ID = :tid AND PROJECT_ID = :pid)")
        public boolean isExisting(int tid, int pid);

    }




}
