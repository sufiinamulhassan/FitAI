package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName, tvBMIDisplay, tvBMIValue;
    private android.widget.ProgressBar pbBMIProgress;
    private BottomNavigationView bottomNav;
    private ImageView ivMainProfile;
    // Water Intake Views
    private TextView tvWaterTotal;
    private android.view.View viewWaterProgress, viewWaterSpace;
    private android.widget.LinearLayout llWaterLogs;
    // Sleep View
    private TextView tvSleepTotal;
    // Calories Views
    private TextView tvCaloriesTotal, tvCaloriesLeft;
    private android.widget.ProgressBar pbCaloriesProgress;
    // Graph Tooltip Views
    private TextView tvGraphDate, tvGraphPercent, tvGraphWorkout;
    private android.view.View rlTooltip;
    private TextView tvWorkoutProgressDropdown;
    private android.widget.LinearLayout llGraphBars, llGraphLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUserName = findViewById(R.id.tvUserName);
        tvBMIDisplay = findViewById(R.id.tvBMIDisplay);
        tvBMIValue = findViewById(R.id.tvBMIValue);
        pbBMIProgress = findViewById(R.id.pbBMIProgress);
        bottomNav = findViewById(R.id.bottomNav);
        ivMainProfile = findViewById(R.id.ivMainProfilePic);
        
        tvWaterTotal = findViewById(R.id.tvWaterTotal);
        viewWaterProgress = findViewById(R.id.viewWaterProgress);
        viewWaterSpace = findViewById(R.id.viewWaterSpace);
        llWaterLogs = findViewById(R.id.llWaterLogs);
        tvSleepTotal = findViewById(R.id.tvSleepTotal);
        tvCaloriesTotal = findViewById(R.id.tvCaloriesTotal);
        tvCaloriesLeft = findViewById(R.id.tvCaloriesLeft);
        pbCaloriesProgress = findViewById(R.id.pbCaloriesProgress);
        tvGraphDate = findViewById(R.id.tvGraphDate);
        tvGraphPercent = findViewById(R.id.tvGraphPercent);
        tvGraphWorkout = findViewById(R.id.tvGraphWorkout);
        rlTooltip = findViewById(R.id.rlTooltip);
        tvWorkoutProgressDropdown = findViewById(R.id.tvWorkoutProgressDropdown);
        llGraphBars = findViewById(R.id.llGraphBars);
        llGraphLabels = findViewById(R.id.llGraphLabels);

        // Real-time Greeting and Image from Firebase
        FirebaseHelper fbHelper = FirebaseHelper.getInstance();
        if (fbHelper.getAuth().getCurrentUser() != null) {
            String uid = fbHelper.getAuth().getUid();
            fbHelper.getUsersCollection().document(uid).addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null || !snapshot.exists()) return;
                String name = snapshot.getString("name");
                String profilePicUrl = snapshot.getString("profilePicUrl");

                if (name != null && !name.isEmpty()) {
                    tvUserName.setText("Welcome Back,\n" + name);
                } else if (fbHelper.getAuth().getCurrentUser() != null && fbHelper.getAuth().getCurrentUser().getDisplayName() != null) {
                    tvUserName.setText("Welcome Back,\n" + fbHelper.getAuth().getCurrentUser().getDisplayName());
                } else {
                    tvUserName.setText("Welcome Back!");
                }
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    if (profilePicUrl.startsWith("http")) {
                        Glide.with(this).load(profilePicUrl).into(ivMainProfile);
                    } else {
                        android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(profilePicUrl);
                        if (bitmap != null) ivMainProfile.setImageBitmap(bitmap);
                    }
                }
                
                updateBMIData(snapshot);
            });

            setupWaterTracker(uid);
            setupSleepTracker(uid);
            setupLatestWorkouts(uid);
            setupCaloriesTracker(uid);
            setupWorkoutProgressGraph(uid);
        }

        setupNavigation();

        // Profile icon click
        ivMainProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Initialize SyncManager
        SyncManager syncManager = new SyncManager(this);
        syncManager.syncWorkoutsFromCloud();

        findViewById(R.id.ivNotification).setOnClickListener(v ->
            startActivity(new Intent(this, NotificationActivity.class)));
        
        findViewById(R.id.btnCheckTarget).setOnClickListener(v ->
            startActivity(new Intent(this, ActivityTrackerActivity.class)));
            
        findViewById(R.id.tvSeeMoreWorkout).setOnClickListener(v ->
            startActivity(new Intent(this, WorkoutTrackerActivity.class)));
            
        findViewById(R.id.btnViewBMI).setOnClickListener(v ->
            startActivity(new Intent(this, ProgressActivity.class)));
    }

    private void setupSleepTracker(String uid) {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        
        FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("sleep_logs")
            .document(today)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null || !snapshot.exists()) {
                    tvSleepTotal.setText("0h 0m");
                    return;
                }
                
                SleepLog log = snapshot.toObject(SleepLog.class);
                if (log != null) {
                    tvSleepTotal.setText(log.getHours() + "h " + log.getMinutes() + "m");
                } else {
                    tvSleepTotal.setText("0h 0m");
                }
            });

        findViewById(R.id.cardSleep).setOnClickListener(v -> showAddSleepDialog(uid));
        findViewById(R.id.btnAddSleep).setOnClickListener(v -> showAddSleepDialog(uid));
    }

    private void showAddSleepDialog(String uid) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etHours = new EditText(this);
        etHours.setHint("Hours");
        etHours.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etHours);

        final EditText etMinutes = new EditText(this);
        etMinutes.setHint("Minutes");
        etMinutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etMinutes);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Log Your Sleep")
            .setView(layout)
            .setPositiveButton("Log", (dialog, which) -> {
                String hStr = etHours.getText().toString();
                String mStr = etMinutes.getText().toString();
                if (!hStr.isEmpty()) {
                    int hours = Integer.parseInt(hStr);
                    int minutes = mStr.isEmpty() ? 0 : Integer.parseInt(mStr);
                    String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                    SleepLog log = new SleepLog(today, hours, minutes);
                    FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("sleep_logs").document(today).set(log);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setupWaterTracker(String uid) {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        
        FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("water_logs")
            .whereEqualTo("date", today)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                
                int totalMl = 0;
                llWaterLogs.removeAllViews();
                
                if (snapshot.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("No logs today");
                    tv.setTextSize(10);
                    tv.setTextColor(android.graphics.Color.parseColor("#ADA4A5"));
                    llWaterLogs.addView(tv);
                } else {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        WaterIntake log = doc.toObject(WaterIntake.class);
                        if (log != null) {
                            totalMl += log.getAmountMl();
                            addWaterLogView(log);
                        }
                    }
                }
                
                updateWaterUI(totalMl);
            });

        findViewById(R.id.cardWater).setOnClickListener(v -> showAddWaterDialog(uid));
        findViewById(R.id.btnAddWaterLog).setOnClickListener(v -> showAddWaterDialog(uid));
        findViewById(R.id.btnSetWaterGoal).setOnClickListener(v -> showSetGoalDialog(uid));
    }

    private void showSetGoalDialog(String uid) {
        EditText etGoal = new EditText(this);
        etGoal.setHint("Goal in Liters (e.g. 4.0)");
        etGoal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set Water Goal")
            .setView(etGoal)
            .setPositiveButton("Save", (dialog, which) -> {
                String goalStr = etGoal.getText().toString();
                if (!goalStr.isEmpty()) {
                    float liters = Float.parseFloat(goalStr);
                    getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit()
                        .putFloat("water_goal_liters", liters)
                        .apply();
                    // Trigger UI update by fetching again or re-calculating
                    recalculateWaterProgress();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void recalculateWaterProgress() {
         String uid = FirebaseHelper.getInstance().getAuth().getUid();
         if (uid == null) return;
         String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
         FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("water_logs")
            .whereEqualTo("date", today).get().addOnSuccessListener(snapshot -> {
                int totalMl = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    WaterIntake log = doc.toObject(WaterIntake.class);
                    if (log != null) totalMl += log.getAmountMl();
                }
                updateWaterUI(totalMl);
            });
    }

    private void addWaterLogView(WaterIntake log) {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.item_water_log, llWaterLogs, false);
        TextView tvTime = view.findViewById(R.id.tvLogTime);
        TextView tvAmount = view.findViewById(R.id.tvLogAmount);
        
        tvTime.setText(log.getTime());
        tvAmount.setText(log.getAmountMl() + "ml");
        
        llWaterLogs.addView(view);
    }

    private void updateWaterUI(int totalMl) {
        float totalLiters = totalMl / 1000f;
        
        float goalLiters = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).getFloat("water_goal_liters", 4.0f);
        
        tvWaterTotal.setText(String.format("%.1f / %.1f Liters", totalLiters, goalLiters));
        
        // Accurate Progress bar logic with weight splitting
        float progress = Math.min(totalLiters / goalLiters, 1f);
        
        android.widget.LinearLayout.LayoutParams progressParams = (android.widget.LinearLayout.LayoutParams) viewWaterProgress.getLayoutParams();
        progressParams.weight = progress;
        viewWaterProgress.setLayoutParams(progressParams);

        android.widget.LinearLayout.LayoutParams spaceParams = (android.widget.LinearLayout.LayoutParams) viewWaterSpace.getLayoutParams();
        spaceParams.weight = 1.0f - progress;
        viewWaterSpace.setLayoutParams(spaceParams);
    }

    private void showAddWaterDialog(String uid) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etTime = new EditText(this);
        etTime.setHint("Tap to select Time");
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> {
            java.util.Calendar mcurrentTime = java.util.Calendar.getInstance();
            int hour = mcurrentTime.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(java.util.Calendar.MINUTE);
            android.app.TimePickerDialog mTimePicker;
            mTimePicker = new android.app.TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                String amPm = (selectedHour >= 12) ? "PM" : "AM";
                int displayHour = (selectedHour > 12) ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
                etTime.setText(String.format("%02d:%02d %s", displayHour, selectedMinute, amPm));
            }, hour, minute, false);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });
        layout.addView(etTime);

        final EditText etAmount = new EditText(this);
        etAmount.setHint("Amount (ML)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etAmount);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Water Intake")
            .setView(layout)
            .setPositiveButton("Add", (dialog, which) -> {
                String time = etTime.getText().toString();
                String amountStr = etAmount.getText().toString();
                if (!time.isEmpty() && !amountStr.isEmpty()) {
                    int amount = Integer.parseInt(amountStr);
                    String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                    WaterIntake log = new WaterIntake(time, amount, today);
                    FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("water_logs").add(log);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the Home icon is selected when returning to MainActivity
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }


    private void updateBMICard() {
        String weightStr = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).getString("weight", "70");
        String heightStr = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).getString("height", "170");

        try {
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr) / 100f; // CM to M
            float bmi = weight / (height * height);
            
            String bmiFormatted = String.format("%.1f", bmi);
            tvBMIDisplay.setText(bmiFormatted);
            
            // Percentage for circular arc (Ideal BMI is ~22, map 0-40 range)
            int progress = (int) ((bmi / 40f) * 100);
            pbBMIProgress.setProgress(progress);

            if (bmi < 18.5) {
                tvBMIValue.setText("You are underweight");
            } else if (bmi < 25) {
                tvBMIValue.setText("You have a normal weight");
            } else if (bmi < 30) {
                tvBMIValue.setText("You are overweight");
            } else {
                tvBMIValue.setText("You are in obesity range");
            }
        } catch (Exception e) {
            tvBMIDisplay.setText("20.1");
        }
    }

    private void updateBMIData(com.google.firebase.firestore.DocumentSnapshot snapshot) {
        if (!snapshot.exists()) return;

        try {
            Double weight = snapshot.getDouble("weight");
            Double height = snapshot.getDouble("height"); // in cm

            if (weight != null && height != null && height > 0) {
                float heightM = height.floatValue() / 100f;
                float bmi = weight.floatValue() / (heightM * heightM);

                tvBMIValue.setText(String.format("%.1f", bmi));

                String category;
                int progress;

                if (bmi < 18.5) {
                    category = "Underweight";
                    progress = (int) (bmi / 18.5 * 25);
                } else if (bmi < 25) {
                    category = "Normal Weight";
                    progress = 25 + (int) ((bmi - 18.5) / 6.5 * 25);
                } else if (bmi < 30) {
                    category = "Overweight";
                    progress = 50 + (int) ((bmi - 25) / 5.0 * 25);
                } else {
                    category = "Obesity";
                    progress = 75 + (int) (Math.min((bmi - 30) / 10.0, 1.0) * 25);
                }

                tvBMIDisplay.setText(category);
                pbBMIProgress.setProgress(progress);

                // Update BMI back to database for persistent tracking
                Double existingBmi = snapshot.getDouble("bmi");
                if (existingBmi == null || Math.abs(existingBmi - bmi) > 0.05) {
                    snapshot.getReference().update("bmi", (double) bmi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_activity) {
                Intent intent = new Intent(this, WorkoutTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
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

    private void setupLatestWorkouts(String uid) {
        android.widget.LinearLayout llLatestWorkouts = findViewById(R.id.llLatestWorkouts);
        if (llLatestWorkouts == null) return;

        // Load both plan-based progress AND standalone workout history
        FirebaseHelper fb = FirebaseHelper.getInstance();
        llLatestWorkouts.removeAllViews();
        final int[] addedCount = {0};

        // 1. Load standalone workout history (most recent 3)
        fb.getUsersCollection().document(uid).collection("workout_history")
            .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3).get()
            .addOnSuccessListener(histSnap -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : histSnap.getDocuments()) {
                    String title = doc.getString("workoutTitle");
                    Long cal = doc.getLong("caloriesBurned");
                    Long timeSec = doc.getLong("totalTimeSeconds");
                    if (title == null) continue;

                    android.view.View view = android.view.LayoutInflater.from(this)
                            .inflate(R.layout.item_latest_workout, llLatestWorkouts, false);
                    TextView tvTitle = view.findViewById(R.id.tvWorkoutTitle);
                    TextView tvStats = view.findViewById(R.id.tvWorkoutStats);
                    android.widget.ProgressBar pb = view.findViewById(R.id.pbWorkoutProgress);

                    tvTitle.setText(title);
                    int mins = timeSec != null ? (int)(timeSec / 60) : 0;
                    tvStats.setText(mins + " min | " + (cal != null ? cal : 0) + " Calories Burn");
                    pb.setProgress(100);

                    view.setOnClickListener(v -> startActivity(new Intent(this, WorkoutTrackerActivity.class)));
                    llLatestWorkouts.addView(view);
                    addedCount[0]++;

                    // Update graph tooltip with most recent workout
                    if (addedCount[0] == 1) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                        if (ts != null && tvGraphDate != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, dd MMM", java.util.Locale.getDefault());
                            tvGraphDate.setText(sdf.format(ts.toDate()));
                        }
                        if (tvGraphWorkout != null) tvGraphWorkout.setText(title);
                        if (tvGraphPercent != null) tvGraphPercent.setText("100% \u2191");
                    }
                }

                // 2. Also load plan-based progress
                fb.getUserProgressCollection(uid).limit(2).get()
                    .addOnSuccessListener(planSnap -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : planSnap.getDocuments()) {
                            UserProgress progress = doc.toObject(UserProgress.class);
                            if (progress == null) continue;

                            android.view.View view = android.view.LayoutInflater.from(this)
                                    .inflate(R.layout.item_latest_workout, llLatestWorkouts, false);
                            TextView tvTitle = view.findViewById(R.id.tvWorkoutTitle);
                            TextView tvStats = view.findViewById(R.id.tvWorkoutStats);
                            android.widget.ProgressBar pb = view.findViewById(R.id.pbWorkoutProgress);

                            tvTitle.setText(progress.getPlanTitle());
                            int daysDone = (progress.getCompletedDays() != null) ? progress.getCompletedDays().size() : 0;
                            int cals = progress.getTotalCaloriesBurned();
                            tvStats.setText(daysDone + " Days Done | " + cals + " Calories Burn");
                            int pct = Math.min((daysDone * 100) / 28, 100);
                            if (pct < 5 && daysDone > 0) pct = 5;
                            pb.setProgress(pct);

                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(this, WorkoutPlanDetailActivity.class);
                                intent.putExtra("PLAN_ID", progress.getPlanId());
                                startActivity(intent);
                            });
                            llLatestWorkouts.addView(view);
                            addedCount[0]++;
                        }

                        if (addedCount[0] == 0) {
                            TextView tv = new TextView(this);
                            tv.setText("No workouts yet. Head to Tracker to start!");
                            tv.setTextSize(14);
                            tv.setTextColor(android.graphics.Color.parseColor("#ADA4A5"));
                            tv.setPadding(0, 20, 0, 20);
                            llLatestWorkouts.addView(tv);
                        }
                    });
            });
    }

    private void setupCaloriesTracker(String uid) {
        int dailyGoal = 500; // Default daily calorie burn goal

        FirebaseHelper.getInstance().getUsersCollection().document(uid)
            .collection("workout_history")
            .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;

                // Sum today's calories
                String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                int todayCalories = 0;

                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                    if (ts != null) {
                        String docDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(ts.toDate());
                        if (today.equals(docDate)) {
                            Long cal = doc.getLong("caloriesBurned");
                            if (cal != null) todayCalories += cal.intValue();
                        }
                    }
                }

                if (tvCaloriesTotal != null) tvCaloriesTotal.setText(todayCalories + " kCal");
                int left = Math.max(dailyGoal - todayCalories, 0);
                if (tvCaloriesLeft != null) tvCaloriesLeft.setText(left + "kCal\nleft");
                int progress = Math.min((todayCalories * 100) / dailyGoal, 100);
                if (pbCaloriesProgress != null) pbCaloriesProgress.setProgress(progress);
            });
    }

    private void setupWorkoutProgressGraph(String uid) {
        if (tvWorkoutProgressDropdown == null) return;

        // Default to Weekly
        loadWorkoutProgressGraph(uid, "Weekly");

        tvWorkoutProgressDropdown.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, tvWorkoutProgressDropdown);
            popup.getMenu().add("Daily");
            popup.getMenu().add("Weekly");
            popup.getMenu().add("Monthly");

            popup.setOnMenuItemClickListener(item -> {
                String choice = item.getTitle().toString();
                tvWorkoutProgressDropdown.setText(choice);
                loadWorkoutProgressGraph(uid, choice);
                return true;
            });
            popup.show();
        });
    }

    private void loadWorkoutProgressGraph(String uid, String timeframe) {
        if (llGraphBars == null || llGraphLabels == null) return;

        llGraphBars.removeAllViews();
        llGraphLabels.removeAllViews();
        if (rlTooltip != null) rlTooltip.setVisibility(android.view.View.INVISIBLE);

        FirebaseHelper.getInstance().getUsersCollection().document(uid)
            .collection("workout_history")
            .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = snapshot.getDocuments();

                if ("Daily".equals(timeframe)) {
                    int[] cals = new int[4];
                    String[] blockNames = {"Night", "Morning", "Afternoon", "Evening"};
                    String[] blockWorkouts = {"None", "None", "None", "None"};

                    String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

                    for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                        if (ts == null) continue;

                        java.util.Date d = ts.toDate();
                        String docDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(d);

                        if (todayStr.equals(docDate)) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(d);
                            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
                            int blockIdx = hour / 6;
                            if (blockIdx >= 0 && blockIdx < 4) {
                                Long calVal = doc.getLong("caloriesBurned");
                                int c = calVal != null ? calVal.intValue() : 0;
                                cals[blockIdx] += c;
                                String title = doc.getString("workoutTitle");
                                if (title != null) {
                                    blockWorkouts[blockIdx] = title;
                                }
                            }
                        }
                    }

                    int max = 300;
                    for (int c : cals) if (c > max) max = c;

                    String[] times = {"12am-6am", "6am-12pm", "12pm-6pm", "6pm-12am"};
                    for (int i = 0; i < 4; i++) {
                        int pct = max > 0 ? (cals[i] * 100) / max : 0;
                        String w = blockWorkouts[i];
                        drawGraphBar(blockNames[i], pct, times[i], w, cals[i] + " Cal");
                    }

                } else if ("Weekly".equals(timeframe)) {
                    int[] cals = new int[7];
                    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                    String[] dayWorkouts = {"None", "None", "None", "None", "None", "None", "None"};

                    java.util.Calendar currentCal = java.util.Calendar.getInstance();
                    int currentWeek = currentCal.get(java.util.Calendar.WEEK_OF_YEAR);
                    int currentYear = currentCal.get(java.util.Calendar.YEAR);

                    for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                        if (ts == null) continue;

                        java.util.Date d = ts.toDate();
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(d);

                        if (cal.get(java.util.Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(java.util.Calendar.YEAR) == currentYear) {
                            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
                            int idx = dayOfWeek - 1;
                            if (idx >= 0 && idx < 7) {
                                Long calVal = doc.getLong("caloriesBurned");
                                int c = calVal != null ? calVal.intValue() : 0;
                                cals[idx] += c;
                                String title = doc.getString("workoutTitle");
                                if (title != null) {
                                    dayWorkouts[idx] = title;
                                }
                            }
                        }
                    }

                    int max = 400;
                    for (int c : cals) if (c > max) max = c;

                    for (int i = 0; i < 7; i++) {
                        int pct = max > 0 ? (cals[i] * 100) / max : 0;
                        String w = dayWorkouts[i];
                        drawGraphBar(days[i], pct, days[i], w, cals[i] + " Cal");
                    }

                } else if ("Monthly".equals(timeframe)) {
                    int[] cals = new int[4];
                    String[] labels = {"Wk 1", "Wk 2", "Wk 3", "Wk 4"};
                    String[] wkWorkouts = {"None", "None", "None", "None"};

                    java.util.Calendar currentCal = java.util.Calendar.getInstance();
                    int currentMonth = currentCal.get(java.util.Calendar.MONTH);
                    int currentYear = currentCal.get(java.util.Calendar.YEAR);

                    for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("completedAt");
                        if (ts == null) continue;

                        java.util.Date d = ts.toDate();
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(d);

                        if (cal.get(java.util.Calendar.MONTH) == currentMonth && cal.get(java.util.Calendar.YEAR) == currentYear) {
                            int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
                            int weekIdx = (dayOfMonth - 1) / 7;
                            if (weekIdx > 3) weekIdx = 3;

                            Long calVal = doc.getLong("caloriesBurned");
                            int c = calVal != null ? calVal.intValue() : 0;
                            cals[weekIdx] += c;
                            String title = doc.getString("workoutTitle");
                            if (title != null) {
                                wkWorkouts[weekIdx] = title;
                            }
                        }
                    }

                    int max = 1500;
                    for (int c : cals) if (c > max) max = c;

                    for (int i = 0; i < 4; i++) {
                        int pct = max > 0 ? (cals[i] * 100) / max : 0;
                        String w = wkWorkouts[i];
                        drawGraphBar(labels[i], pct, labels[i], w, cals[i] + " Cal");
                    }
                }
            });
    }

    private void drawGraphBar(String labelText, int percentage, String tooltipDate, String tooltipWorkout, String tooltipPercent) {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                0, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        container.setLayoutParams(params);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

        android.widget.ProgressBar bar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        int barWidth = (int) (18 * getResources().getDisplayMetrics().density);
        int barHeight = (int) (160 * getResources().getDisplayMetrics().density);
        android.widget.LinearLayout.LayoutParams barParams = new android.widget.LinearLayout.LayoutParams(barWidth, barHeight);
        barParams.bottomMargin = (int) (4 * getResources().getDisplayMetrics().density);
        bar.setLayoutParams(barParams);
        bar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable));
        bar.setRotation(180);
        bar.setProgress(percentage);

        container.setOnClickListener(v -> {
            if (rlTooltip != null) {
                rlTooltip.setVisibility(android.view.View.VISIBLE);

                float barX = container.getX() + container.getWidth() / 2f - rlTooltip.getWidth() / 2f;
                float parentWidth = llGraphBars.getWidth();
                if (barX < 0) barX = 0;
                if (barX + rlTooltip.getWidth() > parentWidth) barX = parentWidth - rlTooltip.getWidth();
                rlTooltip.setX(barX);

                if (tvGraphDate != null) tvGraphDate.setText(tooltipDate);
                if (tvGraphWorkout != null) tvGraphWorkout.setText(tooltipWorkout);
                if (tvGraphPercent != null) tvGraphPercent.setText(tooltipPercent);
            }
        });

        container.addView(bar);
        llGraphBars.addView(container);

        TextView tvLabel = new TextView(this);
        android.widget.LinearLayout.LayoutParams labelParams = new android.widget.LinearLayout.LayoutParams(
                0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvLabel.setLayoutParams(labelParams);
        tvLabel.setText(labelText);
        tvLabel.setTextSize(10);
        tvLabel.setTextColor(android.graphics.Color.parseColor("#ADA4A5"));
        tvLabel.setGravity(android.view.Gravity.CENTER);
        llGraphLabels.addView(tvLabel);
    }
}
