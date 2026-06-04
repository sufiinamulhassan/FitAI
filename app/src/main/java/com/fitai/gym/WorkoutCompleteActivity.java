/*
 * WorkoutCompleteActivity displays statistics, calories burned, and total duration upon completing a workout session.
 */
package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WorkoutCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_complete);

        String planTitle = getIntent().getStringExtra("PLAN_TITLE");
        int dayNumber = getIntent().getIntExtra("DAY_NUMBER", 0);
        int totalExercises = getIntent().getIntExtra("TOTAL_EXERCISES", 0);
        int totalTime = getIntent().getIntExtra("TOTAL_TIME", 0);
        int caloriesBurned = getIntent().getIntExtra("CALORIES_BURNED", totalExercises * 15);

        TextView tvCompleteMsg = findViewById(R.id.tvCompleteMsg);
        if (dayNumber > 0) {
            tvCompleteMsg.setText("You completed Day " + dayNumber + " of\n" +
                    (planTitle != null ? planTitle : "your workout") + "!");
        } else {
            tvCompleteMsg.setText("You completed\n" +
                    (planTitle != null ? planTitle : "your workout") + "!");
        }

        TextView tvExDone = findViewById(R.id.tvExDone);
        tvExDone.setText(String.valueOf(totalExercises));

        TextView tvTimeDone = findViewById(R.id.tvTimeDone);
        int min = totalTime / 60;
        int sec = totalTime % 60;
        tvTimeDone.setText(String.format("%02d:%02d", min, sec));

        
        TextView tvCalDone = findViewById(R.id.tvCalDone);
        if (tvCalDone != null) {
            tvCalDone.setText(String.valueOf(caloriesBurned));
        }

        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
