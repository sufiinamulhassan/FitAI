/*
 * Exercise represents a single workout movement with reps, sets, duration, rest time, image references, and instructional details.
 */
package com.fitai.gym;


public class Exercise {
    private String name;
    private String reps;
    private int sets;
    private int duration;
    private int restTime;
    private String imageRes;
    private String instructions;

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

    public String getName() { return name; }
    public String getReps() { return reps; }
    public int getSets() { return sets; }
    public int getDuration() { return duration; }
    public int getRestTime() { return restTime; }
    public String getImageRes() { return imageRes; }
    public String getInstructions() { return instructions; }

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
