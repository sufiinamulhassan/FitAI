/*
 * GoalAdapter binds and renders different fitness goals during profile onboarding.
 */
package com.fitai.gym;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private int[] images;
    private String[] titles;
    private String[] descriptions;

    public GoalAdapter(int[] images, String[] titles, String[] descriptions) {
        this.images = images;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.ivGoalImg.setImageResource(images[position]);
        holder.tvGoalName.setText(titles[position]);
        holder.tvGoalDesc.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGoalImg;
        TextView tvGoalName, tvGoalDesc;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGoalImg = itemView.findViewById(R.id.ivGoalImg);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvGoalDesc = itemView.findViewById(R.id.tvGoalDesc);
        }
    }
}
