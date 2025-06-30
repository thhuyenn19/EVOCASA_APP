package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.evocasa.chat.ChatActivity;
import com.mobile.evocasa.order.TrackOrderActivity;
import com.mobile.utils.UserSessionManager;

import java.util.List;
import java.util.Map;

public class VoucherDetailActivity extends AppCompatActivity {
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private TextView txtCartBadge;
    ImageView imgCart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher_detail);
        setupBottomNav(); // Gắn sự kiện cho bottom nav
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView imgChat = findViewById(R.id.imgChat);

        if (imgChat != null) {
            imgChat.setOnClickListener(v -> {
                Intent intent = new Intent(VoucherDetailActivity.this, ChatActivity.class);
                startActivity(intent);
            });
        }
        sessionManager = new UserSessionManager(this);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        imgCart = findViewById(R.id.imgCart);
        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                Intent intent = new Intent(VoucherDetailActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }


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

    private void setupBottomNav() {
        findViewById(R.id.tabHome).setOnClickListener(v -> goToTab(0));
        findViewById(R.id.tabShop).setOnClickListener(v -> goToTab(1));
        findViewById(R.id.tabNotification).setOnClickListener(v -> goToTab(2));
        findViewById(R.id.tabProfile).setOnClickListener(v -> goToTab(3));
    }

    private void goToTab(int tabPos) {
        Intent intent = new Intent(VoucherDetailActivity.this, NarBarActivity.class);
        intent.putExtra("tab_pos", tabPos);
        startActivity(intent);
        overridePendingTransition(0, 0); // không animation
        finish(); // kết thúc VoucherActivity
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
    // CartBadge
    /**
     * Start listening for cart changes and update badge
     */
    private void startCartBadgeListener() {
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "User not logged in, hiding badge");
            if (txtCartBadge != null) {
                txtCartBadge.setVisibility(View.GONE);
            }
            return;
        }

        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (isFinishing() || isDestroyed()) {
                        Log.d("CartBadge", "Activity finishing or destroyed, skipping");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
                        safeUpdateCartBadge(0);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                        int totalQuantity = 0;

                        if (cartList != null) {
                            for (Map<String, Object> item : cartList) {
                                Object qtyObj = item.get("cartQuantity");
                                if (qtyObj instanceof Number) {
                                    totalQuantity += ((Number) qtyObj).intValue();
                                }
                            }
                        }

                        safeUpdateCartBadge(totalQuantity);
                    } else {
                        Log.d("CartBadge", "No customer document found");
                        safeUpdateCartBadge(0);
                    }
                });
    }

    private void safeUpdateCartBadge(int totalQuantity) {
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        if (txtCartBadge == null) {
            Log.w("CartBadge", "Cart badge view is null, cannot update");
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            if (isFinishing() || isDestroyed() || txtCartBadge == null) {
                Log.d("CartBadge", "Activity finishing or destroyed, skipping");
                return;
            }

            try {
                if (totalQuantity > 0) {
                    txtCartBadge.setVisibility(View.VISIBLE);
                    String displayText = totalQuantity >= 100 ? "99+" : String.valueOf(totalQuantity);
                    txtCartBadge.setText(displayText);
                    Log.d("CartBadge", "Badge updated: " + displayText);
                } else {
                    txtCartBadge.setVisibility(View.GONE);
                    Log.d("CartBadge", "Badge hidden (quantity = 0)");
                }
            } catch (Exception ex) {
                Log.e("CartBadge", "Error updating cart badge UI", ex);
            }
        });
    }

    private void updateCartBadge(int totalQuantity) {
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (isFinishing() || isDestroyed() || txtCartBadge == null) {
                Log.d("CartBadge", "Activity finishing or destroyed, skipping");
                return;
            }

            if (totalQuantity > 0) {
                txtCartBadge.setVisibility(View.VISIBLE);
                String displayText = totalQuantity >= 100 ? "99+" : String.valueOf(totalQuantity);
                txtCartBadge.setText(displayText);
                Log.d("CartBadge", "Badge updated: " + displayText);
            } else {
                txtCartBadge.setVisibility(View.GONE);
                Log.d("CartBadge", "Badge hidden (quantity = 0)");
            }
        });
    }

    public void refreshCartBadge() {
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "Cannot refresh badge - user not logged in");
            safeUpdateCartBadge(0);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isFinishing() || isDestroyed()) return;

                    int totalQuantity = 0;
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                        if (cartList != null) {
                            for (Map<String, Object> item : cartList) {
                                Object qtyObj = item.get("cartQuantity");
                                if (qtyObj instanceof Number) {
                                    totalQuantity += ((Number) qtyObj).intValue();
                                }
                            }
                        }
                    }
                    safeUpdateCartBadge(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    safeUpdateCartBadge(0);
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CartBadge", "Activity onStart()");
        if (sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CartBadge", "Activity onResume()");
        if (cartListener == null && sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupCartListener();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CartBadge", "Activity onStop()");
        cleanupCartListener();
    }

    private void cleanupCartListener() {
        if (cartListener != null) {
            Log.d("CartBadge", "Removing cart listener");
            cartListener.remove();
            cartListener = null;
        }
    }
}