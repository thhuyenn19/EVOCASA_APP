package com.mobile.evocasa;

import static androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.adapters.OrderGroupAdapter;
import com.mobile.adapters.OrderStatusAdapter;
import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.models.OrderStatus;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {
    private List<OrderGroup> allOrderGroups;
    private OrderGroupAdapter orderGroupAdapter;


    public OrdersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gán trạng thái đơn hàng
        RecyclerView rvStatus = view.findViewById(R.id.rvOrdersStatus);
        rvStatus.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // ✅ Nhận trạng thái được truyền từ ProfileFragment
        String selectedStatus = "Pending"; // mặc định
        if (getArguments() != null && getArguments().containsKey("selectedStatus")) {
            selectedStatus = getArguments().getString("selectedStatus");
        }

        // ✅ Tạo danh sách trạng thái
        List<OrderStatus> statusList = new ArrayList<>();
        String[] statuses = {"Pending", "Pick Up", "In Transit", "Review", "Completed", "Cancelled"};

        for (String s : statuses) {
            boolean isSelected = s.equals(selectedStatus);
            statusList.add(new OrderStatus(s, isSelected));
        }

        // Gắn adapter cho tab và xử lý click
        OrderStatusAdapter statusAdapter = new OrderStatusAdapter(statusList,
                status -> filterOrdersByStatus(status)
        );
        rvStatus.setAdapter(statusAdapter);

        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
        // 3) Khởi tạo danh sách đơn hàng
        allOrderGroups = new ArrayList<>();
        

// 4) Thiết lập RecyclerView hiển thị đơn hàng
        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderGroupAdapter = new OrderGroupAdapter(new ArrayList<>(), group -> {
            String status = group.getStatus();
            String orderId = group.getOrderId();
            switch (status) {
                case "Pending":
                case "Pick Up":
                case "In Transit":
                    OrderDetailFragment detail = new OrderDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("orderId", orderId);
                    detail.setArguments(bundle);
                    FragmentActivity activity = (FragmentActivity) getContext();
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detail)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "Review":
                    Intent reviewIntent = new Intent(getContext(), LeaveReviewActivity.class);
                    reviewIntent.putExtra("orderId", orderId);
                    startActivity(reviewIntent);
                    break;
                case "Completed":
                    startActivity(new Intent(getContext(), BuyAgainActivity.class));
                    break;
            }

        });


        rvOrders.setAdapter(orderGroupAdapter);

        // ✅ Tự động scroll khi chọn status
        statusAdapter.setOnStatusClickListener(position -> {
            rvStatus.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvStatus.getLayoutManager();
                if (layoutManager != null) {
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                        @Override
                        protected int getHorizontalSnapPreference() {
                            return SNAP_TO_START; // hoặc SNAP_TO_CENTER
                        }

                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return 100f / displayMetrics.densityDpi; // ✅ tăng số này nếu muốn chậm hơn
                        }
                        @Override
                        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                            int viewCenter = (viewStart + viewEnd) / 2;
                            int boxCenter = (boxStart + boxEnd) / 2;
                            return boxCenter - viewCenter;
                        }
                    };
                    smoothScroller.setTargetPosition(position);
                    layoutManager.startSmoothScroll(smoothScroller);

                }
            });
        });
        loadOrdersFromFirestore(selectedStatus);
        // 5) Lọc và hiển thị lần đầu
