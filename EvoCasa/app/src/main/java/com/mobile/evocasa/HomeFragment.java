package com.mobile.evocasa;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.mobile.adapters.CategoryAdapter;
import com.mobile.utils.FontUtils;

import com.mobile.adapters.BannerPagerAdapter;
import com.mobile.adapters.CollectionAdapter;
import com.mobile.adapters.FlashSaleAdapter;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.models.Category;
import com.mobile.models.Collection;
import com.mobile.models.FlashSaleProduct;
import com.mobile.models.HotProducts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.utils.UserSessionManager;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerViewCategories;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private List<HotProducts> hotProductList;
    private HotProductsAdapter hotProductsAdapter;

    private View view;
    private List<Integer> imageList;

    private ImageView imgCart;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private EditText edtSearch;
    private ImageView imgMic;

    private ViewPager2 viewPagerBanner;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (viewPagerBanner.getCurrentItem() + 1) % imageList.size();
                viewPagerBanner.setCurrentItem(nextItem, false); // <--- Tắt hiệu ứng trượt ngang
                sliderHandler.postDelayed(this, 4000);
            }
        };

    // Countdown timer for Flash Sale
    private TextView timerHour, timerMinute, timerSecond;
    private Handler countdownHandler = new Handler();
    private Runnable countdownRunnable;
    private long saleEndTimeMillis = 0L; // thời điểm kết thúc đợt Flash Sale hiện tại

    // SharedPreferences keys for Flash Sale caching
    private static final String PREFS_NAME = "flash_sale_prefs";
    private static final String KEY_FS_LIST = "fs_list";
    private static final String KEY_FS_EXPIRES = "fs_expires";
    private SharedPreferences flashPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
       
        db = FirebaseFirestore.getInstance();
        /*Category*/
        // 1. Gán layout cho view
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo SharedPreferences sớm để sử dụng ngay phía dưới
        flashPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Bind countdown timer views (cần trước khi chạy startCountdownTimer)
        timerHour = view.findViewById(R.id.timerHour);
        timerMinute = view.findViewById(R.id.timerMinute);
        timerSecond = view.findViewById(R.id.timerSecond);
        edtSearch = view.findViewById(R.id.edtSearch);



        imgMic = view.findViewById(R.id.imgMic);
        imgMic.setOnClickListener(v -> startVoiceInput());

        // Áp dụng font cho các TextView
        applyCustomFonts();

        // 3. Danh sách dữ liệu
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();

        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));


        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Lấy giờ hiện tại
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Không giới hạn theo khung giờ; mỗi đợt Flash Sale kéo dài 6 tiếng kể từ khi được tạo

        /* FlashSale */
        RecyclerView recyclerViewFlashSale = view.findViewById(R.id.recyclerViewFlashSale);
        recyclerViewFlashSale.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<FlashSaleProduct> flashSaleList = new ArrayList<>();
        FlashSaleAdapter flashSaleAdapter = new FlashSaleAdapter(flashSaleList);
        flashSaleAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        recyclerViewFlashSale.setAdapter(flashSaleAdapter);

        long now = System.currentTimeMillis();
        long expiresAt = flashPrefs.getLong(KEY_FS_EXPIRES, 0L);
        String cachedJson = flashPrefs.getString(KEY_FS_LIST, null);

        if (cachedJson != null && now < expiresAt) {
            // Dùng danh sách đã cache
            List<FlashSaleProduct> cachedList = new Gson().fromJson(cachedJson,
                    new TypeToken<List<FlashSaleProduct>>(){}.getType());
            if (cachedList != null) {
                flashSaleList.addAll(cachedList);
            }
            flashSaleAdapter.notifyDataSetChanged();
            recyclerViewFlashSale.setVisibility(View.VISIBLE);
            saleEndTimeMillis = expiresAt;
            startCountdownTimer();
        } else {
            // Hết hiệu lực hoặc chưa có – random mới
            db = FirebaseFirestore.getInstance();
            db.collection("Product").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                        Collections.shuffle(allDocs);

                        flashSaleList.clear();
                        for (int i = 0; i < Math.min(6, allDocs.size()); i++) {
                            FlashSaleProduct product = allDocs.get(i).toObject(FlashSaleProduct.class);
                            product.setId(allDocs.get(i).getId());
                            flashSaleList.add(product);
                        }

                        flashSaleAdapter.notifyDataSetChanged();
                        recyclerViewFlashSale.setVisibility(View.VISIBLE);

                        // Đặt thời gian kết thúc 3 tiếng kể từ bây giờ
                        saleEndTimeMillis = System.currentTimeMillis() + 3 * 60 * 60 * 1000;

                        String json = new Gson().toJson(flashSaleList);
                        flashPrefs.edit()
                                .putString(KEY_FS_LIST, json)
                                .putLong(KEY_FS_EXPIRES, saleEndTimeMillis)
                                .apply();

                        startCountdownTimer();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi khi tải Flash Sale", Toast.LENGTH_SHORT).show();
                        recyclerViewFlashSale.setVisibility(View.GONE);
                    });
        }

        /* Hot Products */
        RecyclerView recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);
        recyclerViewHotProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        hotProductList = new ArrayList<>();
        hotProductsAdapter = new HotProductsAdapter(hotProductList);
        hotProductsAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        recyclerViewHotProducts.setAdapter(hotProductsAdapter);

        // Gọi hàm load từ Firestore
        loadHotProducts();


        /*Collection*/
        RecyclerView recyclerViewCollections = view.findViewById(R.id.recyclerViewCollections);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCollections.setLayoutManager(layoutManager);

        List<Collection> collectionList = new ArrayList<>();
        collectionList.add(new Collection(R.mipmap.ic_sabi_collection, "Sabi Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_disc_collection, "The Disc Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_pavillon_collection, "The Pavillon Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_bromley_collection, "The Bromley Collection"));

        CollectionAdapter collectionAdapter = new CollectionAdapter(collectionList);
        recyclerViewCollections.setAdapter(collectionAdapter);

        // Khởi tạo ViewPager2 cho Banner
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        imageList = new ArrayList<>();
        imageList.add(R.mipmap.ic_banner);
        imageList.add(R.mipmap.ic_banner2);
        imageList.add(R.mipmap.ic_banner3);
        // Thêm các hình ảnh khác vào đây nếu có

        BannerPagerAdapter bannerAdapter = new BannerPagerAdapter(imageList);
        viewPagerBanner.setAdapter(bannerAdapter);

// Tạo hiệu ứng fade mượt + triệt tiêu hiệu ứng slide ngang
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            // Giữ ảnh đứng yên tại chỗ, không trượt ngang
            page.setTranslationX(0);

            // Fade in – fade out hiệu ứng
            float alpha = 1 - Math.abs(position);
            page.setAlpha(alpha);
        });
        viewPagerBanner.setPageTransformer(transformer);

        viewPagerBanner.setPageTransformer(transformer);

