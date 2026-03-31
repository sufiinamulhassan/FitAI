package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword;
    private ImageView ivShowPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private android.view.View btnGoogleSignup, btnFacebookSignup;
    private boolean passVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        btnGoogleSignup = findViewById(R.id.btnGoogleSignup);
        btnFacebookSignup = findViewById(R.id.btnFacebookSignup);

        ivShowPassword.setOnClickListener(v -> {
            passVisible = !passVisible;
            etPassword.setTransformationMethod(passVisible ? SingleLineTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> { startActivity(new Intent(this, LoginActivity.class)); finish(); });
        btnGoogleSignup.setOnClickListener(v -> Toast.makeText(this, "Google signup coming soon!", Toast.LENGTH_SHORT).show());
        btnFacebookSignup.setOnClickListener(v -> Toast.makeText(this, "Facebook signup coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void attemptRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (firstName.isEmpty()) { etFirstName.setError("First name is required"); etFirstName.requestFocus(); return; }
        if (lastName.isEmpty()) { etLastName.setError("Last name is required"); etLastName.requestFocus(); return; }
        if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        if (password.isEmpty()) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }
        if (!cbTerms.isChecked()) { Toast.makeText(this, "Please accept Terms and Privacy Policy", Toast.LENGTH_SHORT).show(); return; }

        // Firebase Register
        FirebaseHelper helper = FirebaseHelper.getInstance();
        helper.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    String fullName = firstName + " " + lastName;
                    UserModel user = new UserModel(uid, fullName, email, "user"); // Default role is user
                    
                    helper.getUsersCollection().document(uid).set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ProfileSetupActivity.class));
                            finish();
                        });
                } else {
                    Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
