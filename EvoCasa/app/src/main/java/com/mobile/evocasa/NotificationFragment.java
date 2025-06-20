package com.mobile.evocasa;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mobile.adapters.NotificationAdapter;
import com.mobile.models.NotificationItem;
import com.mobile.utils.FontUtils;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> items = new ArrayList<>();
    private FirebaseFirestore db;
    private UserSessionManager session;


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

        adapter = new NotificationAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        session = new UserSessionManager(getContext());

        // Bắt sự kiện nút "Mark all as read"
        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            txtMarkAllRead.setOnClickListener(v -> adapter.markAllAsRead());
        }

        loadNotifications();

        return view;
    }

    private void loadNotifications() {

        String customerId = session.getUid();
        Log.d(TAG, "Customer ID: " + customerId);

        if (customerId == null) {
            Log.w(TAG, "Customer ID is null, cannot load notifications.");
            return;
        }

        db.collection("Customers")
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("Notifications")) {
                        List<Map<String, Object>> notiList = (List<Map<String, Object>>) doc.get("Notifications");
                        Log.d(TAG, "Fetched " + notiList.size() + " notifications from array.");

                        items.clear();
                        String lastDate = "";

                        for (Map<String, Object> noti : notiList) {
                            String title = (String) noti.get("Title");
                            String content = (String) noti.get("Content");
                            String status = (String) noti.get("Status");
                            String type = (String) noti.get("Type");

                            Timestamp ts = null;
                            Object createdAtRaw = noti.get("CreatedAt");
                            if (createdAtRaw instanceof Timestamp) {
                                ts = (Timestamp) createdAtRaw;
                            } else if (createdAtRaw instanceof String) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    Date date = sdf.parse((String) createdAtRaw);
                                    ts = new Timestamp(date);
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to parse CreatedAt string: " + createdAtRaw, e);
                                }
                            }

                            Log.d(TAG, "Noti: title=" + title + ", type=" + type + ", status=" + status);

                            int iconRes = getIconForType(type);
                            if (iconRes == -1) {
                                Log.w(TAG, "Invalid type: " + type + ", skipping");
                                continue;
                            }

                            String dateKey = formatDateGroup(ts);

// ❗ Chỉ thêm header nếu noti hợp lệ và ngày khác
                            if (!dateKey.equals(lastDate)) {
                                items.add(new NotificationItem(dateKey));
                                lastDate = dateKey;
                            }

                            boolean isRead = "Read".equalsIgnoreCase(status);
                            String timeStr = formatTime(ts);

// Thêm notification sau header
                            items.add(new NotificationItem(iconRes, title, content, timeStr, isRead));


                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "No Notifications field found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load notifications", e);
                    Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                });
    }
        private int getIconForType(String type) {
        if (type == null) return -1; // icon không hợp lệ
        switch (type) {
            case "OrderDelivered":
                return R.drawable.ic_order_delivered;
            case "CompleteYourPayment":
                return R.drawable.ic_complete_payment;
            case "PaymentConfirmed":
                return R.drawable.ic_payment_confirmed;
            case "OrderCancelled":
                return R.drawable.ic_order_cancelled;
            default:
                return -1; // icon không xác định, sẽ bị bỏ qua
        }
    }

    private String formatDateGroup(Timestamp ts) {
        Date date = ts.toDate();

        // Lấy thời điểm hiện tại và reset giờ
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        // Lấy ngày của notification
        Calendar notiCal = Calendar.getInstance();
        notiCal.setTime(date);
        notiCal.set(Calendar.HOUR_OF_DAY, 0);
        notiCal.set(Calendar.MINUTE, 0);
        notiCal.set(Calendar.SECOND, 0);
        notiCal.set(Calendar.MILLISECOND, 0);

        long diff = todayCal.getTimeInMillis() - notiCal.getTimeInMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        if (diff == 0) {
            return "Today";
        } else if (diff == oneDay) {
            return "Yesterday";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return sdf.format(date);
        }
    }


    private String formatTime(Timestamp ts) {
        Date date = ts.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        return sdf.format(date);
    }

    private void applyCustomFonts(View view) {

        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            FontUtils.setRegularFont(getContext(), txtMarkAllRead);
        }
    }
}
