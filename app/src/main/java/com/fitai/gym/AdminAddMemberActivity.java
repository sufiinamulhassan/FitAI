/*
 * AdminAddMemberActivity enables administrators to register new member accounts and setup initial roles.
 */
package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminAddMemberActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnCreate;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_member);

        fbHelper = FirebaseHelper.getInstance();
        etName = findViewById(R.id.etAdminName);
        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);
        btnCreate = findViewById(R.id.btnCreateAdmin);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> showCreationWarning());
    }

    private void showCreationWarning() {
        new AlertDialog.Builder(this)
            .setTitle("Add New Admin")
            .setMessage("To securely create a new administrator, you will be temporarily logged out. You can log back in immediately after. Proceed?")
            .setPositiveButton("Proceed", (dialog, which) -> attemptCreateAdmin())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void attemptCreateAdmin() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid Email");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return;
        }

        
        fbHelper.getAuth().signOut();
        getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in", false).apply();

        fbHelper.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    UserModel newAdmin = new UserModel(uid, name, email, "admin");
                    
                    fbHelper.getUsersCollection().document(uid).set(newAdmin)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Admin Account Created! Please log back in.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
