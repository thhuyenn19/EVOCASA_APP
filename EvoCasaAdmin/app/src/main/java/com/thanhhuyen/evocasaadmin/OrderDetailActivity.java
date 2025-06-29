package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.untils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    private TextView tvShippingAddress, tvShippingMethod, tvShippingDate, tvTrackingNumber, tvDeliveryFee;
    private TextView tvVoucherName, tvDiscountPercent, tvDiscountAmount;
    private TextView tvPaymentMethod, tvTotalPrice;

    // Data
    private String trackingNumber;
    private Order currentOrder;
    private DocumentSnapshot rawDocument; // Để debug

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
//        tvShippingDate = findViewById(R.id.tv_shipping_date);
        tvTrackingNumber = findViewById(R.id.tv_tracking_number);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvVoucherName = findViewById(R.id.tv_voucher_name);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvDiscountAmount = findViewById(R.id.tv_discount_amount);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTotalPrice = findViewById(R.id.tv_total_price);
    }

    private void loadOrderDataByTrackingNumber() {
        db.collection("Order")
                .whereEqualTo("TrackingNumber", trackingNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        rawDocument = document; // Lưu raw document để debug

                        // DEBUG: Log raw document data
                        debugRawDocument(document);

                        try {
                            currentOrder = document.toObject(Order.class);
                            if (currentOrder != null) {
                                currentOrder.setOrderId(document.getId());
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

    // DEBUG: Method để log raw document data
    private void debugRawDocument(DocumentSnapshot document) {
        Log.d(TAG, "=== RAW DOCUMENT DEBUG ===");
        Log.d(TAG, "Document ID: " + document.getId());

        Map<String, Object> data = document.getData();
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String valueType = (value != null) ? value.getClass().getSimpleName() : "null";
                Log.d(TAG, "Field: " + key + " = " + value + " (Type: " + valueType + ")");
            }
        }
        Log.d(TAG, "========================");
    }

    private void displayOrderData() {
        if (currentOrder == null) return;

        // DEBUG: Log parsed order data
        debugParsedOrder();

        // Order Header - với fallback từ raw document
        tvOrderId.setText("Order ID: " + currentOrder.getOrderId());
        tvOrderDate.setText("Order Date: " + currentOrder.getFormattedOrderDate());  // Hiển thị Order Date
        tvOrderStatus.setText("Status: " + currentOrder.getStatus());

        // Customer Information
        tvCustomerId.setText("Customer ID: " + getCustomerIdSafe());
        tvCustomerName.setText("Name: " + currentOrder.getShippingName());
        tvCustomerPhone.setText("Phone: " + currentOrder.getShippingPhone());

        // Product Information - với fallback từ raw document
        tvProductId.setText("Product ID: " + getProductIdSafe());
        tvProductQuantity.setText("Quantity: " + getQuantitySafe());
        tvProductPrice.setText("Price: " + currentOrder.getFormattedPrePrice());

        // Shipping Information
        tvShippingAddress.setText("Address: " + currentOrder.getShippingAddress());
        tvShippingMethod.setText("Shipping Method: " + currentOrder.getShippingMethod());
//        tvShippingDate.setText("Ship Date: " + currentOrder.getFormattedShipDate());  // Hiển thị Ship Date
        tvTrackingNumber.setText("Tracking Number: " + currentOrder.getTrackingNumber());
        tvDeliveryFee.setText("Delivery Fee: " + currentOrder.getFormattedDeliveryFee());

        // Voucher Information
        tvVoucherName.setText("Voucher: " + currentOrder.getVoucherName());
        tvDiscountPercent.setText("Discount: " + currentOrder.getDiscountPercent() + "%");
        tvDiscountAmount.setText("Discount Amount: " + currentOrder.getFormattedDiscountAmount());

        // Payment Information
        tvPaymentMethod.setText("Payment Method: " + currentOrder.getPaymentMethod());
        tvTotalPrice.setText("Total Price: " + currentOrder.getFormattedTotalPrice());
    }

    // DEBUG: Method để log parsed order data
    private void debugParsedOrder() {
        Log.d(TAG, "=== PARSED ORDER DEBUG ===");
        Log.d(TAG, "OrderDate from getOrderDate(): " + currentOrder.getOrderDate());
        Log.d(TAG, "ProductId from getProductId(): " + currentOrder.getProductId());
        Log.d(TAG, "Quantity from getQuantity(): " + currentOrder.getQuantity());
        Log.d(TAG, "ShipDate from getFormattedShipDate(): " + currentOrder.getFormattedShipDate());
        Log.d(TAG, "=========================");
    }

    private String getProductIdSafe() {
        // Thử từ parsed order trước
        String productId = currentOrder.getProductId();
        if (productId != null && !productId.equals("N/A") && !productId.isEmpty()) {
            return productId;
        }

        // Fallback: đọc trực tiếp từ raw document
        if (rawDocument != null && rawDocument.getData() != null) {
            Map<String, Object> data = rawDocument.getData();

            // Thử các field name khác nhau
            String[] possibleFields = {"ProductId", "productId", "product_id", "ProductID", "Product_ID"};

            for (String field : possibleFields) {
                if (data.containsKey(field)) {
                    Object value = data.get(field);
                    if (value != null) {
                        Log.d(TAG, "Found ProductId in field: " + field);
                        return value.toString();
                    }
                }
            }
        }

        return "N/A";
    }

    private String getQuantitySafe() {
        // Thử từ parsed order trước
        int quantity = currentOrder.getQuantity();
        if (quantity > 0) {
            return String.valueOf(quantity);
        }

        // Fallback: đọc trực tiếp từ raw document
        if (rawDocument != null && rawDocument.getData() != null) {
            Map<String, Object> data = rawDocument.getData();

            // Thử các field name khác nhau
            String[] possibleFields = {"Quantity", "quantity", "qty", "Qty", "amount", "Amount"};

            for (String field : possibleFields) {
                if (data.containsKey(field)) {
                    Object value = data.get(field);
                    if (value instanceof Number) {
                        Log.d(TAG, "Found Quantity in field: " + field);
                        return String.valueOf(((Number) value).intValue());
                    }
                }
            }
        }

        return "0";
    }


    private String getCustomerIdSafe() {
        // Thử từ parsed order trước
        Map<String, String> customerMap = currentOrder.getCustomer_id();
        if (customerMap != null && customerMap.containsKey("$oid")) {
            String customerId = customerMap.get("$oid");
            if (customerId != null && !customerId.isEmpty()) {
                return customerId;
            }
        }

        // Fallback: đọc trực tiếp từ raw document
        if (rawDocument != null && rawDocument.getData() != null) {
            Map<String, Object> data = rawDocument.getData();

            // Thử các field name khác nhau
            String[] possibleFields = {"Customer_id", "customer_id", "customerId", "CustomerID", "userId", "user_id"};

            for (String field : possibleFields) {
                if (data.containsKey(field)) {
                    Object value = data.get(field);
                    if (value instanceof Map) {
                        Map<?, ?> idMap = (Map<?, ?>) value;
                        if (idMap.containsKey("$oid")) {
                            Object oidValue = idMap.get("$oid");
                            if (oidValue != null) {
                                Log.d(TAG, "Found Customer ID in field: " + field + " with $oid: " + oidValue);
                                return oidValue.toString();
                            }
                        }
                    } else if (value instanceof String) {
                        String stringValue = (String) value;
                        if (!stringValue.isEmpty()) {
                            Log.d(TAG, "Found Customer ID in field: " + field);
                            return stringValue;
                        }
                    }
                }
            }
        }

        return "N/A";
    }

    private String formatDateFromRaw(Object dateObj) {
        if (dateObj == null) return "N/A";

        try {
            if (dateObj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format((Date) dateObj);
            } else if (dateObj instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dateObj;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(timestamp.toDate());
            } else if (dateObj instanceof Map) {
                Map<?, ?> dateMap = (Map<?, ?>) dateObj;

                // Handle Firestore Timestamp format
                if (dateMap.containsKey("_seconds")) {
                    long seconds = Long.parseLong(dateMap.get("_seconds").toString());
                    Date date = new Date(seconds * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return sdf.format(date);
                }

                // Handle other timestamp formats
                if (dateMap.containsKey("$date")) {
                    Object dateValue = dateMap.get("$date");
                    if (dateValue instanceof Number) {
                        Date date = new Date(((Number) dateValue).longValue());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        return sdf.format(date);
                    }
                }
            } else if (dateObj instanceof Number) {
                // Timestamp as number
                Date date = new Date(((Number) dateObj).longValue());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            } else if (dateObj instanceof String) {
                // Try to parse string date
                String dateStr = (String) dateObj;
                if (!dateStr.isEmpty()) {
                    try {
                        // Try different date formats
                        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd HH:mm:ss"};
                        for (String format : formats) {
                            try {
                                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.getDefault());
                                Date date = parser.parse(dateStr);
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                return formatter.format(date);
                            } catch (Exception ignored) {
                                // Try next format
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date string: " + dateStr, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + dateObj, e);
        }

        return "N/A";
    }

    private String formatDate(Object dateObj) {
        return formatDateFromRaw(dateObj);
    }
}
