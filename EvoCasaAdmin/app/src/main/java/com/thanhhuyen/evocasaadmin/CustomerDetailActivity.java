package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thanhhuyen.untils.FontUtils;

public class CustomerDetailActivity extends AppCompatActivity {

    private TextView txtTitle;
    private ImageView imgBack;
    private TextView tv_customer_name, tv_customer_gender, tv_customer_email, tv_customer_phone, tv_customer_address;

    private FirebaseFirestore db;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_detail);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get customerId from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("customerId")) {
            customerId = intent.getStringExtra("customerId");
        }

        initViews();

        loadCustomerDetails();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerDetailActivity.this, CustomerManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // Font setup
        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }

        tv_customer_name = findViewById(R.id.tv_customer_name);
        tv_customer_gender = findViewById(R.id.tv_customer_gender);
        tv_customer_email = findViewById(R.id.tv_customer_email);
        tv_customer_phone = findViewById(R.id.tv_customer_phone);
        tv_customer_address = findViewById(R.id.tv_customer_address);
    }

    private void loadCustomerDetails() {
        if (customerId == null) return;

        DocumentReference customerRef = db.collection("Customers").document(customerId);
        customerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                String gender = documentSnapshot.getString("Gender");
                String email = documentSnapshot.getString("Mail");
                String phone = documentSnapshot.getString("Phone");
                String address = documentSnapshot.getString("Address");

                tv_customer_name.setText(name);
                tv_customer_gender.setText("Gender: " + gender);
                tv_customer_email.setText("Email: " + email);
                tv_customer_phone.setText("Phone: " + phone);
                tv_customer_address.setText("Address: " + address);
            }
        });
    }
}