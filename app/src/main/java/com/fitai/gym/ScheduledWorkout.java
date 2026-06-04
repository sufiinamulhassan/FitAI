/*
 * ScheduledWorkout is a data model representing a user's scheduled workout session.
 */
package com.fitai.gym;

public class ScheduledWorkout {
    private String name;
    private String time;
    private String date;
    private String color;

    public ScheduledWorkout() {
    }

    public ScheduledWorkout(String name, String time, String date, String color) {
        this.name = name;
        this.time = time;
        this.date = date;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
