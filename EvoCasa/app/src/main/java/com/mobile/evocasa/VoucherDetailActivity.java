package com.mobile.evocasa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VoucherDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ==== Nhận dữ liệu từ Intent ====
        android.content.Intent intent = getIntent();
        String voucherId = intent.getStringExtra("voucher_id");
        String name = intent.getStringExtra("voucher_name");
        double discountPercent = intent.getDoubleExtra("voucher_discount_percent", 0);
        double maxDiscount = intent.getDoubleExtra("voucher_max_discount", 0);
        double minOrder = intent.getDoubleExtra("voucher_min_order", 0);
        long expireMillis = intent.getLongExtra("voucher_expire_millis", -1);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

        // ==== Ánh xạ View ====
        android.widget.TextView txtCode = findViewById(R.id.txtVoucherCodeDetail);
        android.widget.TextView txtDesc1 = findViewById(R.id.txtVoucherDesc1Detail);
        android.widget.TextView txtDesc2 = findViewById(R.id.txtVoucherDesc2Detail);
        android.widget.TextView txtExpire = findViewById(R.id.txtVoucherExpireDetail);
        android.widget.TextView btnSave = findViewById(R.id.btnSaveVoucher);

        if (txtCode != null) txtCode.setText(name);
        if (txtDesc1 != null) txtDesc1.setText(String.format(java.util.Locale.getDefault(), "%d%% off Capped at $%.0f", (int) discountPercent, maxDiscount));
        if (txtDesc2 != null) txtDesc2.setText(String.format(java.util.Locale.getDefault(), "Min. Spend $%.0f", minOrder));
        if (txtExpire != null) {
            String exp = expireMillis > 0 ? "Expired: " + sdf.format(new java.util.Date(expireMillis)) : "No Expire Date";
            txtExpire.setText(exp);
        }

        // Khởi tạo trạng thái nút SAVE (đã lưu hay chưa)
        initializeSaveState(voucherId, btnSave);

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveVoucherToCustomer(voucherId, btnSave));
        }

        // ==== Nút back ====
        android.widget.LinearLayout btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    /** Kiểm tra voucher đã tồn tại trong Customer hay chưa để set trạng thái nút */
    private void initializeSaveState(String voucherId, android.widget.TextView btnSave) {
        if (btnSave == null || voucherId == null || voucherId.isEmpty()) return;

        com.mobile.utils.UserSessionManager session = new com.mobile.utils.UserSessionManager(this);
        String uid = session.getUid();
        if (uid == null) return;

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("Customers").document(uid).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            java.util.List<java.util.Map<String, Object>> arr = (java.util.List<java.util.Map<String, Object>>) doc.get("Voucher");
            if (arr != null) {
                for (java.util.Map<String, Object> item : arr) {
                    String id = (String) item.get("VoucherId");
                    if (voucherId.equals(id)) {
                        btnSave.setText("SAVED");
                        btnSave.setEnabled(false);
                        break;
                    }
                }
            }
        });
    }

    /** Lưu voucherId vào Customers */
    private void saveVoucherToCustomer(String voucherId, android.widget.TextView btnSave) {
        if (voucherId == null || voucherId.isEmpty()) {
            android.widget.Toast.makeText(this, "Voucher ID không hợp lệ", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.mobile.utils.UserSessionManager session = new com.mobile.utils.UserSessionManager(this);
        String uid = session.getUid();
        if (uid == null) {
            android.widget.Toast.makeText(this, "Bạn chưa đăng nhập", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        java.util.Map<String, Object> entry = new java.util.HashMap<>();
        entry.put("VoucherId", voucherId);

        db.collection("Customers").document(uid)
                .update("Voucher", com.google.firebase.firestore.FieldValue.arrayUnion(entry))
                .addOnSuccessListener(a -> {
                    android.widget.Toast.makeText(this, "Voucher saved", android.widget.Toast.LENGTH_SHORT).show();
                    if (btnSave != null) {
                        btnSave.setText("SAVED");
                        btnSave.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, "Save failed", android.widget.Toast.LENGTH_SHORT).show());
    }
}