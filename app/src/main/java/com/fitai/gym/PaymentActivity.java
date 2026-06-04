package com.fitai.gym;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private String selectedMethod = "";
    private String selectedPlan = "yearly"; // default to best value
    private double selectedAmount = 59.99;
    private FirebaseHelper fbHelper;

    // Plan cards
    private CardView cvMonthly, cvYearly;
    // Payment method cards
    private CardView btnEasypaisa, btnJazzcash, btnCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        fbHelper = FirebaseHelper.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Plan cards
        cvMonthly = findViewById(R.id.cvMonthly);
        cvYearly = findViewById(R.id.cvYearly);

        // Payment method cards
        btnEasypaisa = findViewById(R.id.btnEasypaisa);
        btnJazzcash = findViewById(R.id.btnJazzcash);
        btnCard = findViewById(R.id.btnCard);

        // Default: yearly selected
        highlightPlan("yearly");

        // Plan selection
        cvMonthly.setOnClickListener(v -> {
            selectedPlan = "monthly";
            selectedAmount = 9.99;
            highlightPlan("monthly");
        });

        cvYearly.setOnClickListener(v -> {
            selectedPlan = "yearly";
            selectedAmount = 59.99;
            highlightPlan("yearly");
        });

        // Payment Method Selectors
        btnEasypaisa.setOnClickListener(v -> selectMethod("EasyPaisa"));
        btnJazzcash.setOnClickListener(v -> selectMethod("JazzCash"));
        btnCard.setOnClickListener(v -> selectMethod("Debit Card"));

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            if (selectedMethod.isEmpty()) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }
            processPayment();
        });
    }

    private void highlightPlan(String plan) {
        if (plan.equals("monthly")) {
            cvMonthly.setCardElevation(4f);
            cvYearly.setCardElevation(0f);
            findViewById(R.id.llMonthly).setBackgroundResource(R.drawable.bg_selected_plan);
            findViewById(R.id.llYearly).setBackgroundResource(R.drawable.bg_unselected_plan);
        } else {
            cvMonthly.setCardElevation(0f);
            cvYearly.setCardElevation(4f);
            findViewById(R.id.llMonthly).setBackgroundResource(R.drawable.bg_unselected_plan);
            findViewById(R.id.llYearly).setBackgroundResource(R.drawable.bg_selected_plan);
        }
    }

    private void selectMethod(String method) {
        selectedMethod = method;

        // Reset all method card backgrounds
        btnEasypaisa.setCardBackgroundColor(Color.parseColor("#E6F7ED"));
        btnJazzcash.setCardBackgroundColor(Color.parseColor("#FFF5E6"));
        btnCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        btnEasypaisa.setCardElevation(0f);
        btnJazzcash.setCardElevation(0f);
        btnCard.setCardElevation(0f);

        // Highlight the selected one
        switch (method) {
            case "EasyPaisa":
                btnEasypaisa.setCardElevation(6f);
                btnEasypaisa.setCardBackgroundColor(Color.parseColor("#C8F0D7"));
                break;
            case "JazzCash":
                btnJazzcash.setCardElevation(6f);
                btnJazzcash.setCardBackgroundColor(Color.parseColor("#FFE8C2"));
                break;
            case "Debit Card":
                btnCard.setCardElevation(6f);
                btnCard.setCardBackgroundColor(Color.parseColor("#E8EEFF"));
                break;
        }

        Toast.makeText(this, "Selected: " + method, Toast.LENGTH_SHORT).show();
    }

    private void processPayment() {
        String planLabel = selectedPlan.equals("yearly") ? "12 Months" : "1 Month";

        new AlertDialog.Builder(this)
            .setTitle("Confirm Subscription")
            .setMessage("Plan: " + planLabel + "\nMethod: " + selectedMethod + "\nAmount: $" + selectedAmount)
            .setPositiveButton("Pay Now", (d, w) -> savePaymentToFirebase())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void savePaymentToFirebase() {
        if (fbHelper.getCurrentUserUid() == null) {
            Toast.makeText(this, "Login required to complete payment", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("userId", fbHelper.getCurrentUserUid());
        paymentData.put("method", selectedMethod);
        paymentData.put("plan", selectedPlan);
        paymentData.put("amount", selectedAmount);
        paymentData.put("timestamp", System.currentTimeMillis());

        fbHelper.getPaymentsCollection().add(paymentData).addOnSuccessListener(documentReference -> {
            // Also mark user as premium
            fbHelper.getUsersCollection().document(fbHelper.getCurrentUserUid())
                .update("isPremium", true, "premiumPlan", selectedPlan, "premiumSince", System.currentTimeMillis())
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Payment Successful! You are now a Premium user.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, PaymentConfirmationActivity.class));
                    finish();
                });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
