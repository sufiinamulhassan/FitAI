package com.fitai.gym;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExerciseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        String name = getIntent().getStringExtra("exercise_name");
        String reps = getIntent().getStringExtra("exercise_reps");
        int imageRes = getIntent().getIntExtra("exercise_image", R.drawable.workout_1);

        TextView tvTitle = findViewById(R.id.tvExerciseTitle);
        TextView tvInfo = findViewById(R.id.tvExerciseInfo);
        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivPlay = findViewById(R.id.ivPlayBtn);

        tvTitle.setText(name != null ? name : "Jumping Jack");
        tvInfo.setText(reps != null ? reps + " | 390 Calories Burn" : "Easy | 390 Calories Burn");

        ivBack.setOnClickListener(v -> finish());
        ivPlay.setOnClickListener(v -> Toast.makeText(this, "Playing exercise video...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Toast.makeText(this, "Exercise started!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

