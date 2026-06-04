package com.fitai.gym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword;
    private ImageView ivShowPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private android.view.View btnGoogleSignup;
    private boolean passVisible = false;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignUpLauncher;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignUpLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() == null) return;
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null && account.getIdToken() != null) {
                        signUpWithGoogleToken(account.getIdToken(), account);
                    } else {
                        Toast.makeText(this, "Google Sign-Up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (ApiException e) {
                    Toast.makeText(this, "Google Sign-Up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        );

        ivShowPassword.setOnClickListener(v -> {
            passVisible = !passVisible;
            etPassword.setTransformationMethod(passVisible ? SingleLineTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> { startActivity(new Intent(this, LoginActivity.class)); finish(); });
        btnGoogleSignup.setOnClickListener(v -> startGoogleSignUp());
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
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void startGoogleSignUp() {
        googleSignInClient.signOut().addOnCompleteListener(task ->
            googleSignUpLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void signUpWithGoogleToken(String idToken, GoogleSignInAccount account) {
        FirebaseHelper helper = FirebaseHelper.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        helper.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                    Toast.makeText(this,
                        "Google authentication failed: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
                    return;
                }

                String uid = task.getResult().getUser().getUid();
                String email = task.getResult().getUser().getEmail() != null
                    ? task.getResult().getUser().getEmail()
                    : (account.getEmail() != null ? account.getEmail() : "");
                String displayName = task.getResult().getUser().getDisplayName() != null
                    ? task.getResult().getUser().getDisplayName()
                    : (account.getDisplayName() != null ? account.getDisplayName() : "FitAI User");

                helper.getUsersCollection().document(uid).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        if (role == null || role.trim().isEmpty()) role = "user";
                        saveLoginState(email, role);

                        Toast.makeText(this, "Google account already exists. Logged in.", Toast.LENGTH_SHORT).show();
                        if ("admin".equalsIgnoreCase(role)) {
                            startActivity(new Intent(this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                        finish();
                        return;
                    }

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("name", displayName);
                    user.put("email", email);
                    user.put("role", "user");

                    helper.getUsersCollection().document(uid).set(user)
                        .addOnSuccessListener(v -> {
                            saveLoginState(email, "user");
                            Toast.makeText(this, "Google signup successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ProfileSetupActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to check account: " + e.getMessage(), Toast.LENGTH_LONG).show());
            });
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
