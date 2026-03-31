package com.fitai.gym;

public class ExerciseModel {
    private String name, reps;
    private int imageRes;

    public ExerciseModel(String name, String reps, int imageRes) {
        this.name = name;
        this.reps = reps;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getReps() { return reps; }
    public int getImageRes() { return imageRes; }
}
