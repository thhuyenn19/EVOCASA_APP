package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.NotificationAdapter;
import com.mobile.models.NotificationItem;
import com.mobile.utils.FontUtils;
import com.mobile.evocasa.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Áp dụng font cho TextView
        applyCustomFonts(view);

        // Khởi tạo RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewNotification);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo danh sách NotificationItem
        List<NotificationItem> items = new ArrayList<>();
        items.add(new NotificationItem("Today"));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Order Delivered",
                "Order 250421T3W123 is completed. Your feedback matters!", "11:10", false));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Complete Your Payment",
                "Your order of 239.000 VND has not been paid.", "08:50", true));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Complete Your Payment",
                "Your order of 239.000 VND has not been paid.", "08:50", true));
        items.add(new NotificationItem("Yesterday"));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Payment Confirmed",
                "Payment for order 250417T4W506 has been confirmed.", "07:30", false));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Payment Confirmed",
                "Payment for order 250417T4W506 has been confirmed.", "07:30", false));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Order Delivered",
                "Order 250421T3W123 is completed. Your feedback matters!", "11:10", true));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Order Delivered",
                "Order 250421T3W123 is completed. Your feedback matters!", "11:10", true));

        // Gắn Adapter
        adapter = new NotificationAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);

        // Bắt sự kiện nút "Mark all as read"
        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            txtMarkAllRead.setOnClickListener(v -> adapter.markAllAsRead());
        }

        return view;
    }

    private void applyCustomFonts(View view) {
        TextView txtNotificationDate = view.findViewById(R.id.txtNotificationDate);
        if (txtNotificationDate != null) {
            FontUtils.setRegularFont(getContext(), txtNotificationDate);
        }

        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            FontUtils.setRegularFont(getContext(), txtMarkAllRead);
        }
    }
}
