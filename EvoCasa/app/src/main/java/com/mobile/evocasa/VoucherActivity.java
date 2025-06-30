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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.adapters.VoucherProfileAdapter;
import com.mobile.evocasa.chat.ChatActivity;
import com.mobile.models.Voucher;
import com.mobile.utils.UserSessionManager;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoucherActivity extends AppCompatActivity {
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private TextView txtCartBadge;
    ImageView imgCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher);
        setupBottomNav();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView imgChat = findViewById(R.id.imgChat);

        if (imgChat != null) {
            imgChat.setOnClickListener(v -> {
                Intent intent = new Intent(VoucherActivity.this, ChatActivity.class);
                startActivity(intent);
            });
        }
        sessionManager = new UserSessionManager(this);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        imgCart = findViewById(R.id.imgCart);
        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                Intent intent = new Intent(VoucherActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }
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

    private void setupBottomNav() {
        findViewById(R.id.tabHome).setOnClickListener(v -> goToTab(0));
        startActivity(intent);
        overridePendingTransition(0, 0); // không animation
        finish(); // kết thúc VoucherActivity
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