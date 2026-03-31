package com.fitai.gym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.TimePickerDialog;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WorkoutScheduleActivity extends AppCompatActivity {

    private LinearLayout llWeekDays;
    private TextView tvMonth;
    private java.util.Calendar calendar = java.util.Calendar.getInstance();
    private String selectedDateStr; // YYYY-MM-DD
    private java.util.List<ScheduledWorkout> dailyWorkouts = new java.util.ArrayList<>();
    private RecyclerView rvSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_schedule);

        llWeekDays = findViewById(R.id.llWeekDays);
        tvMonth = findViewById(R.id.tvMonth);
        rvSchedule = findViewById(R.id.rvSchedule);
        selectedDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        setupCalendarStrip();
        fetchWorkouts();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.fabAddSchedule).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddScheduleActivity.class);
            intent.putExtra("selected_date", selectedDateStr);
            startActivity(intent);
        });
        findViewById(R.id.ivPrevMonth).setOnClickListener(v -> {
            calendar.add(java.util.Calendar.MONTH, -1);
            setupCalendarStrip();
        });
        findViewById(R.id.ivNextMonth).setOnClickListener(v -> {
            calendar.add(java.util.Calendar.MONTH, 1);
            setupCalendarStrip();
        });
    }

    private void setupCalendarStrip() {
        llWeekDays.removeAllViews();
        tvMonth.setText(new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.getTime()));

        java.util.Calendar tempCal = (java.util.Calendar) calendar.clone();
        tempCal.set(java.util.Calendar.DAY_OF_WEEK, tempCal.getFirstDayOfWeek());

        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault());
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("d", java.util.Locale.getDefault());
        java.text.SimpleDateFormat fullDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            final String dateKey = fullDateFormat.format(tempCal.getTime());
            View view = LayoutInflater.from(this).inflate(R.layout.item_calendar_day, llWeekDays, false);
            TextView tvDay = view.findViewById(R.id.tvDayName);
            TextView tvDate = view.findViewById(R.id.tvDayDate);
            LinearLayout llBg = view.findViewById(R.id.llDayBg);
            
            tvDay.setText(dayFormat.format(tempCal.getTime()));
            tvDate.setText(dateFormat.format(tempCal.getTime()));

            if (dateKey.equals(selectedDateStr)) {
                llBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF9DCEFF));
                tvDay.setTextColor(0xFFFFFFFF);
                tvDate.setTextColor(0xFFFFFFFF);
            } else {
                llBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF7F8F8));
                tvDay.setTextColor(0xFFADA4A5);
                tvDate.setTextColor(0xFFADA4A5);
            }

            view.setOnClickListener(v -> {
                selectedDateStr = dateKey;
                setupCalendarStrip();
                fetchWorkouts();
            });

            llWeekDays.addView(view);
            tempCal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void fetchWorkouts() {
        String uid = FirebaseHelper.getInstance().getAuth().getUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getUsersCollection().document(uid)
            .collection("workout_schedule")
            .whereEqualTo("date", selectedDateStr)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                dailyWorkouts.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    ScheduledWorkout sw = doc.toObject(ScheduledWorkout.class);
                    if (sw != null) dailyWorkouts.add(sw);
                }
                setupTimeline();
            });
    }

    private void setupTimeline() {
        String[] timeSlots = {
            "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM",
            "11:00 AM", "12:00 AM", "01:00 PM", "02:00 PM", "03:00 PM",
            "04:00 PM", "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM"
        };

        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(new RecyclerView.Adapter<TimelineViewHolder>() {
            @Override
            public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_schedule, parent, false);
                return new TimelineViewHolder(v);
            }

            @Override
            public void onBindViewHolder(TimelineViewHolder holder, int position) {
                String slotTime = timeSlots[position];
                holder.tvTime.setText(slotTime);
                holder.cardWorkout.setVisibility(View.GONE);
                
                for (ScheduledWorkout sw : dailyWorkouts) {
                    if (sw.getTime().equals(slotTime)) {
                        holder.cardWorkout.setVisibility(View.VISIBLE);
                        holder.tvWorkoutName.setText(sw.getName() + ", " + sw.getTime().toLowerCase());
                        try {
                            holder.tvWorkoutName.setBackgroundColor(android.graphics.Color.parseColor(sw.getColor()));
                            // Set contrast text color
                            holder.tvWorkoutName.setTextColor(0xFFFFFFFF);
                        } catch (Exception ex) {
                            holder.tvWorkoutName.setBackgroundColor(0xFFC58BF2);
                        }
                        break;
                    }
                }
            }
            @Override public int getItemCount() { return timeSlots.length; }
        });
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvWorkoutName;
        androidx.cardview.widget.CardView cardWorkout;
        TimelineViewHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvTime);
            tvWorkoutName = v.findViewById(R.id.tvWorkoutName);
            cardWorkout = v.findViewById(R.id.cardWorkout);
        }
    }
}
