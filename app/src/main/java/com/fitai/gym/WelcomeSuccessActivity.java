/*
 * WelcomeSuccessActivity shows a congratulatory landing page after profile setup.
 */
package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeSuccessActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private Button btnGoToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_success);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnGoToHome = findViewById(R.id.btnGoToHome);

        
        String userName = getSharedPreferences("FitnesX_Prefs", MODE_PRIVATE)
                .getString("user_name", "Stefani"); 

        tvWelcomeUser.setText("Welcome, " + userName);

        btnGoToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
