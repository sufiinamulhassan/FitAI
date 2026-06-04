/*
 * DayPlan is a data model representing a single day's workout plan containing exercise collections.
 */
package com.fitai.gym;

import java.util.ArrayList;
import java.util.List;


public class DayPlan {
    private int dayNumber;
    private String dayTitle;
    private boolean restDay;
    private List<Exercise> exercises;

    public DayPlan() {
        exercises = new ArrayList<>();
    }

    public DayPlan(int dayNumber, String dayTitle, boolean restDay, List<Exercise> exercises) {
        this.dayNumber = dayNumber;
        this.dayTitle = dayTitle;
        this.restDay = restDay;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    
    public int getDayNumber() { return dayNumber; }
    public String getDayTitle() { return dayTitle; }
    public boolean isRestDay() { return restDay; }
    public List<Exercise> getExercises() { return exercises; }

    
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
    public void setDayTitle(String dayTitle) { this.dayTitle = dayTitle; }
    public void setRestDay(boolean restDay) { this.restDay = restDay; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public int getTotalDuration() {
        int total = 0;
        for (Exercise e : exercises) {
            total += e.getDuration() + e.getRestTime();
        }
        return total;
    }
}
