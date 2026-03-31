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

public class ActivityTrackerActivity extends AppCompatActivity {

    private RecyclerView rvLatestActivity;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_tracker);

        btnBack = findViewById(R.id.btnBack);
        // rvLatestActivity = findViewById(R.id.rvLatestActivity);
        
        btnBack.setOnClickListener(v -> finish());

        // setupLatestActivityList();
    }

    // private void setupLatestActivityList() {
    //     List<LatestActivityItem> list = new ArrayList<>();
    //     list.add(new LatestActivityItem("Drinking 300ml Water", "About 3 minutes ago", R.drawable.pp_2));
    //     list.add(new LatestActivityItem("Eat Snack (Fitbar)", "About 10 minutes ago", R.drawable.pp_3));

    //     rvLatestActivity.setLayoutManager(new LinearLayoutManager(this));
    //     rvLatestActivity.setAdapter(new ActivityAdapter(list));
    // }

    private static class LatestActivityItem {
        String title, time;
        int icon;
        LatestActivityItem(String title, String time, int icon) {
            this.title = title;
            this.time = time;
            this.icon = icon;
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<LatestActivityItem> items;
        ActivityAdapter(List<LatestActivityItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_latest_activity, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LatestActivityItem item = items.get(position);
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
                ivIcon = v.findViewById(R.id.ivActivityIcon);
                tvTitle = v.findViewById(R.id.tvActivityTitle);
                tvTime = v.findViewById(R.id.tvActivityTime);
            }
        }
    }
}
