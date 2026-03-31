package com.fitai.gym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivShowPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private View btnGoogleLogin, btnFacebookLogin;
    private boolean passwordVisible = false;
    private FirebaseHelper fbHelper;

    // Dummy credentials for demo
    private static final String DEMO_EMAIL = "demo@FitnesX.com";
    private static final String DEMO_PASSWORD = "FitnesX123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fbHelper = FirebaseHelper.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);

        ivShowPassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            etPassword.setTransformationMethod(
                passwordVisible ? SingleLineTransformationMethod.getInstance()
                                : PasswordTransformationMethod.getInstance()
            );
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            // Keep in backstack or finish? Usually keep so they can go back.
        });

        tvForgotPassword.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("A password reset link will be sent to your email address.")
                .setPositiveButton("Send", (d, w) ->
                    Toast.makeText(this, "Reset link sent!", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
        });

        btnGoogleLogin.setOnClickListener(v -> loginWithSocial("Google"));
        btnFacebookLogin.setOnClickListener(v -> loginWithSocial("Facebook"));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Admin check
        if (email.equals("admin.fitai@gmail.com") && password.equals("admin@1122")) {
            saveLoginState(email, "admin");
            // FirebaseHelper.getInstance().getUsersCollection().document(...) 
            // We should ensure the role is set to "admin" in Firestore too
            String adminUid = "SUFI_ADMIN_UID"; // Placeholder or real UID
            
            Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        }

        // Firebase auth logic
        fbHelper.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    
                    // Check if role is admin
                    fbHelper.getUsersCollection().document(uid).get().addOnSuccessListener(snapshot -> {
                        String role = snapshot.getString("role");
                        if (role == null) role = "user";
                        
                        saveLoginState(email, role);
                        
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        if ("admin".equalsIgnoreCase(role)) {
                            startActivity(new Intent(this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                        finish();
                    });
                } else {
                    new AlertDialog.Builder(this)
                        .setTitle("Login Failed")
                        .setMessage(task.getException().getMessage())
                        .setPositiveButton("OK", null)
                        .show();
                }
            });
    }

    private void loginWithSocial(String provider) {
        if (provider.equals("Google")) {
            Toast.makeText(this, "Google Sign-In initialized...", Toast.LENGTH_SHORT).show();
        }
        // Fallback for demo
        saveLoginState(provider.toLowerCase() + "@FitAI.com", "user");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void saveLoginState(String email, String role) {
        SharedPreferences prefs = getSharedPreferences("FitAI_Prefs", MODE_PRIVATE);
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_role", role)
            .apply();
    }
}
