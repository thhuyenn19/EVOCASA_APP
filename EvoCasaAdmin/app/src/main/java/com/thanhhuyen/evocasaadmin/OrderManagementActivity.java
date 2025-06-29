package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thanhhuyen.adapters.OrderAdapter;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.untils.FontUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private View btnFilter; // filter button view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
    }

    private void initViews() {

        // Back button in topbar -> navigate to MainActivity
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> {
                Intent intent = new Intent(OrderManagementActivity.this, MainActivity.class);
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
        edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            FontUtils.setRegularFont(this, edtSearch);
        }

        // Filter button setup
        btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
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

    private void showFilterDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_order, null);
        bottomSheetDialog.setContentView(view);

        // Setup buttons in BottomSheet
        TextView txtSortDate = view.findViewById(R.id.txtSortDate);
        TextView txtSortPriceAsc = view.findViewById(R.id.txtSortPriceAsc);
        TextView txtSortPriceDesc = view.findViewById(R.id.txtSortPriceDesc);

        txtSortDate.setOnClickListener(v -> {
            sortByDateDescending();
            bottomSheetDialog.dismiss();
        });

        txtSortPriceAsc.setOnClickListener(v -> {
            sortByPriceAscending();
            bottomSheetDialog.dismiss();
        });

        txtSortPriceDesc.setOnClickListener(v -> {
            sortByPriceDescending();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }


    private void sortByDateDescending() {
        Collections.sort(orderList, (o1, o2) -> {
            String date1 = o1.getOrderDate() != null ? o1.getOrderDate().get("$date") : "";
            String date2 = o2.getOrderDate() != null ? o2.getOrderDate().get("$date") : "";
            return date2.compareTo(date1); // newest first
        });
        adapter.notifyDataSetChanged();
    }

    private void sortByPriceAscending() {
        Collections.sort(orderList, Comparator.comparingInt(Order::getTotalPrice));
        adapter.notifyDataSetChanged();
    }

    private void sortByPriceDescending() {
        Collections.sort(orderList, (o1, o2) -> Integer.compare(o2.getTotalPrice(), o1.getTotalPrice()));
        adapter.notifyDataSetChanged();
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
