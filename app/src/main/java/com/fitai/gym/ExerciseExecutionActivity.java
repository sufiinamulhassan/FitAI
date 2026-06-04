/*
 * ExerciseExecutionActivity handles the active workout execution timer, exercise sequence navigation, and calorie-burn logs.
 */
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
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExerciseExecutionActivity extends AppCompatActivity {

    private String planId, planTitle, level;
    private int dayNumber;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentExIndex = 0;
    private int currentSet = 1;
    private int timeRemaining;
    private int totalTime;
    private long totalTimeSpent = 0;
    private int completedExerciseCount = 0;

    private boolean isPaused = false;
    private CountDownTimer countDownTimer;
    private CountDownTimer restTimer;

    private TextView tvExProgress, tvExerciseName, tvExerciseInfo, tvTimer;
    private TextView btnPauseResume, btnSkip, btnDone, btnMinus10, btnPlus10;
    private ProgressBar pbTimer;
    private ImageView ivExerciseImage;
    private View llRestOverlay;
    private TextView tvRestTimer, btnSkipRest;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_execution);

        fbHelper = FirebaseHelper.getInstance();
        planId = getIntent().getStringExtra("PLAN_ID");
        dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
        planTitle = getIntent().getStringExtra("PLAN_TITLE");
        level = getIntent().getStringExtra("LEVEL");

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

        findViewById(R.id.btnClose).setOnClickListener(v -> confirmQuit());
        btnPauseResume.setOnClickListener(v -> togglePause());
        btnSkip.setOnClickListener(v -> skipExercise());
        btnDone.setOnClickListener(v -> completeCurrentExercise());
        btnMinus10.setOnClickListener(v -> adjustTime(-10));
        btnPlus10.setOnClickListener(v -> adjustTime(10));
        btnSkipRest.setOnClickListener(v -> skipRest());

        loadExercises();
    }

    private void loadExercises() {
        fbHelper.getDayDocument(planId, dayNumber).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "Day data not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

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

                if (exercises.isEmpty()) {
                    Toast.makeText(this, "No exercises found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                startExercise(0, 1);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
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
            completedExerciseCount++;
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

    private void skipRest() {
        if (restTimer != null) restTimer.cancel();
        llRestOverlay.setVisibility(View.GONE);
    }

    private void workoutComplete() {
        LocalDatabaseHelper localDb = new LocalDatabaseHelper(this);
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        int estCal = completedExerciseCount * 15;
        localDb.insertWorkoutHistory((planTitle != null ? planTitle : "Workout") + " - Day " + dayNumber, completedExerciseCount, (int) totalTimeSpent, estCal, nowStr);

        String uid = fbHelper.getCurrentUserUid();
        if (uid != null && planId != null) {
            fbHelper.getUserProgressCollection(uid).document(planId).get()
                .addOnSuccessListener(doc -> {
                    UserProgress progress;
                    if (doc.exists()) {
                        progress = doc.toObject(UserProgress.class);
                    } else {
                        progress = new UserProgress(planId, planTitle != null ? planTitle : "Workout", level);
                    }
                    if (progress != null) {
                        int cal = completedExerciseCount * 15;
                        int finalCal = cal;
                        progress.markDayComplete(dayNumber, cal, (int) totalTimeSpent);
                        fbHelper.getUserProgressCollection(uid).document(planId)
                            .set(progress)
                            .addOnSuccessListener(v -> {
                                java.util.Map<String, Object> historyEntry = new java.util.HashMap<>();
                                historyEntry.put("workoutTitle", (planTitle != null ? planTitle : "Workout") + " - Day " + dayNumber);
                                historyEntry.put("exerciseCount", completedExerciseCount);
                                historyEntry.put("totalExercises", exercises.size());
                                historyEntry.put("skippedCount", exercises.size() - completedExerciseCount);
                                historyEntry.put("totalTimeSeconds", (int) totalTimeSpent);
                                historyEntry.put("caloriesBurned", finalCal);
                                historyEntry.put("completedAt", com.google.firebase.Timestamp.now());

                                fbHelper.getUsersCollection().document(uid)
                                    .collection("workout_history")
                                    .add(historyEntry)
                                    .addOnCompleteListener(t -> {
                                         java.util.Map<String, Object> notif = new java.util.HashMap<>();
                                         notif.put("title", "Congratulations! You completed " + (planTitle != null ? planTitle : "Workout") + " - Day " + dayNumber);
                                         notif.put("timestamp", System.currentTimeMillis());
                                         notif.put("type", "workout");
                                         fbHelper.getUsersCollection().document(uid).collection("notifications").add(notif);
                                         
                                         launchCompleteScreen();
                                     });
                            })
                            .addOnFailureListener(e -> launchCompleteScreen());
                    } else {
                        launchCompleteScreen();
                    }
                })
                .addOnFailureListener(e -> launchCompleteScreen());
        } else {
            launchCompleteScreen();
        }
    }

    private void launchCompleteScreen() {
        Intent intent = new Intent(this, WorkoutCompleteActivity.class);
        intent.putExtra("PLAN_TITLE", planTitle);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.putExtra("TOTAL_EXERCISES", completedExerciseCount);
        intent.putExtra("TOTAL_TIME", (int) totalTimeSpent);
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

    private int toInt(Object obj, int fallback) {
        if (obj == null) return fallback;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); }
        catch (Exception e) { return fallback; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (restTimer != null) restTimer.cancel();
    }
}
