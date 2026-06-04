/*
 * UserProgress represents workout milestones and completion achievements.
 */
package com.fitai.gym;

import java.util.ArrayList;
import java.util.List;


public class UserProgress {
    private String planId;
    private String planTitle;
    private String level;                
    private List<Integer> completedDays; 
    private long startDate;
    private long lastWorkoutDate;
    private int totalCaloriesBurned;
    private int totalTimeSpent;          

    public UserProgress() {
        completedDays = new ArrayList<>();
    }

    public UserProgress(String planId, String planTitle, String level) {
        this.planId = planId;
        this.planTitle = planTitle;
        this.level = level;
        this.completedDays = new ArrayList<>();
        this.startDate = System.currentTimeMillis();
        this.lastWorkoutDate = 0;
        this.totalCaloriesBurned = 0;
        this.totalTimeSpent = 0;
    }

    
    public String getPlanId() { return planId; }
    public String getPlanTitle() { return planTitle; }
    public String getLevel() { return level; }
    public List<Integer> getCompletedDays() { return completedDays; }
    public long getStartDate() { return startDate; }
    public long getLastWorkoutDate() { return lastWorkoutDate; }
    public int getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public int getTotalTimeSpent() { return totalTimeSpent; }

    
    public void setPlanId(String planId) { this.planId = planId; }
    public void setPlanTitle(String planTitle) { this.planTitle = planTitle; }
    public void setLevel(String level) { this.level = level; }
    public void setCompletedDays(List<Integer> completedDays) { this.completedDays = completedDays; }
    public void setStartDate(long startDate) { this.startDate = startDate; }
    public void setLastWorkoutDate(long lastWorkoutDate) { this.lastWorkoutDate = lastWorkoutDate; }
    public void setTotalCaloriesBurned(int totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }
    public void setTotalTimeSpent(int totalTimeSpent) { this.totalTimeSpent = totalTimeSpent; }

    public boolean isDayCompleted(int dayNumber) {
        return completedDays != null && completedDays.contains(dayNumber);
    }

    public void markDayComplete(int dayNumber, int calories, int timeSpent) {
        if (completedDays == null) completedDays = new ArrayList<>();
        if (!completedDays.contains(dayNumber)) {
            completedDays.add(dayNumber);
        }
        this.lastWorkoutDate = System.currentTimeMillis();
        this.totalCaloriesBurned += calories;
        this.totalTimeSpent += timeSpent;
    }

    public int getProgressPercent(int totalDays) {
        if (totalDays <= 0 || completedDays == null) return 0;
        return (int) ((completedDays.size() * 100.0) / totalDays);
    }
}
