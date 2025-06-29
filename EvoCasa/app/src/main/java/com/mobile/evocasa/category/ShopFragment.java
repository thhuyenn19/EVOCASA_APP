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
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";

    // UI Components
    private RecyclerView recyclerViewCategories;
    private CategoryShopAdapter adapter;
    private View view;
    private TextView txtCartBadge;
    private ImageView imgCart, imgSearch;
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

        initializeComponents();

        if (getArguments() != null) {
            preloadedCategories = (List<Category>) getArguments().getSerializable("preloadedCategories");
            if (preloadedCategories != null && !preloadedCategories.isEmpty()) {
                categoryList = preloadedCategories;
            }
        }
        initializeComponents();
        setupClickListeners();
        applyCustomFonts();
        loadInitialData();  // không override lại categoryList trong đây
        showLoading(false); // Ẩn loading nếu đã có data

        return view;
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        imgSearch = view.findViewById(R.id.imgSearch);

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
            return;
        }
        fetchCategoryIds(); // nhưng không cần chờ xong mới render
    }

    private void refreshData() {
        isPreloadStarted.set(false);
        preloadManager.clearCache(); // Add this method to ProductPreloadManager
        loadInitialData();
    }

    private void setupRecyclerView() {
        if (!isFragmentSafe()) return;

        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);

        // Gán categoryList từ preload nếu có
        if ((categoryList == null || categoryList.isEmpty()) && preloadedCategories != null && !preloadedCategories.isEmpty()) {
            categoryList = preloadedCategories;
        }

        // Nếu vẫn chưa có, fallback danh sách thủ công
        if (categoryList == null || categoryList.isEmpty()) {
            categoryList = createCategoryList();
        }

        // Setup layout
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

        // Gán adapter NGAY LẬP TỨC nếu có data
        if (adapter == null) {
            adapter = new CategoryShopAdapter(getContext(), categoryList, this::onCategorySelected);
            recyclerViewCategories.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

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
                        }
                    }

                    mainHandler.post(() -> {
                        if (isFragmentSafe()) {
                            setupRecyclerView();
                            showLoading(false);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> {
                        if (isFragmentSafe()) {
                            setupRecyclerView(); // Setup UI even if query fails
                            showLoading(false);
                            showError("Failed to load categories. Please try again.");
                        }
                    });
                });
    }


    // Utility methods
    private boolean isFragmentSafe() {
        return isAdded() && getContext() != null && getActivity() != null && isFragmentActive.get();
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null && !show) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showError(String message) {
        if (isFragmentSafe()) {
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
            safeUpdateCartBadge(0);
            return;
        }

        cleanupCartListener(); // Remove existing listener

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (!isFragmentSafe()) {
                        return;
                    }

                    if (e != null) {
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
            return;
        }

        mainHandler.post(() -> {
            if (!isFragmentSafe() || txtCartBadge == null) {
                return;
            }

            try {
                if (totalQuantity > 0) {
                    txtCartBadge.setVisibility(View.VISIBLE);
                    String displayText = totalQuantity >= 100 ? "99+" : String.valueOf(totalQuantity);
                    txtCartBadge.setText(displayText);
                } else {
                    txtCartBadge.setVisibility(View.GONE);
                }
            } catch (Exception ex) {
            }
        });
    }

    public void refreshCartBadge() {
        if (!isFragmentSafe()) {
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            safeUpdateCartBadge(0);
            return;
        }


        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isFragmentSafe()) {
                        return;
                    }

                    int totalQuantity = calculateCartQuantity(documentSnapshot);
                    safeUpdateCartBadge(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    if (!isFragmentSafe()) {
                        return;
                    }
                    safeUpdateCartBadge(0);
                });
    }

    private void cleanupCartListener() {
        if (cartListener != null) {
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

        // KHÔNG GỌI LẠI SETUP RECYCLERVIEW NẾU ĐÃ TỒN TẠI
        if (adapter == null || recyclerViewCategories.getAdapter() == null) {
            setupRecyclerView();
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