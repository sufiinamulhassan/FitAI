/*
 * ProfileActivity displays the user profile settings, height, weight, activity goals, and logouts.
 */
package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseHelper fbHelper;
    private TextView tvName, tvGoal, tvHeight, tvWeight, tvAge;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fbHelper = FirebaseHelper.getInstance();
        initViews();
        loadUserData();

        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        
        findViewById(R.id.llPersonalInfo).setOnClickListener(v -> 
            startActivity(new Intent(this, PersonalDataActivity.class)));

        findViewById(R.id.llEditGoal).setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalSelectionActivity.class);
            intent.putExtra("is_edit", true);
            startActivity(intent);
        });

        findViewById(R.id.llAchievement).setOnClickListener(v -> startActivity(new Intent(this, AchievementActivity.class)));
        findViewById(R.id.llActivityHistory).setOnClickListener(v -> startActivity(new Intent(this, ActivityTrackerActivity.class)));
        findViewById(R.id.llWorkoutProgress).setOnClickListener(v -> startActivity(new Intent(this, ProgressActivity.class)));
        
        findViewById(R.id.llContactUs).setOnClickListener(v -> showContactDialog());
        findViewById(R.id.llPrivacyPolicy).setOnClickListener(v -> showPrivacyDialog());
        
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseHelper.getInstance().getAuth().signOut();
            getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in", false).apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());

        findViewById(R.id.llPremium).setOnClickListener(v -> startActivity(new Intent(this, PaymentActivity.class)));
        findViewById(R.id.llAdminPanel).setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));

        
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileSetupActivity.class);
            intent.putExtra("is_edit", true);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        
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
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvGoal = findViewById(R.id.tvProfileGoal);
        tvHeight = findViewById(R.id.tvProfileHeight);
        tvWeight = findViewById(R.id.tvProfileWeight);
        tvAge = findViewById(R.id.tvProfileAge);
        ivProfile = findViewById(R.id.ivProfilePic);
    }

    private void loadUserData() {
        if (fbHelper.getAuth().getCurrentUser() == null) return;
        
        String uid = fbHelper.getAuth().getUid();
        
        
        fbHelper.getUsersCollection().document(uid)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null || !snapshot.exists()) return;

                String name = snapshot.getString("name");
                String goal = snapshot.getString("goal");
                String height = snapshot.getString("height");
                String weight = snapshot.getString("weight");
                String dob = snapshot.getString("dob");
                String age = snapshot.getString("age");
                String role = snapshot.getString("role");
                
                
                if (age == null && dob != null) {
                    try {
                        String[] parts = dob.split("/");
                        int bDay = Integer.parseInt(parts[0]);
                        int bMonth = Integer.parseInt(parts[1]);
                        int bYear = Integer.parseInt(parts[2]);
                        java.util.Calendar now = java.util.Calendar.getInstance();
                        int yearNow = now.get(java.util.Calendar.YEAR);
                        age = String.valueOf(yearNow - bYear);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }

                String profilePicUrl = snapshot.getString("profilePicUrl");

                if (name != null) tvName.setText(name);
                if (goal != null) tvGoal.setText(goal);
                if (height != null) tvHeight.setText(height);
                if (weight != null) tvWeight.setText(weight);
                if (age != null) tvAge.setText(age);

                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    if (profilePicUrl.startsWith("http")) {
                        Glide.with(this).load(profilePicUrl).into(ivProfile);
                    } else {
                        
                        android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(profilePicUrl);
                        if (bitmap != null) ivProfile.setImageBitmap(bitmap);
                    }
                }

                
                View adminPanel = findViewById(R.id.llAdminPanel);
                if (adminPanel != null) {
                    adminPanel.setVisibility("admin".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
                }

                
                Boolean isPremium = snapshot.getBoolean("isPremium");
                String premiumPlan = snapshot.getString("premiumPlan");
                View cvPremiumBanner = findViewById(R.id.cvPremiumBanner);
                TextView tvPremiumPlan = findViewById(R.id.tvPremiumPlan);
                if (cvPremiumBanner != null) {
                    if (isPremium != null && isPremium) {
                        cvPremiumBanner.setVisibility(View.VISIBLE);
                        if (tvPremiumPlan != null && premiumPlan != null) {
                            String planLabel = premiumPlan.equals("yearly") ? "12-Month Plan" : "1-Month Plan";
                            tvPremiumPlan.setText(planLabel + " • Active");
                        }
                        
                        LinearLayout llPremium = findViewById(R.id.llPremium);
                        if (llPremium != null) {
                            ((TextView) llPremium.getChildAt(1)).setText("Manage Subscription");
                        }
                    } else {
                        cvPremiumBanner.setVisibility(View.GONE);
                    }
                }
            });
    }

    private void showContactDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Contact Us")
            .setMessage("Email: support@FitAI.com\nPhone: +1 (800) FIT-AIML\nHours: Mon-Fri 9am-6pm")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("FitAI respects your privacy. We collect only necessary fitness data to personalize your experience. Your data is never sold to third parties.")
            .setPositiveButton("I Understand", null)
            .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? All your data will be erased. This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void deleteUserAccount() {
        if (fbHelper.getAuth().getCurrentUser() == null) return;
        
        String uid = fbHelper.getAuth().getUid();
        
        
        fbHelper.getUsersCollection().document(uid).delete()
            .addOnSuccessListener(aVoid -> {
                
                fbHelper.getAuth().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in", false).apply();
                            android.widget.Toast.makeText(this, "Account Deleted", android.widget.Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            android.widget.Toast.makeText(this, "Failed to delete from Auth: " + task.getException().getMessage(), android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to delete data: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            });
    }
}
