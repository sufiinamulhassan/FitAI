package com.fitai.gym;

public class FoodModel {
    private String name, calories;
    private int imageRes;

    public FoodModel(String name, String calories, int imageRes) {
        this.name = name;
        this.calories = calories;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getCalories() { return calories; }
    public int getImageRes() { return imageRes; }
}
