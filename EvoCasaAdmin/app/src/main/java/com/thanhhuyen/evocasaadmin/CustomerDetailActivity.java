package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thanhhuyen.adapters.OrderDetailAdapter;
import com.thanhhuyen.adapters.ShippingAddressAdapter;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.models.ShippingAddress;
import com.thanhhuyen.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerDetailActivity extends AppCompatActivity {
    private static final String TAG = "CustomerDetailActivity";

    private TextView txtTitle;
    private ImageView imgBack;
    private TextView tv_customer_name, tv_customer_gender, tv_customer_email, tv_customer_phone, tv_customer_address, txtShippingAddress;

    private TextView tv_total_orders, tv_total_amount;

    private FirebaseFirestore db;
    private String customerId;

    private RecyclerView recyclerViewOrderDetails;
    private OrderDetailAdapter orderDetailAdapter;
    private ArrayList<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("customerId")) {
            customerId = intent.getStringExtra("customerId");
            Log.d(TAG, "Received customerId: " + customerId);
        }

        initViews();

        loadCustomerDetails();
        loadCustomerOrders();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerDetailActivity.this, CustomerManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }

        tv_customer_name = findViewById(R.id.tv_customer_name);
        tv_customer_gender = findViewById(R.id.tv_customer_gender);
        tv_customer_email = findViewById(R.id.tv_customer_email);
        tv_customer_phone = findViewById(R.id.tv_customer_phone);
        tv_customer_address = findViewById(R.id.tv_customer_address);

        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        if (txtShippingAddress != null) {
            txtShippingAddress.setPaintFlags(txtShippingAddress.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            txtShippingAddress.setOnClickListener(v -> {
                showShippingAddressesPopup();
            });
        }

        tv_total_orders = findViewById(R.id.tv_total_orders);
        tv_total_amount = findViewById(R.id.tv_total_amount);

        recyclerViewOrderDetails = findViewById(R.id.recyclerViewOrderDetails);
        recyclerViewOrderDetails.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(this, orderList);
        recyclerViewOrderDetails.setAdapter(orderDetailAdapter);
    }

    private void loadCustomerDetails() {
        if (customerId == null) {
            Log.e(TAG, "CustomerId is null");
            return;
        }

        DocumentReference customerRef = db.collection("Customers").document(customerId);
        customerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                String gender = documentSnapshot.getString("Gender");
                String email = documentSnapshot.getString("Mail");
                String phone = documentSnapshot.getString("Phone");
                String address = documentSnapshot.getString("Address");

                tv_customer_name.setText(name != null ? name : "N/A");
                tv_customer_gender.setText("Gender: " + (gender != null ? gender : "N/A"));
                tv_customer_email.setText("Email: " + (email != null ? email : "N/A"));
                tv_customer_phone.setText("Phone: " + (phone != null ? phone : "N/A"));
                tv_customer_address.setText("Address: " + (address != null ? address : "N/A"));
            } else {
                Log.w(TAG, "Customer document not found");
                Toast.makeText(this, "Customer not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading customer details", e);
            Toast.makeText(this, "Failed to load customer details", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCustomerOrders() {
        if (customerId == null) {
            Log.e(TAG, "CustomerId is null, cannot load orders");
            return;
        }

        Log.d(TAG, "Loading orders for customerId: " + customerId);

        db.collection("Order")
                .whereEqualTo("Customer_id.$oid", customerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Orders query successful, found " + queryDocumentSnapshots.size() + " orders");
                    orderList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                            Log.d(TAG, "Added order: " + order.getOrderId() + " with tracking: " + order.getTrackingNumber());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order document: " + doc.getId(), e);
                        }
                    }

                    orderDetailAdapter.notifyDataSetChanged();
                    updateOrderSummary();

                    if (orderList.isEmpty()) {
                        Log.w(TAG, "No orders found for customer: " + customerId);
                        Toast.makeText(this, "No orders found for this customer", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load orders", e);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    tryAlternativeOrderQuery();
                });
    }

    private void tryAlternativeOrderQuery() {
        Log.d(TAG, "Trying alternative order query methods...");

        String[] possiblePaths = {
                "customer_id",
                "Customer_id",
                "customerId",
                "CustomerID"
        };

        for (String path : possiblePaths) {
            db.collection("Order")
                    .whereEqualTo(path, customerId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "Alternative query successful with path: " + path);
                            orderList.clear();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                try {
                                    Order order = doc.toObject(Order.class);
                                    order.setOrderId(doc.getId());
                                    orderList.add(order);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing order document in alternative query", e);
                                }
                            }

                            orderDetailAdapter.notifyDataSetChanged();
                            updateOrderSummary();
                            return;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Alternative query failed for path: " + path, e);
                    });
        }
    }

    private void updateOrderSummary() {
        int totalOrders = orderList.size();
        double totalAmount = 0.0;

        for (Order order : orderList) {
            if (order != null) {
                totalAmount += order.getTotalPrice();
            }
        }

        if (tv_total_orders != null) {
            tv_total_orders.setText(String.valueOf(totalOrders));
        }

        if (tv_total_amount != null) {
            tv_total_amount.setText(String.format(Locale.getDefault(), "$%,.0f", totalAmount));
        }

        Log.d(TAG, "Order summary updated - Total Orders: " + totalOrders + ", Total Amount: $" + totalAmount);
    }

    private void showShippingAddressesPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_shipping_addresses, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(popupView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerShippingAddresses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("Customers").document(customerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> addressListMap = (List<Map<String, Object>>) documentSnapshot.get("ShippingAddresses");

                        List<ShippingAddress> addressList = new ArrayList<>();
                        if (addressListMap != null) {
                            for (Map<String, Object> map : addressListMap) {
                                ShippingAddress address = new ShippingAddress();
                                address.setName((String) map.get("Name"));
                                address.setPhone((String) map.get("Phone"));
                                address.setAddress((String) map.get("Address"));
                                address.setDefault(Boolean.TRUE.equals(map.get("IsDefault")));
                                addressList.add(address);
                            }
                        }

                        ShippingAddressAdapter adapter = new ShippingAddressAdapter(addressList);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load shipping addresses", Toast.LENGTH_SHORT).show();
                    Log.e("ShippingPopup", "Error: ", e);
                });
    }
}
