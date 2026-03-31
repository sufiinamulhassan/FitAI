package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MembershipActivity extends AppCompatActivity {

    private CardView cardMonthly, cardQuarterly, cardYearly;
    private int selectedPlan = 0;
    private String[] planNames = {"Monthly Premium", "Quarterly Premium", "Yearly Premium"};
    private String[] planPrices = {"$9.99", "$24.99", "$71.99"};
    private String[] planDurations = {"30 days", "90 days", "365 days"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        cardMonthly = findViewById(R.id.cardMonthly);
        cardQuarterly = findViewById(R.id.cardQuarterly);
        cardYearly = findViewById(R.id.cardYearly);

        cardMonthly.setOnClickListener(v -> selectPlan(0));
        cardQuarterly.setOnClickListener(v -> selectPlan(1));
        cardYearly.setOnClickListener(v -> selectPlan(2));
        selectPlan(0);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnGetPlan).setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentConfirmationActivity.class);
            intent.putExtra("plan_name", planNames[selectedPlan]);
            intent.putExtra("plan_price", planPrices[selectedPlan]);
            intent.putExtra("plan_duration", planDurations[selectedPlan]);
            startActivity(intent);
        });
    }

    private void selectPlan(int index) {
        selectedPlan = index;
        int selected = 0xFFEEF0FF;
        int normal = 0xFFFFFFFF;
        cardMonthly.setCardBackgroundColor(index == 0 ? selected : normal);
        cardQuarterly.setCardBackgroundColor(index == 1 ? selected : normal);
        cardYearly.setCardBackgroundColor(index == 2 ? selected : normal);
    }
}
