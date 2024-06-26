package edu.lockhaven.bloom_gnatt;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDateTime;

import edu.lockhaven.bloom_gnatt.database.GanttModel;
import edu.lockhaven.bloom_gnatt.database.GanttTask;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TaskActivity extends AppCompatActivity {

    private final static int ACTIVITY_STRING_TO_YEAR = 0;
    private final static int ACTIVITY_STRING_TO_MONTH = 1;
    private final static int ACTIVITY_STRING_TO_DAY = 2;

    private GanttModel model;
    private EditText name, description;
    private TextView startDate, endDate;

    private int projectID, taskID;
    private int startYear = 0, startMonth = 0, startDay = 0;
    private int endYear = 0, endMonth = 0, endDay = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        this.configureViews();
        this.configureModel();
        this.configureListeners();
        this.configureIntent();
    }

    /**
     * Configure the activity's data.
     */
    protected void configureModel(){
        this.model = new ViewModelProvider(this).get(GanttModel.class);
    }

    /**
     * Configures the activity's views.
     */
    protected void configureViews(){

        //Find views
        this.name = this.findViewById(R.id.edittext_name);
        this.description = this.findViewById(R.id.edittext_description);
        this.startDate = this.findViewById(R.id.textview_start_date);
        this.endDate = this.findViewById(R.id.textview_end_date);

    }

    /**
     * Configures the activity's listeners.
     */
    protected void configureListeners(){
        //Configure listener for the start date button.
        ((Button) this.findViewById(R.id.button_start_date))
                .setOnClickListener(this::displayDialogSDate);

        //Configure listener for the end date button.
        ((Button) this.findViewById(R.id.button_end_date))
                .setOnClickListener(this::displayDialogEDate);
    }

    /**
     * Configures the activity's intent.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void configureIntent(){

        //Retrieve the status from intent.

        Bundle bundle = this.getIntent().getExtras();

        String code = bundle.getString(this.getString(R.string.key_status));
        this.projectID = bundle.getInt(this.getString(R.string.key_current_project));

        //Check if status was ok.
        if(this.getString(R.string.status_ok).equals(code)){

            //Extract task information.
            this.taskID = bundle.getInt(this.getString(R.string.key_current_task));
            GanttTask task = model.getTask(this.taskID, projectID);

            //Send information to edit text.
            this.name.setText(task.name);
            this.description.setText(task.description);

            //Update the start date.
            this.updateSDate(
                    this.parseDate(task.start, TaskActivity.ACTIVITY_STRING_TO_YEAR),
                    this.parseDate(task.start, TaskActivity.ACTIVITY_STRING_TO_MONTH),
                    this.parseDate(task.start, TaskActivity.ACTIVITY_STRING_TO_DAY)
            );

            //Update the end date.
            this.updateEDate(
                    this.parseDate(task.end, TaskActivity.ACTIVITY_STRING_TO_YEAR),
                    this.parseDate(task.end, TaskActivity.ACTIVITY_STRING_TO_MONTH),
                    this.parseDate(task.end, TaskActivity.ACTIVITY_STRING_TO_DAY)
            );

            ((Button) this.findViewById(R.id.activity_task_button_accept)).setOnClickListener(this::updateTask);

        } else {

            //Configure the current time.
            LocalDateTime now = LocalDateTime.now();

            int month = now.getMonthValue();
            int day = now.getDayOfMonth();
            int year = now.getYear();

            this.updateSDate(year, month, day);
            this.updateEDate(year, month, day);

            ((Button) this.findViewById(R.id.activity_task_button_accept)).setOnClickListener(this::createTask);
        }
    }

    /**
     * Update the start date.
     * @param year Is the start year.
     * @param month Is the start month.
     * @param day Is the start day.
     * @return The date as a string.
     */
    protected String updateSDate(int year, int month, int day){

        //Assign all values to the start date.
        this.startYear = year;
        this.startMonth = month;
        this.startDay = day;

        //Update the view.
        String date = this.buildDate(year, month, day);
        this.startDate.setText(date);

        return date;
    }

    /**
     * Update the end date.
     * @param year Is the end year.
     * @param month Is the end month.
     * @param day Is the end day.
     * @return The date as a string.
     */
    protected String updateEDate(int year, int month, int day){

        //Assign all values to the end date.
        this.endYear = year;
        this.endMonth = month;
        this.endDay = day;

        //Update the view.
        String date = this.buildDate(year, month, day);
        this.endDate.setText(date);

        return date;
    }

    /**
     * Displays a dialog for the selecting a start date.
     * @param view Is the view being used.
     */
    protected void displayDialogSDate(View view){
        DatePickerDialog dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((datePicker, year, month, day) -> {

            //Increment month since months start with 0.
            ++month;

            //Check if start date is after end date.
            if(this.endYear < year ||
                    (this.endMonth < month && this.endYear == year) ||
                    (this.endDay < day && this.endMonth == month)){
                Toast.makeText(this, "Cannot set the start date after the end date.", Toast.LENGTH_LONG).show();
                return;
            }

            //Update the end date.
            String date = this.updateSDate(year, month, day);

            //Update the user that a start date has been set.
            Toast.makeText(this, "Start date set to '" + date + "'.", Toast.LENGTH_LONG).show();

        });
        dialog.show();
    }

    /**
     *
     * @param view
     */
    protected void displayDialogEDate(View view){
        DatePickerDialog dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((datePicker, year, month, day) -> {

            //Increment month since months start with 0.
            ++month;

            //Check if end date is before start date.
            if(this.startYear > year ||
                    (this.startMonth > month && this.startYear == year ) ||
                    (this.startDay > day && this.startMonth == month)){
                Toast.makeText(this, "Cannot set the end date before the start date.", Toast.LENGTH_LONG).show();
                return;
            }

            //Update the end date.
            String date = this.updateEDate(year, month, day);

            //Update the user that a end date has been set.
            Toast.makeText(this, "End date set to '" + date + "'.", Toast.LENGTH_LONG).show();
        });
        dialog.show();
    }

    /**
     * Updates a task.
     * @param view Is the parent view.
     */
    protected void updateTask(View view){

        //Update name and description.
        String name = this.name.getText().toString();
        String description = this.description.getText().toString();

        //Checks the edit text.
        if(isTextEmpty(name, description)){
            return;
        }

        //Update the current task.
        this.model.updateTask(
                this.projectID,
                this.taskID,
                name,
                description,
                this.startYear,
                this.startMonth,
                this.startDay,
                this.endYear,
                this.endMonth,
                this.endDay
        );

        //Ends the current activity.
        this.finish();
    }

    /**
     * Creates a new task.
     * @param view Is the parent view.
     */
    protected void createTask(View view){

        //Update name and description.
        String name = this.name.getText().toString();
        String description = this.description.getText().toString();

        //Checks the edit text.
        if(this.isTextEmpty(name, description)){
            return;
        }

        //Create a new task.
        this.model.createTask(
                this.projectID,
                name,
                description,
                this.startYear,
                this.startMonth,
                this.startDay,
                this.endYear,
                this.endMonth,
                this.endDay
        );

        //Ends the current activity.
        this.finish();
    }

    /**
     * Constructs a date.
     * @param year The year used in the date.
     * @param month The month used in the date.
     * @param day The day used in the date.
     * @return
     */
    protected String buildDate(int year, int month, int day){
        StringBuilder builder = new StringBuilder(month + "/" + day + "/" + year);
        return builder.toString();
    }

    /**
     * Parses a date into a value.
     * @param date The date for parsing.
     * @param type A constant for which part of the date is to be returned.
     * @return A type of date information.
     */
    protected int parseDate(String date, int type){

        int value;
        int index1, index2;

        switch (type){
            case 0:
                index1 = date.lastIndexOf('/') + 1;
                value = Integer.parseInt(date.substring(index1));
                Log.i("DATE YEAR", "" + value);
                return value;
            case 2:
                index1 = date.indexOf('/') + 1;
                index2 = date.lastIndexOf('/');
                value =  Integer.parseInt(date.substring(index1, index2));
                Log.i("DATE MONTH", "" + value);
                return value;
            case 1:
                index1 = date.indexOf('/');
                value = Integer.parseInt(date.substring(0, index1));
                Log.i("DATE DAY", "" + value);
                return value;
            default:
                return 0;
        }
    }

    /**
     * Checks if the strings for the edit text are empty.
     * @param name The name data.
     * @param description The description data.
     * @return True if the data is missing.
     */
    protected boolean isTextEmpty(String name, String description){

        //Checks the name string.
        if(name.isEmpty()){
            Toast.makeText(this, "Cannot leave name blank.", Toast.LENGTH_SHORT).show();
            return true;
        }

        //Checks the description string.
        if(description.isEmpty()){
            Toast.makeText(this, "Cannot leave description blank.", Toast.LENGTH_SHORT).show();
            return true;
        }

        //Returns false when all the fields are not empty.
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //ID values.
        outState.putInt(this.getString(R.string.key_current_project), this.projectID);
        outState.putInt(this.getString(R.string.key_current_task), this.taskID);

        //Start year values.
        outState.putInt(this.getString(R.string.key_start_year), this.startYear);
        outState.putInt(this.getString(R.string.key_start_month), this.startMonth);
        outState.putInt(this.getString(R.string.key_start_day), this.startDay);

        //End date values.
        outState.putInt(getString(R.string.key_end_year), this.endYear);
        outState.putInt(getString(R.string.key_end_month), this.endMonth);
        outState.putInt(getString(R.string.key_end_day), this.endDay);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Update the ids.
        this.projectID = savedInstanceState.getInt(this.getString(R.string.key_current_project));
        this.taskID = savedInstanceState.getInt(this.getString(R.string.key_current_task));

        //Update the starting dates.
        this.updateSDate(
                savedInstanceState.getInt(this.getString(R.string.key_start_year)),
                savedInstanceState.getInt(this.getString(R.string.key_start_month)),
                savedInstanceState.getInt(this.getString(R.string.key_start_day))
        );

        //Update the end dates.
        this.updateEDate(
                savedInstanceState.getInt(this.getString(R.string.key_end_year)),
                savedInstanceState.getInt(this.getString(R.string.key_end_month)),
                savedInstanceState.getInt(this.getString(R.string.key_end_day))
        );

    }
}
