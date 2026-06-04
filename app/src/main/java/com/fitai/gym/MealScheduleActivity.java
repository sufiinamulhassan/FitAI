/*
 * MealScheduleActivity handles setting up and listing scheduled meal reminders.
 */
package com.fitai.gym;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealScheduleActivity extends AppCompatActivity {

    private TextView tvMonthYear;
    private LinearLayout llDateSliderContainer;
    private LinearLayout llBreakfastContainer, llLunchContainer, llSnacksContainer, llDinnerContainer;
    private TextView tvBreakfastSubheader, tvLunchSubheader, tvSnacksSubheader, tvDinnerSubheader;
    private TextView tvProgressCalVal, tvProgressProtVal, tvProgressFatsVal;
    private ProgressBar pbProgressCal, pbProgressProt, pbProgressFats;

    private Calendar selectedCalendar;
    private int selectedDay;
    private List<Map<String, Object>> mealLogs = new ArrayList<>();
    private ListenerRegistration logsListener;
    private SharedPreferences reminderPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_schedule);

        reminderPrefs = getSharedPreferences("meal_reminders", MODE_PRIVATE);

        
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        tvMonthYear = findViewById(R.id.tvMonthYear);
        llDateSliderContainer = findViewById(R.id.llDateSliderContainer);

        llBreakfastContainer = findViewById(R.id.llBreakfastContainer);
        llLunchContainer = findViewById(R.id.llLunchContainer);
        llSnacksContainer = findViewById(R.id.llSnacksContainer);
        llDinnerContainer = findViewById(R.id.llDinnerContainer);

        tvBreakfastSubheader = findViewById(R.id.tvBreakfastSubheader);
        tvLunchSubheader = findViewById(R.id.tvLunchSubheader);
        tvSnacksSubheader = findViewById(R.id.tvSnacksSubheader);
        tvDinnerSubheader = findViewById(R.id.tvDinnerSubheader);

        tvProgressCalVal = findViewById(R.id.tvProgressCalVal);
        tvProgressProtVal = findViewById(R.id.tvProgressProtVal);
        tvProgressFatsVal = findViewById(R.id.tvProgressFatsVal);

        pbProgressCal = findViewById(R.id.pbProgressCal);
        pbProgressProt = findViewById(R.id.pbProgressProt);
        pbProgressFats = findViewById(R.id.pbProgressFats);

        
        selectedCalendar = Calendar.getInstance();
        selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, -1);
            selectedDay = 1;
            updateCalendarAndSlider();
            updateDataForSelectedDate();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedCalendar.add(Calendar.MONTH, 1);
            selectedDay = 1;
            updateCalendarAndSlider();
            updateDataForSelectedDate();
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            
            startActivity(new Intent(this, MealPlannerActivity.class));
        });

        updateCalendarAndSlider();
        startListeningToLogs();
    }

    private void startListeningToLogs() {
        String uid = FirebaseHelper.getInstance().getCurrentUserUid();
        if (uid == null) {
            Toast.makeText(this, "Please log in to load meal schedule", Toast.LENGTH_SHORT).show();
            return;
        }

        logsListener = FirebaseHelper.getInstance().getUsersCollection().document(uid)
            .collection("meal_logs")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    return;
                }

                mealLogs.clear();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            
                            data.put("_id", doc.getId());
                            mealLogs.add(data);
                        }
                    }
                    updateDataForSelectedDate();
                }
            });
    }

    private void updateCalendarAndSlider() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(selectedCalendar.getTime()));

        llDateSliderContainer.removeAllViews();

        Calendar sliderCal = (Calendar) selectedCalendar.clone();
        int maxDays = sliderCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= maxDays; day++) {
            sliderCal.set(Calendar.DAY_OF_MONTH, day);
            final int dayNum = day;
            String dayName = new SimpleDateFormat("EEE", Locale.getDefault()).format(sliderCal.getTime());

            LinearLayout chip = new LinearLayout(this);
            chip.setOrientation(LinearLayout.VERTICAL);
            chip.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dpToPx(60), dpToPx(80)
            );
            lp.setMargins(0, 0, dpToPx(12), 0);
            chip.setLayoutParams(lp);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

            TextView tvNum = new TextView(this);
            tvNum.setText(String.valueOf(day));
            tvNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvNum.setTypeface(null, Typeface.BOLD);
            
            LinearLayout.LayoutParams numLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            numLp.topMargin = dpToPx(5);
            tvNum.setLayoutParams(numLp);

            if (day == selectedDay) {
                chip.setBackgroundResource(R.drawable.bg_date_selected);
                tvDay.setTextColor(Color.WHITE);
                tvNum.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_light);
                tvDay.setTextColor(Color.parseColor("#ADA4A5"));
                tvNum.setTextColor(Color.parseColor("#ADA4A5"));
            }

            chip.addView(tvDay);
            chip.addView(tvNum);

            chip.setOnClickListener(v -> {
                selectedDay = dayNum;
                updateCalendarAndSlider();
                updateDataForSelectedDate();
            });

            llDateSliderContainer.addView(chip);
        }
    }

    private void updateDataForSelectedDate() {
        llBreakfastContainer.removeAllViews();
        llLunchContainer.removeAllViews();
        llSnacksContainer.removeAllViews();
        llDinnerContainer.removeAllViews();

        int breakfastCount = 0, breakfastCal = 0;
        int lunchCount = 0, lunchCal = 0;
        int snacksCount = 0, snacksCal = 0;
        int dinnerCount = 0, dinnerCal = 0;

        int totalCal = 0, totalProt = 0, totalFats = 0;

        Calendar checkCal = Calendar.getInstance();

        for (Map<String, Object> log : mealLogs) {
            Long ts = (Long) log.get("timestamp");
            if (ts == null) continue;

            checkCal.setTimeInMillis(ts);
            if (checkCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                checkCal.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                checkCal.get(Calendar.DAY_OF_MONTH) == selectedDay) {

                String cat = (String) log.get("category");
                String name = (String) log.get("mealName");
                int cal = getIntVal(log.get("calories"), 0);
                int prot = getIntVal(log.get("proteins"), 0);
                int fat = getIntVal(log.get("fats"), 0);

                totalCal += cal;
                totalProt += prot;
                totalFats += fat;

                SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String timeStr = timeSdf.format(new Date(ts));

                if ("Breakfast".equalsIgnoreCase(cat)) {
                    breakfastCount++;
                    breakfastCal += cal;
                    addMealCard(llBreakfastContainer, name, timeStr, cat, log);
                } else if ("Lunch".equalsIgnoreCase(cat)) {
                    lunchCount++;
                    lunchCal += cal;
                    addMealCard(llLunchContainer, name, timeStr, cat, log);
                } else if ("Snacks".equalsIgnoreCase(cat)) {
                    snacksCount++;
                    snacksCal += cal;
                    addMealCard(llSnacksContainer, name, timeStr, cat, log);
                } else if ("Dinner".equalsIgnoreCase(cat)) {
                    dinnerCount++;
                    dinnerCal += cal;
                    addMealCard(llDinnerContainer, name, timeStr, cat, log);
                }
            }
        }

        
        tvBreakfastSubheader.setText(breakfastCount + " meals | " + breakfastCal + " calories");
        tvLunchSubheader.setText(lunchCount + " meals | " + lunchCal + " calories");
        tvSnacksSubheader.setText(snacksCount + " meals | " + snacksCal + " calories");
        tvDinnerSubheader.setText(dinnerCount + " meals | " + dinnerCal + " calories");

        
        showEmptyIfEmpty(llBreakfastContainer, "Breakfast");
        showEmptyIfEmpty(llLunchContainer, "Lunch");
        showEmptyIfEmpty(llSnacksContainer, "Snacks");
        showEmptyIfEmpty(llDinnerContainer, "Dinner");

        
        tvProgressCalVal.setText(totalCal + " kCal");
        tvProgressProtVal.setText(totalProt + "g");
        tvProgressFatsVal.setText(totalFats + "g");

        
        pbProgressCal.setProgress(Math.min(100, (int) ((totalCal / 2000.0) * 100)));
        pbProgressProt.setProgress(Math.min(100, (int) ((totalProt / 150.0) * 100)));
        pbProgressFats.setProgress(Math.min(100, (int) ((totalFats / 70.0) * 100)));
    }

    private void addMealCard(LinearLayout container, String name, String timeStr, String category, Map<String, Object> log) {
        String logId = (String) log.get("_id");
        if (logId == null) logId = "";

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowLp.bottomMargin = dpToPx(15);
        row.setLayoutParams(rowLp);

        
        ImageView ivMeal = new ImageView(this);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
        ivMeal.setLayoutParams(ivLp);
        ivMeal.setImageResource(getMealIcon(name, category));
        
        int bgRes = "Lunch".equalsIgnoreCase(category) || "Dinner".equalsIgnoreCase(category) ?
                R.drawable.bg_chip_blue : R.drawable.bg_chip_purple;
        ivMeal.setBackgroundResource(bgRes);
        ivMeal.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        ivMeal.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.setMarginStart(dpToPx(15));
        info.setLayoutParams(infoLp);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.BLACK);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextColor(Color.parseColor("#ADA4A5"));
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvTime.setTypeface(null, Typeface.NORMAL);

        info.addView(tvName);
        info.addView(tvTime);

        
        ImageView ivBell = new ImageView(this);
        LinearLayout.LayoutParams bellLp = new LinearLayout.LayoutParams(dpToPx(26), dpToPx(26));
        ivBell.setLayoutParams(bellLp);
        ivBell.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        boolean hasReminder = reminderPrefs.getBoolean(logId, false);
        ivBell.setImageResource(hasReminder ? R.drawable.bell : R.drawable.no_bell);
        ivBell.setColorFilter(hasReminder ? Color.parseColor("#C58BF2") : Color.parseColor("#ADA4A5"));
        ivBell.setBackgroundResource(R.drawable.bg_circle_hollow_nav);

        
        final String finalLogId = logId;
        View.OnClickListener clickListener = v -> {
            if (reminderPrefs.getBoolean(finalLogId, false)) {
                
                new AlertDialog.Builder(this)
                        .setTitle("Cancel Reminder")
                        .setMessage("Do you want to cancel the notification reminder for " + name + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            cancelAlarm(finalLogId);
                            updateDataForSelectedDate();
                            Toast.makeText(this, "Reminder cancelled", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                
                Calendar now = Calendar.getInstance();
                new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                    scheduleAlarm(finalLogId, name, category, hourOfDay, minute);
                    updateDataForSelectedDate();
                    Toast.makeText(this, "Reminder scheduled successfully!", Toast.LENGTH_SHORT).show();
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
            }
        };

        row.setOnClickListener(clickListener);
        ivBell.setOnClickListener(clickListener);

        row.addView(ivMeal);
        row.addView(info);
        row.addView(ivBell);

        container.addView(row);
    }

    private void showEmptyIfEmpty(LinearLayout container, String cat) {
        if (container.getChildCount() == 0) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = dpToPx(15);
            tv.setLayoutParams(lp);
            tv.setText("No scheduled meals for " + cat);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setTextColor(Color.parseColor("#ADA4A5"));
            tv.setTypeface(null, Typeface.ITALIC);
            container.addView(tv);
        }
    }

    private void scheduleAlarm(String id, String mealName, String category, int hour, int minute) {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR));
        target.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH));
        target.set(Calendar.DAY_OF_MONTH, selectedDay);
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        
        if (target.getTimeInMillis() < System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MealReminderReceiver.class);
        intent.putExtra("meal_name", mealName);
        intent.putExtra("meal_category", category);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException se) {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        target.getTimeInMillis(),
                        pendingIntent
                );
            }
        }

        reminderPrefs.edit().putBoolean(id, true).apply();
    }

    private void cancelAlarm(String id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MealReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        reminderPrefs.edit().putBoolean(id, false).apply();
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

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logsListener != null) {
            logsListener.remove();
        }
    }
}
