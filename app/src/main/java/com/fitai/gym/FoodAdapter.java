package com.fitai.gym;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private Context context;
    private List<FoodModel> foods;

    public FoodAdapter(Context context, List<FoodModel> foods) {
        this.context = context;
        this.foods = foods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel f = foods.get(position);
        holder.tvName.setText(f.getName());
        holder.tvCalories.setText(f.getCalories());
        holder.ivImage.setImageResource(f.getImageRes());
    }

    @Override
    public int getItemCount() { return foods.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCalories;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivFoodImage);
            tvName = v.findViewById(R.id.tvFoodName);
            tvCalories = v.findViewById(R.id.tvFoodCalories);
        }
    }
}
