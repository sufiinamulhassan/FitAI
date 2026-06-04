/*
 * SettingsActivity controls user notification preferences and privacy configurations.
 */
package com.fitai.gym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch switchVibration = findViewById(R.id.switchVibration);
        Switch switchPushNotif = findViewById(R.id.switchPushNotif);
        Switch switchUnits = findViewById(R.id.switchUnits);
        Switch switchDarkMode = findViewById(R.id.switchDarkMode);
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);

        String[] languages = {"English", "Spanish", "French", "German", "Indonesian"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(langAdapter);

        switchVibration.setOnCheckedChangeListener((btn, checked) ->
            Toast.makeText(this, "Vibration " + (checked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show());
        switchPushNotif.setOnCheckedChangeListener((btn, checked) ->
            Toast.makeText(this, "Push notifications " + (checked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show());
        switchUnits.setOnCheckedChangeListener((btn, checked) ->
            Toast.makeText(this, "Units: " + (checked ? "lbs" : "kg"), Toast.LENGTH_SHORT).show());
        switchDarkMode.setOnCheckedChangeListener((btn, checked) ->
            Toast.makeText(this, "Dark mode " + (checked ? "on" : "off"), Toast.LENGTH_SHORT).show());

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                if (pos > 0) Toast.makeText(SettingsActivity.this, "Language: " + languages[pos], Toast.LENGTH_SHORT).show();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    SharedPreferences prefs = getSharedPreferences("FitnesX_Prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("is_logged_in", false).apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }
}
