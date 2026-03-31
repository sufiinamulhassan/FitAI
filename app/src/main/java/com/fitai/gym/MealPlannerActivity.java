package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MealPlannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupCategoryClicks();
        setupBottomNav();

        findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> {
            startActivity(new Intent(this, MealScheduleActivity.class));
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_meals);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_activity) {
                Intent intent = new Intent(this, WorkoutTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_meals) {
                return true;
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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_meals);
        }
    }

    private void setupCategoryClicks() {
        findViewById(R.id.btnSelectBreakfast).setOnClickListener(v -> openCategory("Breakfast"));
        findViewById(R.id.btnSelectLunch).setOnClickListener(v -> openCategory("Lunch"));
        findViewById(R.id.btnSelectSnacks).setOnClickListener(v -> openCategory("Snacks"));
        findViewById(R.id.btnSelectDinner).setOnClickListener(v -> openCategory("Dinner"));
    }

    private void openCategory(String type) {
        Intent intent = new Intent(this, NutritionActivity.class);
        intent.putExtra("meal_type", type);
        startActivity(intent);
    }
}
