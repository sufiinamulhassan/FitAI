package com.fitai.gym;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDetailActivity extends AppCompatActivity {

    private String planId;
    private WorkoutPlan plan;
    private UserProgress userProgress;
    private String selectedLevel = "beginner";
    private int totalDays = 30;

    private TextView tvPlanTitle, tvPlanDesc, tvPlanCalories, tvProgressPercent;
    private TextView btnLevelBeginner, btnLevelIntermediate, btnLevelAdvanced;
    private ProgressBar progressBar;
    private RecyclerView rvDaysGrid;
    private ImageView ivPlanImage;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plan_detail);

        fbHelper = FirebaseHelper.getInstance();
        planId = getIntent().getStringExtra("PLAN_ID");

        if (planId == null) {
            Toast.makeText(this, "Plan not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        tvPlanTitle = findViewById(R.id.tvPlanTitle);
        tvPlanDesc = findViewById(R.id.tvPlanDesc);
        tvPlanCalories = findViewById(R.id.tvPlanCalories);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBar = findViewById(R.id.progressBar);
        rvDaysGrid = findViewById(R.id.rvDaysGrid);
        ivPlanImage = findViewById(R.id.ivPlanImage);
        btnLevelBeginner = findViewById(R.id.btnLevelBeginner);
        btnLevelIntermediate = findViewById(R.id.btnLevelIntermediate);
        btnLevelAdvanced = findViewById(R.id.btnLevelAdvanced);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Level selectors
        btnLevelBeginner.setOnClickListener(v -> selectLevel("beginner"));
        btnLevelIntermediate.setOnClickListener(v -> selectLevel("intermediate"));
        btnLevelAdvanced.setOnClickListener(v -> selectLevel("advanced"));

        // Start next day
        findViewById(R.id.btnStartPlan).setOnClickListener(v -> startNextDay());

        loadPlan();
        loadUserProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProgress();
    }

    private void loadPlan() {
        fbHelper.getWorkoutPlansCollection().document(planId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    plan = doc.toObject(WorkoutPlan.class);
                    if (plan != null) {
                        plan.setId(doc.getId());
                        totalDays = plan.getTotalDays();
                        tvPlanTitle.setText(plan.getTitle());
                        tvPlanDesc.setText(totalDays + " Days Challenge");
                        tvPlanCalories.setText("🔥 " + plan.getCalories() + " Cal/session");
                        ivPlanImage.setImageResource(plan.getImageResourceId(this));
                        findViewById(R.id.tvHeaderTitle).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(plan.getTitle());
                        setupDaysGrid();
                    }
                }
            });
    }

    private void loadUserProgress() {
        String uid = fbHelper.getCurrentUserUid();
        if (uid == null || planId == null) return;

        fbHelper.getUserProgressCollection(uid).document(planId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    userProgress = doc.toObject(UserProgress.class);
                    if (userProgress != null) {
                        selectedLevel = userProgress.getLevel();
                        selectLevel(selectedLevel);
                    }
                } else {
                    userProgress = new UserProgress(planId, "", selectedLevel);
                }
                updateProgressUI();
                setupDaysGrid();
            })
            .addOnFailureListener(e -> {
                userProgress = new UserProgress(planId, "", selectedLevel);
                setupDaysGrid();
            });
    }

    private void updateProgressUI() {
        if (userProgress == null) return;
        int percent = userProgress.getProgressPercent(totalDays);
        progressBar.setProgress(percent);
        tvProgressPercent.setText(percent + "%");
    }

    private void selectLevel(String level) {
        selectedLevel = level;

        // Reset all
        btnLevelBeginner.setTextColor(0xFFADA4A5);
        btnLevelBeginner.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F8F8));
        btnLevelIntermediate.setTextColor(0xFFADA4A5);
        btnLevelIntermediate.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F8F8));
        btnLevelAdvanced.setTextColor(0xFFADA4A5);
        btnLevelAdvanced.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F8F8));

        // Highlight selected
        switch (level) {
            case "beginner":
                btnLevelBeginner.setTextColor(0xFFFFFFFF);
                btnLevelBeginner.setBackgroundTintList(ColorStateList.valueOf(0xFF9DCEFF));
                break;
            case "intermediate":
                btnLevelIntermediate.setTextColor(0xFFFFFFFF);
                btnLevelIntermediate.setBackgroundTintList(ColorStateList.valueOf(0xFFC58BF2));
                break;
            case "advanced":
                btnLevelAdvanced.setTextColor(0xFFFFFFFF);
                btnLevelAdvanced.setBackgroundTintList(ColorStateList.valueOf(0xFFFF6B6B));
                break;
        }

        // Save level preference
        if (userProgress != null) {
            userProgress.setLevel(level);
            String uid = fbHelper.getCurrentUserUid();
            if (uid != null) {
                fbHelper.getUserProgressCollection(uid).document(planId)
                    .set(userProgress);
            }
        }
    }

    private void setupDaysGrid() {
        rvDaysGrid.setLayoutManager(new GridLayoutManager(this, 7));
        rvDaysGrid.setNestedScrollingEnabled(false);
        rvDaysGrid.setAdapter(new RecyclerView.Adapter<DayVH>() {

            @Override
            public DayVH onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_day_grid, parent, false);
                return new DayVH(v);
            }

            @Override
            public void onBindViewHolder(DayVH holder, int position) {
                int dayNum = position + 1;
                holder.tvDayNum.setText(String.valueOf(dayNum));

                boolean completed = userProgress != null && userProgress.isDayCompleted(dayNum);
                if (completed) {
                    // Green = completed
                    holder.tvDayNum.setTextColor(0xFFFFFFFF);
                    holder.tvDayNum.setBackgroundTintList(ColorStateList.valueOf(0xFF5DD672));
                } else if (dayNum == getNextDay()) {
                    // Blue = current
                    holder.tvDayNum.setTextColor(0xFFFFFFFF);
                    holder.tvDayNum.setBackgroundTintList(ColorStateList.valueOf(0xFF9DCEFF));
                } else {
                    // Grey = locked/future
                    holder.tvDayNum.setTextColor(0xFFADA4A5);
                    holder.tvDayNum.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F8F8));
                }

                holder.itemView.setOnClickListener(v -> openDay(dayNum));
            }

            @Override
            public int getItemCount() { return totalDays; }
        });
    }

    private int getNextDay() {
        if (userProgress == null || userProgress.getCompletedDays() == null
                || userProgress.getCompletedDays().isEmpty()) return 1;
        int max = 0;
        for (int d : userProgress.getCompletedDays()) {
            if (d > max) max = d;
        }
        return Math.min(max + 1, totalDays);
    }

    private void openDay(int dayNumber) {
        Intent intent = new Intent(this, DayExerciseListActivity.class);
        intent.putExtra("PLAN_ID", planId);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.putExtra("PLAN_TITLE", plan != null ? plan.getTitle() : "Workout");
        intent.putExtra("LEVEL", selectedLevel);
        startActivity(intent);
    }

    private void startNextDay() {
        openDay(getNextDay());
    }

    static class DayVH extends RecyclerView.ViewHolder {
        TextView tvDayNum;
        DayVH(View v) {
            super(v);
            tvDayNum = v.findViewById(R.id.tvDayNum);
        }
    }
}
