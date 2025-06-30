package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.thanhhuyen.models.Order;
import com.thanhhuyen.untils.FontUtils;

import java.util.ArrayList;

public class CustomerDetailActivity extends AppCompatActivity {
    private static final String TAG = "CustomerDetailActivity";

    private TextView txtTitle;
    private ImageView imgBack;
    private TextView tv_customer_name, tv_customer_gender, tv_customer_email, tv_customer_phone, tv_customer_address;

    private FirebaseFirestore db;
    private String customerId;

    // ✅ Order RecyclerView components
    private RecyclerView recyclerViewOrderDetails;
    private OrderDetailAdapter orderDetailAdapter;
    private ArrayList<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get customerId from Intent
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

        // Font setup
        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }

        tv_customer_name = findViewById(R.id.tv_customer_name);
        tv_customer_gender = findViewById(R.id.tv_customer_gender);
        tv_customer_email = findViewById(R.id.tv_customer_email);
        tv_customer_phone = findViewById(R.id.tv_customer_phone);
        tv_customer_address = findViewById(R.id.tv_customer_address);

        // ✅ Init order RecyclerView
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

    // ✅ Load orders by customerId - Updated to use correct collection name
    private void loadCustomerOrders() {
        if (customerId == null) {
            Log.e(TAG, "CustomerId is null, cannot load orders");
            return;
        }

        Log.d(TAG, "Loading orders for customerId: " + customerId);

        // ✅ Try both collection names since your OrderDetailActivity uses "Order" collection
        db.collection("Order") // Changed from "Orders" to "Order" to match OrderDetailActivity
                .whereEqualTo("Customer_id.$oid", customerId) // Updated field path
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Orders query successful, found " + queryDocumentSnapshots.size() + " orders");
                    orderList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId()); // Set the document ID as order ID
                            orderList.add(order);
                            Log.d(TAG, "Added order: " + order.getOrderId() + " with tracking: " + order.getTrackingNumber());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order document: " + doc.getId(), e);
                        }
                    }

                    orderDetailAdapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        Log.w(TAG, "No orders found for customer: " + customerId);
                        Toast.makeText(this, "No orders found for this customer", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load orders", e);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // ✅ Fallback: try with different field structure
                    tryAlternativeOrderQuery();
                });
    }

    // ✅ Alternative query method if the first one fails
    private void tryAlternativeOrderQuery() {
        Log.d(TAG, "Trying alternative order query methods...");

        // Try different field paths
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
                            return; // Stop trying other paths
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Alternative query failed for path: " + path, e);
                    });
        }
    }
}