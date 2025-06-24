package com.mobile.evocasa;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mobile.adapters.VoucherProfileAdapter;
import com.mobile.models.Voucher;
import com.mobile.utils.UserSessionManager;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class VoucherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerVoucher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load vouchers from Firestore
        loadCustomerVouchers(recyclerView);

        // Handle back button in topbar
        LinearLayout btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadCustomerVouchers(RecyclerView recyclerView) {
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            android.widget.Toast.makeText(this, "User not logged in", android.widget.Toast.LENGTH_SHORT).show();
            displayVouchers(recyclerView, new ArrayList<>());
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        displayVouchers(recyclerView, new ArrayList<>());
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> voucherArray = (List<Map<String, Object>>) doc.get("Voucher");

                    if (voucherArray == null || voucherArray.isEmpty()) {
                        displayVouchers(recyclerView, new ArrayList<>());
                        return;
                    }

                    List<String> voucherIds = new ArrayList<>();
                    for (Map<String, Object> item : voucherArray) {
                        String id = (String) item.get("VoucherId");
                        if (id != null && !id.isEmpty()) voucherIds.add(id);
                    }

                    if (voucherIds.isEmpty()) {
                        displayVouchers(recyclerView, new ArrayList<>());
                    } else {
                        loadVoucherDetails(recyclerView, voucherIds);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    android.widget.Toast.makeText(this, "Failed to load vouchers", android.widget.Toast.LENGTH_SHORT).show();
                    displayVouchers(recyclerView, new ArrayList<>());
                });
    }

    private void loadVoucherDetails(RecyclerView recyclerView, List<String> voucherIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Voucher> vouchers = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        int total = voucherIds.size();

        for (String id : voucherIds) {
            db.collection("Voucher").document(id)
                    .get()
                    .addOnSuccessListener(vDoc -> {
                        if (vDoc.exists()) {
                            Voucher v = vDoc.toObject(Voucher.class);
                            if (v != null) {
                                v.setId(vDoc.getId());
                                vouchers.add(v);
                            }
                        }
                        if (counter.incrementAndGet() == total) {
                            displayVouchers(recyclerView, vouchers);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (counter.incrementAndGet() == total) {
                            displayVouchers(recyclerView, vouchers);
                        }
                    });
        }
    }

    private void displayVouchers(RecyclerView recyclerView, List<Voucher> voucherList) {
        VoucherProfileAdapter adapter = new VoucherProfileAdapter(voucherList, voucher -> {
            android.content.Intent intent = new android.content.Intent(VoucherActivity.this, VoucherDetailActivity.class);

            intent.putExtra("voucher_id", voucher.getId());
            intent.putExtra("voucher_name", voucher.getName());
            intent.putExtra("voucher_discount_percent", voucher.getDiscountPercent());
            intent.putExtra("voucher_max_discount", voucher.getMaxDiscount());
            intent.putExtra("voucher_min_order", voucher.getMinOrderValue());
            long expireMillis = voucher.getExpireDate() != null ? voucher.getExpireDate().toDate().getTime() : -1;
            intent.putExtra("voucher_expire_millis", expireMillis);

            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        if (voucherList.isEmpty()) {
            android.widget.Toast.makeText(this, "No vouchers available", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}