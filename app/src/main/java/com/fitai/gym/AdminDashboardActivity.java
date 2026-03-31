package com.fitai.gym;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity
        implements AdminUserAdapter.OnUserActionListener {

    private TextView tvTotalUsers, tvRevenue;
    private RecyclerView rvUserList;
    private AdminUserAdapter userAdapter;
    private List<UserModel> userList;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        fbHelper = FirebaseHelper.getInstance();
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvRevenue = findViewById(R.id.tvRevenue);
        rvUserList = findViewById(R.id.rvUserList);

        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(userList);
        userAdapter.setOnUserActionListener(this);
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        rvUserList.setAdapter(userAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadStats();
        loadUsers();

        findViewById(R.id.cardManageWorkouts).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddWorkoutActivity.class));
        });

        findViewById(R.id.cardManageMeals).setOnClickListener(v -> {
            Toast.makeText(this, "Manage Meals Feature Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardAddAdmin).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddMemberActivity.class));
        });

        findViewById(R.id.cardLogoutAdmin).setOnClickListener(v -> {
            fbHelper.getAuth().signOut();
            getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in", false).apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.cardSystemSettings).setOnClickListener(v -> {
            Toast.makeText(this, "System Settings Feature Coming Soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadUsers();
    }

    private void loadStats() {
        fbHelper.getUsersCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    tvTotalUsers.setText(String.format("%,d", snapshot.size()));
                }
            } else {
                tvTotalUsers.setText("N/A");
            }
        });

        fbHelper.getPaymentsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                double total = snapshot != null ? snapshot.size() * 19.99 : 0.0;
                tvRevenue.setText(String.format("$%,.2f", total));
            } else {
                tvRevenue.setText("$0.00");
            }
        });
    }

    private void loadUsers() {
        fbHelper.getUsersCollection().get().addOnSuccessListener(snapshot -> {
            userList.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                UserModel user = doc.toObject(UserModel.class);
                if (user != null) {
                    if (user.getUid() == null || user.getUid().isEmpty()) {
                        // Set uid from document ID if not stored in model
                        user.setUid(doc.getId());
                    }
                    userList.add(user);
                }
            }
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // ── CRUD Callbacks ──────────────────────────────────────

    @Override
    public void onEditUser(UserModel user, int position) {
        showEditUserDialog(user, position);
    }

    @Override
    public void onDeleteUser(UserModel user, int position) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            Toast.makeText(this, "Cannot delete: User ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        fbHelper.getUsersCollection().document(user.getUid()).delete()
            .addOnSuccessListener(v -> {
                userList.remove(position);
                userAdapter.notifyItemRemoved(position);
                userAdapter.notifyItemRangeChanged(position, userList.size());
                loadStats();
                Toast.makeText(this, user.getName() + " deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showEditUserDialog(UserModel user, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etGoal = dialogView.findViewById(R.id.etEditGoal);
        EditText etAge = dialogView.findViewById(R.id.etEditAge);
        EditText etHeight = dialogView.findViewById(R.id.etEditHeight);
        EditText etWeight = dialogView.findViewById(R.id.etEditWeight);
        Spinner spRole = dialogView.findViewById(R.id.spEditRole);

        // Pre-fill fields
        etName.setText(user.getName());
        etGoal.setText(user.getGoal());
        etAge.setText(user.getAge());
        etHeight.setText(user.getHeight());
        etWeight.setText(user.getWeight());

        // Role spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roles);
        spRole.setAdapter(adapter);
        if ("admin".equals(user.getRole())) spRole.setSelection(1);
        else spRole.setSelection(0);

        new AlertDialog.Builder(this)
            .setTitle("Edit " + user.getName())
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String uid = user.getUid();
                if (uid == null || uid.isEmpty()) {
                    Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", etName.getText().toString().trim());
                updates.put("goal", etGoal.getText().toString().trim());
                updates.put("age", etAge.getText().toString().trim());
                updates.put("height", etHeight.getText().toString().trim());
                updates.put("weight", etWeight.getText().toString().trim());
                updates.put("role", spRole.getSelectedItem().toString());

                fbHelper.getUsersCollection().document(uid).update(updates)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "User updated!", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
