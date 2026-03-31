package com.fitai.gym;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddWorkoutActivity extends AppCompatActivity {

    private EditText etPlanTitle, etPlanDesc, etTotalDays, etCalories;
    private Chip chipBeginner, chipIntermediate, chipAdvanced;
    private RecyclerView rvDays;
    private List<DayPlan> dayPlans = new ArrayList<>();
    private DayAdapter dayAdapter;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_workout);

        fbHelper = FirebaseHelper.getInstance();

        etPlanTitle = findViewById(R.id.etPlanTitle);
        etPlanDesc = findViewById(R.id.etPlanDesc);
        etTotalDays = findViewById(R.id.etTotalDays);
        etCalories = findViewById(R.id.etCalories);
        chipBeginner = findViewById(R.id.chipBeginner);
        chipIntermediate = findViewById(R.id.chipIntermediate);
        chipAdvanced = findViewById(R.id.chipAdvanced);
        rvDays = findViewById(R.id.rvDays);

        dayAdapter = new DayAdapter();
        rvDays.setLayoutManager(new LinearLayoutManager(this));
        rvDays.setNestedScrollingEnabled(false);
        rvDays.setAdapter(dayAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddDay).setOnClickListener(v -> showAddDayDialog());
        findViewById(R.id.btnPublish).setOnClickListener(v -> publishPlan());
    }

    private void showAddDayDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_day, null);
        EditText etDayTitle = dialogView.findViewById(R.id.etDayTitle);
        int nextDay = dayPlans.size() + 1;
        etDayTitle.setText("Day " + nextDay);

        new AlertDialog.Builder(this)
            .setTitle("Add Day " + nextDay)
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
                String title = etDayTitle.getText().toString().trim();
                if (title.isEmpty()) title = "Day " + nextDay;
                DayPlan day = new DayPlan(nextDay, title, false, new ArrayList<>());
                dayPlans.add(day);
                dayAdapter.notifyItemInserted(dayPlans.size() - 1);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddExerciseDialog(int dayIndex) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        EditText etName = dialogView.findViewById(R.id.etExName);
        EditText etReps = dialogView.findViewById(R.id.etExReps);
        EditText etSets = dialogView.findViewById(R.id.etExSets);
        EditText etDuration = dialogView.findViewById(R.id.etExDuration);
        EditText etRest = dialogView.findViewById(R.id.etExRest);
        EditText etInstructions = dialogView.findViewById(R.id.etExInstructions);

        new AlertDialog.Builder(this)
            .setTitle("Add Exercise")
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
                String name = etName.getText().toString().trim();
                String reps = etReps.getText().toString().trim();
                int sets = parseIntSafe(etSets.getText().toString(), 3);
                int duration = parseIntSafe(etDuration.getText().toString(), 30);
                int rest = parseIntSafe(etRest.getText().toString(), 15);
                String instructions = etInstructions.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Exercise name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                Exercise exercise = new Exercise(name, reps.isEmpty() ? "x10" : reps,
                        sets, duration, rest, "workout_1", instructions);
                dayPlans.get(dayIndex).getExercises().add(exercise);
                dayAdapter.notifyItemChanged(dayIndex);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void publishPlan() {
        String title = etPlanTitle.getText().toString().trim();
        String desc = etPlanDesc.getText().toString().trim();
        String daysStr = etTotalDays.getText().toString().trim();
        String calStr = etCalories.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dayPlans.isEmpty()) {
            Toast.makeText(this, "Add at least one day with exercises", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build difficulty list from chips
        List<String> difficulty = new ArrayList<>();
        if (chipBeginner.isChecked()) difficulty.add("beginner");
        if (chipIntermediate.isChecked()) difficulty.add("intermediate");
        if (chipAdvanced.isChecked()) difficulty.add("advanced");
        if (difficulty.isEmpty()) difficulty.add("beginner");

        int totalDays = parseIntSafe(daysStr, dayPlans.size());
        int calories = parseIntSafe(calStr, 200);

        // Create plan document
        Map<String, Object> planData = new HashMap<>();
        planData.put("title", title);
        planData.put("description", desc);
        planData.put("imageRes", "workout_1");
        planData.put("difficulty", difficulty);
        planData.put("totalDays", totalDays);
        planData.put("calories", calories);
        planData.put("createdAt", System.currentTimeMillis());

        fbHelper.getWorkoutPlansCollection().add(planData)
            .addOnSuccessListener(docRef -> {
                String planId = docRef.getId();
                // Save each day as a sub-document
                saveDays(planId, 0);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void saveDays(String planId, int index) {
        if (index >= dayPlans.size()) {
            Toast.makeText(this, "✅ Workout Plan Published!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        DayPlan day = dayPlans.get(index);
        Map<String, Object> dayData = new HashMap<>();
        dayData.put("dayNumber", day.getDayNumber());
        dayData.put("dayTitle", day.getDayTitle());
        dayData.put("restDay", day.isRestDay());

        // Convert exercises to list of maps
        List<Map<String, Object>> exerciseList = new ArrayList<>();
        for (Exercise ex : day.getExercises()) {
            Map<String, Object> exMap = new HashMap<>();
            exMap.put("name", ex.getName());
            exMap.put("reps", ex.getReps());
            exMap.put("sets", ex.getSets());
            exMap.put("duration", ex.getDuration());
            exMap.put("restTime", ex.getRestTime());
            exMap.put("imageRes", ex.getImageRes());
            exMap.put("instructions", ex.getInstructions());
            exerciseList.add(exMap);
        }
        dayData.put("exercises", exerciseList);

        fbHelper.getDayDocument(planId, day.getDayNumber()).set(dayData)
            .addOnSuccessListener(v -> saveDays(planId, index + 1))
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error saving day " + day.getDayNumber() + ": " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    // ── Inner Day Adapter ──────────────────────────────────────

    private class DayAdapter extends RecyclerView.Adapter<DayAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_day, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            DayPlan day = dayPlans.get(position);
            holder.tvDayNumber.setText(String.valueOf(day.getDayNumber()));
            holder.tvDayTitle.setText(day.getDayTitle());
            int exCount = day.getExercises() != null ? day.getExercises().size() : 0;
            holder.tvExerciseCount.setText(exCount + " exercise" + (exCount != 1 ? "s" : ""));

            holder.btnEditDay.setOnClickListener(v -> showAddExerciseDialog(position));
            holder.btnDeleteDay.setOnClickListener(v -> {
                dayPlans.remove(position);
                // Renumber remaining days
                for (int i = 0; i < dayPlans.size(); i++) {
                    dayPlans.get(i).setDayNumber(i + 1);
                }
                notifyDataSetChanged();
            });

            holder.itemView.setOnClickListener(v -> showAddExerciseDialog(position));
        }

        @Override
        public int getItemCount() { return dayPlans.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDayNumber, tvDayTitle, tvExerciseCount;
            View btnEditDay, btnDeleteDay;

            VH(View v) {
                super(v);
                tvDayNumber = v.findViewById(R.id.tvDayNumber);
                tvDayTitle = v.findViewById(R.id.tvDayTitle);
                tvExerciseCount = v.findViewById(R.id.tvExerciseCount);
                btnEditDay = v.findViewById(R.id.btnEditDay);
                btnDeleteDay = v.findViewById(R.id.btnDeleteDay);
            }
        }
    }
}
