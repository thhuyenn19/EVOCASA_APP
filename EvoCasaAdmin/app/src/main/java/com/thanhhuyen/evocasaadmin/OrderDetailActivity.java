package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.utils.FontUtils;

import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String TAG = "OrderDetailActivity";

    private ImageView imgBack;
    private TextView txtTitle;

    // Firestore
    private FirebaseFirestore db;

    // UI components
    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private TextView tvCustomerId, tvCustomerName, tvCustomerPhone;
    private TextView tvProductId, tvProductQuantity, tvProductPrice;
    private TextView tvShippingAddress, tvShippingMethod, tvTrackingNumber, tvDeliveryFee;
    private TextView tvVoucherName, tvDiscountPercent, tvDiscountAmount;
    private TextView tvPaymentMethod, tvTotalPrice;

    // Status update views
    private TextView tvPending, tvPickup, tvInTransit, tvReview, btnSaveStatus;
    private String selectedStatus = "";

    // Data
    private String trackingNumber;
    private Order currentOrder;
    private DocumentSnapshot rawDocument;
    private String productName = ""; // Để lưu tên sản phẩm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        db = FirebaseFirestore.getInstance();

        trackingNumber = getIntent().getStringExtra("trackingNumber");
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            Toast.makeText(this, "Tracking number not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDataByTrackingNumber();
        setupStatusTapLogic(); // setup logic tap status
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }

        // Initialize UI components
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvCustomerId = findViewById(R.id.tv_customer_id);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvProductId = findViewById(R.id.tv_product_id);
        tvProductQuantity = findViewById(R.id.tv_product_quantity);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        tvShippingMethod = findViewById(R.id.tv_shipping_method);
        tvTrackingNumber = findViewById(R.id.tv_tracking_number);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvVoucherName = findViewById(R.id.tv_voucher_name);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvDiscountAmount = findViewById(R.id.tv_discount_amount);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        // Status update views
        tvPending = findViewById(R.id.tv_pending);
        tvPickup = findViewById(R.id.tv_pickup);
        tvInTransit = findViewById(R.id.tv_in_transit);
        tvReview = findViewById(R.id.tv_review);
        btnSaveStatus = findViewById(R.id.btn_save_status);
    }

    private void setupStatusTapLogic() {
        tvPending.setOnClickListener(v -> {
            selectedStatus = "Pending";
            setSelectedStatusUI(selectedStatus);
        });

        tvPickup.setOnClickListener(v -> {
            selectedStatus = "Pick Up";
            setSelectedStatusUI(selectedStatus);
        });

        tvInTransit.setOnClickListener(v -> {
            selectedStatus = "In Transit";
            setSelectedStatusUI(selectedStatus);
        });

        tvReview.setOnClickListener(v -> {
            selectedStatus = "Review";
            setSelectedStatusUI(selectedStatus);
        });

        btnSaveStatus.setOnClickListener(v -> {
            if (!selectedStatus.isEmpty() && currentOrder != null) {
                db.collection("Order")
                        .document(currentOrder.getOrderId())
                        .update("Status", selectedStatus)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(OrderDetailActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                            tvOrderStatus.setText("Status: " + selectedStatus); // update UI
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrderDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void setSelectedStatusUI(String status) {
        // Reset all
        tvPending.setBackgroundResource(R.drawable.bg_status_unselected);
        tvPickup.setBackgroundResource(R.drawable.bg_status_unselected);
        tvInTransit.setBackgroundResource(R.drawable.bg_status_unselected);
        tvReview.setBackgroundResource(R.drawable.bg_status_unselected);

        tvPending.setTextColor(Color.parseColor("#5E4C3E"));
        tvPickup.setTextColor(Color.parseColor("#5E4C3E"));
        tvInTransit.setTextColor(Color.parseColor("#5E4C3E"));
        tvReview.setTextColor(Color.parseColor("#5E4C3E"));

        // Set selected
        switch (status) {
            case "Pending":
                tvPending.setBackgroundResource(R.drawable.bg_status_selected);
                tvPending.setTextColor(Color.WHITE);
                break;
            case "Pick Up":
                tvPickup.setBackgroundResource(R.drawable.bg_status_selected);
                tvPickup.setTextColor(Color.WHITE);
                break;
            case "In Transit":
                tvInTransit.setBackgroundResource(R.drawable.bg_status_selected);
                tvInTransit.setTextColor(Color.WHITE);
                break;
            case "Review":
                tvReview.setBackgroundResource(R.drawable.bg_status_selected);
                tvReview.setTextColor(Color.WHITE);
                break;
        }
    }

    private void loadOrderDataByTrackingNumber() {
        db.collection("Order")
                .whereEqualTo("TrackingNumber", trackingNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        rawDocument = document;

                        try {
                            currentOrder = document.toObject(Order.class);
                            if (currentOrder != null) {
                                currentOrder.setOrderId(document.getId());
                                selectedStatus = currentOrder.getStatus();
                                setSelectedStatusUI(selectedStatus);
                                displayOrderData();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order data", e);
                            Toast.makeText(this, "Error loading order data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Order not found with tracking number: " + trackingNumber);
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayOrderData() {
        if (currentOrder == null) return;

        tvOrderId.setText("Order ID: " + currentOrder.getOrderId());
        tvOrderDate.setText(currentOrder.getFormattedOrderDate());
        tvOrderStatus.setText("Status: " + currentOrder.getStatus());
        tvCustomerId.setText(getCustomerIdSafe());
        tvCustomerName.setText(currentOrder.getShippingName());
        tvCustomerPhone.setText(currentOrder.getShippingPhone());
        tvProductId.setText(getProductDisplayName());
        tvProductQuantity.setText(getQuantitySafe());
        tvProductPrice.setText("PrePrice: " + currentOrder.getFormattedPrePrice());
        tvShippingAddress.setText(currentOrder.getShippingAddress());
        tvShippingMethod.setText(currentOrder.getShippingMethod());
        tvTrackingNumber.setText(currentOrder.getTrackingNumber());
        tvDeliveryFee.setText(currentOrder.getFormattedDeliveryFee());
        tvVoucherName.setText(currentOrder.getVoucherName());
        tvDiscountPercent.setText("Discount Percent: " + currentOrder.getDiscountPercent() + "%");
        tvDiscountAmount.setText("Discount Amount: " + currentOrder.getFormattedDiscountAmount());
        tvPaymentMethod.setText(currentOrder.getPaymentMethod());
        tvTotalPrice.setText(currentOrder.getFormattedTotalPrice());
    }

    private String getProductDisplayName() {
        if (productName != null && !productName.trim().isEmpty() && !productName.equals("N/A")) {
            return productName;
        }
        String productId = getProductIdFromOrderProduct();
        return "ID: " + productId;
    }

    private String getProductIdFromOrderProduct() {
        if (rawDocument == null || rawDocument.getData() == null) {
            return "N/A";
        }

        Map<String, Object> data = rawDocument.getData();
        if (data.containsKey("OrderProduct")) {
            Object orderProductObj = data.get("OrderProduct");
            if (orderProductObj instanceof List) {
                List<?> orderProductList = (List<?>) orderProductObj;
                if (!orderProductList.isEmpty() && orderProductList.get(0) instanceof Map) {
                    Map<?, ?> firstProduct = (Map<?, ?>) orderProductList.get(0);
                    if (firstProduct.containsKey("id")) {
                        Object idObj = firstProduct.get("id");
                        if (idObj instanceof String) {
                            return (String) idObj;
                        }
                    }
                }
            }
        }

        return "N/A";
    }

    private String getQuantitySafe() {
        if (rawDocument == null || rawDocument.getData() == null) {
            return "0";
        }

        Map<String, Object> data = rawDocument.getData();
        if (data.containsKey("OrderProduct")) {
            Object orderProductObj = data.get("OrderProduct");
            if (orderProductObj instanceof List) {
                List<?> orderProductList = (List<?>) orderProductObj;
                if (!orderProductList.isEmpty() && orderProductList.get(0) instanceof Map) {
                    Map<?, ?> firstProduct = (Map<?, ?>) orderProductList.get(0);
                    if (firstProduct.containsKey("Quantity")) {
                        Object quantityObj = firstProduct.get("Quantity");
                        if (quantityObj instanceof Number) {
                            return String.valueOf(((Number) quantityObj).intValue());
                        }
                    }
                }
            }
        }

        return "0";
    }

    private String getCustomerIdSafe() {
        Map<String, String> customerMap = currentOrder.getCustomer_id();
        if (customerMap != null && customerMap.containsKey("$oid")) {
            String customerId = customerMap.get("$oid");
            if (customerId != null && !customerId.isEmpty()) {
                return customerId;
            }
        }
        return "N/A";
    }
}
