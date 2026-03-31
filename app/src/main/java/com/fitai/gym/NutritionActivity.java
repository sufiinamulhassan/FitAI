package com.fitai.gym;

import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NutritionActivity extends AppCompatActivity {

    private TextView tvHeaderTitle;
    private RecyclerView rvPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rvPopular = findViewById(R.id.rvPopular);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        String mealType = getIntent().getStringExtra("meal_type");
        if (mealType != null) {
            tvHeaderTitle.setText(mealType);
        }

        setupPopularList(mealType);
        setupBottomNav();

        findViewById(R.id.btnViewPancake).setOnClickListener(v -> openMealDetails("Honey Pancake"));
        findViewById(R.id.btnViewBread).setOnClickListener(v -> openMealDetails("Canai Bread"));
    }

    private void openMealDetails(String name) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("meal_name", name);
        startActivity(intent);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_camera);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            return true;
        });
    }

    private void setupPopularList(String mealType) {
        List<FoodModel> popularFoods = new ArrayList<>();
        
        // Mock data based on design
        popularFoods.add(new FoodModel("Blueberry Pancake", "Medium | 30mins | 230kCal", R.drawable.pancake_1));
        popularFoods.add(new FoodModel("Salmon Nigiri", "Medium | 20mins | 120kCal", R.drawable.nigiri));
        popularFoods.add(new FoodModel("Chicken Salad", "Easy | 15mins | 180kCal", R.drawable.chicken));

        rvPopular.setLayoutManager(new LinearLayoutManager(this));
        rvPopular.setAdapter(new FoodAdapter(this, popularFoods));
    }
}
