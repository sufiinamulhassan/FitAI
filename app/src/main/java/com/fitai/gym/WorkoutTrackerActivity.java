package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class WorkoutTrackerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView rvWorkoutPlans;
    private List<WorkoutPlan> plans = new ArrayList<>();
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        fbHelper = FirebaseHelper.getInstance();
        bottomNav = findViewById(R.id.bottomNav);
        setupNavigation();

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // Link the 'Daily Workout Schedule' button
        findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WorkoutScheduleActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Workout Schedule Feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup workout plan cards from Firebase
        setupWorkoutCardLinks();
        loadWorkoutPlans();
    }

    private void loadWorkoutPlans() {
        fbHelper.getWorkoutPlansCollection().get()
            .addOnSuccessListener(snapshot -> {
                plans.clear();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    WorkoutPlan plan = doc.toObject(WorkoutPlan.class);
                    if (plan != null) {
                        plan.setId(doc.getId());
                        plans.add(plan);
                    }
                }
                // Populate the "What Do You Want to Train" section dynamically
                populateTrainCards();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading plans", Toast.LENGTH_SHORT).show();
            });
    }

    private void populateTrainCards() {
        // Use existing cards if plans exist, map them to Firebase plans
        // Card 1 → first plan, Card 2 → second plan, Card 3 → third plan
        View cardFullBody = findViewById(R.id.cardFullBody);
        View cardLowerBody = findViewById(R.id.cardLowerBody);
        View cardAB = findViewById(R.id.cardAB);

        if (plans.size() > 0) {
            cardFullBody.setOnClickListener(v -> openPlanDetail(plans.get(0)));
        }
        if (plans.size() > 1) {
            cardLowerBody.setOnClickListener(v -> openPlanDetail(plans.get(1)));
        }
        if (plans.size() > 2) {
            cardAB.setOnClickListener(v -> openPlanDetail(plans.get(2)));
        }
    }

    private void openPlanDetail(WorkoutPlan plan) {
        Intent intent = new Intent(this, WorkoutPlanDetailActivity.class);
        intent.putExtra("PLAN_ID", plan.getId());
        startActivity(intent);
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_activity);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_activity) {
                return true;
            } else if (id == R.id.nav_meals) {
                Intent intent = new Intent(this, MealPlannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
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

    private void setupWorkoutCardLinks() {
        // Fallback to static WorkoutDetail if no Firebase plans
        java.util.function.Consumer<String> openDetails = title -> {
            if (!plans.isEmpty()) {
                // Try to find matching plan
                for (WorkoutPlan p : plans) {
                    if (p.getTitle().toLowerCase().contains(title.toLowerCase().split(" ")[0])) {
                        openPlanDetail(p);
                        return;
                    }
                }
            }
            // Fallback to old detail page
            Intent intent = new Intent(this, WorkoutDetailActivity.class);
            intent.putExtra("WORKOUT_TITLE", title);
            startActivity(intent);
        };

        findViewById(R.id.cardUpcoming1).setOnClickListener(v -> openDetails.accept("Fullbody Workout"));
        findViewById(R.id.cardUpcoming2).setOnClickListener(v -> openDetails.accept("Upperbody Workout"));
        findViewById(R.id.cardFullBody).setOnClickListener(v -> openDetails.accept("Fullbody Workout"));
        findViewById(R.id.cardLowerBody).setOnClickListener(v -> openDetails.accept("Lowerbody Workout"));
        findViewById(R.id.cardAB).setOnClickListener(v -> openDetails.accept("AB Workout"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_activity);
        loadWorkoutPlans(); // Refresh on resume
    }
}
