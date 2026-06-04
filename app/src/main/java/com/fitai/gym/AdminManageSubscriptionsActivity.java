/*
 * AdminManageSubscriptionsActivity provides administration for user subscription plans, pricing, and status updates.
 */
package com.fitai.gym;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManageSubscriptionsActivity extends AppCompatActivity {

    private RecyclerView rvSubscriptions;
    private TextView tvTotalPremium, tvTotalFree, tvEstRevenue;
    private List<UserModel> userList = new ArrayList<>();
    private SubscriptionAdapter adapter;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_subscriptions);

        fbHelper = FirebaseHelper.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTotalPremium = findViewById(R.id.tvTotalPremium);
        tvTotalFree    = findViewById(R.id.tvTotalFree);
        tvEstRevenue   = findViewById(R.id.tvEstRevenue);

        rvSubscriptions = findViewById(R.id.rvSubscriptions);
        rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter();
        rvSubscriptions.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    

    private void loadUsers() {
        fbHelper.getUsersCollection().get()
            .addOnSuccessListener(snapshot -> {
                userList.clear();
                int premium = 0;
                double revenue = 0;

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    UserModel user = doc.toObject(UserModel.class);
                    if (user == null) continue;
                    
                    if ("admin".equalsIgnoreCase(user.getRole()) ||
                        "admin.fitai@gmail.com".equalsIgnoreCase(user.getEmail())) continue;
                    if (user.getUid() == null || user.getUid().isEmpty()) user.setUid(doc.getId());

                    userList.add(user);

                    if (user.getIsPremium()) {
                        premium++;
                        revenue += "yearly".equals(user.getPremiumPlan()) ? 59.99 : 9.99;
                    }
                }

                adapter.notifyDataSetChanged();

                tvTotalPremium.setText(String.valueOf(premium));
                tvTotalFree.setText(String.valueOf(userList.size() - premium));
                tvEstRevenue.setText(String.format("$%.0f", revenue));
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }

    

    private void showGrantDialog(UserModel user, int position) {
        String[] plans = {"Monthly ($9.99)", "Yearly ($59.99)", "Free Trial (7 days)"};
        final String[] planKeys = {"monthly", "yearly", "trial"};

        new AlertDialog.Builder(this)
            .setTitle("Grant Premium to " + user.getName())
            .setItems(plans, (dialog, which) -> {
                String planKey = planKeys[which];
                Map<String, Object> updates = new HashMap<>();
                updates.put("isPremium", true);
                updates.put("premiumPlan", planKey);
                updates.put("premiumSince", System.currentTimeMillis());

                fbHelper.getUsersCollection().document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(v -> {
                        user.setIsPremium(true);
                        user.setPremiumPlan(planKey);
                        adapter.notifyItemChanged(position);
                        refreshStats();
                        Toast.makeText(this,
                            user.getName() + " granted " + plans[which], Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void revokeSubscription(UserModel user, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Revoke Subscription")
            .setMessage("Remove premium access from " + user.getName() + "?")
            .setPositiveButton("Revoke", (d, w) -> {
                Map<String, Object> updates = new HashMap<>();
                updates.put("isPremium", false);
                updates.put("premiumPlan", "");

                fbHelper.getUsersCollection().document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(v -> {
                        user.setIsPremium(false);
                        user.setPremiumPlan("");
                        adapter.notifyItemChanged(position);
                        refreshStats();
                        Toast.makeText(this,
                            user.getName() + "'s premium revoked.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void refreshStats() {
        int premium = 0;
        double revenue = 0;
        for (UserModel u : userList) {
            if (u.getIsPremium()) {
                premium++;
                revenue += "yearly".equals(u.getPremiumPlan()) ? 59.99 : 9.99;
            }
        }
        tvTotalPremium.setText(String.valueOf(premium));
        tvTotalFree.setText(String.valueOf(userList.size() - premium));
        tvEstRevenue.setText(String.format("$%.0f", revenue));
    }

    

    private class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_subscription, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            UserModel user = userList.get(position);

            
            String name = user.getName() != null && !user.getName().isEmpty()
                    ? user.getName() : "?";
            holder.tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());

            holder.tvName.setText(name);
            holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            boolean isPremium = user.getIsPremium();
            String plan = user.getPremiumPlan();

            if (isPremium) {
                holder.tvBadge.setText("PREMIUM");
                holder.tvBadge.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C58BF2")));

                String planLabel;
                if ("yearly".equals(plan))       planLabel = "Yearly Plan • $59.99/yr";
                else if ("monthly".equals(plan))  planLabel = "Monthly Plan • $9.99/mo";
                else if ("trial".equals(plan))    planLabel = "Free Trial (7 days)";
                else                              planLabel = "Premium Active";
                holder.tvPlanLabel.setText(planLabel);
                holder.tvPlanLabel.setTextColor(android.graphics.Color.parseColor("#C58BF2"));
            } else {
                holder.tvBadge.setText("FREE");
                holder.tvBadge.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#ADA4A5")));
                holder.tvPlanLabel.setText("No active subscription");
                holder.tvPlanLabel.setTextColor(android.graphics.Color.parseColor("#ADA4A5"));
            }

            holder.btnGrant.setOnClickListener(v -> showGrantDialog(user, holder.getAdapterPosition()));
            holder.btnRevoke.setOnClickListener(v -> revokeSubscription(user, holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() { return userList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvInitial, tvName, tvEmail, tvBadge, tvPlanLabel, btnGrant, btnRevoke;

            VH(View v) {
                super(v);
                tvInitial  = v.findViewById(R.id.tvUserInitial);
                tvName     = v.findViewById(R.id.tvSubUserName);
                tvEmail    = v.findViewById(R.id.tvSubUserEmail);
                tvBadge    = v.findViewById(R.id.tvPremiumBadge);
                tvPlanLabel = v.findViewById(R.id.tvSubPlanLabel);
                btnGrant   = v.findViewById(R.id.btnGrantPremium);
                btnRevoke  = v.findViewById(R.id.btnRevokePremium);
            }
        }
    }
}
