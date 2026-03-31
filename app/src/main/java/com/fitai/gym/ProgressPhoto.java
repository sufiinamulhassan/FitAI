package com.fitai.gym;

public class ProgressPhoto {
    private String photoUrl;
    private String date; // YYYY-MM-DD
    private long timestamp;

    public ProgressPhoto() {}

    public ProgressPhoto(String photoUrl, String date, long timestamp) {
        this.photoUrl = photoUrl;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
