package com.fitai.gym;

public class WorkoutModel {
    private String name, info;
    private int imageRes, progress;

    public WorkoutModel(String name, String info, int imageRes, int progress) {
        this.name = name;
        this.info = info;
        this.imageRes = imageRes;
        this.progress = progress;
    }

    public String getName() { return name; }
    public String getInfo() { return info; }
    public int getImageRes() { return imageRes; }
    public int getProgress() { return progress; }
}
