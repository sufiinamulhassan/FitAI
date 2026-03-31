package com.fitai.gym;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private Context context;
    private List<ExerciseModel> exercises;

    public ExerciseAdapter(Context context, List<ExerciseModel> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseModel e = exercises.get(position);
        holder.tvName.setText(e.getName());
        holder.tvReps.setText(e.getReps());
        holder.ivThumb.setImageResource(e.getImageRes());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseDetailActivity.class);
            intent.putExtra("exercise_name", e.getName());
            intent.putExtra("exercise_reps", e.getReps());
            intent.putExtra("exercise_image", e.getImageRes());
            context.startActivity(intent);
        });
        holder.ivNext.setOnClickListener(v -> holder.itemView.performClick());
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivNext;
        TextView tvName, tvReps;

        ViewHolder(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.ivExerciseThumb);
            ivNext = v.findViewById(R.id.ivExerciseNext);
            tvName = v.findViewById(R.id.tvExerciseName);
            tvReps = v.findViewById(R.id.tvExerciseReps);
        }
    }
}
