/*
 * WorkoutTrackerActivity displays and manages the user's overall workout plans, progress logs, and history.
 */
package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkoutTrackerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView rvUpcomingWorkouts;
    private RecyclerView rvWhatToTrain;
    private List<WorkoutPlan> plans = new ArrayList<>();
    private List<DocumentSnapshot> historyDocs = new ArrayList<>();
    private FirebaseHelper fbHelper;
    private String currentFilter = "Daily";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        fbHelper = FirebaseHelper.getInstance();
        bottomNav = findViewById(R.id.bottomNav);
        setupNavigation();

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WorkoutScheduleActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Workout Schedule is unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        rvUpcomingWorkouts = findViewById(R.id.rvUpcomingWorkouts);
        rvUpcomingWorkouts.setLayoutManager(new LinearLayoutManager(this));

        rvWhatToTrain = findViewById(R.id.rvWhatToTrain);
        rvWhatToTrain.setLayoutManager(new LinearLayoutManager(this));

        Spinner spChartFilter = findViewById(R.id.spChartFilter);
        String[] filterOptions = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChartFilter.setAdapter(spAdapter);
        spChartFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filterOptions[position];
                updateChartFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadWorkoutPlans();
        loadWorkoutHistory();
        loadUpcomingWorkouts();
    }

    private void loadWorkoutPlans() {
        WorkoutSeeder.checkAndSeed(this, () -> {
            fbHelper.getWorkoutPlansCollection().get()
                .addOnSuccessListener(snapshot -> {
                    plans.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        WorkoutPlan plan = doc.toObject(WorkoutPlan.class);
                        if (plan != null) {
                            plan.setId(doc.getId());
                            plans.add(plan);
                        }
                    }
                    rvWhatToTrain.setAdapter(new TrainWorkoutAdapter(plans));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading plans", Toast.LENGTH_SHORT).show();
                });
        });
    }

    private void loadWorkoutHistory() {
        String uid = fbHelper.getCurrentUserUid();
        if (uid == null) return;

        fbHelper.getUsersCollection().document(uid).collection("workout_history")
            .get()
            .addOnSuccessListener(snapshot -> {
                historyDocs.clear();
                if (snapshot != null) {
                    historyDocs.addAll(snapshot.getDocuments());
                }
                updateChartFilter();
            });
    }

    private void loadUpcomingWorkouts() {
        String uid = fbHelper.getCurrentUserUid();
        if (uid == null) return;

        fbHelper.getUsersCollection().document(uid).collection("workout_schedule")
            .get()
            .addOnSuccessListener(snapshot -> {
                List<ScheduledWorkoutDocument> upcomingList = new ArrayList<>();
                if (snapshot != null) {
                    String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ScheduledWorkout sw = doc.toObject(ScheduledWorkout.class);
                        if (sw != null) {
                            if (sw.getDate() != null && sw.getDate().compareTo(todayStr) >= 0) {
                                upcomingList.add(new ScheduledWorkoutDocument(doc.getId(), sw));
                            }
                        }
                    }
                }
                
                Collections.sort(upcomingList, (a, b) -> {
                    int dateComp = a.workout.getDate().compareTo(b.workout.getDate());
                    if (dateComp != 0) return dateComp;
                    return a.workout.getTime().compareTo(b.workout.getTime());
                });

                rvUpcomingWorkouts.setAdapter(new UpcomingWorkoutAdapter(upcomingList));
            });
    }

    private void updateChartFilter() {
        WorkoutChart lineChart = findViewById(R.id.lineChart);
        if (lineChart == null) return;

        int numPoints = 7;
        String[] xLabels;
        float[] data1;
        float[] data2;
        int highlightIndex = 0;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        if ("Daily".equals(currentFilter)) {
            numPoints = 7;
            xLabels = new String[numPoints];
            data1 = new float[numPoints];
            data2 = new float[numPoints];

            java.util.Calendar tempCal = java.util.Calendar.getInstance();
            tempCal.add(java.util.Calendar.DAY_OF_YEAR, -6);
            java.text.SimpleDateFormat sdfDay = new java.text.SimpleDateFormat("E", java.util.Locale.getDefault());

            for (int i = 0; i < numPoints; i++) {
                xLabels[i] = sdfDay.format(tempCal.getTime());
                String dateKey = sdfDate.format(tempCal.getTime());
                
                float sumCals = 0;
                for (DocumentSnapshot doc : historyDocs) {
                    com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                    if (ts != null) {
                        String tsStr = sdfDate.format(ts.toDate());
                        if (dateKey.equals(tsStr)) {
                            Long cb = doc.getLong("caloriesBurned");
                            if (cb != null) sumCals += cb.floatValue();
                        }
                    }
                }
                data1[i] = sumCals;
                tempCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }
            highlightIndex = numPoints - 1;

        } else if ("Weekly".equals(currentFilter)) {
            numPoints = 4;
            xLabels = new String[]{"W1", "W2", "W3", "W4"};
            data1 = new float[numPoints];
            data2 = new float[numPoints];

            for (int i = 0; i < numPoints; i++) {
                long weekStartMs = getStartOfWeekAgoMs(4 - i);
                long weekEndMs = getStartOfWeekAgoMs(3 - i);

                float sumCals = 0;
                for (DocumentSnapshot doc : historyDocs) {
                    com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                    if (ts != null) {
                        long time = ts.toDate().getTime();
                        if (time >= weekStartMs && time < weekEndMs) {
                            Long cb = doc.getLong("caloriesBurned");
                            if (cb != null) sumCals += cb.floatValue();
                        }
                    }
                }
                data1[i] = sumCals;
            }
            highlightIndex = numPoints - 1;

        } else {
            numPoints = 6;
            xLabels = new String[numPoints];
            data1 = new float[numPoints];
            data2 = new float[numPoints];

            java.text.SimpleDateFormat sdfMonth = new java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault());
            java.util.Calendar tempCal = java.util.Calendar.getInstance();
            tempCal.add(java.util.Calendar.MONTH, -5);

            for (int i = 0; i < numPoints; i++) {
                xLabels[i] = sdfMonth.format(tempCal.getTime());
                int targetMonth = tempCal.get(java.util.Calendar.MONTH);
                int targetYear = tempCal.get(java.util.Calendar.YEAR);

                float sumCals = 0;
                for (DocumentSnapshot doc : historyDocs) {
                    com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                    if (ts != null) {
                        java.util.Calendar itemCal = java.util.Calendar.getInstance();
                        itemCal.setTime(ts.toDate());
                        if (itemCal.get(java.util.Calendar.MONTH) == targetMonth && itemCal.get(java.util.Calendar.YEAR) == targetYear) {
                            Long cb = doc.getLong("caloriesBurned");
                            if (cb != null) sumCals += cb.floatValue();
                        }
                    }
                }
                data1[i] = sumCals;
                tempCal.add(java.util.Calendar.MONTH, 1);
            }
            highlightIndex = numPoints - 1;
        }

        float maxVal = 0;
        for (float val : data1) {
            if (val > maxVal) maxVal = val;
        }

        float displayMax = Math.max(100f, maxVal * 1.2f);
        float[] scaledData1 = new float[numPoints];
        float[] scaledData2 = new float[numPoints];

        for (int i = 0; i < numPoints; i++) {
            scaledData1[i] = (data1[i] / displayMax) * 80f + 10f;
            scaledData2[i] = ((displayMax * 0.6f + (float) Math.sin(i) * (displayMax * 0.1f)) / displayMax) * 80f + 10f;
        }

        String[] yLabels = new String[6];
        for (int i = 0; i < 6; i++) {
            yLabels[i] = String.format(java.util.Locale.getDefault(), "%.0f", (i * displayMax / 5f));
        }

        lineChart.setData(scaledData1, scaledData2, xLabels, yLabels, highlightIndex);
    }

    private long getStartOfWeekAgoMs(int weeksAgo) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        cal.add(java.util.Calendar.WEEK_OF_YEAR, -weeksAgo);
        return cal.getTimeInMillis();
    }

    private void openPlanDetail(WorkoutPlan plan) {
        Intent intent = new Intent(this, WorkoutPlanDetailActivity.class);
        intent.putExtra("PLAN_ID", plan.getId());
        startActivity(intent);
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_activity);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_activity) {
                return true;
            } else if (id == R.id.nav_meals) {
                Intent intent = new Intent(this, MealPlannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_camera) {
                Intent intent = new Intent(this, ProgressPhotoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_activity);
        loadWorkoutPlans();
        loadWorkoutHistory();
        loadUpcomingWorkouts();
    }

    private class TrainWorkoutAdapter extends RecyclerView.Adapter<TrainViewHolder> {
        private List<WorkoutPlan> items;

        TrainWorkoutAdapter(List<WorkoutPlan> items) {
            this.items = items;
        }

        @Override
        public TrainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_train_workout, parent, false);
            return new TrainViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TrainViewHolder holder, int position) {
            WorkoutPlan plan = items.get(position);
            holder.tvTitle.setText(plan.getTitle());
            holder.tvDetail.setText(plan.getTotalDays() + " Days Challenge | " + plan.getCalories() + " Cal/session");

            int cardBgColor;
            int viewMoreColor;
            if (position % 3 == 0) {
                cardBgColor = 0xFFF1F5FF;
                viewMoreColor = 0xFF9DCEFF;
            } else if (position % 3 == 1) {
                cardBgColor = 0xFFF9F1FF;
                viewMoreColor = 0xFFC58BF2;
            } else {
                cardBgColor = 0xFFFFF1F3;
                viewMoreColor = 0xFFFF9DAB;
            }
            holder.cvCard.setCardBackgroundColor(cardBgColor);
            holder.tvViewMore.setTextColor(viewMoreColor);

            ImageLoaderHelper.loadImage(WorkoutTrackerActivity.this, holder.ivIcon, plan.getImageRes(), R.drawable.workout_1);
            holder.itemView.setOnClickListener(v -> openPlanDetail(plan));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class TrainViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cvCard;
        TextView tvTitle, tvDetail, tvViewMore;
        ImageView ivIcon;

        TrainViewHolder(View v) {
            super(v);
            cvCard = v.findViewById(R.id.cvTrainCard);
            tvTitle = v.findViewById(R.id.tvTrainTitle);
            tvDetail = v.findViewById(R.id.tvTrainDetail);
            tvViewMore = v.findViewById(R.id.tvTrainViewMore);
            ivIcon = v.findViewById(R.id.ivTrainIcon);
        }
    }

    private class UpcomingWorkoutAdapter extends RecyclerView.Adapter<UpcomingViewHolder> {
        private List<ScheduledWorkoutDocument> items;

        UpcomingWorkoutAdapter(List<ScheduledWorkoutDocument> items) {
            this.items = items;
        }

        @Override
        public UpcomingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_workout, parent, false);
            return new UpcomingViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UpcomingViewHolder holder, int position) {
            ScheduledWorkoutDocument doc = items.get(position);
            ScheduledWorkout sw = doc.workout;

            holder.tvTitle.setText(sw.getName());
            
            String displayTime = sw.getDate() + " at " + sw.getTime();
            String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            if (sw.getDate().equals(todayStr)) {
                displayTime = "Today, " + sw.getTime();
            } else {
                try {
                    java.util.Date d = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(sw.getDate());
                    if (d != null) {
                        displayTime = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(d) + ", " + sw.getTime();
                    }
                } catch (Exception e) {
                }
            }
            holder.tvTime.setText(displayTime);

            int imgRes = R.drawable.img_1;
            if (sw.getName().toLowerCase().contains("upper")) {
                imgRes = R.drawable.img_2;
            } else if (sw.getName().toLowerCase().contains("abs") || sw.getName().toLowerCase().contains("ab ")) {
                imgRes = R.drawable.img_1;
            } else if (position % 2 == 1) {
                imgRes = R.drawable.img_2;
            }
            holder.ivImg.setImageResource(imgRes);

            holder.switchUpcoming.setOnCheckedChangeListener(null);
            holder.switchUpcoming.setChecked(true);
            holder.switchUpcoming.setOnCheckedChangeListener((btn, isChecked) -> {
                if (!isChecked) {
                    fbHelper.getUsersCollection().document(fbHelper.getCurrentUserUid())
                        .collection("workout_schedule")
                        .document(doc.id)
                        .delete()
                        .addOnSuccessListener(v -> {
                            Toast.makeText(WorkoutTrackerActivity.this, "Workout canceled", Toast.LENGTH_SHORT).show();
                            loadUpcomingWorkouts();
                        });
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    static class UpcomingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImg;
        TextView tvTitle, tvTime;
        androidx.appcompat.widget.SwitchCompat switchUpcoming;

        UpcomingViewHolder(View v) {
            super(v);
            ivImg = v.findViewById(R.id.ivUpcomingImg);
            tvTitle = v.findViewById(R.id.tvUpcomingTitle);
            tvTime = v.findViewById(R.id.tvUpcomingTime);
            switchUpcoming = v.findViewById(R.id.switchUpcoming);
        }
    }

    private static class ScheduledWorkoutDocument {
        String id;
        ScheduledWorkout workout;
        ScheduledWorkoutDocument(String id, ScheduledWorkout workout) {
            this.id = id;
            this.workout = workout;
        }
    }
}
