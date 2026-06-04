/*
 * AddScheduleActivity allows users to schedule new workouts with custom dates, times, and tags.
 */
package com.fitai.gym;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddScheduleActivity extends AppCompatActivity {

    private String selectedDate;
    private TextView tvSelectedDate, tvWorkoutValue, tvDifficultyValue;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        selectedDate = getIntent().getStringExtra("selected_date");
        if (selectedDate == null) {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        }

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvWorkoutValue = findViewById(R.id.tvWorkoutValue);
        tvDifficultyValue = findViewById(R.id.tvDifficultyValue);
        timePicker = findViewById(R.id.timePicker);

        
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate);
            tvSelectedDate.setText(new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date));
        } catch (Exception e) {
            tvSelectedDate.setText(selectedDate);
        }

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        findViewById(R.id.llChooseWorkout).setOnClickListener(v -> {
            String[] workouts = {"Upperbody Workout", "Fullbody Workout", "Abs Workout", "Lowerbody Workout", "Stretching"};
            new AlertDialog.Builder(this)
                .setTitle("Select Workout")
                .setItems(workouts, (dialog, which) -> tvWorkoutValue.setText(workouts[which]))
                .show();
        });

        findViewById(R.id.llDifficulty).setOnClickListener(v -> {
            String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
            new AlertDialog.Builder(this)
                .setTitle("Select Difficulty")
                .setItems(difficulties, (dialog, which) -> tvDifficultyValue.setText(difficulties[which]))
                .show();
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveSchedule());
    }

    private void saveSchedule() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String ampm = hour >= 12 ? "PM" : "AM";
        int dispHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d %s", dispHour, minute, ampm);

        String workoutName = tvWorkoutValue.getText().toString();
        String uid = FirebaseHelper.getInstance().getAuth().getUid();

        if (uid != null) {
            String[] colors = {"#C58BF2", "#92A3FD", "#F3CEFF"}; 
            String randomColor = colors[new Random().nextInt(colors.length)];
            
            ScheduledWorkout sw = new ScheduledWorkout(workoutName, timeStr, selectedDate, randomColor);
            
            FirebaseHelper.getInstance().getUsersCollection().document(uid)
                .collection("workout_schedule")
                .add(sw)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Schedule saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save schedule", Toast.LENGTH_SHORT).show());
        }
    }
}
