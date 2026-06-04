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
    private final androidx.activity.result.ActivityResultLauncher<Intent> uCropLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        imageUri = resultUri;
                        ivProfileSelect.setImageURI(resultUri);
                    }
                } else if (result.getResultCode() == com.yalantis.ucrop.UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = com.yalantis.ucrop.UCrop.getError(result.getData());
                    if (cropError != null) cropError.printStackTrace();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String destinationFileName = "CROP_" + System.currentTimeMillis() + ".jpg";
                    android.net.Uri destinationUri = android.net.Uri.fromFile(new java.io.File(getCacheDir(), destinationFileName));
                    
                    com.yalantis.ucrop.UCrop.Options options = new com.yalantis.ucrop.UCrop.Options();
                    options.setCircleDimmedLayer(true);
                    options.setShowCropGrid(false);
                    options.setToolbarTitle("Adjust Photo");
                    options.setToolbarColor(android.graphics.Color.parseColor("#92A3FD"));
                    options.setStatusBarColor(android.graphics.Color.parseColor("#92A3FD"));
                    options.setToolbarWidgetColor(android.graphics.Color.WHITE);
                    
                    Intent uCropIntent = com.yalantis.ucrop.UCrop.of(uri, destinationUri)
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(500, 500)
                            .withOptions(options)
                            .getIntent(this);
                            
                    uCropLauncher.launch(uCropIntent);
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

        boolean isEdit = getIntent().getBooleanExtra("is_edit", false);
        if (isEdit) {
            btnNext.setText("Save");
        }

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

        // Fetch existing user data to pre-fill the form
        FirebaseHelper fbHelper = FirebaseHelper.getInstance();
        if (fbHelper.getAuth().getCurrentUser() != null) {
            String uid = fbHelper.getAuth().getUid();
            fbHelper.getUsersCollection().document(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    if (snapshot.contains("dob")) {
                        etDateOfBirth.setText(snapshot.getString("dob"));
                    }
                    if (snapshot.contains("weight")) {
                        String w = snapshot.getString("weight");
                        if (w != null && w.length() > 2) {
                            etWeight.setText(w.substring(0, w.length() - 2));
                            tvWeightUnit.setText(w.substring(w.length() - 2).toUpperCase());
                        }
                    }
                    if (snapshot.contains("height")) {
                        String h = snapshot.getString("height");
                        if (h != null && h.length() > 2) {
                            etHeight.setText(h.substring(0, h.length() - 2));
                            tvHeightUnit.setText(h.substring(h.length() - 2).toUpperCase());
                        }
                    }
                    if (snapshot.contains("gender")) {
                        String gender = snapshot.getString("gender");
                        for (int i = 0; i < genders.length; i++) {
                            if (genders[i].equalsIgnoreCase(gender)) {
                                spinnerGender.setSelection(i);
                                break;
                            }
                        }
                    }
                    if (snapshot.contains("profilePicUrl")) {
                        String base64 = snapshot.getString("profilePicUrl");
                        if (base64 != null && !base64.isEmpty()) {
                            try {
                                byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfileSelect.setImageBitmap(decodedByte);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

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
                FirebaseHelper fbHelperBackground = FirebaseHelper.getInstance();
                if (fbHelperBackground.getAuth().getCurrentUser() != null) {
                    String uid = fbHelperBackground.getAuth().getUid();
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
                    fbHelperBackground.getUsersCollection().document(uid)
                        .set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileSetupActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                if (isEdit) {
                                    finish();
                                } else {
                                    startActivity(new Intent(this, GoalSelectionActivity.class));
                                    finish();
                                }
                            });
                        });
                } else {
                    runOnUiThread(() -> {
                        if (isEdit) {
                            finish();
                        } else {
                            startActivity(new Intent(this, GoalSelectionActivity.class));
                            finish();
                        }
                    });
                }
            }).start();
        });
    }
}
