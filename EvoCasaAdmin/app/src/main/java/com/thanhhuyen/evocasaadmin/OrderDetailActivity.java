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
import java.util.List;
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
    }

    private void loadOrderDataByTrackingNumber() {
        db.collection("Order")
                .whereEqualTo("TrackingNumber", trackingNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        rawDocument = document;

                        // DEBUG: Log raw document data
                        debugRawDocument(document);

                        try {
                            currentOrder = document.toObject(Order.class);
                            if (currentOrder != null) {
                                currentOrder.setOrderId(document.getId());
                                // Load product name từ OrderProduct array
                                loadProductNameFromOrderProduct();
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

    private void loadProductNameFromOrderProduct() {
        String productId = getProductIdFromOrderProduct();

        if (productId == null || productId.equals("N/A") || productId.isEmpty()) {
            Log.w(TAG, "No valid product ID found in OrderProduct");
            displayOrderData(); // Hiển thị data mà không có tên sản phẩm
            return;
        }

        Log.d(TAG, "Loading product name for ID: " + productId);

        // Tham chiếu đến collection Product để lấy tên sản phẩm
        db.collection("Product")
                .document(productId)
                .get()
                .addOnCompleteListener(productTask -> {
                    if (productTask.isSuccessful() && productTask.getResult().exists()) {
                        DocumentSnapshot productDoc = productTask.getResult();

                        // Thử các field name có thể có cho tên sản phẩm
                        String[] possibleNameFields = {"Name", "name", "ProductName", "product_name", "title", "Title"};

                        for (String field : possibleNameFields) {
                            if (productDoc.contains(field)) {
                                String name = productDoc.getString(field);
                                if (name != null && !name.trim().isEmpty()) {
                                    productName = name.trim();
                                    Log.d(TAG, "Found product name: " + productName + " in field: " + field);
                                    break;
                                }
                            }
                        }

                        if (productName.isEmpty()) {
                            Log.w(TAG, "Product name not found in any field for ID: " + productId);
                            productName = "Product ID: " + productId; // Fallback
                        }
                    } else {
                        Log.w(TAG, "Product document not found for ID: " + productId);
                        productName = "Product ID: " + productId; // Fallback
                    }

                    // Hiển thị data với tên sản phẩm đã load
                    displayOrderData();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading product data for ID: " + productId, e);
                    productName = "Product ID: " + productId; // Fallback
                    displayOrderData();
                });
    }

    private String getProductIdFromOrderProduct() {
        if (rawDocument == null || rawDocument.getData() == null) {
            return "N/A";
        }

        Map<String, Object> data = rawDocument.getData();

        // Kiểm tra OrderProduct array
        if (data.containsKey("OrderProduct")) {
            Object orderProductObj = data.get("OrderProduct");

            if (orderProductObj instanceof List) {
                List<?> orderProductList = (List<?>) orderProductObj;

                if (!orderProductList.isEmpty() && orderProductList.get(0) instanceof Map) {
                    Map<?, ?> firstProduct = (Map<?, ?>) orderProductList.get(0);

                    // Thử tìm ID trong các cấu trúc khác nhau
                    if (firstProduct.containsKey("id")) {
                        Object idObj = firstProduct.get("id");

                        if (idObj instanceof Map) {
                            Map<?, ?> idMap = (Map<?, ?>) idObj;
                            if (idMap.containsKey("$oid")) {
                                String productId = idMap.get("$oid").toString();
                                Log.d(TAG, "Found ProductId in OrderProduct->id->$oid: " + productId);
                                return productId;
                            }
                        } else if (idObj instanceof String) {
                            String productId = (String) idObj;
                            Log.d(TAG, "Found ProductId in OrderProduct->id: " + productId);
                            return productId;
                        }
                    }

                    // Thử các field name khác
                    String[] possibleIdFields = {"productId", "ProductId", "product_id", "_id"};
                    for (String field : possibleIdFields) {
                        if (firstProduct.containsKey(field)) {
                            Object value = firstProduct.get(field);
                            if (value != null) {
                                if (value instanceof Map) {
                                    Map<?, ?> idMap = (Map<?, ?>) value;
                                    if (idMap.containsKey("$oid")) {
                                        String productId = idMap.get("$oid").toString();
                                        Log.d(TAG, "Found ProductId in OrderProduct->" + field + "->$oid: " + productId);
                                        return productId;
                                    }
                                } else {
                                    String productId = value.toString();
                                    Log.d(TAG, "Found ProductId in OrderProduct->" + field + ": " + productId);
                                    return productId;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fallback: thử tìm ProductId ở root level
        String[] rootFields = {"ProductId", "productId", "product_id", "ProductID", "Product_ID"};
        for (String field : rootFields) {
            if (data.containsKey(field)) {
                Object value = data.get(field);
                if (value != null) {
                    Log.d(TAG, "Found ProductId in root field: " + field);
                    return value.toString();
                }
            }
        }

        return "N/A";
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

        // Order Header
        tvOrderId.setText("Order ID: " + currentOrder.getOrderId());
        tvOrderDate.setText(currentOrder.getFormattedOrderDate());
        tvOrderStatus.setText("Status: " + currentOrder.getStatus());

        // Customer Information
        tvCustomerId.setText(getCustomerIdSafe());
        tvCustomerName.setText(currentOrder.getShippingName());
        tvCustomerPhone.setText(currentOrder.getShippingPhone());

        // Product Information - hiển thị tên sản phẩm thay vì ID
        tvProductId.setText(getProductDisplayName());
        tvProductQuantity.setText(getQuantitySafe());
        tvProductPrice.setText("PrePrice: " + currentOrder.getFormattedPrePrice());

        // Shipping Information
        tvShippingAddress.setText(currentOrder.getShippingAddress());
        tvShippingMethod.setText(currentOrder.getShippingMethod());
        tvTrackingNumber.setText(currentOrder.getTrackingNumber());
        tvDeliveryFee.setText(currentOrder.getFormattedDeliveryFee());

        // Voucher Information
        tvVoucherName.setText(currentOrder.getVoucherName());
        tvDiscountPercent.setText("Discount Percent: " + currentOrder.getDiscountPercent() + "%");
        tvDiscountAmount.setText("Discount Amount: " + currentOrder.getFormattedDiscountAmount());

        // Payment Information
        tvPaymentMethod.setText(currentOrder.getPaymentMethod());
        tvTotalPrice.setText(currentOrder.getFormattedTotalPrice());
    }

    private String getProductDisplayName() {
        // Ưu tiên hiển thị tên sản phẩm nếu có
        if (productName != null && !productName.trim().isEmpty() && !productName.equals("N/A")) {
            return productName;
        }

        // Fallback về Product ID
        String productId = getProductIdFromOrderProduct();
        return "ID: " + productId;
    }

    // DEBUG: Method để log parsed order data
    private void debugParsedOrder() {
        Log.d(TAG, "=== PARSED ORDER DEBUG ===");
        Log.d(TAG, "OrderDate from getOrderDate(): " + currentOrder.getOrderDate());
        Log.d(TAG, "ProductId from OrderProduct: " + getProductIdFromOrderProduct());
        Log.d(TAG, "ProductName loaded: " + productName);
        Log.d(TAG, "Quantity from OrderProduct: " + getQuantitySafe());
        Log.d(TAG, "ShipDate from getFormattedShipDate(): " + currentOrder.getFormattedShipDate());
        Log.d(TAG, "=========================");
    }

    private String getQuantitySafe() {
        if (rawDocument == null || rawDocument.getData() == null) {
            return "0";
        }

        Map<String, Object> data = rawDocument.getData();

        // Kiểm tra OrderProduct array
        if (data.containsKey("OrderProduct")) {
            Object orderProductObj = data.get("OrderProduct");

            if (orderProductObj instanceof List) {
                List<?> orderProductList = (List<?>) orderProductObj;

                if (!orderProductList.isEmpty() && orderProductList.get(0) instanceof Map) {
                    Map<?, ?> firstProduct = (Map<?, ?>) orderProductList.get(0);

                    if (firstProduct.containsKey("Quantity")) {
                        Object quantityObj = firstProduct.get("Quantity");
                        if (quantityObj instanceof Number) {
                            Log.d(TAG, "Found Quantity in OrderProduct array: " + quantityObj);
                            return String.valueOf(((Number) quantityObj).intValue());
                        }
                    }
                }
            }
        }

        // Fallback: thử tìm ở root level
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
}