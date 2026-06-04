package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailActivity extends AppCompatActivity {

    private String workoutTitle;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentExIndex = 0;
    private int currentSet = 1;
    private int timeRemaining;
    private int totalTime;
    private long totalTimeSpent = 0;
    private boolean isPaused = false;
    private boolean isWorkoutStarted = false;
    private CountDownTimer countDownTimer;
    private CountDownTimer restTimer;

    // Detail views
    private View detailScrollView;
    private TextView tvWorkoutTitle, tvWorkoutSubtitle;
    private ImageView ivWorkoutImage;

    // Execution views (reuse same layout elements dynamically)
    private View executionLayout;
    private TextView tvExProgress, tvExerciseName, tvExerciseInfo, tvTimer;
    private TextView btnPauseResume, btnSkip, btnDone;
    private android.widget.ProgressBar pbTimer;
    private ImageView ivExerciseImage;
    private View llRestOverlay;
    private TextView tvRestTimer, btnSkipRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        // Bind Detail Views
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        tvWorkoutSubtitle = findViewById(R.id.tvWorkoutSubtitle);
        ivWorkoutImage = findViewById(R.id.ivWorkoutImage);
        ivWorkoutImage.setImageResource(R.drawable.detail_top);

        // Retrieve workout data
        Intent intent = getIntent();
        workoutTitle = "Fullbody Workout";
        if (intent != null && intent.hasExtra("WORKOUT_TITLE")) {
            String title = intent.getStringExtra("WORKOUT_TITLE");
            if (title != null) workoutTitle = title;
        }

        tvWorkoutTitle.setText(workoutTitle);
        buildExerciseList();
        updateSubtitle();

        // Action Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isWorkoutStarted) {
                confirmQuit();
            } else {
                finish();
            }
        });

        findViewById(R.id.btnStartWorkout).setOnClickListener(v -> startWorkoutSession());
    }

    private void buildExerciseList() {
        exercises.clear();
        String title = workoutTitle.toLowerCase();

        if (title.contains("upperbody")) {
            exercises.add(new Exercise("Arm Raises", "x15", 3, 30, 15, "workout_1", "Raise arms overhead"));
            exercises.add(new Exercise("Push-Ups", "x12", 3, 40, 15, "workout_2", "Standard push-ups"));
            exercises.add(new Exercise("Tricep Dips", "x10", 3, 35, 15, "workout_3", "Use a bench or chair"));
            exercises.add(new Exercise("Shoulder Press", "x12", 3, 35, 15, "workout_1", "Press overhead"));
            exercises.add(new Exercise("Bicep Curls", "x15", 3, 30, 15, "workout_2", "Curl with control"));
        } else if (title.contains("lowerbody")) {
            exercises.add(new Exercise("Squats", "x20", 3, 40, 15, "workout_1", "Deep squats"));
            exercises.add(new Exercise("Lunges", "x15", 3, 35, 15, "workout_2", "Alternate legs"));
            exercises.add(new Exercise("Calf Raises", "x20", 3, 30, 10, "workout_3", "Rise on toes"));
            exercises.add(new Exercise("Wall Sit", "30s", 3, 30, 15, "workout_1", "Hold against wall"));
            exercises.add(new Exercise("Glute Bridge", "x15", 3, 35, 15, "workout_2", "Squeeze at top"));
        } else if (title.contains("ab")) {
            exercises.add(new Exercise("Crunches", "x20", 3, 30, 10, "workout_1", "Contract abs"));
            exercises.add(new Exercise("Plank", "30s", 3, 30, 15, "workout_2", "Hold plank position"));
            exercises.add(new Exercise("Leg Raises", "x15", 3, 30, 10, "workout_3", "Keep legs straight"));
            exercises.add(new Exercise("Mountain Climbers", "x20", 3, 35, 15, "workout_1", "Fast pace"));
            exercises.add(new Exercise("Bicycle Crunches", "x20", 3, 30, 10, "workout_2", "Twist and crunch"));
            exercises.add(new Exercise("Russian Twists", "x20", 3, 30, 10, "workout_3", "Twist side to side"));
        } else {
            // Fullbody default
            exercises.add(new Exercise("Warm Up", "5 min", 1, 60, 10, "workout_1", "Light stretching and jogging"));
            exercises.add(new Exercise("Jumping Jack", "x20", 3, 30, 15, "workout_2", "Full range of motion"));
            exercises.add(new Exercise("Skipping", "x15", 3, 30, 15, "workout_3", "Jump rope or air skip"));
            exercises.add(new Exercise("Squats", "x20", 3, 40, 15, "workout_1", "Deep bodyweight squats"));
            exercises.add(new Exercise("Push-Ups", "x12", 3, 35, 15, "workout_2", "Standard push-ups"));
            exercises.add(new Exercise("Burpees", "x10", 3, 45, 20, "workout_3", "Full body explosive"));
        }
    }

    private void updateSubtitle() {
        int totalSec = 0;
        int calPerEx = 15;
        for (Exercise e : exercises) {
            totalSec += (e.getDuration() * e.getSets()) + e.getRestTime();
        }
        int totalCal = exercises.size() * calPerEx * 3;
        tvWorkoutSubtitle.setText(exercises.size() + " Exercises | " + (totalSec / 60) + "mins | " + totalCal + " Calories Burn");
    }

    // ===================== WORKOUT SESSION =====================

    private void startWorkoutSession() {
        isWorkoutStarted = true;

        // Switch to execution layout
        Intent intent = new Intent(this, WorkoutSessionActivity.class);
        intent.putExtra("WORKOUT_TITLE", workoutTitle);
        intent.putExtra("EXERCISE_COUNT", exercises.size());

        // Pass exercise data as arrays
        String[] names = new String[exercises.size()];
        String[] reps = new String[exercises.size()];
        int[] sets = new int[exercises.size()];
        int[] durations = new int[exercises.size()];
        int[] restTimes = new int[exercises.size()];
        String[] images = new String[exercises.size()];

        for (int i = 0; i < exercises.size(); i++) {
            Exercise ex = exercises.get(i);
            names[i] = ex.getName();
            reps[i] = ex.getReps();
            sets[i] = ex.getSets();
            durations[i] = ex.getDuration();
            restTimes[i] = ex.getRestTime();
            images[i] = ex.getImageRes() != null ? ex.getImageRes() : "workout_1";
        }

        intent.putExtra("EX_NAMES", names);
        intent.putExtra("EX_REPS", reps);
        intent.putExtra("EX_SETS", sets);
        intent.putExtra("EX_DURATIONS", durations);
        intent.putExtra("EX_REST_TIMES", restTimes);
        intent.putExtra("EX_IMAGES", images);

        startActivity(intent);
        finish();
    }

    private void confirmQuit() {
        new AlertDialog.Builder(this)
            .setTitle("Quit Workout?")
            .setMessage("Are you sure you want to quit?")
            .setPositiveButton("Quit", (d, w) -> finish())
            .setNegativeButton("Stay", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        if (isWorkoutStarted) {
            confirmQuit();
        } else {
            super.onBackPressed();
        }
    }
}
