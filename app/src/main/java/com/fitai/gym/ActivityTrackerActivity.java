package com.fitai.gym;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityTrackerActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvWaterTarget, tvStepsTarget, tvWeeklyToggle;
    private ImageView btnAddTarget;
    private LinearLayout llActivityBars, llActivityLabels, llLatestActivities;
    private RelativeLayout rlActivityTooltip;
    private TextView tvActivityGraphDate, tvActivityGraphPercent, tvActivityGraphWorkout;

    private FirebaseHelper fbHelper;
    private String uid;
    private SharedPreferences prefs;

    private List<ListenerRegistration> listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_tracker);

        fbHelper = FirebaseHelper.getInstance();
        uid = fbHelper.getCurrentUserUid();
        prefs = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE);

        // Bind view elements
        btnBack = findViewById(R.id.btnBack);
        tvWaterTarget = findViewById(R.id.tvWaterTarget);
        tvStepsTarget = findViewById(R.id.tvStepsTarget);
        tvWeeklyToggle = findViewById(R.id.tvWeeklyToggle);
        btnAddTarget = findViewById(R.id.btnAddTarget);

        llActivityBars = findViewById(R.id.llActivityBars);
        llActivityLabels = findViewById(R.id.llActivityLabels);
        llLatestActivities = findViewById(R.id.llLatestActivities);

        rlActivityTooltip = findViewById(R.id.rlActivityTooltip);
        tvActivityGraphDate = findViewById(R.id.tvActivityGraphDate);
        tvActivityGraphPercent = findViewById(R.id.tvActivityGraphPercent);
        tvActivityGraphWorkout = findViewById(R.id.tvActivityGraphWorkout);

        btnBack.setOnClickListener(v -> finish());

        if (uid == null) {
            Toast.makeText(this, "Please log in to track activities", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTargetUI();
        setupTargetEditor();
        setupActivityGraphToggle();
        observeLatestActivities();
    }

    private void loadTargetUI() {
        float waterGoal = prefs.getFloat("water_goal_liters", 4.0f);
        int stepsGoal = prefs.getInt("steps_goal", 2400);

        tvWaterTarget.setText(String.format(Locale.getDefault(), "%.1fL", waterGoal));
        tvStepsTarget.setText(String.valueOf(stepsGoal));
    }

    private void setupTargetEditor() {
        btnAddTarget.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(60, 40, 60, 10);

            final EditText etWater = new EditText(this);
            etWater.setHint("Water Target (Liters) e.g. 4.0");
            etWater.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etWater.setText(String.valueOf(prefs.getFloat("water_goal_liters", 4.0f)));
            layout.addView(etWater);

            final EditText etSteps = new EditText(this);
            etSteps.setHint("Steps Target e.g. 5000");
            etSteps.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            etSteps.setText(String.valueOf(prefs.getInt("steps_goal", 2400)));
            layout.addView(etSteps);

            new AlertDialog.Builder(this)
                .setTitle("Update Daily Targets")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String waterStr = etWater.getText().toString();
                    String stepsStr = etSteps.getText().toString();

                    SharedPreferences.Editor editor = prefs.edit();
                    if (!waterStr.isEmpty()) {
                        editor.putFloat("water_goal_liters", Float.parseFloat(waterStr));
                    }
                    if (!stepsStr.isEmpty()) {
                        editor.putInt("steps_goal", Integer.parseInt(stepsStr));
                    }
                    editor.apply();

                    loadTargetUI();
                    Toast.makeText(this, "Targets updated!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void setupActivityGraphToggle() {
        if (tvWeeklyToggle == null) return;

        // Default to Weekly
        loadActivityGraph(uid, "Weekly");

        tvWeeklyToggle.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, tvWeeklyToggle);
            popup.getMenu().add("Daily");
            popup.getMenu().add("Weekly");
            popup.getMenu().add("Monthly");

            popup.setOnMenuItemClickListener(item -> {
                String choice = item.getTitle().toString();
                tvWeeklyToggle.setText(choice);
                loadActivityGraph(uid, choice);
                return true;
            });
            popup.show();
        });
    }

    private void loadActivityGraph(String userId, String timeframe) {
        if (llActivityBars == null || llActivityLabels == null) return;

        llActivityBars.removeAllViews();
        llActivityLabels.removeAllViews();
        if (rlActivityTooltip != null) rlActivityTooltip.setVisibility(View.INVISIBLE);

        // Track water intake milliliters dynamically for the graph
        fbHelper.getUsersCollection().document(userId).collection("water_logs")
            .get()
            .addOnSuccessListener(snapshot -> {
                List<DocumentSnapshot> docs = snapshot.getDocuments();

                if ("Daily".equals(timeframe)) {
                    int[] water = new int[4];
                    String[] blockNames = {"Night", "Morning", "Afternoon", "Evening"};
                    String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    for (DocumentSnapshot doc : docs) {
                        String logDate = doc.getString("date");
                        if (todayStr.equals(logDate)) {
                            String logTime = doc.getString("time"); // e.g. "08:30 AM"
                            Long amt = doc.getLong("amountMl");
                            int amount = amt != null ? amt.intValue() : 0;

                            int hour = parseHourFromTime(logTime);
                            int blockIdx = hour / 6;
                            if (blockIdx >= 0 && blockIdx < 4) {
                                water[blockIdx] += amount;
                            }
                        }
                    }

                    int max = 500;
                    for (int w : water) if (w > max) max = w;

                    String[] times = {"12am-6am", "6am-12pm", "12pm-6pm", "6pm-12am"};
                    for (int i = 0; i < 4; i++) {
                        int pct = (water[i] * 100) / max;
                        drawGraphBar(blockNames[i], pct, times[i], "Water Intake", water[i] + " ml");
                    }

                } else if ("Weekly".equals(timeframe)) {
                    int[] water = new int[7];
                    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

                    Calendar currentCal = Calendar.getInstance();
                    int currentWeek = currentCal.get(Calendar.WEEK_OF_YEAR);
                    int currentYear = currentCal.get(Calendar.YEAR);

                    for (DocumentSnapshot doc : docs) {
                        String logDate = doc.getString("date"); // YYYY-MM-DD
                        if (logDate != null) {
                            try {
                                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(logDate);
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(d);

                                if (cal.get(Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(Calendar.YEAR) == currentYear) {
                                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                                    int idx = dayOfWeek - 1;
                                    Long amt = doc.getLong("amountMl");
                                    if (idx >= 0 && idx < 7 && amt != null) {
                                        water[idx] += amt.intValue();
                                    }
                                }
                            } catch (Exception e) { /* ignore */ }
                        }
                    }

                    int max = 1000;
                    for (int w : water) if (w > max) max = w;

                    for (int i = 0; i < 7; i++) {
                        int pct = (water[i] * 100) / max;
                        drawGraphBar(days[i], pct, days[i], "Water Intake", water[i] + " ml");
                    }

                } else if ("Monthly".equals(timeframe)) {
                    int[] water = new int[4];
                    String[] labels = {"Wk 1", "Wk 2", "Wk 3", "Wk 4"};

                    Calendar currentCal = Calendar.getInstance();
                    int currentMonth = currentCal.get(Calendar.MONTH);
                    int currentYear = currentCal.get(Calendar.YEAR);

                    for (DocumentSnapshot doc : docs) {
                        String logDate = doc.getString("date");
                        if (logDate != null) {
                            try {
                                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(logDate);
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(d);

                                if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                                    int weekIdx = (dayOfMonth - 1) / 7;
                                    if (weekIdx > 3) weekIdx = 3;

                                    Long amt = doc.getLong("amountMl");
                                    if (amt != null) {
                                        water[weekIdx] += amt.intValue();
                                    }
                                }
                            } catch (Exception e) { /* ignore */ }
                        }
                    }

                    int max = 3000;
                    for (int w : water) if (w > max) max = w;

                    for (int i = 0; i < 4; i++) {
                        int pct = (water[i] * 100) / max;
                        drawGraphBar(labels[i], pct, labels[i], "Water Intake", water[i] + " ml");
                    }
                }
            });
    }

    private int parseHourFromTime(String timeStr) {
        // e.g. "08:30 AM" or "20:00"
        try {
            if (timeStr == null || timeStr.isEmpty()) return 12;
            Date date = new SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(timeStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.HOUR_OF_DAY);
        } catch (Exception e) {
            try {
                Date date = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(timeStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal.get(Calendar.HOUR_OF_DAY);
            } catch (Exception ex) {
                return 12; // fallback to midday
            }
        }
    }

    private void drawGraphBar(String labelText, int percentage, String tooltipDate, String tooltipWorkout, String tooltipPercent) {
        LinearLayout container = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        int barWidth = (int) (16 * getResources().getDisplayMetrics().density);
        int barHeight = (int) (130 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, barHeight);
        barParams.bottomMargin = (int) (4 * getResources().getDisplayMetrics().density);
        bar.setLayoutParams(barParams);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable));
        bar.setRotation(180);
        bar.setProgress(percentage);

        container.setOnClickListener(v -> {
            if (rlActivityTooltip != null) {
                rlActivityTooltip.setVisibility(View.VISIBLE);

                float barX = container.getX() + container.getWidth() / 2f - rlActivityTooltip.getWidth() / 2f;
                float parentWidth = llActivityBars.getWidth();
                if (barX < 0) barX = 0;
                if (barX + rlActivityTooltip.getWidth() > parentWidth) barX = parentWidth - rlActivityTooltip.getWidth();
                rlActivityTooltip.setX(barX);

                if (tvActivityGraphDate != null) tvActivityGraphDate.setText(tooltipDate);
                if (tvActivityGraphWorkout != null) tvActivityGraphWorkout.setText(tooltipWorkout);
                if (tvActivityGraphPercent != null) tvActivityGraphPercent.setText(tooltipPercent);
            }
        });

        container.addView(bar);
        llActivityBars.addView(container);

        TextView tvLabel = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvLabel.setLayoutParams(labelParams);
        tvLabel.setText(labelText);
        tvLabel.setTextSize(10);
        tvLabel.setTextColor(Color.parseColor("#ADA4A5"));
        tvLabel.setGravity(android.view.Gravity.CENTER);
        llActivityLabels.addView(tvLabel);
    }

    private void observeLatestActivities() {
        clearListeners();
        llLatestActivities.removeAllViews();

        final List<ActivityLogItem> items = new ArrayList<>();

        // 1. Fetch completed workouts
        ListenerRegistration workoutListener = fbHelper.getUsersCollection().document(uid)
            .collection("workout_history")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                
                // Rebuild and refresh lists to handle updates properly
                synchronized (items) {
                    // Remove old workout history logs
                    items.removeIf(item -> item.type == ActivityType.WORKOUT);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String title = doc.getString("workoutTitle");
                        Long cal = doc.getLong("caloriesBurned");
                        Timestamp ts = doc.getTimestamp("completedAt");
                        Date d = ts != null ? ts.toDate() : new Date();

                        String desc = (cal != null ? cal + " Calories Burned" : "Completed Workout");
                        items.add(new ActivityLogItem(
                                title != null ? title : "Workout Completed",
                                desc,
                                R.drawable.barbell,
                                d,
                                ActivityType.WORKOUT
                        ));
                    }
                    refreshLatestActivitiesUI(items);
                }
            });
        listeners.add(workoutListener);

        // 2. Fetch water intake logs
        ListenerRegistration waterListener = fbHelper.getUsersCollection().document(uid)
            .collection("water_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;

                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.WATER);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String time = doc.getString("time");
                        String dateStr = doc.getString("date");
                        Long amt = doc.getLong("amountMl");
                        int amount = amt != null ? amt.intValue() : 250;

                        Date d = new Date();
                        try {
                            d = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).parse(dateStr + " " + time);
                        } catch (Exception ex) {
                            try {
                                d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                            } catch (Exception exc) { /* ignore */ }
                        }

                        items.add(new ActivityLogItem(
                                "Drank " + amount + "ml Water",
                                "Logged at " + (time != null ? time : "today"),
                                R.drawable.drinking,
                                d,
                                ActivityType.WATER
                        ));
                    }
                    refreshLatestActivitiesUI(items);
                }
            });
        listeners.add(waterListener);

        // 3. Fetch sleep logs
        ListenerRegistration sleepListener = fbHelper.getUsersCollection().document(uid)
            .collection("sleep_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;

                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.SLEEP);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String dateStr = doc.getString("date");
                        Long hours = doc.getLong("hours");
                        Long mins = doc.getLong("minutes");

                        int h = hours != null ? hours.intValue() : 0;
                        int m = mins != null ? mins.intValue() : 0;

                        Date d = new Date();
                        try {
                            d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                        } catch (Exception ex) { /* ignore */ }

                        items.add(new ActivityLogItem(
                                "Logged Sleep: " + h + "h " + m + "m",
                                "Logged for date: " + (dateStr != null ? dateStr : "today"),
                                R.drawable.bed,
                                d,
                                ActivityType.SLEEP
                        ));
                    }
                    refreshLatestActivitiesUI(items);
                }
            });
        // 4. Fetch meal logs
        ListenerRegistration mealListener = fbHelper.getUsersCollection().document(uid)
            .collection("meal_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;

                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.MEAL);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String mealName = doc.getString("mealName");
                        String calories = doc.getString("calories");
                        Long ts = doc.getLong("timestamp");

                        Date d = ts != null ? new Date(ts) : new Date();
                        String calStr = calories != null ? calories + " kCal" : "Healthy Meal";

                        items.add(new ActivityLogItem(
                                "Ate: " + (mealName != null ? mealName : "Meal"),
                                "Intake: " + calStr,
                                R.drawable.apple_pie,
                                d,
                                ActivityType.MEAL
                        ));
                    }
                    refreshLatestActivitiesUI(items);
                }
            });
        listeners.add(mealListener);
    }

    private void refreshLatestActivitiesUI(List<ActivityLogItem> items) {
        runOnUiThread(() -> {
            llLatestActivities.removeAllViews();

            // Sort descending by timestamp
            Collections.sort(items, (o1, o2) -> o2.timestamp.compareTo(o1.timestamp));

            // Show top 6 activities
            int limit = Math.min(items.size(), 6);
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < limit; i++) {
                ActivityLogItem item = items.get(i);
                View view = inflater.inflate(R.layout.item_latest_activity, llLatestActivities, false);

                ImageView ivIcon = view.findViewById(R.id.ivActivityIcon);
                TextView tvTitle = view.findViewById(R.id.tvActivityTitle);
                TextView tvTime = view.findViewById(R.id.tvActivityTime);

                ivIcon.setImageResource(item.icon);
                tvTitle.setText(item.title);

                // Format friendly time description
                long diffMs = new Date().getTime() - item.timestamp.getTime();
                long diffMins = diffMs / (60 * 1000);
                long diffHours = diffMins / 60;
                long diffDays = diffHours / 24;

                String timeStr;
                if (diffMins < 1) {
                    timeStr = "Just now";
                } else if (diffMins < 60) {
                    timeStr = diffMins + " minutes ago";
                } else if (diffHours < 24) {
                    timeStr = diffHours + " hours ago";
                } else {
                    timeStr = diffDays + " days ago";
                }

                tvTime.setText(timeStr + " (" + item.description + ")");
                llLatestActivities.addView(view);
            }
        });
    }

    private void clearListeners() {
        for (ListenerRegistration l : listeners) {
            if (l != null) l.remove();
        }
        listeners.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearListeners();
    }

    enum ActivityType {
        WORKOUT, WATER, SLEEP, MEAL
    }

    private static class ActivityLogItem {
        String title, description;
        int icon;
        Date timestamp;
        ActivityType type;

        ActivityLogItem(String title, String description, int icon, Date timestamp, ActivityType type) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.timestamp = timestamp;
            this.type = type;
        }
    }
}
