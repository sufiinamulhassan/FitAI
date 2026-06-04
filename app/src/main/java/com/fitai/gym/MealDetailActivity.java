package com.fitai.gym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_details);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Get extras from dynamic intent
        String name = getIntent().getStringExtra("meal_name");
        String calories = getIntent().getStringExtra("meal_calories");
        String instructions = getIntent().getStringExtra("meal_instructions");
        String ingredients = getIntent().getStringExtra("meal_ingredients");
        String imageName = getIntent().getStringExtra("meal_image_name");

        TextView tvTitle = findViewById(R.id.tvMealTitle);
        TextView tvCalories = findViewById(R.id.tvMealCaloriesVal);
        TextView tvDesc = findViewById(R.id.tvMealDescVal);
        TextView tvIngredients = findViewById(R.id.tvMealIngredientsVal);
        ImageView ivFood = findViewById(R.id.ivFoodMain);
        TextView btnAddMeal = findViewById(R.id.btnAddMeal);

        if (name != null) tvTitle.setText(name);
        if (calories != null) {
            tvCalories.setText(calories.contains("Cal") ? calories : calories + " kCal");
        }
        if (instructions != null) tvDesc.setText(instructions);
        if (ingredients != null) tvIngredients.setText(ingredients);

        // Resolve Image
        ImageLoaderHelper.loadImage(this, ivFood, imageName, R.drawable.pancake_1);

        if (name != null) {
            btnAddMeal.setText("Add to " + name);
        }

        // Populate dynamic step-by-step timeline
        populateSteps(instructions);

        btnAddMeal.setOnClickListener(v -> {
            String uid = FirebaseHelper.getInstance().getCurrentUserUid();
            if (uid == null) {
                Toast.makeText(this, "Please log in to track meals!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract numeric calorie value
            int calVal = 180;
            if (calories != null) {
                String clean = calories;
                if (clean.contains("|")) {
                    String[] parts = clean.split("\\|");
                    clean = parts[parts.length - 1].trim();
                }
                clean = clean.replaceAll("[^0-9]", "");
                try {
                    if (!clean.isEmpty()) {
                        calVal = Integer.parseInt(clean);
                    }
                } catch (Exception e) {}
            }

            // Generate realistic macronutrient values based on meal name & category
            int proteins = 15;
            int carbs = 20;
            int fats = 5;
            int fiber = 3;
            int sugar = 4;

            String mealLower = (name != null) ? name.toLowerCase() : "";
            if (mealLower.contains("chicken") || mealLower.contains("steak") || mealLower.contains("salmon") || mealLower.contains("nigiri")) {
                proteins = Math.max(5, (int) (calVal * 0.35 / 4)); // 35% protein
                fats = Math.max(2, (int) (calVal * 0.30 / 9));     // 30% fat
                carbs = Math.max(5, (int) (calVal * 0.35 / 4));    // 35% carbs
                fiber = 1;
                sugar = 1;
            } else if (mealLower.contains("pancake") || mealLower.contains("pie") || mealLower.contains("bread") || mealLower.contains("oatmeal") || mealLower.contains("oats")) {
                carbs = Math.max(10, (int) (calVal * 0.65 / 4));    // 65% carbs
                proteins = Math.max(3, (int) (calVal * 0.15 / 4)); // 15% protein
                fats = Math.max(2, (int) (calVal * 0.20 / 9));     // 20% fat
                fiber = Math.max(1, (int) (carbs * 0.15));
                sugar = Math.max(1, (int) (carbs * 0.25));
            } else if (mealLower.contains("salad")) {
                carbs = Math.max(5, (int) (calVal * 0.40 / 4));
                proteins = Math.max(2, (int) (calVal * 0.15 / 4));
                fats = Math.max(2, (int) (calVal * 0.45 / 9));
                fiber = 5;
                sugar = 2;
            } else {
                proteins = Math.max(3, (int) (calVal * 0.20 / 4));
                carbs = Math.max(10, (int) (calVal * 0.50 / 4));
                fats = Math.max(2, (int) (calVal * 0.30 / 9));
                fiber = Math.max(1, (int) (carbs * 0.10));
                sugar = Math.max(1, (int) (carbs * 0.15));
            }

            // Detect category based on calories instruction or pass from intent if available
            String category = getIntent().getStringExtra("meal_type");
            if (category == null) {
                // fallback detection
                if (mealLower.contains("pancake") || mealLower.contains("coffee") || mealLower.contains("bread") || mealLower.contains("oatmeal")) {
                    category = "Breakfast";
                } else if (mealLower.contains("chicken") || mealLower.contains("steak") || mealLower.contains("nigiri")) {
                    category = "Lunch";
                } else if (mealLower.contains("salad") || mealLower.contains("soup")) {
                    category = "Dinner";
                } else {
                    category = "Snacks";
                }
            }

            // Save to Firestore under user progress
            final int finalCalVal = calVal;
            Map<String, Object> mealLog = new HashMap<>();
            mealLog.put("mealName", name != null ? name : "Healthy Meal");
            mealLog.put("calories", String.valueOf(finalCalVal));
            mealLog.put("category", category);
            mealLog.put("proteins", proteins);
            mealLog.put("carbs", carbs);
            mealLog.put("fats", fats);
            mealLog.put("fiber", fiber);
            mealLog.put("sugar", sugar);
            mealLog.put("timestamp", System.currentTimeMillis());

            FirebaseHelper.getInstance().getUsersCollection().document(uid)
                .collection("meal_logs")
                .add(mealLog)
                .addOnSuccessListener(ref -> {
                    // Log dynamic notification
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("title", "Logged Meal: " + (name != null ? name : "Healthy Meal") + " - " + finalCalVal + " kCal");
                    notif.put("timestamp", System.currentTimeMillis());
                    notif.put("type", "meal");
                    FirebaseHelper.getInstance().getUsersCollection().document(uid).collection("notifications").add(notif);

                    Toast.makeText(this, "Meal added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to log meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }

    private void populateSteps(String instructions) {
        LinearLayout container = findViewById(R.id.llDetailsStepsContainer);
        TextView tvStepCount = findViewById(R.id.tvDetailsStepCount);
        if (container == null || instructions == null || instructions.trim().isEmpty()) return;

        // Parse steps: split on "Step N:" pattern
        List<String> steps = new ArrayList<>();
        if (instructions.contains("Step 1:")) {
            String[] parts = instructions.split("Step \\d+:");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    steps.add(trimmed);
                }
            }
        } else {
            // Fallback: treat the whole string as a single step
            steps.add(instructions.trim());
        }

        // Update step count label
        tvStepCount.setText(steps.size() + (steps.size() == 1 ? " Step" : " Steps"));

        // Inflate one timeline row per step
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_recipe_step_timeline, container, false);

            TextView tvNum    = row.findViewById(R.id.tvStepNumText);
            View     dot      = row.findViewById(R.id.vTimelineDot);
            View     line     = row.findViewById(R.id.vTimelineLine);
            TextView tvTitle  = row.findViewById(R.id.tvStepTitle);
            TextView tvDesc   = row.findViewById(R.id.tvStepDesc);

            // Format step number as "01", "02" ...
            tvNum.setText(String.format("%02d", i + 1));
            tvTitle.setText("Step " + (i + 1));
            tvDesc.setText(steps.get(i));

            // First step gets the active (purple) styling
            if (i == 0) {
                tvNum.setTextColor(android.graphics.Color.parseColor("#C58BF2"));
                dot.setBackgroundResource(R.drawable.bg_timeline_dot_active);
                if (line != null) {
                    line.setBackgroundColor(android.graphics.Color.parseColor("#C58BF2"));
                    line.setAlpha(0.3f);
                }
            }

            // Hide the connecting line on the last step
            if (i == steps.size() - 1 && line != null) {
                line.setVisibility(View.INVISIBLE);
            }

            container.addView(row);
        }
    }
}
