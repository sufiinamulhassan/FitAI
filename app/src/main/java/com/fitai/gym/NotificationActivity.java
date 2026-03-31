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
import java.util.ArrayList;
import java.util.List;

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

        setupNotificationList();
    }

    private void setupNotificationList() {
        List<NotificationData> list = new ArrayList<>();
        // Using matching icons from drawable folder
        list.add(new NotificationData("Hey, it's time for lunch", "About 1 minutes ago", R.drawable.apple_pie));
        list.add(new NotificationData("Don't miss your lowerbody workout", "About 3 hours ago", R.drawable.workout_2));
        list.add(new NotificationData("Hey, let's add some meals for your b..", "About 3 hours ago", R.drawable.honey_pan));
        list.add(new NotificationData("Congratulations, You have finished A..", "29 May", R.drawable.complete_workout));
        list.add(new NotificationData("Hey, it's time for lunch", "8 April", R.drawable.orange));
        list.add(new NotificationData("Ups, You have missed your Lowerbo...", "3 April", R.drawable.workout_1));

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(new NotificationAdapter(list));
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
