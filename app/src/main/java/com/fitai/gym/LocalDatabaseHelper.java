package com.fitai.gym;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FitAI_Local.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Workouts
    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESC = "description";
    private static final String COL_CALORIES = "calories";
    private static final String COL_DURATION = "duration";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
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
                // Map to existing model (adapting to available fields)
                list.add(new WorkoutModel(title, desc + " | " + duration + "min", R.drawable.barbell, 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
