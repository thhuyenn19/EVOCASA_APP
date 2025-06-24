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

        // Load ALL vouchers from Firestore
        loadAllVouchers(recyclerView);

        // Handle back button in topbar
        LinearLayout btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    /**
     * Lấy toàn bộ voucher trong collection "Voucher" trên Firestore.
     */
    private void loadAllVouchers(RecyclerView recyclerView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Voucher")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Voucher> list = new ArrayList<>();
                long now = System.currentTimeMillis();
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Voucher v = doc.toObject(Voucher.class);
                    if (v != null) {
                        v.setId(doc.getId());
                        // Bỏ qua voucher hết hạn (nếu muốn)
                        if (v.getExpireDate() == null || v.getExpireDate().toDate().getTime() > now) {
                            list.add(v);
                        }
                    }
                }
                displayVouchers(recyclerView, list);
            })
            .addOnFailureListener(e -> {
                e.printStackTrace();
                android.widget.Toast.makeText(this, "Failed to load vouchers", android.widget.Toast.LENGTH_SHORT).show();
            });
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