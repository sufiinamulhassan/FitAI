/*
 * AdminViewUserProgressActivity displays detailed workout execution logs and statistics for a selected member account.
 */
package com.fitai.gym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminViewUserProgressActivity extends AppCompatActivity {

    private String userUid;
    private String userName;
    private String userEmail;

    private TextView tvUserName, tvUserEmail, tvStatWorkouts, tvStatMeals;
    private de.hdodenhof.circleimageview.CircleImageView ivUserProfile;
    private LinearLayout llLatestActivities;

    private FirebaseHelper fbHelper;
    private final List<ListenerRegistration> listeners = new ArrayList<>();
    private final List<ActivityLogItem> items = new ArrayList<>();

    private enum ActivityType {
        WORKOUT, WATER, SLEEP, MEAL
    }

    private static class ActivityLogItem {
        String title;
        String subtitle;
        int iconRes;
        Date timestamp;
        ActivityType type;

        ActivityLogItem(String title, String subtitle, int iconRes, Date timestamp, ActivityType type) {
            this.title = title;
            this.subtitle = subtitle;
            this.iconRes = iconRes;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_user_progress);

        fbHelper = FirebaseHelper.getInstance();

        userUid = getIntent().getStringExtra("user_uid");
        userName = getIntent().getStringExtra("user_name");
        userEmail = getIntent().getStringExtra("user_email");

        if (userUid == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvStatWorkouts = findViewById(R.id.tvStatWorkouts);
        tvStatMeals = findViewById(R.id.tvStatMeals);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        llLatestActivities = findViewById(R.id.llLatestActivities);

        tvUserName.setText(userName != null ? userName : "User");
        tvUserEmail.setText(userEmail != null ? userEmail : "");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        
        fbHelper.getUsersCollection().document(userUid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String profilePicUrl = snapshot.getString("profilePicUrl");
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(profilePicUrl);
                    if (bitmap != null) {
                        ivUserProfile.setImageBitmap(bitmap);
                    }
                }
            }
        });

        loadUserProgressHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ListenerRegistration listener : listeners) {
            if (listener != null) listener.remove();
        }
    }

    private void loadUserProgressHistory() {
        items.clear();

        
        ListenerRegistration workoutListener = fbHelper.getUsersCollection().document(userUid)
            .collection("workout_progress")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.WORKOUT);
                    int workoutCount = snapshot.size();
                    tvStatWorkouts.setText(String.valueOf(workoutCount));

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("workoutName");
                        Long ts = doc.getLong("timestamp");
                        Date d = ts != null ? new Date(ts) : new Date();

                        items.add(new ActivityLogItem(
                                "Finished: " + (name != null ? name : "Workout"),
                                "Completed successfully",
                                R.drawable.barbell,
                                d,
                                ActivityType.WORKOUT
                        ));
                    }
                    refreshLatestActivitiesUI();
                }
            });
        listeners.add(workoutListener);

        
        ListenerRegistration waterListener = fbHelper.getUsersCollection().document(userUid)
            .collection("water_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.WATER);
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long amount = doc.getLong("amount");
                        Long ts = doc.getLong("timestamp");
                        Date d = ts != null ? new Date(ts) : new Date();

                        int ml = amount != null ? amount.intValue() : 0;
                        items.add(new ActivityLogItem(
                                "Logged: " + ml + "ml Water",
                                "Hydration goal progress",
                                R.drawable.glass_of_milk,
                                d,
                                ActivityType.WATER
                        ));
                    }
                    refreshLatestActivitiesUI();
                }
            });
        listeners.add(waterListener);

        
        ListenerRegistration sleepListener = fbHelper.getUsersCollection().document(userUid)
            .collection("sleep_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.SLEEP);
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String dateStr = doc.getString("date");
                        Long hours = doc.getLong("hours");
                        Long mins = doc.getLong("minutes");

                        int h = hours != null ? hours.intValue() : 0;
                        int m = mins != null ? mins.intValue() : 0;

                        Date d = new Date();
                        try {
                            d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                        } catch (Exception ex) {  }

                        items.add(new ActivityLogItem(
                                "Logged Sleep: " + h + "h " + m + "m",
                                "Logged for date: " + (dateStr != null ? dateStr : "today"),
                                R.drawable.bed,
                                d,
                                ActivityType.SLEEP
                        ));
                    }
                    refreshLatestActivitiesUI();
                }
            });
        listeners.add(sleepListener);

        
        ListenerRegistration mealListener = fbHelper.getUsersCollection().document(userUid)
            .collection("meal_logs")
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                synchronized (items) {
                    items.removeIf(item -> item.type == ActivityType.MEAL);
                    int mealCount = snapshot.size();
                    tvStatMeals.setText(String.valueOf(mealCount));

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String mealName = doc.getString("mealName");
                        String calories = doc.getString("calories");
                        Long ts = doc.getLong("timestamp");

                        Date d = ts != null ? new Date(ts) : new Date();
                        String calStr = calories != null ? calories + " kCal" : "Healthy Meal";

                        items.add(new ActivityLogItem(
                                "Ate: " + (mealName != null ? mealName : "Meal"),
                                "Intake: " + calStr,
                                R.drawable.apple_pie,
                                d,
                                ActivityType.MEAL
                        ));
                    }
                    refreshLatestActivitiesUI();
                }
            });
        listeners.add(mealListener);
    }

    private void refreshLatestActivitiesUI() {
        runOnUiThread(() -> {
            llLatestActivities.removeAllViews();

            
            Collections.sort(items, (o1, o2) -> o2.timestamp.compareTo(o1.timestamp));

            int limit = Math.min(items.size(), 10);
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < limit; i++) {
                ActivityLogItem item = items.get(i);
                View view = inflater.inflate(R.layout.item_latest_activity, llLatestActivities, false);

                ImageView ivIcon = view.findViewById(R.id.ivActivityIcon);
                TextView tvTitle = view.findViewById(R.id.tvActivityTitle);
                TextView tvSubtitle = view.findViewById(R.id.tvActivityTime);

                ivIcon.setImageResource(item.iconRes);
                tvTitle.setText(item.title);

                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                tvSubtitle.setText(item.subtitle + " • " + sdf.format(item.timestamp));

                llLatestActivities.addView(view);
            }
        });
    }
}
