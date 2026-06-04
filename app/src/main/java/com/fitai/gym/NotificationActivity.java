/*
 * NotificationActivity displays system alerts, reminders, and workout announcements.
 */
package com.fitai.gym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        btnBack = findViewById(R.id.btnBack);
        rvNotifications = findViewById(R.id.rvNotifications);

        btnBack.setOnClickListener(v -> finish());

        fetchNotifications();
    }

    private void fetchNotifications() {
        String uid = FirebaseHelper.getInstance().getCurrentUserUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getUsersCollection()
            .document(uid)
            .collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<NotificationData> list = new ArrayList<>();
                if (snapshot != null && !snapshot.isEmpty()) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String title = doc.getString("title");
                        Long ts = doc.getLong("timestamp");
                        String timeStr = "Just now";

                        if (ts != null) {
                            long diff = System.currentTimeMillis() - ts;
                            long mins = diff / (60 * 1000);
                            long hours = mins / 60;
                            if (mins < 1) {
                                timeStr = "Just now";
                            } else if (mins < 60) {
                                timeStr = "About " + mins + " minutes ago";
                            } else if (hours < 24) {
                                timeStr = "About " + hours + " hours ago";
                            } else {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());
                                timeStr = sdf.format(new java.util.Date(ts));
                            }
                        }

                        String type = doc.getString("type");
                        int icon = R.drawable.apple_pie; 
                        if ("workout".equalsIgnoreCase(type)) {
                            icon = R.drawable.workout_1;
                        } else if ("meal".equalsIgnoreCase(type)) {
                            icon = R.drawable.honey_pan;
                        } else if ("system".equalsIgnoreCase(type)) {
                            icon = R.drawable.complete_workout;
                        }

                        list.add(new NotificationData(title, timeStr, icon));
                    }
                }

                
                if (list.isEmpty()) {
                    Map<String, Object> welcome = new HashMap<>();
                    welcome.put("title", "Welcome to FitAI! Start your health journey today.");
                    welcome.put("timestamp", System.currentTimeMillis());
                    welcome.put("type", "system");
                    FirebaseHelper.getInstance().getUsersCollection()
                        .document(uid)
                        .collection("notifications")
                        .add(welcome);

                    list.add(new NotificationData("Welcome to FitAI! Start your health journey today.", "Just now", R.drawable.complete_workout));
                }

                rvNotifications.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
                rvNotifications.setAdapter(new NotificationAdapter(list));
            })
            .addOnFailureListener(e -> {
                
                List<NotificationData> list = new ArrayList<>();
                list.add(new NotificationData("Welcome to FitAI! Start your health journey today.", "Just now", R.drawable.complete_workout));
                rvNotifications.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
                rvNotifications.setAdapter(new NotificationAdapter(list));
            });
    }

    private static class NotificationData {
        String title, time;
        int icon;
        NotificationData(String title, String time, int icon) {
            this.title = title;
            this.time = time;
            this.icon = icon;
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<NotificationData> items;
        NotificationAdapter(List<NotificationData> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationData item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvTime.setText(item.time);
            holder.ivIcon.setImageResource(item.icon);
        }

        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvTitle, tvTime;
            ViewHolder(View v) {
                super(v);
                ivIcon = v.findViewById(R.id.ivNotifIcon);
                tvTitle = v.findViewById(R.id.tvNotifTitle);
                tvTime = v.findViewById(R.id.tvNotifTime);
            }
        }
    }
}
