/*
 * FoodModel represents nutritional items, tracking calories, proteins, carbohydrates, fats, and meal categorization.
 */
package com.fitai.gym;

public class FoodModel {
    private String id;
    private String name;
    private String calories; 
    private String mealType; 
    private String instructions;
    private String ingredients;
    private String imageName; 
    private long createdAt;

    public FoodModel() {}

    public FoodModel(String name, String calories, String mealType, String instructions, String ingredients, String imageName) {
        this.name = name;
        this.calories = calories;
        this.mealType = mealType;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.imageName = imageName;
        this.createdAt = System.currentTimeMillis();
    }

    
    public FoodModel(String name, String calories, int imageRes) {
        this.name = name;
        this.calories = calories;
        this.imageName = "pancake_1"; 
        this.mealType = "Breakfast";
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCalories() { return calories; }
    public void setCalories(String calories) { this.calories = calories; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    
    public int getImageRes() {
        if (imageName == null) return R.drawable.pancake_1;
        switch (imageName.toLowerCase()) {
            case "pancake_1": return R.drawable.pancake_1;
            case "nigiri": return R.drawable.nigiri;
            case "chicken": return R.drawable.chicken;
            case "salad": return R.drawable.salad;
            case "apple_pie": return R.drawable.apple_pie;
            case "orange": return R.drawable.orange;
            case "coffee": return R.drawable.coffee;
            case "glass_of_milk": return R.drawable.glass_of_milk;
            case "oatmeal": return R.drawable.oatmeal;
            default: return R.drawable.pancake_1;
        }
    }
}
