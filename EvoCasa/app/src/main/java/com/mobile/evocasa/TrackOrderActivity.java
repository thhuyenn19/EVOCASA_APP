package com.mobile.evocasa;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.TimelineAdapter;
import com.mobile.models.EventItem;
import com.mobile.models.HeaderItem;
import com.mobile.models.TimelineItem;
import com.mobile.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.mobile.utils.UserSessionManager;

public class TrackOrderActivity extends AppCompatActivity {
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

        txtTrackingNumber = findViewById(R.id.txtTrackingNumber);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtEstimatedDelivery = findViewById(R.id.txtEstimatedDelivery);

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

    }

    private void loadOrderTrackingInfo(String orderId, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Order").document(orderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) return;

            Map<String, Object> customerIdMap = (Map<String, Object>) orderDoc.get("Customer_id");
            if (customerIdMap == null) return;

            String orderUid = (String) customerIdMap.get("$oid");
            if (!uid.equals(orderUid)) return;

            // ✅ Tracking Number
            String trackingNumber = orderDoc.getString("TrackingNumber");
            txtTrackingNumber.setText(trackingNumber != null ? trackingNumber : "N/A");

            // ✅ Order ID
            txtOrderId.setText(orderId);

            // ✅ Order Date
            Map<String, Object> orderDateMap = (Map<String, Object>) orderDoc.get("OrderDate");
            if (orderDateMap != null && orderDateMap.get("$date") != null) {
                String rawDate = (String) orderDateMap.get("$date");
                txtOrderDate.setText(formatDate(rawDate));

                // ✅ Estimated Delivery = OrderDate + 4 days
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
}