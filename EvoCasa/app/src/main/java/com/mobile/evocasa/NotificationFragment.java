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
        items.add(new NotificationItem("TODAY"));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Order Delivered",
                "Order 250421T3W123 is completed. Your feedback matters to others! Rate the products by 26-05-2025 and get up 200 coins.", " Today 11:10", false));
        items.add(new NotificationItem(R.drawable.ic_complete_payment, "Complete Your Payment",
                "Your order of 239.000 VND has not been paid. Please complete your payment by  20-05-2025 18:50. Ignore this message if you've already paid.", "Today 08:50", true));
        items.add(new NotificationItem(R.drawable.ic_payment_confirmed, "Payment Confirmed",
                "Payment for order 250417T4W506 has been confirmed. Kindly wait for your shipment and be ready to receive order in the next 3 days.", " Today 07:30", true));
        items.add(new NotificationItem("YESTERDAY"));
        items.add(new NotificationItem(R.drawable.ic_order_cancelled, "Order Cancelled",
                "Order 250416T7W124 has been cancelled by Evocasa System. Apologies for any inconvenience caused.", "Yesterday 22:05", false));
        items.add(new NotificationItem(R.drawable.ic_order_delivered, "Order  Delivered",
                "Order 250410T9W302 is completed. Your feedback matters to others! Rate the products by 25-05-2025 and get up 200 coins.", "Yesterday 21:45", false));
        items.add(new NotificationItem(R.drawable.ic_complete_payment, "Complete Your Payment",
                "Your order of 850.000 VND has not been paid. Please complete your payment by  20-05-2025 13:50. Ignore this message if you've already paid.", "Yesterday 07:30", true));
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

        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            FontUtils.setRegularFont(getContext(), txtMarkAllRead);
        }
    }
}
