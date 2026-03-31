package com.fitai.gym;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SleepTrackerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> 
            startActivity(new Intent(this, SleepScheduleActivity.class)));
    }
}
