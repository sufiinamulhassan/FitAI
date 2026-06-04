/*
 * AchievementActivity displays the badges, progress landmarks, and user achievements unlocked in the app.
 */
package com.fitai.gym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    private GridLayout glAchievements;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        fbHelper = FirebaseHelper.getInstance();
        glAchievements = findViewById(R.id.glAchievements);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        loadAchievements();
    }

    private void loadAchievements() {
        String uid = fbHelper.getCurrentUserUid();
        if (uid == null) return;

        
        Task<QuerySnapshot> tWorkouts = fbHelper.getUsersCollection().document(uid).collection("workout_progress").get();
        Task<QuerySnapshot> tMeals = fbHelper.getUsersCollection().document(uid).collection("meal_logs").get();
        Task<QuerySnapshot> tWater = fbHelper.getUsersCollection().document(uid).collection("water_logs").get();
        Task<QuerySnapshot> tSleep = fbHelper.getUsersCollection().document(uid).collection("sleep_logs").get();

        Tasks.whenAllSuccess(tWorkouts, tMeals, tWater, tSleep).addOnSuccessListener(results -> {
            int workoutCount = ((QuerySnapshot) results.get(0)).size();
            int mealCount = ((QuerySnapshot) results.get(1)).size();
            int waterCount = ((QuerySnapshot) results.get(2)).size();
            int sleepCount = ((QuerySnapshot) results.get(3)).size();

            renderAchievements(workoutCount, mealCount, waterCount, sleepCount);
        });
    }

    private void renderAchievements(int workouts, int meals, int water, int sleep) {
        glAchievements.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        class Badge {
            String title;
            String desc;
            int iconRes;
            boolean unlocked;
            Badge(String title, String desc, int iconRes, boolean unlocked) {
                this.title = title;
                this.desc = desc;
                this.iconRes = iconRes;
                this.unlocked = unlocked;
            }
        }

        List<Badge> badges = new ArrayList<>();
        
        
        badges.add(new Badge("First Step", "Complete your first workout", R.drawable.barbell, workouts >= 1));
        badges.add(new Badge("Getting Warmer", "Complete 3 workouts", R.drawable.burn, workouts >= 3));
        badges.add(new Badge("Warrior", "Complete 5 workouts", R.drawable.burn, workouts >= 5));
        badges.add(new Badge("Iron Will", "Complete 10 workouts", R.drawable.time_workout, workouts >= 10));
        badges.add(new Badge("Champion", "Complete 20 workouts", R.drawable.time_workout, workouts >= 20));
        badges.add(new Badge("Unstoppable", "Complete 50 workouts", R.drawable.burn, workouts >= 50));
        badges.add(new Badge("Legend", "Complete 100 workouts", R.drawable.time_workout, workouts >= 100));

        
        badges.add(new Badge("Healthy Eater", "Log your first meal", R.drawable.apple_pie, meals >= 1));
        badges.add(new Badge("Chef", "Log 5 meals", R.drawable.pancake_1, meals >= 5));
        badges.add(new Badge("Nutritionist", "Log 10 meals", R.drawable.salad, meals >= 10));
        badges.add(new Badge("Dietitian", "Log 25 meals", R.drawable.chicken, meals >= 25));
        badges.add(new Badge("Master Chef", "Log 50 meals", R.drawable.nigiri, meals >= 50));

        
        badges.add(new Badge("Hydrated", "Log water for the first time", R.drawable.glass_of_milk, water >= 1));
        badges.add(new Badge("Splash", "Log 5 water intakes", R.drawable.glass_of_milk, water >= 5));
        badges.add(new Badge("Aqua", "Log 10 water intakes", R.drawable.glass_of_milk, water >= 10));
        badges.add(new Badge("Ocean", "Log 25 water intakes", R.drawable.glass_of_milk, water >= 25));
        badges.add(new Badge("Poseidon", "Log 50 water intakes", R.drawable.glass_of_milk, water >= 50));

        
        badges.add(new Badge("Restful", "Log your first sleep", R.drawable.bed, sleep >= 1));
        badges.add(new Badge("Dreamer", "Log 10 sleep sessions", R.drawable.bed, sleep >= 10));
        badges.add(new Badge("Deep Sleeper", "Log 30 sleep sessions", R.drawable.bed, sleep >= 30));

        for (Badge b : badges) {
            View view = inflater.inflate(R.layout.item_achievement_badge, glAchievements, false);
            
            ImageView ivIcon = view.findViewById(R.id.ivBadgeIcon);
            TextView tvTitle = view.findViewById(R.id.tvBadgeTitle);
            TextView tvDesc = view.findViewById(R.id.tvBadgeDesc);
            CardView cvBadge = view.findViewById(R.id.cvBadge);

            tvTitle.setText(b.title);
            tvDesc.setText(b.desc);
            ivIcon.setImageResource(b.iconRes);

            if (b.unlocked) {
                ivIcon.setAlpha(1.0f);
                tvTitle.setTextColor(android.graphics.Color.parseColor("#1D1617")); 
                cvBadge.setCardBackgroundColor(android.graphics.Color.WHITE);
                cvBadge.setCardElevation(4f);
            } else {
                ivIcon.setAlpha(0.2f);
                tvTitle.setTextColor(android.graphics.Color.LTGRAY);
                cvBadge.setCardBackgroundColor(android.graphics.Color.parseColor("#F7F8F8"));
                cvBadge.setCardElevation(0f);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);
            view.setLayoutParams(params);

            glAchievements.addView(view);
        }
    }
}
