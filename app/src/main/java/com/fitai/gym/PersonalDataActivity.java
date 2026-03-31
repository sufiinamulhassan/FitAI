package com.fitai.gym;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalDataActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvGender, tvDOB, tvWeight, tvHeight;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        tvName = findViewById(R.id.tvDataName);
        tvEmail = findViewById(R.id.tvDataEmail);
        tvGender = findViewById(R.id.tvDataGender);
        tvDOB = findViewById(R.id.tvDataDOB);
        tvWeight = findViewById(R.id.tvDataWeight);
        tvHeight = findViewById(R.id.tvDataHeight);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadPersonalData();
    }

    private void loadPersonalData() {
        FirebaseHelper fbHelper = FirebaseHelper.getInstance();
        if (fbHelper.getAuth().getCurrentUser() != null) {
            String uid = fbHelper.getAuth().getUid();
            fbHelper.getUsersCollection().document(uid).addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null || !snapshot.exists()) return;

                String name = snapshot.getString("name");
                String email = snapshot.getString("email");
                String gender = snapshot.getString("gender");
                String dob = snapshot.getString("dob");
                String weight = snapshot.getString("weight");
                String height = snapshot.getString("height");

                if (name != null) tvName.setText(name);
                if (email != null) tvEmail.setText(email);
                if (gender != null) tvGender.setText(gender);
                if (dob != null) tvDOB.setText(dob);
                if (weight != null) tvWeight.setText(weight);
                if (height != null) tvHeight.setText(height);
            });
        }
    }
}
