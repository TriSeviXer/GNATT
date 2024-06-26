package edu.lockhaven.bloom_gnatt.database;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * View model for the preparing data from the repository to the UI.
 */
public class GanttModel extends AndroidViewModel {

    private GanttRepository repository;

    public GanttModel(Application application){
        super(application);
        repository = new GanttRepository(application);
    }

    /**
     * Retrieves all projects from the repository.
     * @return Live data of all the projects.
     */
    public LiveData<List<GanttProject>> getAllProjects() {
        try {
            Log.i("GANTT - Model:", "Gathering all projects.");
            return this.repository.getAllProjects();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new project.
     * @param name Project name.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createProject(String name) {

        //Get the current date and time.
        LocalDateTime now = LocalDateTime.now();
        StringBuilder builder = new StringBuilder();

        //Assign the date values.
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int year = now.getYear();

        builder.append(month + "/" + day + "/" + year);

        //Insert the new project.
        this.repository.insertProject(name, builder.toString());

    }

    /**
     * Retrieves a specific project from the database.
     * @param projectID The id of the project for retrieval.
     * @return The project data.
     */
    public GanttProject getProject(int projectID) {
        try {
            Log.i("GANTT - Model", "Gathering project from repository. (ID = " + projectID + ")");
            return this.repository.getProject(projectID);

        } catch (ExecutionException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        return null;
    }

    /**
     * Deletes a project from the database.
     * @param projectID The id of the project for deletion.
     */
    public void deleteProject(int projectID){
        this.repository.deleteProject(projectID);
    }

    /**
     * Creates a new task from raw data.
     * @param projectID Parent project ID.
     * @param name Name of the task.
     * @param description Task description.
     * @param startYear Starting year of the task.
     * @param startMonth Starting month of the task.
     * @param startDay Starting day of the task.
     * @param endYear End year of the task.
     * @param endMonth End month of the task.
     * @param endDay End day of the task.
     */
    public void createTask(int projectID, String name, String description,
                           int startYear, int startMonth, int startDay,
                           int endYear, int endMonth, int endDay){

        Log.i("GANTT - Model", "Creating a new task for project. (ID = " + projectID + ")");

        StringBuilder builder = new StringBuilder();
        builder.append(startMonth + "/" + startDay + "/" + startYear);
        String start = builder.toString();

        builder.delete(0, builder.length());
        builder.append(endMonth + "/" + endDay + "/" + endYear);
        String end = builder.toString();

        this.repository.insertTask(projectID, name, description, start, end);
    }

    /**
     * Retrieves all the tasks from the repository.
     * @return Lives data of all the tasks.
     */
    public LiveData<List<GanttTask>> getTasks(){
        return this.repository.getAllTasks();
    }

    /**
     * Retrieves all tasks within a project from the repository.
     * @param projectID The parent id.
     * @return All the tasks from the project as live data.
     */
    public LiveData<List<GanttTask>> getTasks(int projectID){
        try {
            Log.i("GANTT - Model", "Gathering tasks for project. (ID = " + projectID + ")");
            return this.repository.getAllTasks(projectID);
        } catch (ExecutionException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        return null;
    }

    /**
     * Retrieves a specific task.
     * @param taskID ID for the task.
     * @param projectID The parent id of the task.
     * @return A Gantt Task from parent project.
     */
    public GanttTask getTask(int taskID, int projectID){
        try {
            return this.repository.getTask(taskID, projectID);

        } catch (ExecutionException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        return null;
    }

    /**
     * Updates a task.
     * @param projectID The parent project id.
     * @param taskID The project for updating.
     * @param name The name of the task.
     * @param description Description of the task.
     * @param startYear Start year of the task.
     * @param startMonth Start month of the task.
     * @param startDay Start day of the task.
     * @param endYear End day of the task.
     * @param endMonth End month of the task.
     * @param endDay End day of the task.
     */
    public void updateTask(int projectID, int taskID, String name, String description,
                           int startYear, int startMonth, int startDay,
                           int endYear, int endMonth, int endDay) {

        Log.i("GANTT - Model", "Creating a new task for project. (ID = " + projectID + ")");

        //Constructs the start date.
        StringBuilder builder = new StringBuilder();
        builder.append(startMonth + "/" + startDay + "/" + startYear);
        String start = builder.toString();

        //Constructs the end date.
        builder.delete(0, builder.length());
        builder.append(endMonth + "/" + endDay + "/" + endYear);
        String end = builder.toString();

        this.repository.updateTask(projectID, taskID, name, description, start, end);
    }

    /**
     * Deletes a specific task.
     * @param taskID ID of the task.
     * @param projectID ID of the parent project of the task.
     */
    public void deleteTask(int taskID, int projectID){
        this.repository.deleteTask(taskID, projectID);
    }
}
