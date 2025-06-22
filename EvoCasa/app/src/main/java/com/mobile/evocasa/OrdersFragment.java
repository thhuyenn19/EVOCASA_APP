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

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private TextView txtCartBadge;
    ImageView imgCart;
    private ListenerRegistration orderListener;

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
        sessionManager = new UserSessionManager(requireContext());
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);

        // Gán trạng thái đơn hàng
        RecyclerView rvStatus = view.findViewById(R.id.rvOrdersStatus);
        rvStatus.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Nhận trạng thái được truyền từ ProfileFragment
        String selectedStatus = "Pending"; // mặc định
        if (getArguments() != null && getArguments().containsKey("selectedStatus")) {
            selectedStatus = getArguments().getString("selectedStatus");
        }

        // Tạo danh sách trạng thái
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
        listenToOrdersRealtime(selectedStatus);
        // 5) Lọc và hiển thị lần đầu
//        filterOrdersByStatus(selectedStatus);
        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void listenToOrdersRealtime(String selectedStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Product").get().addOnSuccessListener(productSnapshots -> {
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

            String uid = new UserSessionManager(getContext()).getUid();
            if (uid == null || uid.isEmpty()) return;

            if (orderListener != null) orderListener.remove(); // Clear old listener if any

            orderListener = db.collection("Order")
                    .addSnapshotListener((orderSnapshots, error) -> {
                        if (error != null || orderSnapshots == null) {
                            Log.e("OrdersFragment", "Listen error: ", error);
                            return;
                        }

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

                                Long deliveryFee = orderDoc.getLong("DeliveryFee");
                                if (deliveryFee != null) total += deliveryFee;

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
                                Log.e("OrdersFragment", "Error parsing order: " + e.getMessage());
                            }
                        }

                        filterOrdersByStatus(selectedStatus);
                    });
        });
    }


    private void filterOrdersByStatus(String status) {
        List<OrderGroup> filtered = new ArrayList<>();
        for (OrderGroup group : allOrderGroups) {
            if (group.getStatus() != null && group.getStatus().trim().equalsIgnoreCase(status.trim())) {
                group.setExpanded(false);
                filtered.add(group);
            }
        }
        orderGroupAdapter.updateData(filtered);
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

        // Remove existing listener before creating new one
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    // CRITICAL: Check if fragment is still attached AND context is not null
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment not attached, ignoring listener callback");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
                        // Safe update with lifecycle check
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
        // First check: Fragment lifecycle
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.w("CartBadge", "Fragment not attached, cannot update badge");
            return;
        }

        // Second check: View availability
        if (txtCartBadge == null) {
            Log.w("CartBadge", "Cart badge view is null, cannot update");
            return;
        }

        // Use Handler with additional safety check
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            // Triple check: Ensure fragment is still attached when handler executes
            if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
                Log.w("CartBadge", "Fragment detached during handler execution, skip update");
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


    /**
     * Update cart badge display
     */
    private void updateCartBadge(int totalQuantity) {
        // Add comprehensive lifecycle checks
        if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
            Log.w("CartBadge", "Fragment not attached or views null, skip update");
            return;
        }

        // Post to main thread with additional safety check
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            // Double-check lifecycle state in the posted runnable
            if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
                Log.w("CartBadge", "Fragment detached during handler post, skip update");
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

    /**
     * Public method for fragments to refresh cart badge
     */
    public void refreshCartBadge() {
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.d("CartBadge", "Cannot refresh badge - fragment not properly attached");
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "Cannot refresh badge - user not logged in");
            safeUpdateCartBadge(0);
            return;
        }

        Log.d("CartBadge", "Manually refreshing cart badge");

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if fragment is still attached when callback returns
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment detached during refresh, ignoring result");
                        return;
                    }

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
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment detached during refresh error, ignoring");
                        return;
                    }
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    safeUpdateCartBadge(0);
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("CartBadge", "Fragment onStart()");
        if (sessionManager != null && txtCartBadge != null && isAdded()) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CartBadge", "Fragment onResume()");
        // Only start listener if we don't already have one and fragment is properly attached
        if (cartListener == null && isAdded() && getContext() != null &&
                sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("CartBadge", "Fragment onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("CartBadge", "Fragment onStop()");
        cleanupCartListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("CartBadge", "Fragment onDestroyView()");
        cleanupCartListener();
        txtCartBadge = null; // Clear view reference
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CartBadge", "Fragment onDestroy()");
        cleanupCartListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("CartBadge", "Fragment onDetach()");
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