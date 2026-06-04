/*
 * UserModel is a data model holding member details, subscription state, role access, and target metrics.
 */
package com.fitai.gym;

public class UserModel {
    private String uid, name, email, role, profilePicUrl;
    private String goal, height, weight, age;
    private boolean isPremium;
    private String premiumPlan;

    public UserModel() {
        
    }

    public UserModel(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getProfilePicUrl() { return profilePicUrl; }
    public String getGoal() { return goal; }
    public String getHeight() { return height; }
    public String getWeight() { return weight; }
    public String getAge() { return age; }
    public boolean getIsPremium() { return isPremium; }
    public String getPremiumPlan() { return premiumPlan; }

    
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
    public void setRole(String role) { this.role = role; }
    public void setGoal(String goal) { this.goal = goal; }
    public void setHeight(String height) { this.height = height; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setAge(String age) { this.age = age; }
    public void setIsPremium(boolean isPremium) { this.isPremium = isPremium; }
    public void setPremiumPlan(String premiumPlan) { this.premiumPlan = premiumPlan; }
}
