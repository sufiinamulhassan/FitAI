package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WorkoutDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        // Bind UI Elements
        TextView tvWorkoutTitle    = findViewById(R.id.tvWorkoutTitle);
        TextView tvWorkoutSubtitle = findViewById(R.id.tvWorkoutSubtitle);
        ImageView ivWorkoutImage   = findViewById(R.id.ivWorkoutImage);

        // The designer-intended hero image is always detail_top (full illustration)
        ivWorkoutImage.setImageResource(R.drawable.detail_top);

        // Retrieve dynamic data from Intent and update text only
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("WORKOUT_TITLE")) {
            String title = intent.getStringExtra("WORKOUT_TITLE");
            tvWorkoutTitle.setText(title != null ? title : "Fullbody Workout");

            // Subtitle varies by workout type
            if (title != null) {
                String subtitle;
                if (title.toLowerCase().contains("upperbody")) {
                    subtitle = "10 Exercises | 28mins | 280 Calories Burn";
                } else if (title.toLowerCase().contains("lowerbody")) {
                    subtitle = "12 Exercises | 40mins | 360 Calories Burn";
                } else if (title.toLowerCase().contains("ab")) {
                    subtitle = "14 Exercises | 20mins | 200 Calories Burn";
                } else {
                    subtitle = "11 Exercises | 32mins | 320 Calories Burn";
                }
                tvWorkoutSubtitle.setText(subtitle);
            }
        }

        // Action Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        final TextView tvTitle = tvWorkoutTitle;
        findViewById(R.id.btnStartWorkout).setOnClickListener(v -> {
            Toast.makeText(this, "Starting " + tvTitle.getText() + "...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to active workout timer page
            finish();
        });
    }
}
