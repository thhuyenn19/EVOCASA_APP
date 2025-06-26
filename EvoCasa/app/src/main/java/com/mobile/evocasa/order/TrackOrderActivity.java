package com.mobile.evocasa.order;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.adapters.TimelineAdapter;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.R;
import com.mobile.models.EventItem;
import com.mobile.models.HeaderItem;
import com.mobile.models.TimelineItem;
import com.mobile.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.mobile.utils.UserSessionManager;

public class TrackOrderActivity extends AppCompatActivity {
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private TextView txtCartBadge;
    ImageView imgCart;
    private TextView txtTrackingNumber, txtOrderId,txtOrderDate,txtEstimatedDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        applyCustomFonts();
        sessionManager = new UserSessionManager(this);

        txtTrackingNumber = findViewById(R.id.txtTrackingNumber);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtEstimatedDelivery = findViewById(R.id.txtEstimatedDelivery);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        imgCart = findViewById(R.id.imgCart);

        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                Intent intent = new Intent(TrackOrderActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        String orderId = getIntent().getStringExtra("orderId");
        String uid = new UserSessionManager(this).getUid();

        if (orderId != null && uid != null && !uid.isEmpty()) {
            loadOrderTrackingInfo(orderId, uid);
        }

        // 1. Chuẩn bị dữ liệu timeline
        List<TimelineItem> timelineItems = new ArrayList<>();

        timelineItems.add(new HeaderItem("23rd, May"));
        timelineItems.add(new EventItem("16:30", "Your order has arrived at the delivery station", true));
        timelineItems.add(new EventItem("09:30", "Your order has left the sorting facility", false));

        timelineItems.add(new HeaderItem("22nd, May"));
        timelineItems.add(new EventItem("16:30", "Your order has arrived at shipping center", false));
        timelineItems.add(new EventItem("09:30", "Your order has been sent to shipping party", false));

        timelineItems.add(new HeaderItem("21st, May"));
        timelineItems.add(new EventItem("16:30", "Your order is being prepared", false));
        timelineItems.add(new EventItem("09:30", "Your order is placed", false));


        // 2. Khởi tạo RecyclerView và Adapter
        RecyclerView rv = findViewById(R.id.rvTimeline);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TimelineAdapter(this, timelineItems));

        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->{
            finish();
        });
        int currentStep = 1;

// Step title TextViews
        TextView[] stepTitles = {
                findViewById(R.id.tvPickedUp),
                findViewById(R.id.tvInTransit),
                findViewById(R.id.tvOutDeli),
                findViewById(R.id.tvDeli)
        };

// Set màu text cố định (đậm)
        for (TextView tv : stepTitles) {
            tv.setTextColor(ContextCompat.getColor(this, R.color.color_active)); // #3F2305
        }

// Step icons
        ImageView[] stepIcons = {
                findViewById(R.id.iconStep1),
                findViewById(R.id.iconStep2),
                findViewById(R.id.iconStep3),
                findViewById(R.id.iconStep4)
        };

// Connecting lines
        View[] lines = {
                findViewById(R.id.line1),
                findViewById(R.id.line2),
                findViewById(R.id.line3)
        };

// Apply color and icon based on current step
        for (int i = 0; i < stepIcons.length; i++) {
            boolean isActive = i <= currentStep;

            stepIcons[i].setImageResource(isActive
                    ? R.drawable.ic_check_circle_active
                    : R.drawable.ic_check_circle_inactive);
        }

        for (int i = 0; i < lines.length; i++) {
            boolean isLineActive = i < currentStep;
            lines[i].setBackgroundColor(ContextCompat.getColor(this,
                    isLineActive ? R.color.color_active : R.color.color_inactive));
        }
        startCartBadgeListener();

    }

    private void loadOrderTrackingInfo(String orderId, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Order").document(orderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) return;

            Map<String, Object> customerIdMap = (Map<String, Object>) orderDoc.get("Customer_id");
            if (customerIdMap == null) return;

            String orderUid = (String) customerIdMap.get("$oid");
            if (!uid.equals(orderUid)) return;

            // Tracking Number
            String trackingNumber = orderDoc.getString("TrackingNumber");
            txtTrackingNumber.setText(trackingNumber != null ? trackingNumber : "N/A");

            // Order ID
            txtOrderId.setText(orderId);

            // Order Date
            Map<String, Object> orderDateMap = (Map<String, Object>) orderDoc.get("OrderDate");
            if (orderDateMap != null && orderDateMap.get("$date") != null) {
                String rawDate = (String) orderDateMap.get("$date");
                txtOrderDate.setText(formatDate(rawDate));

                // Estimated Delivery = OrderDate + 4 days
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date orderDate = sdf.parse(rawDate);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(orderDate);
                    cal.add(Calendar.DAY_OF_MONTH, 4);

                    SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                    txtEstimatedDelivery.setText(out.format(cal.getTime()));
                } catch (Exception e) {
                    txtEstimatedDelivery.setText("N/A");
                }
            } else {
                txtOrderDate.setText("N/A");
                txtEstimatedDelivery.setText("N/A");
            }
        });
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(isoDate);

            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            return out.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void applyCustomFonts() {
        int[] textViewIds = {
                R.id.tvOrderId,
                R.id.tvOrderDate,
                R.id.tvEstimatedDelivery,

        };
        int[] textViewTimeIds = {
                R.id.tvPickedUp,
                R.id.tvInTransit,
                R.id.tvDeli,
                R.id.tvOutDeli
        };

        for (int id : textViewIds) {
            TextView textView = findViewById(id);
            if (textView != null) {
                FontUtils.setSemiBoldFont(this, textView);
            }
        }
        for (int id : textViewTimeIds) {
            TextView textView = findViewById(id);
            if (textView != null) {
                FontUtils.setMediumFont(this, textView);
            }

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