package com.fitai.gym;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        TextView tvBedtimeValue = findViewById(R.id.tvBedtimeValue);

        // Bedtime Picker Logic
        findViewById(R.id.cardBedtime).setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minuteOfHour) -> {
                        String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                        int formattedHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
                        String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, minuteOfHour, amPm);
                        tvBedtimeValue.setText(timeString);
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        // Hours of Sleep Placeholder
        findViewById(R.id.cardHours).setOnClickListener(v -> 
            Toast.makeText(this, "Hours of sleep calculation setting", Toast.LENGTH_SHORT).show()
        );

        // Repeat Placeholder
        findViewById(R.id.cardRepeat).setOnClickListener(v -> 
            Toast.makeText(this, "Repeat configuration setting", Toast.LENGTH_SHORT).show()
        );

        // Add Button logic
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Toast.makeText(this, "Alarm Scheduled successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
