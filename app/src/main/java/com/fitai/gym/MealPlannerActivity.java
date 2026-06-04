/*
 * MealPlannerActivity handles meal recommendation planning and calorie count summaries.
 */
package com.fitai.gym;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealPlannerActivity extends AppCompatActivity {

    private LinearLayout llTodayMealsContainer;
    private List<Map<String, Object>> mealLogs = new ArrayList<>();
    private ListenerRegistration logsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        llTodayMealsContainer = findViewById(R.id.llTodayMealsContainer);

        
        TextView tvGraphFilter = findViewById(R.id.tvGraphFilter);
        if (tvGraphFilter != null) {
            tvGraphFilter.setOnClickListener(v -> showGraphFilterMenu());
        }

        TextView tvTodayMealsFilter = findViewById(R.id.tvTodayMealsFilter);
        if (tvTodayMealsFilter != null) {
            tvTodayMealsFilter.setOnClickListener(v -> showTodayMealsFilterMenu());
        }

        setupCategoryClicks();
        setupBottomNav();

        findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> {
            startActivity(new Intent(this, MealScheduleActivity.class));
        });

        
        loadFoodCounts();

        
        startListeningToLogs();
    }

    private void loadFoodCounts() {
        TextView tvBreakfast = findViewById(R.id.tvBreakfastFoodCount);
        TextView tvLunch = findViewById(R.id.tvLunchFoodCount);
        TextView tvSnacks = findViewById(R.id.tvSnacksFoodCount);
        TextView tvDinner = findViewById(R.id.tvDinnerFoodCount);

        FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
            .get()
            .addOnSuccessListener(snapshot -> {
                int breakfastCount = 0;
                int lunchCount = 0;
                int snacksCount = 0;
                int dinnerCount = 0;

                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String type = doc.getString("mealType");
                        if (type != null) {
                            if ("Breakfast".equalsIgnoreCase(type)) breakfastCount++;
                            else if ("Lunch".equalsIgnoreCase(type)) lunchCount++;
                            else if ("Snacks".equalsIgnoreCase(type)) snacksCount++;
                            else if ("Dinner".equalsIgnoreCase(type)) dinnerCount++;
                        }
                    }
                }

                
                if (tvBreakfast != null) tvBreakfast.setText((breakfastCount > 0 ? breakfastCount : 10) + " Foods");
                if (tvLunch != null) tvLunch.setText((lunchCount > 0 ? lunchCount : 10) + " Foods");
                if (tvSnacks != null) tvSnacks.setText((snacksCount > 0 ? snacksCount : 10) + " Foods");
                if (tvDinner != null) tvDinner.setText((dinnerCount > 0 ? dinnerCount : 10) + " Foods");
            });
    }

    private void startListeningToLogs() {
        String uid = FirebaseHelper.getInstance().getCurrentUserUid();
        if (uid == null) {
            Toast.makeText(this, "Please log in to load meal logs", Toast.LENGTH_SHORT).show();
            return;
        }

        logsListener = FirebaseHelper.getInstance().getUsersCollection().document(uid)
            .collection("meal_logs")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Toast.makeText(this, "Failed to listen to meal logs", Toast.LENGTH_SHORT).show();
                    return;
                }

                mealLogs.clear();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            mealLogs.add(data);
                        }
                    }
                    updateAllUI();
                } else {
                    
                    seedDefaultMealLogs(uid);
                }
            });
    }

    private void updateAllUI() {
        
        TextView tvGraphFilter = findViewById(R.id.tvGraphFilter);
        String graphFilter = (tvGraphFilter != null) ? tvGraphFilter.getText().toString() : "Weekly";
        updateGraphData(graphFilter);

        
        TextView tvTodayMealsFilter = findViewById(R.id.tvTodayMealsFilter);
        String mealsFilter = (tvTodayMealsFilter != null) ? tvTodayMealsFilter.getText().toString() : "Breakfast";
        updateTodayMealsList(mealsFilter);
    }

    private void updateGraphData(String filterType) {
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000L;
        
        float[] points;
        String[] labels;
        
        int totalCal = 0;
        int totalFibre = 0;
        int totalSugar = 0;

        int daysCount = 1;

        if ("Daily".equalsIgnoreCase(filterType)) {
            
            points = new float[4];
            labels = new String[]{"Breakfast", "Lunch", "Snacks", "Dinner"};
            
            int[] calSum = new int[4];
            
            
            long todayStart = getStartOfDayTimestamp();
            for (Map<String, Object> log : mealLogs) {
                Long ts = (Long) log.get("timestamp");
                if (ts != null && ts >= todayStart) {
                    String cat = (String) log.get("category");
                    int idx = getCategoryIndex(cat);
                    
                    int cal = getIntVal(log.get("calories"), 0);
                    int fib = getIntVal(log.get("fiber"), 0);
                    int sug = getIntVal(log.get("sugar"), 0);
                    
                    totalCal += cal;
                    totalFibre += fib;
                    totalSugar += sug;
                    
                    if (idx != -1) {
                        calSum[idx] += cal;
                    }
                }
            }
            
            
            for (int i = 0; i < 4; i++) {
                points[i] = Math.min(1.0f, calSum[i] / 800.0f);
                if (points[i] < 0.1f) points[i] = 0.1f;
            }
            daysCount = 1;

        } else if ("Monthly".equalsIgnoreCase(filterType)) {
            
            points = new float[4];
            labels = new String[]{"Week 1", "Week 2", "Week 3", "Week 4"};
            
            int[] calSum = new int[4];
            long thirtyDaysAgo = now - 30 * oneDay;
            
            for (Map<String, Object> log : mealLogs) {
                Long ts = (Long) log.get("timestamp");
                if (ts != null && ts >= thirtyDaysAgo) {
                    long diff = now - ts;
                    int weekIdx = 3 - (int) (diff / (7 * oneDay));
                    if (weekIdx >= 0 && weekIdx < 4) {
                        int cal = getIntVal(log.get("calories"), 0);
                        int fib = getIntVal(log.get("fiber"), 0);
                        int sug = getIntVal(log.get("sugar"), 0);
                        
                        totalCal += cal;
                        totalFibre += fib;
                        totalSugar += sug;
                        
                        calSum[weekIdx] += cal;
                    }
                }
            }
            
            
            for (int i = 0; i < 4; i++) {
                points[i] = Math.min(1.0f, calSum[i] / 14000.0f);
                if (points[i] < 0.1f) points[i] = 0.1f;
            }
            daysCount = 30;

        } else {
            
            
            points = new float[7];
            labels = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            
            int[] calSum = new int[7];
            long sevenDaysAgo = now - 7 * oneDay;
            
            Calendar calObj = Calendar.getInstance();
            
            for (Map<String, Object> log : mealLogs) {
                Long ts = (Long) log.get("timestamp");
                if (ts != null && ts >= sevenDaysAgo) {
                    calObj.setTimeInMillis(ts);
                    int dayOfWeek = calObj.get(Calendar.DAY_OF_WEEK);
                    int idx = dayOfWeek - 1;
                    
                    int cal = getIntVal(log.get("calories"), 0);
                    int fib = getIntVal(log.get("fiber"), 0);
                    int sug = getIntVal(log.get("sugar"), 0);
                    
                    totalCal += cal;
                    totalFibre += fib;
                    totalSugar += sug;
                    
                    if (idx >= 0 && idx < 7) {
                        calSum[idx] += cal;
                    }
                }
            }
            
            
            for (int i = 0; i < 7; i++) {
                points[i] = Math.min(1.0f, calSum[i] / 2500.0f);
                if (points[i] < 0.1f) points[i] = 0.1f;
            }
            daysCount = 7;
        }

        
        MealNutritionGraphView graphView = findViewById(R.id.nutritionGraphView);
        if (graphView != null) {
            graphView.setDataPoints(points);
        }

        
        updateLabelsUI(labels);

        
        int dailyCalGoal = 2000 * daysCount;
        int dailyFiberGoal = 30 * daysCount;
        int dailySugarGoal = 50 * daysCount;

        int calPct = Math.min(100, (int) (((float) totalCal / dailyCalGoal) * 100));
        int fibPct = Math.min(100, (int) (((float) totalFibre / dailyFiberGoal) * 100));
        int sugPct = Math.min(100, (int) (((float) totalSugar / dailySugarGoal) * 100));

        
        if (calPct < 15) calPct = 15;
        if (fibPct < 15) fibPct = 15;
        if (sugPct < 15) sugPct = 15;

        TextView tvCal = findViewById(R.id.tvGraphCalValue);
        TextView tvFib = findViewById(R.id.tvGraphFibreValue);
        TextView tvSug = findViewById(R.id.tvGraphSugarValue);

        if (tvCal != null) tvCal.setText("Calories " + calPct + "% " + (calPct >= 80 ? "↑" : "↓"));
        if (tvFib != null) tvFib.setText("Fibre " + fibPct + "% " + (fibPct >= 80 ? "↑" : "↓"));
        if (tvSug != null) tvSug.setText("Sugar " + sugPct + "% " + (sugPct >= 50 ? "↑" : "↓"));
    }

    private void updateLabelsUI(String[] labels) {
        LinearLayout llLabels = findViewById(R.id.llDayLabelsContainer);
        if (llLabels == null) return;
        llLabels.removeAllViews();
        
        for (int i = 0; i < labels.length; i++) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            tv.setLayoutParams(lp);
            tv.setText(labels[i]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tv.setGravity(android.view.Gravity.CENTER);
            
            if (i == labels.length - 1) {
                tv.setTextColor(Color.parseColor("#C58BF2"));
                tv.setTypeface(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD));
            } else {
                tv.setTextColor(Color.parseColor("#ADA4A5"));
            }
            llLabels.addView(tv);
        }
    }

    private void updateTodayMealsList(String category) {
        if (llTodayMealsContainer == null) return;
        llTodayMealsContainer.removeAllViews();
        
        long todayStart = getStartOfDayTimestamp();
        int loggedCount = 0;
        
        for (Map<String, Object> log : mealLogs) {
            Long ts = (Long) log.get("timestamp");
            if (ts != null && ts >= todayStart) {
                String cat = (String) log.get("category");
                if (category.equalsIgnoreCase(cat)) {
                    String name = (String) log.get("mealName");
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    String timeStr = "Today | " + sdf.format(new Date(ts));
                    
                    int icon = getMealIcon(name, cat);
                    addTodayMealCard(name, timeStr, icon, true);
                    loggedCount++;
                }
            }
        }
        
        if (loggedCount == 0) {
            TextView tvEmpty = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, dpToPx(24), 0, dpToPx(24));
            tvEmpty.setLayoutParams(lp);
            tvEmpty.setText("No meals logged for " + category + " today.");
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setTextColor(Color.parseColor("#ADA4A5"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvEmpty.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.ITALIC));
            llTodayMealsContainer.addView(tvEmpty);
        }
    }

    private void addTodayMealCard(String name, String timeStr, int imageRes, boolean bellActive) {
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(1));
        
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        container.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        ImageView ivMeal = new ImageView(this);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(dpToPx(45), dpToPx(45));
        ivMeal.setLayoutParams(ivParams);
        ivMeal.setImageResource(imageRes);
        ivMeal.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.setMarginStart(dpToPx(12));
        infoLayout.setLayoutParams(infoParams);
        
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvName.setTextColor(Color.BLACK);
        tvName.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        
        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvTime.setTextColor(Color.parseColor("#ADA4A5"));
        
        infoLayout.addView(tvName);
        infoLayout.addView(tvTime);
        
        ImageView ivBell = new ImageView(this);
        LinearLayout.LayoutParams bellParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        ivBell.setLayoutParams(bellParams);
        ivBell.setImageResource(bellActive ? R.drawable.bell : R.drawable.no_bell);
        if (bellActive) {
            ivBell.setColorFilter(Color.parseColor("#C58BF2"));
        } else {
            ivBell.setColorFilter(Color.parseColor("#ADA4A5"));
        }
        
        container.addView(ivMeal);
        container.addView(infoLayout);
        container.addView(ivBell);
        card.addView(container);
        
        llTodayMealsContainer.addView(card);
    }

    private void showGraphFilterMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, findViewById(R.id.tvGraphFilter));
        popup.getMenu().add("Daily");
        popup.getMenu().add("Weekly");
        popup.getMenu().add("Monthly");
        
        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            TextView tvFilter = findViewById(R.id.tvGraphFilter);
            if (tvFilter != null) tvFilter.setText(selected);
            updateGraphData(selected);
            return true;
        });
        popup.show();
    }

    private void showTodayMealsFilterMenu() {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, findViewById(R.id.tvTodayMealsFilter));
        popup.getMenu().add("Breakfast");
        popup.getMenu().add("Lunch");
        popup.getMenu().add("Snacks");
        popup.getMenu().add("Dinner");
        
        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            TextView tvFilter = findViewById(R.id.tvTodayMealsFilter);
            if (tvFilter != null) tvFilter.setText(selected);
            updateTodayMealsList(selected);
            return true;
        });
        popup.show();
    }

    private int getCategoryIndex(String category) {
        if (category == null) return -1;
        switch (category) {
            case "Breakfast": return 0;
            case "Lunch": return 1;
            case "Snacks": return 2;
            case "Dinner": return 3;
            default: return -1;
        }
    }

    private int getIntVal(Object val, int fallback) {
        if (val == null) return fallback;
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    private long getStartOfDayTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private int getMealIcon(String name, String category) {
        if (name == null) name = "";
        name = name.toLowerCase();
        if (name.contains("pancake")) return R.drawable.pancake_1;
        if (name.contains("coffee")) return R.drawable.coffee;
        if (name.contains("nigiri")) return R.drawable.nigiri;
        if (name.contains("chicken") || name.contains("steak")) return R.drawable.chicken;
        if (name.contains("salad")) return R.drawable.salad;
        if (name.contains("pie")) return R.drawable.apple_pie;
        if (name.contains("orange")) return R.drawable.orange;
        if (name.contains("milk")) return R.drawable.glass_of_milk;
        if (name.contains("oatmeal")) return R.drawable.oatmeal;

        if ("Breakfast".equalsIgnoreCase(category)) return R.drawable.pancake_1;
        if ("Lunch".equalsIgnoreCase(category)) return R.drawable.chicken;
        if ("Snacks".equalsIgnoreCase(category)) return R.drawable.orange;
        return R.drawable.salad;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void seedDefaultMealLogs(String uid) {
        long now = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000L;
        
        List<Map<String, Object>> logs = new ArrayList<>();
        
        
        logs.add(createMealLogMap("Honey Pancake", "180", "Breakfast", 15, 30, 4, 3, 8, now - 11 * 60 * 60 * 1000L));
        logs.add(createMealLogMap("Lowfat Milk", "120", "Breakfast", 8, 12, 2, 0, 6, now - 10 * 60 * 60 * 1000L));
        logs.add(createMealLogMap("Chicken Steak", "420", "Lunch", 35, 25, 12, 2, 1, now - 5 * 60 * 60 * 1000L));
        logs.add(createMealLogMap("Juicy Orange", "80", "Snacks", 1, 18, 0, 3, 12, now - 2 * 60 * 60 * 1000L));

        
        logs.add(createMealLogMap("Canai Bread", "230", "Breakfast", 6, 40, 5, 2, 2, now - 1 * oneDay - 10 * 60 * 60 * 1000L));
        logs.add(createMealLogMap("Salmon Nigiri", "350", "Lunch", 28, 30, 8, 1, 1, now - 1 * oneDay - 5 * 60 * 60 * 1000L));
        logs.add(createMealLogMap("Summer Salad", "140", "Dinner", 4, 15, 6, 5, 3, now - 1 * oneDay - 2 * 60 * 60 * 1000L));

        
        logs.add(createMealLogMap("Organic Oatmeal", "180", "Breakfast", 6, 32, 3, 4, 5, now - 2 * oneDay));
        logs.add(createMealLogMap("Chicken Steak", "420", "Lunch", 35, 25, 12, 2, 1, now - 3 * oneDay));
        logs.add(createMealLogMap("Warm Apple Pie", "310", "Snacks", 3, 50, 8, 4, 25, now - 4 * oneDay));
        logs.add(createMealLogMap("Summer Salad", "140", "Dinner", 4, 15, 6, 5, 3, now - 5 * oneDay));
        logs.add(createMealLogMap("Honey Pancake", "180", "Breakfast", 15, 30, 4, 3, 8, now - 8 * oneDay));
        logs.add(createMealLogMap("Chicken Steak", "420", "Lunch", 35, 25, 12, 2, 1, now - 10 * oneDay));
        logs.add(createMealLogMap("Canai Bread", "230", "Breakfast", 6, 40, 5, 2, 2, now - 14 * oneDay));
        logs.add(createMealLogMap("Salmon Nigiri", "350", "Lunch", 28, 30, 8, 1, 1, now - 20 * oneDay));
        
        for (Map<String, Object> log : logs) {
            FirebaseHelper.getInstance().getUsersCollection().document(uid)
                .collection("meal_logs")
                .add(log);
        }
    }

    private Map<String, Object> createMealLogMap(String name, String cals, String cat, int prot, int carb, int fat, int fib, int sug, long ts) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealName", name);
        map.put("calories", cals);
        map.put("category", cat);
        map.put("proteins", prot);
        map.put("carbs", carb);
        map.put("fats", fat);
        map.put("fiber", fib);
        map.put("sugar", sug);
        map.put("timestamp", ts);
        return map;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logsListener != null) {
            logsListener.remove();
        }
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
