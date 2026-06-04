/*
 * WaterIntake is a data model tracking daily hydration records and fluid ounces consumed.
 */
package com.fitai.gym;

public class WaterIntake {
    private String time;
    private int amountMl;
    private String date; 

    public WaterIntake() {
    }

    public WaterIntake(String time, int amountMl, String date) {
        this.time = time;
        this.amountMl = amountMl;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getAmountMl() {
        return amountMl;
    }

    public void setAmountMl(int amountMl) {
        this.amountMl = amountMl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