// Bắt đầu tự động trượt
        sliderHandler.postDelayed(sliderRunnable, 4000);

        // Bind countdown timer views
        timerHour = view.findViewById(R.id.timerHour);
        timerMinute = view.findViewById(R.id.timerMinute);
        timerSecond = view.findViewById(R.id.timerSecond);

        // Bắt sự kiện click giỏ hàng (imgCart) => Mở Cart Product
        imgCart = view.findViewById(R.id.imgCart);
        if (imgCart != null) {
            imgCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Mở CartActivity
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }

        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        sessionManager = new UserSessionManager(requireContext());
        startCartBadgeListener();

        return view;
    }

    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak the product name...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Your device does not support speech recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String voiceText = result.get(0);
                edtSearch.setText(voiceText);

                // You can call a search/filter function here
                // performSearch(voiceText);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput(); // Retry after permission granted
            } else {
                Toast.makeText(getContext(), "You need to grant microphone access to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
        countdownHandler.removeCallbacks(countdownRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);

        // Khởi động lại đồng hồ đếm nếu còn thời gian sale
        long nowResume = System.currentTimeMillis();
        if (saleEndTimeMillis > nowResume) {
            startCountdownTimer();
        }
        startCartBadgeListener();
    }

    private void applyCustomFonts() {
        // Áp dụng font Zregular cho tên sản phẩm
        TextView txtProductName = view.findViewById(R.id.txtProductName);
        if (txtProductName != null) {
            FontUtils.setZregularFont(getContext(), txtProductName);
        }
        EditText edtSearch = view.findViewById(R.id.edtSearch);
        if (txtProductName != null) {
            FontUtils.setRegularFont(getContext(), edtSearch);
        }

        TextView tvProductName = view.findViewById(R.id.txtProductName);
        if (tvProductName != null) {
            FontUtils.setZregularFont(getContext(), tvProductName);
        }

        // Áp dụng font Regular cho "See All"
        TextView txtSeeAll = view.findViewById(R.id.txtSeeAll);
        if (txtSeeAll != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAll);
        }
        // Áp dụng font Regular cho "See All"
        TextView txtSeeAllCollection = view.findViewById(R.id.txtSeeAllCollection);
        if (txtSeeAllCollection != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAllCollection);
        }

        TextView txtSeeAllHotProducts = view.findViewById(R.id.txtSeeAllHotProducts);
        if (txtSeeAllHotProducts != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAllHotProducts);
        }

        // Áp dụng font Zbold cho tên collection
        TextView txtCollectionName = view.findViewById(R.id.txtCollectionName);
        if (txtCollectionName != null) {
            FontUtils.setZboldFont(getContext(), txtCollectionName);
        }

        // Áp dụng font Zbold cho tất cả TextView còn lại
        applyZboldFontToAllTextViews(view);
    }

    private void applyZboldFontToAllTextViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyZboldFontToAllTextViews(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextView && !(view instanceof EditText)) {
            TextView textView = (TextView) view;
            int id = textView.getId();
            // Bỏ qua các TextView đã được set font riêng
            if (id != R.id.txtProductName &&
                    id != R.id.txtSeeAll &&
                    id != R.id.txtSeeAllHotProducts &&
                    id != R.id.txtSeeAllCollection &&
                    id != R.id.txtCollectionName &&
                    id != R.id.edtSearch) { // Bỏ qua EditText nếu dùng TextView làm base
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
    private void loadCategories() {
        db.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    categoryList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Category category = doc.toObject(Category.class);
                        categoryList.add(category);
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load danh mục", e);
                });
    }
    private void loadHotProducts() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    hotProductList.clear();

                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
                    Collections.shuffle(allDocs); // 🔀 random

                    int limit = Math.min(6, allDocs.size()); // lấy 6 sản phẩm
                    for (int i = 0; i < limit; i++) {
                        HotProducts product = allDocs.get(i).toObject(HotProducts.class);
                        product.setId(allDocs.get(i).getId());
                        hotProductList.add(product);
                    }

                    hotProductsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
                });
    }

    // ================== Flash Sale Countdown ==================
    private void startCountdownTimer() {
        if (saleEndTimeMillis == 0L) return;

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long remaining = saleEndTimeMillis - System.currentTimeMillis();

                if (remaining > 0) {
                    updateTimerViews(remaining);
                    countdownHandler.postDelayed(this, 1000);
                } else {
                    // Hết giờ Flash Sale – đặt về 00:00:00
                    timerHour.setText("00");
                    timerMinute.setText("00");
                    timerSecond.setText("00");
                    countdownHandler.removeCallbacks(this);
                }
            }
        };

        // Cập nhật lần đầu và bắt đầu lặp
        long initial = saleEndTimeMillis - System.currentTimeMillis();
        updateTimerViews(initial);
        countdownHandler.postDelayed(countdownRunnable, 1000);
    }

    private void updateTimerViews(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (timerHour != null) timerHour.setText(String.format("%02d", hours));
        if (timerMinute != null) timerMinute.setText(String.format("%02d", minutes));
        if (timerSecond != null) timerSecond.setText(String.format("%02d", seconds));
    }

    private TextView txtCartBadge;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    /**
     * Bắt đầu lắng nghe thay đổi trong giỏ hàng và cập nhật badge
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

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
                        if (txtCartBadge != null) txtCartBadge.setVisibility(View.GONE);
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

                        updateCartBadge(totalQuantity);
                    } else {
                        Log.d("CartBadge", "No customer document found");
                        updateCartBadge(0);
                    }
                });
    }


    /**
     * Cập nhật hiển thị cart badge
     */
    private void updateCartBadge(int totalQuantity) {
        // First check: Fragment lifecycle state
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.w("CartBadge", "Fragment not attached, cannot update badge");
            return;
        }

        // Second check: View availability
        if (txtCartBadge == null) {
            Log.e("CartBadge", "txtCartBadge is null!");
            return;
        }

        // Get activity reference safely
        Activity activity = getActivity();
        if (activity == null) {
            Log.w("CartBadge", "Activity is null, cannot update badge");
            return;
        }

        // Use activity reference instead of requireActivity()
        activity.runOnUiThread(() -> {
            // Double-check fragment state in UI thread
            if (!isAdded() || getContext() == null || txtCartBadge == null) {
                Log.w("CartBadge", "Fragment detached during UI update, skipping");
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
            } catch (Exception e) {
                Log.e("CartBadge", "Error updating cart badge UI", e);
            }
        });
    }

    /**
     * Public method để fragments có thể gọi refresh cart badge
     */
    public void refreshCartBadge() {
        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "Cannot refresh badge - user not logged in");
            return;
        }

        Log.d("CartBadge", "Manually refreshing cart badge");

        FirebaseFirestore.getInstance()
                .collection("carts")
                .document(uid)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalQuantity = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Long qty = doc.getLong("quantity");
                        if (qty != null) {
                            totalQuantity += qty;
                        }
                    }
                    updateCartBadge(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    if (txtCartBadge != null) {
                        txtCartBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listener đã được start trong onCreate
    }

    @Override
    public void onStop() {
        super.onStop();
        // Không stop listener ở đây vì chúng ta muốn nghe trong suốt lifecycle của activity
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Chỉ remove listener khi activity bị destroy
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
    }
}