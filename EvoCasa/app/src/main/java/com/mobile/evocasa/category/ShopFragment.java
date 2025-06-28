package com.mobile.evocasa.category;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.R;
import com.mobile.evocasa.search.SearchActivity;
import com.mobile.models.Category;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;
import com.mobile.utils.GridSpacingItemDecoration;
import com.mobile.evocasa.category.ProductPreloadManager;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";
    private static final int PRELOAD_DELAY_MS = 1000; // Delay to avoid overwhelming Firebase
    private static final int CACHE_STATUS_LOG_DELAY_MS = 5000;

    // UI Components
    private RecyclerView recyclerViewCategories;
    private CategoryShopAdapter adapter;
    private View view;
    private TextView txtCartBadge;
    private ImageView imgCart, imgSearch;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Data & Firebase
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private Map<String, String> categoryNameToIdMap;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private List<Category> preloadedCategories;


    // Threading & Caching
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ProductPreloadManager preloadManager;
    private final AtomicBoolean isPreloadStarted = new AtomicBoolean(false);
    private final AtomicBoolean isFragmentActive = new AtomicBoolean(false);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shop, container, false);
        if (getArguments() != null) {
            preloadedCategories = (List<Category>) getArguments().getSerializable("preloadedCategories");
        }
        initializeComponents();
        setupClickListeners();
        applyCustomFonts();

        // Start data loading with proper error handling
        loadInitialData();
        if (preloadedCategories != null && !preloadedCategories.isEmpty()) {
            setupRecyclerView(); // ← Gọi ngay lập tức để hiển thị luôn
            showLoading(false);
        }
        return view;
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        imgSearch = view.findViewById(R.id.imgSearch);
        progressBar = view.findViewById(R.id.progressBar); // Add to layout if needed
