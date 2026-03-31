package com.fitai.gym;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentConfirmationActivity extends AppCompatActivity {

    private EditText etCardNumber, etExpiry, etCVV;
    private RadioButton rbCard, rbPaypal;
    private LinearLayout llCardForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        String planName = getIntent().getStringExtra("plan_name");
        String planPrice = getIntent().getStringExtra("plan_price");
        String planDuration = getIntent().getStringExtra("plan_duration");

        TextView tvPlanName = findViewById(R.id.tvPlanName);
        TextView tvTotal = findViewById(R.id.tvTotal);
        TextView tvDuration = findViewById(R.id.tvDuration);

        if (planName != null) tvPlanName.setText(planName);
        if (planPrice != null) tvTotal.setText(planPrice);
        if (planDuration != null) tvDuration.setText(planDuration);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);
        rbCard = findViewById(R.id.rbCard);
        rbPaypal = findViewById(R.id.rbPaypal);
        llCardForm = findViewById(R.id.llCardForm);

        rbCard.setOnCheckedChangeListener((btn, checked) -> llCardForm.setVisibility(checked ? android.view.View.VISIBLE : android.view.View.GONE));
        rbPaypal.setOnCheckedChangeListener((btn, checked) -> llCardForm.setVisibility(checked ? android.view.View.GONE : android.view.View.VISIBLE));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnPayNow).setOnClickListener(v -> processPayment(planName, planPrice));
    }

    private void processPayment(String planName, String planPrice) {
        if (rbCard.isChecked()) {
            String cardNum = etCardNumber.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (cardNum.length() < 16) { etCardNumber.setError("Enter 16-digit card number"); return; }
            if (expiry.length() < 5) { etExpiry.setError("Enter valid expiry (MM/YY)"); return; }
            if (cvv.length() < 3) { etCVV.setError("Enter 3-digit CVV"); return; }
        }

        new AlertDialog.Builder(this)
            .setTitle("Payment Successful! 🎉")
            .setMessage("You've subscribed to " + planName + " for " + planPrice + ".\n\nYour membership is now active. Enjoy unlimited access to FitnesX!")
            .setPositiveButton("Start Workout", (d, w) -> {
                startActivity(new android.content.Intent(this, WorkoutTrackerActivity.class));
                finishAffinity();
            })
            .setCancelable(false)
            .show();
    }
}
