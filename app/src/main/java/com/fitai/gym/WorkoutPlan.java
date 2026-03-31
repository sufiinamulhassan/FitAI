package com.fitai.gym;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete workout plan created by an admin.
 * Stored in Firestore: workout_plans/{planId}
 */
public class WorkoutPlan {
    private String id;
    private String title;
    private String description;
    private String imageRes;         // drawable resource name e.g. "workout_1"
    private List<String> difficulty; // ["beginner", "intermediate", "advanced"]
    private int totalDays;
    private int calories;
    private long createdAt;

    // Required empty constructor for Firestore
    public WorkoutPlan() {
        difficulty = new ArrayList<>();
    }

    public WorkoutPlan(String title, String description, String imageRes,
                       List<String> difficulty, int totalDays, int calories) {
        this.title = title;
        this.description = description;
        this.imageRes = imageRes;
        this.difficulty = difficulty != null ? difficulty : new ArrayList<>();
        this.totalDays = totalDays;
        this.calories = calories;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageRes() { return imageRes; }
    public List<String> getDifficulty() { return difficulty; }
    public int getTotalDays() { return totalDays; }
    public int getCalories() { return calories; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageRes(String imageRes) { this.imageRes = imageRes; }
    public void setDifficulty(List<String> difficulty) { this.difficulty = difficulty; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Get a drawable resource ID by name at runtime
     */
    public int getImageResourceId(android.content.Context context) {
        if (imageRes == null || imageRes.isEmpty()) return R.drawable.workout_1;
        int resId = context.getResources().getIdentifier(imageRes, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.workout_1;
    }
}
