package edu.lockhaven.bloom_gnatt;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import edu.lockhaven.bloom_gnatt.database.GanttProject;

public class ProjectAdapter extends ListAdapter<GanttProject, ProjectAdapter.ProjectVH> {

    private Animation selection;
    private AdapterInterface adapterInterface;

    protected ProjectAdapter(@NonNull DiffUtil.ItemCallback<GanttProject> diffCallback, Context context, AdapterInterface adapterInterface) {
        super(diffCallback);
        this.selection = AnimationUtils.loadAnimation(context, R.anim.selection);
        this.adapterInterface = adapterInterface;
    }

    @NonNull
    @Override
    public ProjectVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ProjectVH.create(parent, this.adapterInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectVH holder, int position) {
        GanttProject current = this.getItem(position);

        //Set the current project information into the cardview.
        holder.id = current.id;
        holder.name.setText(current.name);
        holder.cdate.setText(current.created);
        holder.mdate.setText(current.modified);
        holder.selection = selection;
    }

    public static class ProjectDF extends DiffUtil.ItemCallback<GanttProject> {

        @Override
        public boolean areItemsTheSame(@NonNull GanttProject oldItem, @NonNull GanttProject newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull GanttProject oldItem, @NonNull GanttProject newItem) {
            return oldItem.id == newItem.id;
        }

    }

    public static class ProjectVH extends RecyclerView.ViewHolder {

        public Animation selection;
        public TextView name, cdate, mdate;

        public int id = 0;

        public ProjectVH(@NonNull View itemView, AdapterInterface adapterInterface) {
            super(itemView);

            //Set the views.
            name = itemView.findViewById(R.id.recycler_project_textview_name);
            cdate = itemView.findViewById(R.id.recycler_project_textview_created);
            mdate = itemView.findViewById(R.id.recycler_project_textview_modified);
            Button delete = itemView.findViewById(R.id.recycler_project_button_delete);

            //Set the click.
            itemView.setOnClickListener(view -> {
                view.startAnimation(this.selection);
                adapterInterface.onClick(this.id);
            });

            delete.setOnClickListener(view -> {
                //Alert the user.
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Delete Project");
                builder.setMessage("Are you sure you want to delete '" + name.getText().toString() + "'? All progress will be lost.");
                builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                    adapterInterface.onDelete(this.id);
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }

        public static ProjectVH create(ViewGroup parent, AdapterInterface adapterInterface){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_project, parent, false);
            return new ProjectVH(view, adapterInterface);
        }
    }
}