//        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout); // Add to layout if needed

        sessionManager = new UserSessionManager(requireContext());
        categoryNameToIdMap = new HashMap<>();
        preloadManager = ProductPreloadManager.getInstance();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        }
    }

    private void setupClickListeners() {
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> navigateToCart());
        }

        if (imgSearch != null) {
            imgSearch.setOnClickListener(v -> navigateToSearch());
        }
    }

    private void navigateToCart() {
        if (isFragmentSafe()) {
            Intent intent = new Intent(requireContext(), CartActivity.class);
            startActivity(intent);
        }
    }

    private void navigateToSearch() {
        if (isFragmentSafe()) {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            intent.putExtra("openProgress", true);
            startActivity(intent);
        }
    }

    private void loadInitialData() {
        showLoading(true);
        // Nếu đã có preloaded thì không cần fetch lại
        if (preloadedCategories != null && !preloadedCategories.isEmpty()) {
            Log.d(TAG, "✅ Using preloaded categories, skipping fetch");
            return;
        }

        // Tiếp tục gọi Firebase để lấy ID phục vụ việc điều hướng sau
        fetchCategoryIds(); // nhưng không cần chờ xong mới render
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing data...");
        isPreloadStarted.set(false);
        preloadManager.clearCache(); // Add this method to ProductPreloadManager
        loadInitialData();
    }

    private void setupRecyclerView() {
        if (!isFragmentSafe()) return;

        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);

        // Nếu categoryList đã được gán trước thì không gán lại nữa
        if (categoryList == null || categoryList.isEmpty()) {
            if (preloadedCategories != null && !preloadedCategories.isEmpty()) {
                categoryList = preloadedCategories;
            } else {
                categoryList = createCategoryList(); // fallback nếu preload null
            }
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == categoryList.size() - 1) ? 2 : 1;
            }
        });
        recyclerViewCategories.setLayoutManager(layoutManager);

        int spacingInPixels = (int) (17 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        recyclerViewCategories.setClipToPadding(false);
        recyclerViewCategories.setHasFixedSize(true);

        if (adapter == null) {
            adapter = new CategoryShopAdapter(getContext(), categoryList, this::onCategorySelected);
            recyclerViewCategories.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged(); // nếu adapter đã có thì chỉ cần update
        }

        // Bắt đầu preload data (nếu chưa preload)
        mainHandler.postDelayed(this::startPreloadingData, PRELOAD_DELAY_MS);
    }

    private List<Category> createCategoryList() {
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

    private void onCategorySelected(Category category) {
        if (!isFragmentSafe()) return;

        CategoryFragment categoryFragment = new CategoryFragment();
        categoryFragment.setPreloadedData(
                ProductPreloadManager.getInstance().getSubCategories(category.getName()),
                ProductPreloadManager.getInstance().getCategoryProducts(category.getName())
        );
        Bundle bundle = new Bundle();
        bundle.putString("selectedCategory", category.getName());

        if (!category.getName().equals("Shop All")) {
            String categoryId = categoryNameToIdMap.get(category.getName().toLowerCase());
            if (categoryId != null) {
                bundle.putString("categoryId", categoryId);
                Log.d(TAG, "Navigating to CategoryFragment with category: " + category.getName() + ", ID: " + categoryId);
            } else {
                Log.w(TAG, "No category ID found for: " + category.getName());
            }
        } else {
            Log.d(TAG, "Navigating to CategoryFragment for Shop All");
        }

        categoryFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, categoryFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchCategoryIds() {
        db.collection("Category")
                .whereEqualTo("ParentCategory", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFragmentSafe()) return;

                    for (var doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        String id = doc.getId();
                        if (name != null) {
                            categoryNameToIdMap.put(name.toLowerCase(), id);
                            Log.d(TAG, "Mapped category: " + name + " to ID: " + id);
                        }
                    }
                    Log.d(TAG, "Category ID mapping completed: " + categoryNameToIdMap.size() + " categories");

                    mainHandler.post(() -> {
                        if (isFragmentSafe()) {
                            setupRecyclerView();
                            showLoading(false);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch category IDs: ", e);
                    mainHandler.post(() -> {
                        if (isFragmentSafe()) {
                            setupRecyclerView(); // Setup UI even if query fails
                            showLoading(false);
                            showError("Failed to load categories. Please try again.");
                        }
                    });
                });
    }

    private void startPreloadingData() {
        if (!isPreloadStarted.compareAndSet(false, true) ||
                categoryNameToIdMap.isEmpty() ||
                !isFragmentSafe()) {
            Log.d(TAG, "Preload conditions not met");
            return;
        }

        Log.d(TAG, "Starting preload for " + categoryNameToIdMap.size() + " categories");

        executor.execute(() -> {
            try {
                preloadManager.preloadAllCategoryData(categoryNameToIdMap);
                Log.d(TAG, "Preload initiated successfully");

                // Log cache status after delay
                mainHandler.postDelayed(this::logCacheStatus, CACHE_STATUS_LOG_DELAY_MS);

            } catch (Exception e) {
                Log.e(TAG, "Error during preload", e);
                isPreloadStarted.set(false); // Reset flag on error
            }
        });
    }

    private void logCacheStatus() {
        if (!isFragmentSafe()) return;

        Log.d(TAG, "=== CACHE STATUS ===");
        Log.d(TAG, "Shop All cached: " + preloadManager.isShopAllCached());

        int cachedCount = 0;
        for (String categoryName : categoryNameToIdMap.keySet()) {
            boolean isCached = preloadManager.isCategoryCached(categoryName);
            if (isCached) cachedCount++;

            Log.d(TAG, "Category '" + categoryName + "' cached: " + isCached);

            if (isCached) {
                List<ProductItem> products = preloadManager.getCategoryProducts(categoryName);
                Map<String, String> subCategories = preloadManager.getSubCategories(categoryName);
                Log.d(TAG, "  - Products: " + (products != null ? products.size() : 0));
                Log.d(TAG, "  - Subcategories: " + (subCategories != null ? subCategories.size() : 0));
            }
        }
        Log.d(TAG, "Total cached: " + cachedCount + "/" + categoryNameToIdMap.size());
        Log.d(TAG, "==================");
    }

    // Static methods for accessing cached data
    public static List<ProductItem> getCachedShopAllProducts() {
        return ProductPreloadManager.getInstance().getShopAllProducts();
    }

    public static List<ProductItem> getCachedCategoryProducts(String categoryName) {
        return ProductPreloadManager.getInstance().getCategoryProducts(categoryName);
    }

    public static Map<String, String> getCachedSubCategories(String categoryName) {
        return ProductPreloadManager.getInstance().getSubCategories(categoryName);
    }

    public static List<ProductItem> getCachedSubCategoryProducts(String subCategoryName) {
        return ProductPreloadManager.getInstance().getSubCategoryProducts(subCategoryName);
    }

    public static boolean isCategoryCached(String categoryName) {
        return ProductPreloadManager.getInstance().isCategoryCached(categoryName);
    }

    public static boolean isShopAllCached() {
        return ProductPreloadManager.getInstance().isShopAllCached();
    }

    // Utility methods
    private boolean isFragmentSafe() {
        return isAdded() && getContext() != null && getActivity() != null && isFragmentActive.get();
    }

    private void showLoading(boolean show) {
        if (progressBar != null && isFragmentSafe()) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null && !show) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showError(String message) {
        if (isFragmentSafe()) {
            // Implement your error showing mechanism (Toast, Snackbar, etc.)
            Log.e(TAG, "Error to show user: " + message);
        }
    }

    // Font application methods
    private void applyCustomFonts() {
        TextView txtCategoryShop = view.findViewById(R.id.txtCategoryShop);
        if (txtCategoryShop != null) {
            FontUtils.setZboldFont(getContext(), txtCategoryShop);
        }

        TextView txtDescriptionShop = view.findViewById(R.id.txtDescriptionShop);
        if (txtDescriptionShop != null) {
            FontUtils.setLightitalicFont(getContext(), txtDescriptionShop);
        }

        applyZboldFontToAllTextViews(view);
    }

    private void applyZboldFontToAllTextViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyZboldFontToAllTextViews(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (textView.getId() != R.id.txtCategoryShop && textView.getId() != R.id.txtDescriptionShop) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }

    // Cart Badge Management
    private void startCartBadgeListener() {
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "User not logged in, hiding badge");
            safeUpdateCartBadge(0);
            return;
        }

        cleanupCartListener(); // Remove existing listener

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (!isFragmentSafe()) {
                        Log.d("CartBadge", "Fragment not safe, ignoring listener callback");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
                        safeUpdateCartBadge(0);
                        return;
                    }

                    int totalQuantity = calculateCartQuantity(documentSnapshot);
                    safeUpdateCartBadge(totalQuantity);
                });
    }

    private int calculateCartQuantity(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            return 0;
        }

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

        return totalQuantity;
    }

    private void safeUpdateCartBadge(int totalQuantity) {
        if (!isFragmentSafe() || txtCartBadge == null) {
            Log.w("CartBadge", "Cannot update badge - fragment not safe or view null");
            return;
        }

        mainHandler.post(() -> {
            if (!isFragmentSafe() || txtCartBadge == null) {
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

    public void refreshCartBadge() {
        if (!isFragmentSafe()) {
            Log.d("CartBadge", "Cannot refresh badge - fragment not safe");
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
                    if (!isFragmentSafe()) {
                        Log.d("CartBadge", "Fragment detached during refresh, ignoring result");
                        return;
                    }

                    int totalQuantity = calculateCartQuantity(documentSnapshot);
                    safeUpdateCartBadge(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    if (!isFragmentSafe()) {
                        Log.d("CartBadge", "Fragment detached during refresh error, ignoring");
                        return;
                    }
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    safeUpdateCartBadge(0);
                });
    }

    private void cleanupCartListener() {
        if (cartListener != null) {
            Log.d("CartBadge", "Removing cart listener");
            cartListener.remove();
            cartListener = null;
        }
    }

    // Lifecycle Methods
    @Override
    public void onStart() {
        super.onStart();
        isFragmentActive.set(true);
        Log.d("CartBadge", "Fragment onStart()");
        if (sessionManager != null && txtCartBadge != null && isFragmentSafe()) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CartBadge", "Fragment onResume()");
        if (cartListener == null && isFragmentSafe() &&
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
        isFragmentActive.set(false);
        Log.d("CartBadge", "Fragment onStop()");
        cleanupCartListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("CartBadge", "Fragment onDestroyView()");
        cleanupCartListener();
        txtCartBadge = null;
        recyclerViewCategories = null;
        adapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CartBadge", "Fragment onDestroy()");
        cleanupCartListener();

        // Shutdown executor gracefully
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isFragmentActive.set(false);
        Log.d("CartBadge", "Fragment onDetach()");
        cleanupCartListener();
    }
}