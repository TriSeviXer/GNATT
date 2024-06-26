package edu.lockhaven.bloom_gnatt.database;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Repository for accessing the database.
 */
public class GanttRepository {

    private final GanttDatabase.GanttProjectDao DAO_PROJECT;
    private final GanttDatabase.GanttTaskDao DAO_TASK;

    GanttRepository(Context context){
        GanttDatabase db = GanttDatabase.getDatabase(context);
        this.DAO_PROJECT = db.project();
        this.DAO_TASK = db.task();
    }

    /**
     * Retrieves all projects from the database.
     * @return A live data of all the projects in the database.
     */
    public LiveData<List<GanttProject>> getAllProjects() throws ExecutionException, InterruptedException {

        Future<LiveData<List<GanttProject>>> future = GanttDatabase.DB_WRITER.submit(() -> {
            return this.DAO_PROJECT.getAll();
        });

        return future.get();
    }

    /**
     * Inserts a new project into the database.
     * @param name Name of the project.
     * @param date Date the project was created on.
     */
    public void insertProject(String name, String date) {
        GanttDatabase.DB_WRITER.execute(() -> {

            //Create new project to store values in.
            GanttProject project = new GanttProject();

            //Fill in project fields.
            project.name = name;
            project.created = date;
            project.modified = date;

            int id = 0;

            //Create a unique id for project entry.
            do {
                id = ThreadLocalRandom.current().nextInt();
            } while(this.DAO_PROJECT.isExisting(id));

            project.id = id;

            //Insert project into database.
            this.DAO_PROJECT.insert(project);

        });
    }

    /**
     * Updates a project using an id.
     * @param id The id of the project.
     * @param name New name of the project.
     * @param date New modification date.
     */
    public void updateProject(int id, String name, String date){
        GanttDatabase.DB_WRITER.execute(() -> {

            //Retrieve project from database.
            GanttProject project = this.DAO_PROJECT.get(id);

            //Update project name.
            project.name = name;
            project.modified = date;

            //Update the project in the database.
            this.DAO_PROJECT.update(project);

        });
    }

    /**
     * Updates a project's modification date.
     * @param id The id of the project.
     * @param date New modified date.
     */
    public void updateProject(int id, String date){
        GanttDatabase.DB_WRITER.execute(() -> {

            //Retrieve project form database.
            GanttProject project = this.DAO_PROJECT.get(id);
            project.modified = date;

            //Update the project in the database.
            this.DAO_PROJECT.update(project);

        });
    }

    /**
     * Retrieves a project form the database.
     * @param id The project id.
     * @return A Gantt Project.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public GanttProject getProject(int id) throws ExecutionException, InterruptedException {

        Log.i("GANTT - Repository", "Retrieving project from database. (PID = " + id + ")");

        Future<GanttProject> future = GanttDatabase.DB_WRITER.submit(() -> {
            return this.DAO_PROJECT.get(id);
        });

        Log.i("GANTT - Repository", "Returning project data...");
        return future.get();
    }

    /**
     * Deletes a project.
     * @param id The project id.
     */
    public void deleteProject(int id) {
        GanttDatabase.DB_WRITER.execute(() -> {
            this.DAO_TASK.delete(id);
            this.DAO_PROJECT.delete(id);
        });
    }

    /**
     * Creates a new task and inserts it into the database.
     * @param projectID The parent project id.
     * @param name Name of the project.
     * @param description Description of the project.
     * @param start Start date of the project.
     * @param end End date of the project.
     */
    public void insertTask(int projectID, String name, String description, String start, String end){
        GanttDatabase.DB_WRITER.execute(() -> {

            GanttTask task = new GanttTask();
            Log.i("GANTT - Repository", "Inserting task into project. (PID = " + projectID + ")");

            //Check if task belongs to an existing project.
            //  NOTE: This is to prevent inaccessible entries.
            if(!this.DAO_PROJECT.isExisting(projectID)){
                Log.e("GANTT - Repository", "Could not create task. Project ID does not exist.");
                return;
            }

            //Fill project fields.
            task.name = name;
            task.description = description;
            task.start = start;
            task.end = end;

            int id = 0;

            do {
                //Create an id and check if the id exists.
                id = ThreadLocalRandom.current().nextInt();

            } while (this.DAO_TASK.isExisting(id, projectID));

            task.id = id;
            task.project = projectID;

            Log.i("GANTT - Repository", "Generated id for task. (PID = " + task.project + ", TID = " + task.id + ")");

            //Insert task into database.
            this.DAO_TASK.insert(task);

        });
    }

    /**
     * Updates a task in the database.
     * @param task The task entity to update.
     */
    public void updateTask(GanttTask task){
        GanttDatabase.DB_WRITER.execute(() -> {
            this.DAO_TASK.update(task);
        });
    }

    /**
     * Retrieves all the tasks from the database.
     * @return Live data of all the tasks.
     */
    public LiveData<List<GanttTask>> getAllTasks(){

        Log.i("GANTT - Repository", "Retrieving all tasks from database.");

        Future<LiveData<List<GanttTask>>> future = GanttDatabase.DB_WRITER.submit(() -> {
            return this.DAO_TASK.getAll();
        });

        return this.DAO_TASK.getAll();

    }

    /**
     * Retrieves all tasks within a project.
     * @param id Is the parent project's id.
     * @return Live data of tasks.
     */
    public LiveData<List<GanttTask>> getAllTasks(int id) throws ExecutionException, InterruptedException {

        Log.i("GANTT - Repository", "Retrieving tasks from database for project. (PID = " + id + ")");

        Future<LiveData<List<GanttTask>>> future = GanttDatabase.DB_WRITER.submit(() -> {
            return this.DAO_TASK.getAll(id);
        });

        return future.get();
    }

    /**
     * Retrieves a task from a project.
     * @param taskID The task id.
     * @param projectID The parent project id.
     * @return The desired Gantt Task.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public GanttTask getTask(int taskID, int projectID) throws ExecutionException, InterruptedException {

        Log.i("GANTT - Repository", "Retrieving a task from project. (PID = " +
                projectID + ", TID = " + taskID + ")");

        Future<GanttTask> future = GanttDatabase.DB_WRITER.submit(() -> {
            return this.DAO_TASK.get(taskID, projectID);
        });

        return future.get();
    }

    /**
     * Updates a task in the database with individual properties.
     * @param projectID The parent project id.
     * @param taskID Task id.
     * @param name Name of the task.
     * @param description Description of the task.
     * @param start Start date of the task.
     * @param end End date of the task.
     */
    public void updateTask(int projectID, int taskID, String name, String description, String start, String end) {
        GanttDatabase.DB_WRITER.execute(() -> {

            //Creates task to update table with.
            GanttTask task = new GanttTask();

            //Update task fields.
            task.id = taskID;
            task.project = projectID;
            task.name = name;
            task.description = description;
            task.start = start;
            task.end = end;

            //Updates the task.
            this.DAO_TASK.update(task);

        });
    }

    /**
     * Deletes a task from the database.
     * @param taskID
     * @param projectID
     */
    public void deleteTask(int taskID, int projectID){
        GanttDatabase.DB_WRITER.execute(() -> {
            this.DAO_TASK.delete(taskID, projectID);
        });
    }
}
