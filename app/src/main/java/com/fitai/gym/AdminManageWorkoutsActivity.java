/*
 * AdminManageWorkoutsActivity allows admins to list, edit, or delete existing workout plans.
 */
package com.fitai.gym;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class AdminManageWorkoutsActivity extends AppCompatActivity {

    private RecyclerView rvWorkouts;
    private FloatingActionButton fabAddWorkout;
    private ImageView btnBack;
    private List<WorkoutPlan> workoutList = new ArrayList<>();
    private WorkoutsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_workouts);

        btnBack = findViewById(R.id.btnBack);
        rvWorkouts = findViewById(R.id.rvAdminWorkouts);
        fabAddWorkout = findViewById(R.id.fabAddWorkout);

        btnBack.setOnClickListener(v -> finish());
        fabAddWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAddWorkoutActivity.class);
            startActivity(intent);
        });

        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutsAdapter();
        rvWorkouts.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void loadWorkouts() {
        WorkoutSeeder.checkAndSeed(this, () -> {
            FirebaseHelper.getInstance().getWorkoutPlansCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    workoutList.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            WorkoutPlan plan = doc.toObject(WorkoutPlan.class);
                            if (plan != null) {
                                plan.setId(doc.getId());
                                workoutList.add(plan);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load workout plans: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }

    private void deleteWorkout(WorkoutPlan plan, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Workout Plan")
            .setMessage("Are you sure you want to delete " + plan.getTitle() + "?")
            .setPositiveButton("Delete", (d, w) -> {
                
                FirebaseHelper.getInstance().getDaysCollection(plan.getId()).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null) {
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                doc.getReference().delete();
                            }
                        }
                        
                        FirebaseHelper.getInstance().getWorkoutPlansCollection()
                            .document(plan.getId())
                            .delete()
                            .addOnSuccessListener(v -> {
                                workoutList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, workoutList.size());
                                Toast.makeText(this, "Workout Plan deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to clean days: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private class WorkoutsAdapter extends RecyclerView.Adapter<WorkoutsAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_workout, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            WorkoutPlan plan = workoutList.get(position);
            holder.tvName.setText(plan.getTitle());

            
            String diff = plan.getDifficulty() != null && !plan.getDifficulty().isEmpty() ? plan.getDifficulty().get(0) : "Beginner";
            holder.tvDifficulty.setText(diff.substring(0, 1).toUpperCase() + diff.substring(1));

            holder.tvCalories.setText(plan.getCalories() + " kCal | " + plan.getTotalDays() + " Days");
            ImageLoaderHelper.loadImage(holder.itemView.getContext(), holder.ivIcon, plan.getImageRes(), R.drawable.barbell);

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManageWorkoutsActivity.this, AdminAddWorkoutActivity.class);
                intent.putExtra("plan_id", plan.getId());
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> deleteWorkout(plan, position));
        }

        @Override
        public int getItemCount() {
            return workoutList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon, btnEdit, btnDelete;
            TextView tvName, tvDifficulty, tvCalories;

            VH(View v) {
                super(v);
                ivIcon = v.findViewById(R.id.ivAdminWorkoutIcon);
                tvName = v.findViewById(R.id.tvAdminWorkoutName);
                tvDifficulty = v.findViewById(R.id.tvAdminWorkoutDifficulty);
                tvCalories = v.findViewById(R.id.tvAdminWorkoutCalories);
                btnEdit = v.findViewById(R.id.btnAdminEditWorkout);
                btnDelete = v.findViewById(R.id.btnAdminDeleteWorkout);
            }
        }
    }
}
