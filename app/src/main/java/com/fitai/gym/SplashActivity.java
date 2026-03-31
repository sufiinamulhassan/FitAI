package com.fitai.gym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Check session and onboarding status
        SharedPreferences prefs = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean("onboarding_done", false);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false) && FirebaseHelper.getInstance().getAuth().getCurrentUser() != null;
        String userRole = prefs.getString("user_role", "user");

        // 1. Auto-Redirect if ALREADY logged in (SKIP Splash/Button entirely)
        if (isLoggedIn) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if ("admin".equalsIgnoreCase(userRole)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                    finish();
                } else if (onboardingDone) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                // If not onboardingDone, it will proceed to wait for the button below
            }, 1000); 
            if ("admin".equalsIgnoreCase(userRole)) return; 
        }

        // 2. Otherwise, Wait for "Get Started" button
        btnGetStarted.setOnClickListener(v -> {
            Intent intent;
            if (!onboardingDone) {
                intent = new Intent(this, OnboardingActivity.class);
            } else {
                // If onboarding is done but not logged in, go to Login
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}
