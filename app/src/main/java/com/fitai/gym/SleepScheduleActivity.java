package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SleepScheduleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_schedule);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddSchedule).setOnClickListener(v -> 
            startActivity(new Intent(this, AddAlarmActivity.class)));
    }
}
