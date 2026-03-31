package com.fitai.gym;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private Context context;
    private int[] images;
    private String[] titles;
    private String[] descriptions;

    public OnboardingAdapter(Context context, int[] images, String[] titles, String[] descriptions) {
        this.context = context;
        this.images = images;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_onboarding_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ivImage.setImageResource(images[position]);
        holder.tvTitle.setText(titles[position]);
        holder.tvDesc.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() { return images.length; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivOnboardingImage);
            tvTitle = v.findViewById(R.id.tvOnboardingTitle);
            tvDesc = v.findViewById(R.id.tvOnboardingDesc);
        }
    }
}
