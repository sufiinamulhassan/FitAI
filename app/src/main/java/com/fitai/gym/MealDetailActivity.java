package com.fitai.gym;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MealDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_details);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        
        // Dynamic Title if passed
        String title = getIntent().getStringExtra("meal_name");
        if (title != null) {
            ((TextView)findViewById(R.id.tvMealTitle)).setText(title);
        }

        findViewById(R.id.btnAddMeal).setOnClickListener(v -> {
            // Logic to add to planner
            finish();
        });
    }
}
