package com.fitai.gym;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etDateOfBirth, etWeight, etHeight;
    private Spinner spinnerGender;
    private TextView tvWeightUnit, tvHeightUnit;
    private Button btnNext;
    private ImageView ivProfileSelect;
    private android.net.Uri imageUri;
    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivProfileSelect.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        ivProfileSelect = findViewById(R.id.ivProfileSelect);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        tvWeightUnit = findViewById(R.id.tvWeightUnit);
        tvHeightUnit = findViewById(R.id.tvHeightUnit);
        btnNext = findViewById(R.id.btnNext);

        ivProfileSelect.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Styling the spinner
        String[] genders = {"Choose Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genders) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    ((TextView) v).setTextColor(android.graphics.Color.GRAY);
                } else {
                    ((TextView) v).setTextColor(android.graphics.Color.BLACK);
                }
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        etDateOfBirth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                etDateOfBirth.setText(day + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Toggle units for fun (optional but good for UX)
        tvWeightUnit.setOnClickListener(v -> {
            if (tvWeightUnit.getText().toString().equals("KG")) {
                tvWeightUnit.setText("LB");
            } else {
                tvWeightUnit.setText("KG");
            }
        });

        tvHeightUnit.setOnClickListener(v -> {
            if (tvHeightUnit.getText().toString().equals("CM")) {
                tvHeightUnit.setText("IN");
            } else {
                tvHeightUnit.setText("CM");
            }
        });

        btnNext.setOnClickListener(v -> {
            if (spinnerGender.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please choose your gender", Toast.LENGTH_SHORT).show();
                return;
            }
            if (etDateOfBirth.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
                return;
            }
            if (etWeight.getText().toString().isEmpty()) {
                etWeight.setError("Weight is required");
                return;
            }
            if (etHeight.getText().toString().isEmpty()) {
                etHeight.setError("Height is required");
                return;
            }

            String weight = etWeight.getText().toString();
            String height = etHeight.getText().toString();
            String wUnit = tvWeightUnit.getText().toString().toLowerCase();
            String hUnit = tvHeightUnit.getText().toString().toLowerCase();
            String gender = spinnerGender.getSelectedItem().toString();
            String dob = etDateOfBirth.getText().toString();

            // Calculate precise age from DOB (DD/MM/YYYY)
            int age = 0; // Default
            try {
                String[] parts = dob.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                
                Calendar birth = Calendar.getInstance();
                birth.set(year, month - 1, day);
                Calendar today = Calendar.getInstance();
                
                age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
            } catch (Exception e) { e.printStackTrace(); }

            // Save data locally for quick dashboard calculation
            getSharedPreferences("FitAI_Prefs", MODE_PRIVATE).edit()
                .putString("weight", weight)
                .putString("height", height)
                .putString("weight_unit", wUnit)
                .putString("height_unit", hUnit)
                .putString("gender", gender)
                .apply();

            // Show feedback and disable button
            btnNext.setEnabled(false);
            btnNext.setText("Saving...");
            Toast.makeText(this, "Optimizing your profile...", Toast.LENGTH_SHORT).show();

            // Run in background thread to avoid UI freeze
            final int finalAge = age;
            new Thread(() -> {
                FirebaseHelper fbHelper = FirebaseHelper.getInstance();
                if (fbHelper.getAuth().getCurrentUser() != null) {
                    String uid = fbHelper.getAuth().getUid();
                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    updates.put("weight", weight + wUnit);
                    updates.put("height", height + hUnit);
                    updates.put("gender", gender);
                    updates.put("dob", dob);
                    updates.put("age", String.valueOf(finalAge));

                    if (imageUri != null) {
                        try {
                            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            String base64Image = ImageUtils.bitmapToBase64(bitmap);
                            updates.put("profilePicUrl", base64Image);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // Use set with merge to ensure document exists
                    fbHelper.getUsersCollection().document(uid)
                        .set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            runOnUiThread(() -> {
                                startActivity(new Intent(this, GoalSelectionActivity.class));
                                finish();
                            });
                        });
                } else {
                    runOnUiThread(() -> {
                        startActivity(new Intent(this, GoalSelectionActivity.class));
                        finish();
                    });
                }
            }).start();
        });
    }
}
