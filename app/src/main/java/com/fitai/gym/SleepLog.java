/*
 * SleepLog is a model representing daily sleep duration and log timestamp details.
 */
package com.fitai.gym;

public class SleepLog {
    private String date; 
    private int hours;
    private int minutes;

    public SleepLog() {
    }

    public SleepLog(String date, int hours, int minutes) {
        this.date = date;
        this.hours = hours;
        this.minutes = minutes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
