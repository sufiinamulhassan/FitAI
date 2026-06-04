/*
 * LocalDatabaseHelper manages a local SQLite database that caches completed workout history for offline resilience.
 */
package com.fitai.gym;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FitAI_Local.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_HISTORY = "workout_history";
    private static final String COL_HIST_ID = "id";
    private static final String COL_HIST_TITLE = "title";
    private static final String COL_HIST_COUNT = "exercise_count";
    private static final String COL_HIST_TIME = "total_time_seconds";
    private static final String COL_HIST_CALORIES = "calories_burned";
    private static final String COL_HIST_DATE = "completed_at";

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HIST_TITLE + " TEXT, " +
                COL_HIST_COUNT + " INTEGER, " +
                COL_HIST_TIME + " INTEGER, " +
                COL_HIST_CALORIES + " INTEGER, " +
                COL_HIST_DATE + " TEXT)";
        db.execSQL(createHistoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void insertWorkoutHistory(String title, int count, int timeSeconds, int calories, String completedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_HIST_TITLE, title);
        values.put(COL_HIST_COUNT, count);
        values.put(COL_HIST_TIME, timeSeconds);
        values.put(COL_HIST_CALORIES, calories);
        values.put(COL_HIST_DATE, completedAt);
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public List<Map<String, Object>> getAllWorkoutHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> map = new HashMap<>();
                map.put("workoutTitle", cursor.getString(1));
                map.put("exerciseCount", cursor.getInt(2));
                map.put("totalTimeSeconds", cursor.getInt(3));
                map.put("caloriesBurned", cursor.getInt(4));
                map.put("completedAt", cursor.getString(5));
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
