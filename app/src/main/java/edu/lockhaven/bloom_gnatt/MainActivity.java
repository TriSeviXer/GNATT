package edu.lockhaven.bloom_gnatt;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.lockhaven.bloom_gnatt.database.GanttModel;
import edu.lockhaven.bloom_gnatt.database.GanttProject;
import edu.lockhaven.bloom_gnatt.database.GanttTask;
import edu.lockhaven.bloom_gnatt.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements AdapterInterface {

    //Model and recycler view.
    private GanttModel model;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;

    //Current project id.
    private int projectID = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Configures all activity bindings.
        this.configureBindings();

        //Configures intent information.
        this.configureIntent();

        //Configures creating the model and fetching tasks.
        this.configureModel();

        //Configures the recycler.
        this.configureRecycler();

    }

    /**
     * Configures the main activity bindings.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void configureBindings(){
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);
        binding.fab.setOnClickListener(this::displayTaskActivity);
    }

    /**
     * Configures the tasks of the current project.
     */
    protected void configureModel(){

        //Retrieves saved id.
        SharedPreferences shared = this.getPreferences(Context.MODE_PRIVATE);
        this.projectID = shared.getInt(this.getString(R.string.key_current_project), this.projectID);

        //Creates a view model and retrieves tasks.
        this.model = new ViewModelProvider(this).get(GanttModel.class);

        //Notify the user the project was loaded.
        GanttProject project = this.model.getProject(this.projectID);

        //Check if project was loaded or not.
        if(project == null){
            Toast.makeText(this, "Unable to load project.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Loaded '" + project.name + "'.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Configures the the intent and retrieves values.
     */
    protected void configureIntent(){

        //Retrieve the status from intent.
        String code = this.getIntent().getStringExtra(this.getString(R.string.key_status));

        //Check if status was ok.
        if(this.getString(R.string.status_ok).equals(code)){

            //Extract task information.
            this.projectID = this.getIntent().getIntExtra(this.getString(R.string.key_current_project), this.projectID);

            //Save project id.
            SharedPreferences shared = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putInt(this.getString(R.string.key_current_project),this.projectID);
            editor.apply();

        }

    }

    /**
     * Configures the current recycler view.
     */
    protected void configureRecycler(){
        RecyclerView recyclerView = this.findViewById(R.id.recycler_view);
        this.adapter = new TaskAdapter(new TaskAdapter.TaskDF(), this, this);
        recyclerView.setAdapter(this.adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.model.getTasks(this.projectID).observe(this, tasks -> {
            this.adapter.submitList(tasks);
        });
    }

    /**
     * Retrieves an activity.
     * @param activity The class of the activity to be used.
     * @param bundle A bundle for extras.
     */
    protected void displayActivity(Class<?> activity, Bundle bundle){
        Intent intent = new Intent(this, activity);
        if(bundle != null){
            intent.putExtras(bundle);
        }
        this.startActivity(intent);
    }

    /**
     * Displays the task activity.
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void displayTaskActivity(View view){

        //Create a bundle for info to send.
        Bundle bundle = new Bundle();
        bundle.putInt(this.getString(R.string.key_current_project), this.projectID);

        //Displays the task activity and gives the bundle to the activity.
        this.displayActivity(TaskActivity.class, bundle);

    }

    /**
     * Displays a dialog for creating a new GANTT project.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void displayDialog(){

        //Create a dialog box.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Inflate the dialog layout.
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_dialog_new, null);

        //Retrieve the edit text.
        EditText editText = layout.findViewById(R.id.dialog_edittext_project_name);

        //Set the title and view.
        builder.setTitle(R.string.text_new_project);
        builder.setView(layout);

        //Set the positive button to take data from edit text.
        builder.setPositiveButton(R.string.text_create, (dialogInterface, i) -> {
            String name = editText.getText().toString();
            this.createProject(name);

        });

        //Set the negative button to cancel.
        builder.setNegativeButton(R.string.text_cancel, (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        //Display the new dialog.
        builder.show();

    }

    /**
     * Creates a new project.
     * @param name
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void createProject(String name){
        if(!name.isEmpty()){

            //Creates the new project.
            this.model.createProject(name);

            //Notifies the user a project was created.
            Toast.makeText(this,
                    "Project '" + name + "' was created.",
                    Toast.LENGTH_LONG).show();
        } else {
            //Notifies the user a project was not created.
            Toast.makeText(this, "Unable to create project.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(int id) {

        Log.i("GANTT - Main", "Retrieving task for modification. (TID = " + id + ")");

        //Create information to send to the activity.
        Bundle bundle = new Bundle();
        bundle.putString(this.getString(R.string.key_status), this.getString(R.string.status_ok));
        bundle.putInt(this.getString(R.string.key_current_task), id);
        bundle.putInt(this.getString(R.string.key_current_project), this.projectID);

        //Start the activity.
        this.displayActivity(TaskActivity.class, bundle);

    }

    @Override
    public void onDelete(int id) {
        this.model.deleteTask(id, this.projectID);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(this.getString(R.string.key_current_project), projectID);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.projectID = savedInstanceState.getInt(this.getString(R.string.key_current_project), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //Check menu to open.
        switch (item.getItemId()){
            case R.id.menu_item_new:
                this.displayDialog();
                break;

            case R.id.menu_item_open:
                this.displayActivity(LoadActivity.class, null);
                break;

            case R.id.action_help:
                this.displayActivity(HelpActivity.class, null);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
    }

}