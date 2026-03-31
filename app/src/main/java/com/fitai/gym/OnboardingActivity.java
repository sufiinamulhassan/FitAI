package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private View btnNext;
    private int currentItem = 0;

    private int[] images = {R.drawable.on_1, R.drawable.on_2, R.drawable.on_3, R.drawable.on_4};
    private String[] titles = {
        "Track Your Goal",
        "Get Burn",
        "Eat Well",
        "Improve Sleep Quality"
    };
    private String[] descs = {
        "Don't worry if you have trouble determining your goals, We can help you determine your goals and track your goals",
        "Let’s keep burning, to achieve yours goals, it hurts only temporarily, if you give up now you will be in pain forever",
        "Let's start a healthy lifestyle with us, we can determine your diet every day. healthy eating is fun",
        "Improve the quality of your sleep with us, good quality sleep can bring a good mood in the morning"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.pbOnboardingRing);
        btnNext = findViewById(R.id.flNextBtnInner);

        OnboardingAdapter adapter = new OnboardingAdapter(this, images, titles, descs);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentItem = position;
                // Progress from 25% to 100%
                int progress = (position + 1) * 25;
                if (progressBar != null) {
                    progressBar.setProgress(progress);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentItem < 3) {
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                // Completed onboarding
                getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit()
                    .putBoolean("onboarding_done", true)
                    .apply();
                startActivity(new Intent(this, SignupActivity.class));
                finish();
            }
        });
    }
}
