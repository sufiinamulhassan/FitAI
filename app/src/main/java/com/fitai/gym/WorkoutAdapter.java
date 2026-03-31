package com.fitai.gym;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private Context context;
    private List<WorkoutModel> workouts;

    public WorkoutAdapter(Context context, List<WorkoutModel> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout_card, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutModel workout = workouts.get(position);
        holder.tvWorkoutName.setText(workout.getName());
        holder.tvWorkoutInfo.setText(workout.getInfo());
        holder.ivWorkoutThumb.setImageResource(workout.getImageRes());
        holder.pbWorkoutProgress.setProgress(workout.getProgress());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WorkoutDetailActivity.class);
            intent.putExtra("workout_name", workout.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWorkoutThumb;
        TextView tvWorkoutName, tvWorkoutInfo;
        ProgressBar pbWorkoutProgress;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWorkoutThumb = itemView.findViewById(R.id.ivWorkoutThumb);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvWorkoutInfo = itemView.findViewById(R.id.tvWorkoutInfo);
            pbWorkoutProgress = itemView.findViewById(R.id.pbWorkoutProgress);
        }
    }
}
