package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

public class GoalSelectionActivity extends AppCompatActivity {

    private ViewPager2 viewPagerGoals;
    private Button btnConfirm;

    private int[] goalImages = {R.drawable.goal_1, R.drawable.goal_2, R.drawable.goal_3};
    private String[] goalTitles = {"Improve Shape", "Lean & Tone", "Lose a Fat"};
    private String[] goalDescs = {
            "I have a low amount of body fat and need / want to build more muscle",
            "I'm \"skinny fat\". look thin but have no shape. I want to add learn muscle in the right way",
            "I have over 20 lbs to lose. I want to drop all this fat and gain muscle mass"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_selection);

        viewPagerGoals = findViewById(R.id.viewPagerGoals);
        btnConfirm = findViewById(R.id.btnConfirm);

        boolean isEdit = getIntent().getBooleanExtra("is_edit", false);
        if (isEdit) {
            btnConfirm.setText("Update Goal");
        }

        GoalAdapter adapter = new GoalAdapter(goalImages, goalTitles, goalDescs);
        viewPagerGoals.setAdapter(adapter);

        // Add some fancy transform like the design (scaling side cards)
        viewPagerGoals.setOffscreenPageLimit(3);
        viewPagerGoals.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPagerGoals.setPageTransformer(transformer);

        btnConfirm.setOnClickListener(v -> {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Saving Goal...");
            
            int currentPos = viewPagerGoals.getCurrentItem();
            String selectedGoal = goalTitles[currentPos];

            // Use set with merge to ensure document exists
            FirebaseHelper fbHelper = FirebaseHelper.getInstance();
            if (fbHelper.getAuth().getCurrentUser() != null) {
                String uid = fbHelper.getAuth().getUid();
                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                updates.put("goal", selectedGoal + " Program");

                fbHelper.getUsersCollection().document(uid)
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (isEdit) {
                            android.widget.Toast.makeText(GoalSelectionActivity.this, "Goal Updated!", android.widget.Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            startActivity(new Intent(this, WelcomeSuccessActivity.class));
                            finish();
                        }
                    });
            } else {
                if (isEdit) {
                    finish();
                } else {
                    startActivity(new Intent(this, WelcomeSuccessActivity.class));
                    finish();
                }
            }
        });
    }
}
