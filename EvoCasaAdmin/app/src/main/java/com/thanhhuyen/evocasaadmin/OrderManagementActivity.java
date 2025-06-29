package com.thanhhuyen.evocasaadmin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thanhhuyen.adapters.OrderAdapter;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.untils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderManagementActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private TextView tvOrderSummary;
    private Map<String, String> customerMap = new HashMap<>();

    private ImageView imgBack;
    private EditText edtSearch;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
    }

    private void initViews() {

        // Back button in topbar
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) imgBack.setOnClickListener(v -> finish());

        // Font setup
        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }
        edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            FontUtils.setRegularFont(this, edtSearch);
        }

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        tvOrderSummary = findViewById(R.id.tvOrderSummary);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(orderList);
        ordersRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadCustomersAndOrders();
    }

    private void loadCustomersAndOrders() {
        db.collection("Customers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    customerMap.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("Name");
                        customerMap.put(id, name);
                    }
                    loadOrders(); // only load orders after customerMap ready
                });
    }

    private void loadOrders() {
        db.collection("Order")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    orderList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);

                            // get customer_id as Map<String, String> and extract "$oid"
                            if (order.getCustomer_id() != null && order.getCustomer_id().get("$oid") != null) {
                                String customerId = order.getCustomer_id().get("$oid");
                                String customerName = customerMap.get(customerId);
                                order.setCustomerName(customerName != null ? customerName : "Unknown");
                            } else {
                                order.setCustomerName("Unknown");
                            }

                            orderList.add(order);
                        }

                        tvOrderSummary.setText(getString(R.string.total_orders_management, orderList.size()));
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}