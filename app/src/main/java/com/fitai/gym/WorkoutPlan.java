/*
 * WorkoutPlan represents a multi-day training routine containing nested day and exercise definitions.
 */
package com.fitai.gym;

import java.util.ArrayList;
import java.util.List;


public class WorkoutPlan {
    private String id;
    private String title;
    private String description;
    private String imageRes;         
    private List<String> difficulty; 
    private int totalDays;
    private int calories;
    private long createdAt;
    private int version;

    
    public WorkoutPlan() {
        difficulty = new ArrayList<>();
    }

    public WorkoutPlan(String title, String description, String imageRes,
                       List<String> difficulty, int totalDays, int calories, int version) {
        this.title = title;
        this.description = description;
        this.imageRes = imageRes;
        this.difficulty = difficulty != null ? difficulty : new ArrayList<>();
        this.totalDays = totalDays;
        this.calories = calories;
        this.version = version;
        this.createdAt = System.currentTimeMillis();
    }

    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageRes() { return imageRes; }
    public List<String> getDifficulty() { return difficulty; }
    public int getTotalDays() { return totalDays; }
    public int getCalories() { return calories; }
    public long getCreatedAt() { return createdAt; }
    public int getVersion() { return version; }

    
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageRes(String imageRes) { this.imageRes = imageRes; }
    public void setDifficulty(List<String> difficulty) { this.difficulty = difficulty; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setVersion(int version) { this.version = version; }

    
    public int getImageResourceId(android.content.Context context) {
        if (imageRes == null || imageRes.isEmpty()) return R.drawable.workout_1;
        int resId = context.getResources().getIdentifier(imageRes, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.workout_1;
    }
}
