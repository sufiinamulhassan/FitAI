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
    private static final int DATABASE_VERSION = 2; // Incremented version to recreate database with history table

    // Table: Workouts
    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESC = "description";
    private static final String COL_CALORIES = "calories";
    private static final String COL_DURATION = "duration";

    // Table: Workout History
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
        String createWorkoutsTable = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_TITLE + " TEXT, " +
                COL_DESC + " TEXT, " +
                COL_CALORIES + " INTEGER, " +
                COL_DURATION + " INTEGER)";
        db.execSQL(createWorkoutsTable);

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void insertWorkout(String id, String title, String desc, int calories, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, id);
        values.put(COL_TITLE, title);
        values.put(COL_DESC, desc);
        values.put(COL_CALORIES, calories);
        values.put(COL_DURATION, duration);

        db.insertWithOnConflict(TABLE_WORKOUTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<WorkoutModel> getAllWorkouts() {
        List<WorkoutModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORKOUTS, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(1);
                String desc = cursor.getString(2);
                int calories = cursor.getInt(3);
                int duration = cursor.getInt(4);
                list.add(new WorkoutModel(title, desc + " | " + duration + "min", R.drawable.barbell, 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // Insert completed workout to local database history
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

    // Retrieve all local workout completion history records
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
