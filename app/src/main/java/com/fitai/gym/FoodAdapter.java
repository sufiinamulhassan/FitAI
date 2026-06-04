/*
 * FoodAdapter binds and renders the list of food and meal items in the nutrition screens.
 */
package com.fitai.gym;

import android.content.Context;
import android.content.Intent;
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
        ImageLoaderHelper.loadImage(context, holder.ivImage, f.getImageName(), R.drawable.pancake_1);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MealDetailActivity.class);
            intent.putExtra("meal_type", f.getMealType());
            intent.putExtra("meal_id", f.getId());
            intent.putExtra("meal_name", f.getName());
            intent.putExtra("meal_calories", f.getCalories());
            intent.putExtra("meal_instructions", f.getInstructions());
            intent.putExtra("meal_ingredients", f.getIngredients());
            intent.putExtra("meal_image_name", f.getImageName());
            context.startActivity(intent);
        });
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
