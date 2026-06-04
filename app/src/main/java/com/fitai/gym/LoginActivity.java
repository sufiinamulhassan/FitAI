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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView ivShowPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private View btnGoogleLogin;
    private boolean passwordVisible = false;
    private FirebaseHelper fbHelper;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() == null) return;
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null && account.getIdToken() != null) {
                        signInWithGoogleToken(account.getIdToken(), account);
                    } else {
                        showAuthError("Google Sign-In failed. Please try again.");
                    }
                } catch (ApiException e) {
                    showAuthError("Google Sign-In failed: " + e.getMessage());
                }
            }
        );

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
            showForgotPasswordDialog();
        });

        btnGoogleLogin.setOnClickListener(v -> startGoogleSignIn());
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
            fbHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        // Ensure role is admin
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("name", "Admin");
                        adminData.put("email", email);
                        adminData.put("role", "admin");
                        adminData.put("uid", uid);

                        fbHelper.getUsersCollection().document(uid).set(adminData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnCompleteListener(t -> {
                                saveLoginState(email, "admin");
                                Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                                finish();
                            });
                    } else {
                        // If admin account doesn't exist yet, auto-create it
                        fbHelper.getAuth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, regTask -> {
                                if (regTask.isSuccessful()) {
                                    String uid = regTask.getResult().getUser().getUid();
                                    Map<String, Object> adminData = new HashMap<>();
                                    adminData.put("name", "Admin");
                                    adminData.put("email", email);
                                    adminData.put("role", "admin");
                                    adminData.put("uid", uid);

                                    fbHelper.getUsersCollection().document(uid).set(adminData)
                                        .addOnCompleteListener(t -> {
                                            saveLoginState(email, "admin");
                                            Toast.makeText(this, "Admin Account Registered & Logged In!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, AdminDashboardActivity.class));
                                            finish();
                                        });
                                } else {
                                    Toast.makeText(this, "Admin Authentication failed: " + regTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                });
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
                    }).addOnFailureListener(e -> {
                        new AlertDialog.Builder(this)
                            .setTitle("Database Error")
                            .setMessage("Failed to fetch user data: " + e.getMessage() + "\nMake sure your Firestore rules are set to allow read/write.")
                            .setPositiveButton("OK", null)
                            .show();
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
        if ("Google".equals(provider)) {
            startGoogleSignIn();
            return;
        }
        Toast.makeText(this, provider + " login coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void startGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(task ->
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void showForgotPasswordDialog() {
        final EditText etResetEmail = new EditText(this);
        etResetEmail.setHint("Enter your email");
        etResetEmail.setInputType(android.text.InputType.TYPE_CLASS_TEXT
            | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etResetEmail.setText(etEmail.getText().toString().trim());
        etResetEmail.setSelection(etResetEmail.getText().length());

        new AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We will send a password reset link to your email.")
            .setView(etResetEmail)
            .setPositiveButton("Send", (dialog, which) -> {
                String resetEmail = etResetEmail.getText().toString().trim();

                if (resetEmail.isEmpty()) {
                    Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                fbHelper.getAuth().fetchSignInMethodsForEmail(resetEmail)
                    .addOnSuccessListener(result -> {
                        List<String> methods = result.getSignInMethods();

                        if (methods == null || methods.isEmpty()) {
                            new AlertDialog.Builder(this)
                                .setTitle("Account Not Found")
                                .setMessage("No account exists with this email. Please sign up first.")
                                .setPositiveButton("OK", null)
                                .show();
                            return;
                        }

                        boolean hasPasswordProvider = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
                        if (!hasPasswordProvider) {
                            new AlertDialog.Builder(this)
                                .setTitle("Google Account")
                                .setMessage("This account uses Google Sign-In. Please login with Google, or reset password from your Google account settings.")
                                .setPositiveButton("OK", null)
                                .show();
                            return;
                        }

                        fbHelper.getAuth().sendPasswordResetEmail(resetEmail)
                            .addOnSuccessListener(v ->
                                new AlertDialog.Builder(this)
                                    .setTitle("Email Sent")
                                    .setMessage("Password reset link has been sent to \n" + resetEmail)
                                    .setPositiveButton("OK", null)
                                    .show())
                            .addOnFailureListener(e ->
                                new AlertDialog.Builder(this)
                                    .setTitle("Reset Failed")
                                    .setMessage(e.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show());
                    })
                    .addOnFailureListener(e ->
                        new AlertDialog.Builder(this)
                            .setTitle("Reset Failed")
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void signInWithGoogleToken(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fbHelper.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                    showAuthError(task.getException() != null
                        ? task.getException().getMessage()
                        : "Google authentication failed");
                    return;
                }

                String uid = task.getResult().getUser().getUid();
                String email = task.getResult().getUser().getEmail() != null
                    ? task.getResult().getUser().getEmail()
                    : (account.getEmail() != null ? account.getEmail() : "");
                String displayName = task.getResult().getUser().getDisplayName() != null
                    ? task.getResult().getUser().getDisplayName()
                    : (account.getDisplayName() != null ? account.getDisplayName() : "FitAI User");

                fbHelper.getUsersCollection().document(uid).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        if (role == null || role.trim().isEmpty()) role = "user";
                        saveLoginState(email, role);

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

                    fbHelper.getUsersCollection().document(uid).set(user)
                        .addOnSuccessListener(v -> {
                            saveLoginState(email, "user");
                            startActivity(new Intent(this, ProfileSetupActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> showAuthError("Failed to save Google profile: " + e.getMessage()));
                }).addOnFailureListener(e -> showAuthError("Failed to fetch user profile: " + e.getMessage()));
            });
    }

    private void showAuthError(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Login Failed")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
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
