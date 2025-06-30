package com.mobile.evocasa;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.category.ProductPreloadManager;
import com.mobile.evocasa.category.ShopFragment;
import com.mobile.evocasa.order.OrderDetailFragment;
import com.mobile.evocasa.payment.FinishPaymentFragment;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.Category;
import com.mobile.models.ProductItem;
import com.mobile.utils.UserSessionManager;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NarBarActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavSelectedListener {
    public static List<Category> preloadedCategories = new ArrayList<>();
    public static CategoryShopAdapter categoryAdapter;
    private UserSessionManager session;
    private ListenerRegistration orderStatusListener;


    private BottomNavFragment bottomNavFragment; // Khai báo như instance variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nar_bar);
        preloadProductDataInBackground();
        session = new UserSessionManager(this);
        startOrderStatusListener();

        int tabPos = getIntent().getIntExtra("tab_pos", 0);

        String orderId = getIntent().getStringExtra("orderId");
        boolean fromDirectIntent = getIntent().getBooleanExtra("from_direct", false); // flag từ FinishPayment

        if (orderId != null) {
            // Mở OrderDetailFragment nếu có orderId
            OrderDetailFragment orderDetailFragment = new OrderDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            orderDetailFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, orderDetailFragment)
                    .commit();

            tabPos = 5; // Chọn tab tương ứng
        } else {
            // Nếu không có orderId thì load tab thông thường
            showFragment(tabPos);

            if (!fromDirectIntent) {
                PopupDialog popupDialog = new PopupDialog();
                popupDialog.setOnShopClickListener(this::navigateToShop);
                popupDialog.show(getSupportFragmentManager(), "popup");
            }
        }


        // Truyền tabPos vào BottomNavFragment và lưu reference
        bottomNavFragment = BottomNavFragment.newInstance(tabPos);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_nav_container, bottomNavFragment)
                .commit();


    }

    private void startOrderStatusListener() {
        String uid = session.getUid();
        if (uid == null || uid.isEmpty()) return;

        orderStatusListener = FirebaseFirestore.getInstance()
                .collection("Order")
                .whereEqualTo("Customer_id.$oid", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("OrderStatus", "Listen failed", e);
                        return;
                    }

                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            DocumentSnapshot doc = dc.getDocument();
                            String orderId = doc.getId();
                            String newStatus = doc.getString("Status");

                            // Lưu ý: Để tránh duplicate noti, có thể so sánh với local cache hoặc lưu giá trị cũ trong Notifications

                            sendStatusChangeNotification(orderId, newStatus);
                        }
                    }
                });
    }

    private void sendStatusChangeNotification(String orderId, String status) {
        String title = "Order Update";
        String content = "Your order #" + orderId + " status changed to " + status;
        String type = status; // vì icon theo type

        int iconRes = getIconForType(type);
        if (iconRes == -1) iconRes = R.drawable.ic_pending_noti; // fallback

        // Gửi noti native
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel_id")
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCompat.from(this).notify(new Random().nextInt(), builder.build());
        } else {
            Log.w("Notification", "No POST_NOTIFICATIONS permission. Skip push.");
        }


        // Gửi noti vào Firestore Notifications array
        Map<String, Object> notification = new HashMap<>();
        String notiId = "NOTI" + System.currentTimeMillis();
        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
        notification.put("NotificationId", notiId);
        notification.put("CreatedAt", createdAt);
        notification.put("Title", "Order status updated");
        notification.put("Content", "Order #" + orderId + " is now " + status);
        notification.put("Image", "/images/Notification/OrderPlaced.jpg");
        notification.put("Status", "Unread");
        notification.put("Type", status);

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(session.getUid())
                .update("Notifications", FieldValue.arrayUnion(notification));
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "order_channel_id";
            CharSequence name = "Order Notifications";
            String description = "Notifications related to your orders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void preloadProductDataInBackground() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ProductPreloadManager.getInstance().setApplicationContext(this.getApplicationContext());

        executor.execute(() -> {
            ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();
            preloadManager.preloadAllCategoryDataBlocking(); // ← preload toàn bộ categories
            Log.d("NarBarActivity", "Preload toàn bộ category xong");
        });
        executor.execute(() -> {
            String userId = new UserSessionManager(this).getUid();
            if (userId != null) {
                ProductPreloadManager.getInstance().preloadWishlistForUser(userId, () -> {
                    Log.d("Wishlist", "Wishlist cache loaded: " + ProductPreloadManager.getInstance().getCachedWishlist().size());
                });
            }
        });
        executor.execute(() -> {
            ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();
            List<ProductItem> allProducts = preloadManager.getShopAllProducts();

            for (ProductItem product : allProducts) {
                String imageJson = product.getImage();
                if (imageJson != null) {
                    try {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        List<String> imageUrls = gson.fromJson(imageJson, listType);

                        if (imageUrls != null) {
                            for (String url : imageUrls) {
                                String trimmedUrl = url.trim();
                                if (!preloadManager.isImagePreloaded(trimmedUrl)) {
                                    Glide.with(getApplicationContext())
                                            .load(trimmedUrl)
                                            .preload();
                                    preloadManager.markImageAsPreloaded(trimmedUrl);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("NarBarActivity", "Error parsing/preloading image: " + e.getMessage());
                    }
                }
            }

            Log.d("NarBarActivity", "Preloaded product images into Glide cache");
        });
    }
    public static List<Category> getStaticCategoryList() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(R.mipmap.ic_category_furniture, "Shop All"));
        categories.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categories.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categories.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categories.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categories.add(new Category(R.mipmap.ic_category_art, "Art"));
        categories.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));
        return categories;
    }

    private void navigateToShop() {
        // Chuyển đến ShopFragment
        showFragment(1);

        // Cập nhật bottom navigation để highlight tab Shop
        if (bottomNavFragment != null) {
            bottomNavFragment.setSelectedPosition(1);
        }
    }

    @Override
    public void onBottomNavSelected(int position) {
        showFragment(position);
    }

    private void showFragment(int pos) {
        Fragment frag;
        switch (pos) {
            case 1:
                ShopFragment shopFragment = new ShopFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("preloadedCategories", new ArrayList<>(getStaticCategoryList())); // truyền list vào
                shopFragment.setArguments(bundle);
                frag = shopFragment;
                break;
            case 2:
                frag = new NotificationFragment();
                break;
            case 3:
                frag = new ProfileFragment();
                break;
            case 4:
                frag = new FinishPaymentFragment();
                break;
            case 5:
                frag = new OrderDetailFragment();
                break;
            default:
                frag = new HomeFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
    }


}