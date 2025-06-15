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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mobile.adapters.OrderGroupAdapter;
import com.mobile.adapters.OrderStatusAdapter;
import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.models.OrderStatus;

import java.util.ArrayList;
import java.util.List;

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
        allOrderGroups.add(new OrderGroup("Pending", mockItems("Pending")));
        allOrderGroups.add(new OrderGroup("Pick Up", mockItems("Pick Up")));
        allOrderGroups.add(new OrderGroup("In Transit", mockItems("In Transit")));
        allOrderGroups.add(new OrderGroup("Review", mockItems("Review")));
        allOrderGroups.add(new OrderGroup("Completed", mockItems("Completed")));
        allOrderGroups.add(new OrderGroup("Cancelled", mockItems("Cancelled")));

// 4) Thiết lập RecyclerView hiển thị đơn hàng
        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderGroupAdapter = new OrderGroupAdapter(new ArrayList<>(),  group -> {
            OrderDetailFragment detail = new OrderDetailFragment();
            String status = group.getStatus();
            switch (status) {
                case "Pending":
                case "Pick Up":
                case "In Transit":
                    FragmentActivity activity = (FragmentActivity) getContext();
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detail)
                            .addToBackStack(null)    // để back về list được
                            .commit();
                    break;
                case "Review":
                    startActivity(new Intent(getContext(), LeaveReviewActivity.class));
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
        // 5) Lọc và hiển thị lần đầu
        filterOrdersByStatus(selectedStatus);

    }
    private List<OrderItem> mockItems(String status) {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(R.mipmap.ic_cart_product,  "Product A", 1000, 1));
        items.add(new OrderItem(R.mipmap.ic_cart_product, "Product B", 1200, 2));
        return items;
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