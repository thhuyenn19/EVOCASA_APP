package com.mobile.evocasa;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // ✅ Gắn adapter và xử lý chọn tab
        OrderStatusAdapter adapter = new OrderStatusAdapter(statusList, new OrderStatusAdapter.OnStatusClickListener() {
            @Override
            public void onStatusSelected(String status) {
                filterOrdersByStatus(status); // gọi filter dữ liệu đơn hàng theo status
            }
        });
        rvStatus.setAdapter(adapter);

        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        // ✅ Gọi filter cho tab ban đầu
        filterOrdersByStatus(selectedStatus);
        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        List<OrderGroup> groupList = new ArrayList<>();
        groupList.add(new OrderGroup("Pending", mockItems()));
        groupList.add(new OrderGroup("Review", mockItems()));
        groupList.add(new OrderGroup("Completed", mockItems()));

        OrderGroupAdapter orderadapter = new OrderGroupAdapter(groupList);
        rvOrders.setAdapter(orderadapter);

    }
    private List<OrderItem> mockItems() {
        List<OrderItem> list = new ArrayList<>();
        list.add(new OrderItem(R.mipmap.ic_cart_product, "Travertine Table Lamp", 3500, 1));
        list.add(new OrderItem(R.mipmap.ic_cart_product, "Ceramic Mug", 1500, 2));
        return list;
    }

    // Bạn cần định nghĩa hàm này để lọc đơn hàng theo status
    private void filterOrdersByStatus(String status) {
        // TODO: lọc đơn hàng và cập nhật RecyclerView danh sách đơn
    }

}