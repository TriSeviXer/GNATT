package edu.lockhaven.bloom_gnatt;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.lockhaven.bloom_gnatt.database.GanttModel;

public class LoadActivity extends AppCompatActivity implements AdapterInterface {

    //Model and dapter.
    private GanttModel model;
    private ProjectAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        this.configureRecycler();
        this.configureModel();
    }

    /**
     * Configures the current recycler view.
     */
    protected void configureRecycler(){
        RecyclerView recyclerView = this.findViewById(R.id.recycler_view);
        this.adapter = new ProjectAdapter(new ProjectAdapter.ProjectDF(), this, this);
        recyclerView.setAdapter(this.adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Configures the model.
     */
    protected void configureModel(){
        this.model = new ViewModelProvider(this).get(GanttModel.class);
        this.model.getAllProjects().observe(this, projects -> {
            this.adapter.submitList(projects);
        });
    }

    @Override
    public void onClick(int id) {

        //Set an intent as the main activity.
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(this.getString(R.string.key_status), this.getString(R.string.status_ok));
        intent.putExtra(this.getString(R.string.key_current_project), id);
        startActivity(intent);

    }

    @Override
    public void onDelete(int id) {
        this.model.deleteProject(id);
    }
}
