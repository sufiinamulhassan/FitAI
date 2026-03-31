package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DayExerciseListActivity extends AppCompatActivity {

    private String planId, planTitle, level;
    private int dayNumber;
    private List<Exercise> exercises = new ArrayList<>();
    private RecyclerView rvExercises;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_exercises);

        fbHelper = FirebaseHelper.getInstance();
        planId = getIntent().getStringExtra("PLAN_ID");
        dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
        planTitle = getIntent().getStringExtra("PLAN_TITLE");
        level = getIntent().getStringExtra("LEVEL");
        if (level == null) level = "beginner";

        TextView tvDayHeader = findViewById(R.id.tvDayHeader);
        tvDayHeader.setText("Day " + dayNumber);

        TextView tvLevel = findViewById(R.id.tvLevel);
        tvLevel.setText(capitalize(level));

        rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setNestedScrollingEnabled(false);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnStartExercises).setOnClickListener(v -> startExercises());

        loadDayExercises();
    }

    private void loadDayExercises() {
        fbHelper.getDayDocument(planId, dayNumber).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "No exercises found for this day", Toast.LENGTH_SHORT).show();
                    return;
                }

                String dayTitle = doc.getString("dayTitle");
                TextView tvDayTitle = findViewById(R.id.tvDayTitle);
                tvDayTitle.setText(dayTitle != null ? dayTitle : "Day " + dayNumber);

                // Parse exercises from the document
                List<Map<String, Object>> exList =
                    (List<Map<String, Object>>) doc.get("exercises");

                exercises.clear();
                if (exList != null) {
                    for (Map<String, Object> map : exList) {
                        Exercise ex = new Exercise();
                        ex.setName((String) map.get("name"));
                        ex.setReps((String) map.get("reps"));
                        ex.setSets(toInt(map.get("sets"), 3));
                        ex.setDuration(toInt(map.get("duration"), 30));
                        ex.setRestTime(toInt(map.get("restTime"), 15));
                        ex.setImageRes((String) map.get("imageRes"));
                        ex.setInstructions((String) map.get("instructions"));
                        exercises.add(ex);
                    }
                }

                // Update UI
                TextView tvExCount = findViewById(R.id.tvExCount);
                tvExCount.setText(exercises.size() + " Exercise" + (exercises.size() != 1 ? "s" : ""));

                int totalSec = 0;
                for (Exercise e : exercises) {
                    totalSec += (e.getDuration() * e.getSets()) + e.getRestTime();
                }
                TextView tvTotalDuration = findViewById(R.id.tvTotalDuration);
                tvTotalDuration.setText((totalSec / 60) + " mins");

                setupAdapter();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupAdapter() {
        rvExercises.setAdapter(new RecyclerView.Adapter<ExVH>() {
            @Override
            public ExVH onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_day_exercise, parent, false);
                return new ExVH(v);
            }

            @Override
            public void onBindViewHolder(ExVH holder, int position) {
                Exercise ex = exercises.get(position);
                holder.tvNumber.setText(String.format("%02d", position + 1));
                holder.tvName.setText(ex.getName());
                holder.tvDetail.setText(ex.getReps() + " · " + ex.getSets() + " sets · " + ex.getDuration() + "s");
                holder.ivImage.setImageResource(ex.getImageResourceId(DayExerciseListActivity.this));
            }

            @Override
            public int getItemCount() { return exercises.size(); }
        });
    }

    private void startExercises() {
        if (exercises.isEmpty()) {
            Toast.makeText(this, "No exercises to start", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ExerciseExecutionActivity.class);
        intent.putExtra("PLAN_ID", planId);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.putExtra("PLAN_TITLE", planTitle);
        intent.putExtra("LEVEL", level);
        startActivity(intent);
    }

    private int toInt(Object obj, int fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); }
        catch (Exception e) { return fallback; }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static class ExVH extends RecyclerView.ViewHolder {
        TextView tvNumber, tvName, tvDetail;
        ImageView ivImage;

        ExVH(View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tvExNumber);
            tvName = v.findViewById(R.id.tvExName);
            tvDetail = v.findViewById(R.id.tvExDetail);
            ivImage = v.findViewById(R.id.ivExImage);
        }
    }
}
