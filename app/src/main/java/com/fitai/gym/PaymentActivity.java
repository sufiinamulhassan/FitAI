package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private String selectedMethod = "";
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        fbHelper = FirebaseHelper.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Method Selectors
        findViewById(R.id.btnEasypaisa).setOnClickListener(v -> selectMethod("EasyPaisa"));
        findViewById(R.id.btnJazzcash).setOnClickListener(v -> selectMethod("JazzCash"));
        findViewById(R.id.btnCard).setOnClickListener(v -> selectMethod("Debit Card"));

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            if (selectedMethod.isEmpty()) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }
            processPayment();
        });
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        Toast.makeText(this, "Selected: " + method, Toast.LENGTH_SHORT).show();
    }

    private void processPayment() {
        // Mock processing
        new AlertDialog.Builder(this)
            .setTitle("Processing Payment")
            .setMessage("Confirming payment via " + selectedMethod + "...")
            .setPositiveButton("Confirm", (d, w) -> {
                savePaymentToFirebase();
            })
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
        paymentData.put("amount", 19.99);
        paymentData.put("timestamp", System.currentTimeMillis());

        fbHelper.getPaymentsCollection().add(paymentData).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Payment Successful! You are now a Premium user.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, PaymentConfirmationActivity.class));
            finish();
        });
    }
}
