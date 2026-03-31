package com.fitai.gym;

/**
 * Represents a single exercise within a day plan.
 * Stored as part of DayPlan's exercises array in Firestore.
 */
public class Exercise {
    private String name;
    private String reps;           // e.g. "x12" or "x20"
    private int sets;              // e.g. 3
    private int duration;          // seconds for timed exercises (e.g. 30)
    private int restTime;          // seconds of rest after this exercise (e.g. 15)
    private String imageRes;       // drawable name e.g. "workout_1"
    private String instructions;   // how to perform

    public Exercise() {}

    public Exercise(String name, String reps, int sets, int duration, int restTime,
                    String imageRes, String instructions) {
        this.name = name;
        this.reps = reps;
        this.sets = sets;
        this.duration = duration;
        this.restTime = restTime;
        this.imageRes = imageRes;
        this.instructions = instructions;
    }

    // Getters
    public String getName() { return name; }
    public String getReps() { return reps; }
    public int getSets() { return sets; }
    public int getDuration() { return duration; }
    public int getRestTime() { return restTime; }
    public String getImageRes() { return imageRes; }
    public String getInstructions() { return instructions; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setReps(String reps) { this.reps = reps; }
    public void setSets(int sets) { this.sets = sets; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setRestTime(int restTime) { this.restTime = restTime; }
    public void setImageRes(String imageRes) { this.imageRes = imageRes; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getImageResourceId(android.content.Context context) {
        if (imageRes == null || imageRes.isEmpty()) return R.drawable.workout_1;
        int resId = context.getResources().getIdentifier(imageRes, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.workout_1;
    }
}