//        filterOrdersByStatus(selectedStatus);

    }

    private void loadOrdersFromFirestore(String selectedStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Product")
                .get()
                .addOnSuccessListener(productSnapshots -> {
                    Map<String, String> productNameMap = new HashMap<>();
                    Map<String, Long> productPriceMap = new HashMap<>();
                    Map<String, String> productImageMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : productSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("Name");
                        Long price = doc.getLong("Price");

                        Object rawImageData = doc.get("Image");
                        List<String> imageList = new ArrayList<>();

                        if (rawImageData instanceof List) {
                            imageList = (List<String>) rawImageData;
                        } else if (rawImageData instanceof String) {
                            try {
                                imageList = new com.google.gson.Gson().fromJson((String) rawImageData,
                                        new com.google.gson.reflect.TypeToken<List<String>>() {}.getType());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (id != null && name != null && price != null) {
                            productNameMap.put(id, name);
                            productPriceMap.put(id, price);
                            if (!imageList.isEmpty()) {
                                productImageMap.put(id, imageList.get(0));
                            }
                        }
                    }

                    UserSessionManager sessionManager = new UserSessionManager(getContext());
                    String uid = sessionManager.getUid();
                    Log.d("OrdersFragment", "UID hiện tại: " + uid);

                    if (uid == null || uid.isEmpty()) {
                        Log.e("OrdersFragment", "UID is null or empty");
                        return;
                    }

                    db.collection("Order")
                            .get()
                            .addOnSuccessListener(orderSnapshots -> {
                                Log.d("OrdersFragment", "Tổng số order: " + orderSnapshots.size());

                                allOrderGroups.clear();

                                for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                                    try {
                                        Map<String, Object> customerIdMap = (Map<String, Object>) orderDoc.get("Customer_id");
                                        if (customerIdMap == null) continue;

                                        String orderUid = (String) customerIdMap.get("$oid");
                                        if (!uid.equals(orderUid)) continue;

                                        String status = orderDoc.getString("Status");
                                        List<Map<String, Object>> orderProducts =
                                                (List<Map<String, Object>>) orderDoc.get("OrderProduct");
                                        if (orderProducts == null || orderProducts.isEmpty()) continue;

                                        List<OrderItem> itemList = new ArrayList<>();
                                        int total = 0;

                                        for (Map<String, Object> product : orderProducts) {
                                            Map<String, Object> productIdMap = (Map<String, Object>) product.get("id");
                                            if (productIdMap == null) continue;

                                            String productId = (String) productIdMap.get("$oid");
                                            if (productId == null || !productNameMap.containsKey(productId)) continue;

                                            String productName = productNameMap.get(productId);
                                            Long priceEach = productPriceMap.get(productId);
                                            String imageUrl = productImageMap.get(productId);
                                            Long quantity = (Long) product.get("Quantity");

                                            int qty = quantity != null ? quantity.intValue() : 1;
                                            int unitPrice = priceEach != null ? priceEach.intValue() : 0;

                                            total += unitPrice * qty;

                                            itemList.add(new OrderItem(imageUrl, productName, unitPrice, qty));
                                        }

                                        // ✅ Add delivery fee
                                        Long deliveryFee = orderDoc.getLong("DeliveryFee");
                                        if (deliveryFee != null) {
                                            total += deliveryFee.intValue();
                                        }

                                        // ✅ Apply discount percent
                                        Map<String, Object> voucher = (Map<String, Object>) orderDoc.get("Voucher");
                                        if (voucher != null) {
                                            Long discountPercent = (Long) voucher.get("DiscountPercent");
                                            if (discountPercent != null) {
                                                total -= (total * discountPercent.intValue()) / 100;
                                            }
                                        }

                                        OrderGroup group = new OrderGroup(status, itemList);
                                        group.setTotal(total);
                                        group.setOrderId(orderDoc.getId());

                                        allOrderGroups.add(group);

                                    } catch (Exception e) {
                                        Log.e("OrdersFragment", "Parse order error: " + e.getMessage());
                                    }
                                }

                                Log.d("OrdersFragment", "Tổng số OrderGroup sau xử lý: " + allOrderGroups.size());
                                filterOrdersByStatus(selectedStatus);
                            })
                            .addOnFailureListener(e -> Log.e("OrdersFragment", "Lỗi khi lấy Order", e));
                })
                .addOnFailureListener(e -> Log.e("OrdersFragment", "Lỗi khi lấy Product", e));
    }



    private void filterOrdersByStatus(String status) {
        List<OrderGroup> filtered = new ArrayList<>();
        for (OrderGroup group : allOrderGroups) {
            if (group.getStatus().equals(status)) {
                group.setExpanded(false);
                filtered.add(group);
            }
        }
        orderGroupAdapter.updateData(filtered);
    }
}