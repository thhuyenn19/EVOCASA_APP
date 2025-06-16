package com.mobile.evocasa.helpcenter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.FaqAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.FaqItem;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class HelpCenterActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FaqAdapter faqAdapter;
    List<FaqItem> faqList;

    LinearLayout policyPurchaseGroup, policyReturnGroup, policyPrivacyGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_help_center);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        //FAQ
        setContentView(R.layout.activity_help_center);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        faqList = new ArrayList<>();
        faqList.add(new FaqItem("How can I place an order on EvoCasa?",
                "You can browse products, select your preferred size/color, and tap “Add to Cart” or “Buy Now.” Then, fill in your delivery details and choose a payment method to complete your order."));
        faqList.add(new FaqItem("Does EvoCasa support returns and exchanges?",
                "Yes. EvoCasa accepts returns or exchanges within 7 days from the delivery date for items that are defective, incorrect, or damaged during shipping. Products must be unused, in original condition, and with complete packaging."));
        faqList.add(new FaqItem("Are there any fees for returns or exchanges?",
                "Returns or exchanges are free of charge if the issue is on EvoCasa’s end. For personal reasons (e.g., change of mind, wrong size/color), customers will cover the round-trip shipping fee."));
        faqList.add(new FaqItem("Does EvoCasa protect my personal information?",
                "Absolutely. We are fully committed to safeguarding all customer information in compliance with data protection laws. We do not share any personal data with third parties without your consent."));
        faqList.add(new FaqItem("How long does delivery take?",
                "Delivery times vary based on your location and the type of product.\n" +
                        "In-stock items: 2–5 business days\n" +
                        "Custom or made-to-order items: 7–14 business days"));

        faqAdapter = new FaqAdapter(this, faqList);
        recyclerView.setAdapter(faqAdapter);

        //Mở Policy
        policyPurchaseGroup = findViewById(R.id.policyPurchaseGroup);

        policyPurchaseGroup.setOnClickListener(v -> {
            Intent intent = new Intent(HelpCenterActivity.this, PurchasePolicyActivity.class);
            startActivity(intent);
        });

        policyReturnGroup = findViewById(R.id.policyReturnGroup);

        policyReturnGroup.setOnClickListener(v -> {
            Intent intent = new Intent(HelpCenterActivity.this, ReturnExchangeActivity.class);
            startActivity(intent);
        });

        policyPrivacyGroup = findViewById(R.id.policyPrivacyGroup);

        policyPrivacyGroup.setOnClickListener(v -> {
            Intent intent = new Intent(HelpCenterActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });



    }
}