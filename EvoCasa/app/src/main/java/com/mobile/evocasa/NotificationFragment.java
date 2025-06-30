package com.mobile.evocasa;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.mobile.adapters.NotificationAdapter;
import com.mobile.evocasa.chat.ChatActivity;
import com.mobile.models.NotificationItem;
import com.mobile.utils.FontUtils;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import android.Manifest;


public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView txtCartBadge;
    private ImageView imgCart;
    private List<NotificationItem> items = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration cartListener;
    private UserSessionManager session;
    private List<Map<String, Object>> originalNotificationData = new ArrayList<>();
    private ListenerRegistration orderStatusListener;


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
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        session = new UserSessionManager(requireContext());
        ImageView imgChat= view.findViewById(R.id.imgChat);
        imgChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            startActivity(intent);
        });
        // Bắt sự kiện nút "Mark all as read"
        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            txtMarkAllRead.setOnClickListener(v -> {
                // 1. Cập nhật trạng thái trong adapter
                adapter.markAllAsRead();

                // 2. Cập nhật trạng thái trong danh sách lưu để ghi xuống Firestore
                for (Map<String, Object> noti : originalNotificationData) {
                    noti.put("Status", "Read");
                }

                // 3. Ghi lại mảng vào Firestore
                String customerId = session.getUid();
                db.collection("Customers")
                        .document(customerId)
                        .update("Notifications", originalNotificationData)
                        .addOnSuccessListener(unused -> Log.d(TAG, "All notifications marked as read."))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update notifications", e));
            });

        }
        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
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
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed", e);
                        return;
                    }

                    if (doc != null && doc.exists() && doc.contains("Notifications")) {
                        List<Map<String, Object>> notiList =
                                (List<Map<String, Object>>) doc.get("Notifications");

                        Log.d(TAG, "Fetched " + notiList.size() + " notifications from snapshot.");
                        originalNotificationData = new ArrayList<>(notiList);

                        items.clear();
                        String lastDate = "";
                        Collections.sort(notiList, (a, b) -> {
                            try {
                                Date dateA = parseDate(a.get("CreatedAt"));
                                Date dateB = parseDate(b.get("CreatedAt"));
                                return dateB.compareTo(dateA);  // Descending
                            } catch (Exception ex) {
                                return 0;
                            }
                        });

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
                                } catch (Exception ex) {
                                    Log.e(TAG, "Parse CreatedAt failed", ex);
                                }
                            }

                            int iconRes = getIconForType(type);
                            if (iconRes == -1) continue;

                            String dateKey = formatDateGroup(ts);
                            if (!dateKey.equals(lastDate)) {
                                items.add(new NotificationItem(dateKey));
                                lastDate = dateKey;
                            }

                            boolean isRead = "Read".equalsIgnoreCase(status);
                            String timeStr = formatTime(ts);

                            items.add(new NotificationItem(iconRes, title, content, timeStr, isRead));
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "No Notifications field found.");
                    }
                });
    }

    private Date parseDate(Object obj) {
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toDate();
        } else if (obj instanceof String) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse((String) obj);
            } catch (Exception e) {
                Log.e(TAG, "parseDate failed for string: " + obj, e);
            }
        }
        return new Date(0); // fallback
    }


    private int getIconForType(String type) {
        if (type == null) return -1; // icon không hợp lệ
        switch (type) {
            case "Pending":
            return R.drawable.ic_pending_noti;
            case "Pick Up":
                return R.drawable.ic_pick_up_noti;
            case "In Transit":
                return R.drawable.ic_in_transit_noti;
            case "OrderDelivered":
                return R.drawable.ic_order_delivered;
            case "Review":
                return R.drawable.ic_review_noti;
            case "Cancelled":
                return R.drawable.ic_order_cancelled;
            case "CompleteYourPayment":
                return R.drawable.ic_complete_payment;
            case "PaymentConfirmed":
                return R.drawable.ic_payment_confirmed;
            default:
                return -1;
        }
    }




    private String formatDateGroup(Timestamp ts) {
        Date date = ts.toDate();

        // Lấy thời điểm hiện tại và reset giờ
        Calendar todayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        // Lấy ngày của notification
        Calendar notiCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(date);
        }
    }


    private String formatTime(Timestamp ts) {
        Date date = ts.toDate();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }


    private void applyCustomFonts(View view) {

        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            FontUtils.setRegularFont(getContext(), txtMarkAllRead);
        }
    }
    // CartBadge
    /**
     * Start listening for cart changes and update badge
     */
    private void startCartBadgeListener() {
        String uid = session.getUid();

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

        String uid = session.getUid();
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
        if (session != null && txtCartBadge != null && isAdded()) {
            startCartBadgeListener();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CartBadge", "Fragment onResume()");
        // Only start listener if we don't already have one and fragment is properly attached
        if (cartListener == null && isAdded() && getContext() != null &&
                session != null && txtCartBadge != null) {
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
