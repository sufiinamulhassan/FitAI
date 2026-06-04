package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone workout execution activity for static (non-Firebase) workout plans.
 * Provides a full timer-based exercise flow with rest periods and completion tracking.
 */
public class WorkoutSessionActivity extends AppCompatActivity {

    private String workoutTitle;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentExIndex = 0;
    private int currentSet = 1;
    private int timeRemaining;
    private int totalTime;
    private long totalTimeSpent = 0;
    private boolean isPaused = false;
    private CountDownTimer countDownTimer;
    private CountDownTimer restTimer;

    // Views
    private TextView tvExProgress, tvExerciseName, tvExerciseInfo, tvTimer;
    private TextView btnPauseResume, btnSkip, btnDone, btnMinus10, btnPlus10;
    private ProgressBar pbTimer;
    private ImageView ivExerciseImage;
    private View llRestOverlay;
    private TextView tvRestTimer, btnSkipRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_execution);

        workoutTitle = getIntent().getStringExtra("WORKOUT_TITLE");
        if (workoutTitle == null) workoutTitle = "Workout";

        // Rebuild exercises from intent extras
        String[] names = getIntent().getStringArrayExtra("EX_NAMES");
        String[] reps = getIntent().getStringArrayExtra("EX_REPS");
        int[] sets = getIntent().getIntArrayExtra("EX_SETS");
        int[] durations = getIntent().getIntArrayExtra("EX_DURATIONS");
        int[] restTimes = getIntent().getIntArrayExtra("EX_REST_TIMES");
        String[] images = getIntent().getStringArrayExtra("EX_IMAGES");

        int count = getIntent().getIntExtra("EXERCISE_COUNT", 0);

        exercises.clear();
        if (names != null && count > 0) {
            for (int i = 0; i < count; i++) {
                Exercise ex = new Exercise();
                ex.setName(names[i]);
                ex.setReps(reps != null ? reps[i] : "x10");
                ex.setSets(sets != null ? sets[i] : 3);
                ex.setDuration(durations != null ? durations[i] : 30);
                ex.setRestTime(restTimes != null ? restTimes[i] : 15);
                ex.setImageRes(images != null ? images[i] : "workout_1");
                exercises.add(ex);
            }
        }

        if (exercises.isEmpty()) {
            Toast.makeText(this, "No exercises found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        tvExProgress = findViewById(R.id.tvExProgress);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvExerciseInfo = findViewById(R.id.tvExerciseInfo);
        tvTimer = findViewById(R.id.tvTimer);
        pbTimer = findViewById(R.id.pbTimer);
        ivExerciseImage = findViewById(R.id.ivExerciseImage);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnSkip = findViewById(R.id.btnSkip);
        btnDone = findViewById(R.id.btnDone);
        btnMinus10 = findViewById(R.id.btnMinus10);
        btnPlus10 = findViewById(R.id.btnPlus10);
        llRestOverlay = findViewById(R.id.llRestOverlay);
        tvRestTimer = findViewById(R.id.tvRestTimer);
        btnSkipRest = findViewById(R.id.btnSkipRest);

        // Listeners
        findViewById(R.id.btnClose).setOnClickListener(v -> confirmQuit());
        btnPauseResume.setOnClickListener(v -> togglePause());
        btnSkip.setOnClickListener(v -> skipExercise());
        btnDone.setOnClickListener(v -> completeCurrentExercise());
        btnMinus10.setOnClickListener(v -> adjustTime(-10));
        btnPlus10.setOnClickListener(v -> adjustTime(10));
        btnSkipRest.setOnClickListener(v -> {
            if (restTimer != null) restTimer.cancel();
            llRestOverlay.setVisibility(View.GONE);
        });

        // Start the first exercise
        startExercise(0, 1);
    }

    private void startExercise(int index, int set) {
        currentExIndex = index;
        currentSet = set;
        Exercise ex = exercises.get(index);

        tvExProgress.setText("Exercise " + (index + 1) + " of " + exercises.size());
        tvExerciseName.setText(ex.getName());
        tvExerciseInfo.setText(ex.getReps() + " · Set " + set + " of " + ex.getSets());
        ivExerciseImage.setImageResource(ex.getImageResourceId(this));

        totalTime = ex.getDuration();
        timeRemaining = totalTime;
        isPaused = false;
        btnPauseResume.setText("Pause");

        startTimer();
    }

    private void startTimer() {
        updateTimerDisplay();
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(timeRemaining * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                totalTimeSpent++;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                totalTimeSpent++;
                updateTimerDisplay();
                completeCurrentExercise();
            }
        }.start();
    }

    private void updateTimerDisplay() {
        int min = timeRemaining / 60;
        int sec = timeRemaining % 60;
        tvTimer.setText(String.format("%02d:%02d", min, sec));

        if (totalTime > 0) {
            int progress = (int) ((timeRemaining * 100.0) / totalTime);
            pbTimer.setProgress(progress);
        }
    }

    private void togglePause() {
        if (isPaused) {
            isPaused = false;
            btnPauseResume.setText("Pause");
            startTimer();
        } else {
            isPaused = true;
            btnPauseResume.setText("Resume");
            if (countDownTimer != null) countDownTimer.cancel();
        }
    }

    private void adjustTime(int seconds) {
        if (countDownTimer != null) countDownTimer.cancel();
        timeRemaining = Math.max(5, timeRemaining + seconds);
        totalTime = Math.max(totalTime, timeRemaining);
        if (!isPaused) {
            startTimer();
        } else {
            updateTimerDisplay();
        }
    }

    private void skipExercise() {
        if (countDownTimer != null) countDownTimer.cancel();
        moveToNext();
    }

    private void completeCurrentExercise() {
        if (countDownTimer != null) countDownTimer.cancel();
        Exercise ex = exercises.get(currentExIndex);

        if (currentSet < ex.getSets()) {
            showRestPeriod(ex.getRestTime(), () -> startExercise(currentExIndex, currentSet + 1));
        } else {
            moveToNext();
        }
    }

    private void moveToNext() {
        Exercise currentEx = exercises.get(currentExIndex);
        if (currentExIndex < exercises.size() - 1) {
            showRestPeriod(currentEx.getRestTime(), () -> startExercise(currentExIndex + 1, 1));
        } else {
            workoutComplete();
        }
    }

    private void showRestPeriod(int restSeconds, Runnable onComplete) {
        if (restSeconds <= 0) {
            onComplete.run();
            return;
        }

        llRestOverlay.setVisibility(View.VISIBLE);

        if (restTimer != null) restTimer.cancel();
        restTimer = new CountDownTimer(restSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int sec = (int) (millisUntilFinished / 1000);
                tvRestTimer.setText(String.format("%02d:%02d", sec / 60, sec % 60));
            }

            @Override
            public void onFinish() {
                llRestOverlay.setVisibility(View.GONE);
                onComplete.run();
            }
        }.start();

        btnSkipRest.setOnClickListener(v -> {
            if (restTimer != null) restTimer.cancel();
            llRestOverlay.setVisibility(View.GONE);
            onComplete.run();
        });
    }

    private void workoutComplete() {
        // Save to local SQLite database for local history/offline resiliency
        LocalDatabaseHelper localDb = new LocalDatabaseHelper(this);
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        localDb.insertWorkoutHistory(workoutTitle, exercises.size(), (int) totalTimeSpent, exercises.size() * 15, nowStr);

        // Save to Firebase if logged in
        FirebaseHelper fbHelper = FirebaseHelper.getInstance();
        String uid = fbHelper.getCurrentUserUid();

        if (uid != null) {
            // Save a simple workout history entry
            java.util.Map<String, Object> historyEntry = new java.util.HashMap<>();
            historyEntry.put("workoutTitle", workoutTitle);
            historyEntry.put("exerciseCount", exercises.size());
            historyEntry.put("totalTimeSeconds", (int) totalTimeSpent);
            historyEntry.put("caloriesBurned", exercises.size() * 15);
            historyEntry.put("completedAt", com.google.firebase.Timestamp.now());

            fbHelper.getUsersCollection().document(uid)
                .collection("workout_history")
                .add(historyEntry)
                .addOnCompleteListener(task -> {
                    // Log dynamic notification
                    java.util.Map<String, Object> notif = new java.util.HashMap<>();
                    notif.put("title", "Congratulations! You completed " + workoutTitle);
                    notif.put("timestamp", System.currentTimeMillis());
                    notif.put("type", "workout");
                    fbHelper.getUsersCollection().document(uid).collection("notifications").add(notif);
                    
                    launchCompleteScreen();
                });
        } else {
            launchCompleteScreen();
        }
    }

    private void launchCompleteScreen() {
        Intent intent = new Intent(this, WorkoutCompleteActivity.class);
        intent.putExtra("PLAN_TITLE", workoutTitle);
        intent.putExtra("DAY_NUMBER", 0);
        intent.putExtra("TOTAL_EXERCISES", exercises.size());
        intent.putExtra("TOTAL_TIME", (int) totalTimeSpent);
        intent.putExtra("CALORIES_BURNED", exercises.size() * 15);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmQuit() {
        new AlertDialog.Builder(this)
            .setTitle("Quit Workout?")
            .setMessage("Your progress for this session won't be saved.")
            .setPositiveButton("Quit", (d, w) -> {
                if (countDownTimer != null) countDownTimer.cancel();
                if (restTimer != null) restTimer.cancel();
                finish();
            })
            .setNegativeButton("Continue", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        confirmQuit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (restTimer != null) restTimer.cancel();
    }
}
