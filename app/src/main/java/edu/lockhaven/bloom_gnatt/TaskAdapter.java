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

import edu.lockhaven.bloom_gnatt.database.GanttTask;

public class TaskAdapter extends ListAdapter<GanttTask, TaskAdapter.TaskVH> {

    private Animation selection;
    private AdapterInterface adapterInterface;

    protected TaskAdapter(@NonNull DiffUtil.ItemCallback<GanttTask> diffCallback, Context context, AdapterInterface adapterInterface) {
        super(diffCallback);
        this.adapterInterface = adapterInterface;
        this.selection = AnimationUtils.loadAnimation(context, R.anim.selection);
        Log.i("GANTT - Task Adapter", "Successfully created adapter.");
    }

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskVH.create(parent, this.adapterInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {

        GanttTask current = this.getItem(position);

        //Set current task information for disply.
        holder.id = current.id;
        holder.name.setText(current.name);
        holder.description.setText(current.description);
        holder.startDate.setText(current.start);
        holder.endDate.setText(current.end);
        holder.selection = selection;

        Log.i("GANTT - Task Adapter", "Viewing task. (PID = " + current.project + ", TID = " + current.id + ")");
    }

    public static class TaskDF extends DiffUtil.ItemCallback<GanttTask> {

        @Override
        public boolean areItemsTheSame(@NonNull GanttTask oldItem, @NonNull GanttTask newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull GanttTask oldItem, @NonNull GanttTask newItem) {
            return (oldItem.id == newItem.id) && (oldItem.project == newItem.project);
        }

    }

    public static class TaskVH extends RecyclerView.ViewHolder {

        public Animation selection;
        public TextView name, description, startDate, endDate;
        public int id = 0;

        public TaskVH(@NonNull View itemView, AdapterInterface adapterInterface) {
            super(itemView);

            //Set views
            name = itemView.findViewById(R.id.recycler_task_textview_name);
            description = itemView.findViewById(R.id.recycler_task_textview_description);
            startDate = itemView.findViewById(R.id.recycler_task_textview_sdate);
            endDate = itemView.findViewById(R.id.recycler_task_textview_edate);
            Button delete = itemView.findViewById(R.id.recycler_task_button_delete);

            //Set the click.
            itemView.setOnClickListener(view -> {
                view.startAnimation(this.selection);
                adapterInterface.onClick(this.id);
            });

            delete.setOnClickListener(view -> {
                //Alert the user.
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Delete Task");
                builder.setMessage("Are you sure you want to delete '" + name.getText().toString() + "'? All progress will be lost.");
                builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                    adapterInterface.onDelete(this.id);
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });

        }

        public static TaskVH create(ViewGroup parent, AdapterInterface adapterInterface){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_task, parent, false);
            return new TaskVH(view, adapterInterface);
        }
    }
}
