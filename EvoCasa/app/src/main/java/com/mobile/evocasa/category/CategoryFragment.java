package com.mobile.evocasa.category;

import static androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.adapters.SubCategoryAdapter;
import com.mobile.adapters.SubCategoryProductAdapter;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.R;
import com.mobile.evocasa.productdetails.ProductDetailsActivity;
import com.mobile.evocasa.search.SearchActivity;
import com.mobile.models.ProductItem;
import com.mobile.models.SubCategory;
import com.mobile.utils.BehaviorLogger;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";
    private String currentSelectedSubCategory = "All products";

    private boolean isFetchingProducts = false;
    private RecyclerView recyclerViewSubCategory;
    private RecyclerView recyclerViewProducts;
    private SubCategoryAdapter subCategoryAdapter;
    private SubCategoryProductAdapter productAdapter;
    private AppBarLayout appBarLayout;
    private TextView txtCollapsedTitle, tvSortBy;
    private TextView txtSubCategoryShop;
    private FrameLayout topBarContainer;
    private View heroSection;
    private View sortFilterSection;
    private Toolbar toolbar;
    private LinearLayout btnBack;

    private List<SubCategory> subCategoryList;
    private List<ProductItem> currentProductList;
    private FirebaseFirestore db;
    private String selectedCategory;
    private String categoryId;
    private Map<String, String> subCategoryIds = new HashMap<>();

    private ValueAnimator backgroundAnimator;
    private ValueAnimator titleAnimator;
    private boolean isAnimating = false;
    private float lastPercentage = -1f;
    private Runnable pendingUpdate;
    private android.os.Handler mainHandler;
    private int backgroundColor;
    private float[] alphaLookupTable;
    private boolean isLookupTableInitialized = false;
    private TextView txtCartBadge;
    private ImageView imgCart, imgSearch;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private Map<String, List<ProductItem>> productCache;

    private Map<String, String> preloadedSubCategoryIds = null;
    private List<ProductItem> preloadedCategoryProducts = null;
    public CategoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey("productCache")) {
            productCache = (Map<String, List<ProductItem>>) getArguments().getSerializable("productCache");
        }
        if (productCache == null) {
            productCache = new HashMap<>();
        }
        return inflater.inflate(R.layout.fragment_category, container, false);
    }
    public void setPreloadedData(Map<String, String> subCategoryIds, List<ProductItem> categoryProducts) {
        this.preloadedSubCategoryIds = subCategoryIds;
        this.preloadedCategoryProducts = categoryProducts;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        recyclerViewSubCategory = view.findViewById(R.id.recyclerViewSubCategory);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        appBarLayout = view.findViewById(R.id.appBarLayout);
        toolbar = view.findViewById(R.id.toolbar);
        tvSortBy = view.findViewById(R.id.tvSortBy);
        txtCollapsedTitle = view.findViewById(R.id.txtCollapsedTitle);
        txtSubCategoryShop = view.findViewById(R.id.txtSubCategoryShop);
        topBarContainer = view.findViewById(R.id.topBarContainer);
        heroSection = view.findViewById(R.id.heroSection);
        sortFilterSection = view.findViewById(R.id.sortFilterSection);
        btnBack = view.findViewById(R.id.btnBack);
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        imgSearch = view.findViewById(R.id.imgSearch);
        sessionManager = new UserSessionManager(requireContext());

        Bundle args = getArguments();
        if (args != null) {
            selectedCategory = args.getString("selectedCategory", "");
            categoryId = args.getString("categoryId", null);
            Log.d(TAG, "Received category: " + selectedCategory + ", ID: " + categoryId);
            if (txtSubCategoryShop != null) {
                txtSubCategoryShop.setText(selectedCategory);
            }
            if (txtCollapsedTitle != null) {
                txtCollapsedTitle.setText(selectedCategory);
            }
        }
        ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();

        if (preloadManager != null && selectedCategory != null && !selectedCategory.isEmpty()) {
            Map<String, String> subcategories = preloadManager.getSubCategories(selectedCategory);
            List<ProductItem> categoryProducts = preloadManager.getCategoryProducts(selectedCategory);

            if (subcategories != null && !subcategories.isEmpty()) {
                subCategoryIds.clear();
                subCategoryIds.putAll(subcategories);
            }

            if (categoryProducts != null && !categoryProducts.isEmpty()) {
                currentProductList = new ArrayList<>(categoryProducts);
                productAdapter = new SubCategoryProductAdapter(currentProductList, requireContext());
                recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                recyclerViewProducts.setAdapter(productAdapter);
            }
        }
        imgSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            intent.putExtra("openProgress", true);
            startActivity(intent);
        });

        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }

        FontUtils.setZboldFont(requireContext(), txtSubCategoryShop);
        FontUtils.setZboldFont(requireContext(), txtCollapsedTitle);
        FontUtils.setMediumFont(requireContext(), tvSortBy);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        params.setBehavior(new FlingBehavior(getContext(), null));
        appBarLayout.setLayoutParams(params);

        if (getArguments() != null) {
            selectedCategory = getArguments().getString("selectedCategory", "");
            categoryId = getArguments().getString("categoryId", null);
            Log.d(TAG, "Received category: " + selectedCategory + ", ID: " + categoryId);
            if (txtSubCategoryShop != null) {
                txtSubCategoryShop.setText(selectedCategory);
            }
            if (txtCollapsedTitle != null) {
                txtCollapsedTitle.setText(selectedCategory);
            }
        }

        // Nếu có productCache chứa sẵn dữ liệu thì load ngay thay vì Firestore
        if (productCache != null && categoryId != null && productCache.containsKey(categoryId)) {
            Log.d(TAG, "Loaded from productCache for categoryId: " + categoryId);
            currentProductList = new ArrayList<>(productCache.get(categoryId));
            productAdapter = new SubCategoryProductAdapter(currentProductList, requireContext());
            recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerViewProducts.setAdapter(productAdapter);
            setupSubCategories();
            setupCollapsingEffect();
            optimizeScrolling();
            initializeLookupTable();
            setupBackButton();
            return;
        }

        // Setup methods
        setupProducts();
        setupSubCategories();
        setupCollapsingEffect();
        optimizeScrolling();
        initializeLookupTable();
        setupBackButton();
    }

    private void setupProducts() {
        currentProductList = new ArrayList<>();
        productAdapter = new SubCategoryProductAdapter(currentProductList, requireContext());
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);

        // Thêm click listener để mở ProductDetailsActivity
        productAdapter.setOnItemClickListener(product -> {
            String uid = new UserSessionManager(requireContext()).getUid(); // hoặc từ SharedPreferences nếu bạn không dùng FirebaseAuth
            String productId = product.getId(); // hoặc product.get_id()

            // Ghi hành vi click
            BehaviorLogger.record(
                    uid,
                    productId,
                    "click",
                    "category_page",
                    null
            );

            Intent intent = new Intent(getContext(), ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivityForResult(intent, 1001);
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            boolean wishlistChanged = data.getBooleanExtra("wishlistChanged", false);
            if (wishlistChanged) {
                if (selectedCategory.equals("Shop All")) {
                    fetchAllProductsForShopAll();
                } else {
                    filterProductsBySubCategory(currentSelectedSubCategory);
                }
            }
        }
    }


    private void fetchAllProductsForShopAll() {
        if (isFetchingProducts) {
            Log.d(TAG, "Fetch already in progress, skipping");
            return;
        }
        isFetchingProducts = true;
        currentProductList.clear();

        // Ưu tiên lấy từ preload cache
        ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();
        List<ProductItem> cachedProducts = preloadManager.getShopAllProducts();
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            currentProductList.addAll(cachedProducts);
            productAdapter.notifyDataSetChanged();
            isFetchingProducts = false;
            Log.d(TAG, "Loaded Shop All products from cache: " + cachedProducts.size());
            return;
        }

        // Fallback: load từ Firestore nếu cache rỗng
        Log.d(TAG, "Fetching all products for Shop All from Firestore");
        db.collection("Product")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            ProductItem product = new ProductItem();
                            product.setId(doc.getId());
                            product.setName(doc.getString("Name"));
                            product.setPrice(doc.getDouble("Price") != null ? doc.getDouble("Price") : 0.0);
                            product.setImage(doc.getString("Image"));
                            product.setDescription(doc.getString("Description"));
                            product.setDimensions(doc.getString("Dimensions"));
                            product.setCustomizeImage(doc.getString("CustomizeImage"));

                            // Ratings
                            ProductItem.Ratings ratings = new ProductItem.Ratings();
                            Object ratingsObj = doc.get("Ratings");
                            if (ratingsObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                                Object averageObj = ratingsMap.get("Average");
                                if (averageObj instanceof Number) {
                                    ratings.setAverage(((Number) averageObj).doubleValue());
                                }
                            }
                            product.setRatings(ratings);

                            // CategoryId
                            Object categoryIdObj = doc.get("category_id");
                            if (categoryIdObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> categoryIdMap = (Map<String, Object>) categoryIdObj;
                                product.setCategoryId(categoryIdMap);
                            }

                            currentProductList.add(product);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing product: " + doc.getId(), e);
                        }
                    }
                    productAdapter.notifyDataSetChanged();
                    isFetchingProducts = false;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch all products for Shop All", e);
                    productAdapter.notifyDataSetChanged();
                    isFetchingProducts = false;
                });
    }

    private void filterProductsBySubCategory(String selectedSubCategory) {
        if (isFetchingProducts) {
            Log.d(TAG, "Fetch already in progress, skipping");
            return;
        }
        isFetchingProducts = true;
        currentProductList.clear();

        ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();

        // Trường hợp: All products của 1 category (không phải là subcategory)
        if (selectedSubCategory.equals("All products")) {
            List<ProductItem> cachedProducts = preloadManager.getCategoryProducts(selectedCategory);
            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                currentProductList.addAll(cachedProducts);
                productAdapter.notifyDataSetChanged();
                isFetchingProducts = false;
                Log.d(TAG, "Loaded category products from cache: " + cachedProducts.size());
                return;
            }

            Log.d(TAG, "Fallback: Fetching all products from Firestore for category: " + selectedCategory);
            db.collection("Product")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> categoryIdObj = (Map<String, Object>) doc.get("category_id");
                            String productCategoryId = categoryIdObj != null ? (String) categoryIdObj.get("$oid") : null;

                            if (productCategoryId != null && productCategoryId.equals(categoryId)) {
                                ProductItem product = parseProductFromDocument(doc);
                                if (product != null) {
                                    currentProductList.add(product);
                                }
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                        isFetchingProducts = false;
                        Log.d(TAG, "Loaded from Firestore: " + currentProductList.size());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore error fetching category products", e);
                        productAdapter.notifyDataSetChanged();
                        isFetchingProducts = false;
                    });

            return;
        }

        // Trường hợp: Subcategory cụ thể
        if (subCategoryIds.containsKey(selectedSubCategory)) {
            List<ProductItem> cachedSubProducts = preloadManager.getSubCategoryProducts(selectedSubCategory);
            if (cachedSubProducts != null && !cachedSubProducts.isEmpty()) {
                currentProductList.addAll(cachedSubProducts);
                productAdapter.notifyDataSetChanged();
                isFetchingProducts = false;
                Log.d(TAG, "Loaded subcategory products from cache: " + cachedSubProducts.size());
                return;
            }

            String subCategoryId = subCategoryIds.get(selectedSubCategory);
            db.collection("Product")
                    .get()
                    .addOnSuccessListener(productSnapshots -> {
                        for (QueryDocumentSnapshot doc : productSnapshots) {
                            Map<String, Object> categoryIdObj = (Map<String, Object>) doc.get("category_id");
                            String productCategoryId = categoryIdObj != null ? (String) categoryIdObj.get("$oid") : null;
                            if (productCategoryId != null && productCategoryId.equals(subCategoryId)) {
                                ProductItem product = parseProductFromDocument(doc);
                                if (product != null) {
                                    currentProductList.add(product);
                                }
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                        isFetchingProducts = false;
                    })
                    .addOnFailureListener(e -> {
                        productAdapter.notifyDataSetChanged();
                        isFetchingProducts = false;
                    });
        } else {
            productAdapter.notifyDataSetChanged();
            isFetchingProducts = false;
        }
    }

    private ProductItem parseProductFromDocument(QueryDocumentSnapshot doc) {
        try {
            ProductItem product = new ProductItem();
            product.setId(doc.getId());
            product.setName(doc.getString("Name"));
            product.setPrice(doc.getDouble("Price") != null ? doc.getDouble("Price") : 0.0);
            product.setImage(doc.getString("Image"));
            product.setDescription(doc.getString("Description"));
            product.setDimensions(doc.getString("Dimensions"));
            product.setCustomizeImage(doc.getString("CustomizeImage"));

            ProductItem.Ratings ratings = new ProductItem.Ratings();
            Object ratingsObj = doc.get("Ratings");
            if (ratingsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                Object avg = ratingsMap.get("Average");
                if (avg instanceof Number) ratings.setAverage(((Number) avg).doubleValue());
            }
            product.setRatings(ratings);

            Object categoryIdMapObj = doc.get("category_id");
            if (categoryIdMapObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> categoryIdMap = (Map<String, Object>) categoryIdMapObj;
                product.setCategoryId(categoryIdMap);
            }

            return product;

        } catch (Exception e) {
            return null;
        }
    }

    private void setupSubCategories() {
        subCategoryList = new ArrayList<>();
        subCategoryList.add(new SubCategory("All products", true));
        subCategoryIds.put("All products", categoryId);  //
        subCategoryAdapter = new SubCategoryAdapter(subCategoryList, this::onSubCategorySelected);
        recyclerViewSubCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSubCategory.setAdapter(subCategoryAdapter);

        if (selectedCategory.equals("Shop All")) {
            Log.d(TAG, "Shop All selected, showing only 'All products'");
            subCategoryAdapter.notifyDataSetChanged();
            fetchAllProductsForShopAll();
            return;
        }

        // Nếu có preload từ ShopFragment
        if (preloadedSubCategoryIds != null && preloadedCategoryProducts != null) {
            Log.d(TAG, "Using preloaded subcategories & products");

            subCategoryIds.clear();
            subCategoryIds.putAll(preloadedSubCategoryIds);

            for (String name : preloadedSubCategoryIds.keySet()) {
                boolean isSelected = name.equals(currentSelectedSubCategory);
                subCategoryList.add(new SubCategory(name, isSelected));
            }

            subCategoryAdapter.notifyDataSetChanged();

            currentProductList.clear();
            currentProductList.addAll(preloadedCategoryProducts);
            productAdapter.notifyDataSetChanged();
            return;
        }

        // Nếu không có preload → truy vấn Firestore như bình thường
        if (categoryId != null) {
            Log.d(TAG, "Fetching subcategories for category ID: " + categoryId);
            db.collection("Category")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d(TAG, "Subcategory query returned " + queryDocumentSnapshots.size() + " documents");
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object parentCategoryObj = doc.get("ParentCategory");
                            String parentCategoryId = null;

                            if (parentCategoryObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> parentCategory = (Map<String, Object>) parentCategoryObj;
                                parentCategoryId = (String) parentCategory.get("$oid");
                            } else if (parentCategoryObj instanceof String) {
                                parentCategoryId = (String) parentCategoryObj;
                            }

                            if (parentCategoryId != null && parentCategoryId.equals(categoryId)) {
                                String name = doc.getString("Name");
                                String id = doc.getId();
                                if (name != null) {
                                    boolean isSelected = name.equals(currentSelectedSubCategory);
                                    subCategoryList.add(new SubCategory(name, isSelected));
                                    subCategoryIds.put(name, id);
                                    Log.d(TAG, " Added subcategory: " + name + " with ID: " + id);
                                }
                            }
                        }

                        subCategoryAdapter.notifyDataSetChanged();

                        // Dùng cache nếu có
                        ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();
                        List<ProductItem> cached = preloadManager.getCategoryProducts(selectedCategory);
                        if (cached != null && !cached.isEmpty()) {
                            Log.d(TAG, "Using cached products after loading subcategories");
                            currentProductList.clear();
                            currentProductList.addAll(cached);
                            productAdapter.notifyDataSetChanged();
                            return;
                        }

                        // Nếu chưa có cache → gọi Firestore
                        Log.d(TAG, "No cache found, triggering filterProductsBySubCategory");
                        filterProductsBySubCategory("All products");

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch subcategories for ID: " + categoryId, e);
                        subCategoryAdapter.notifyDataSetChanged();
                    });
        }

    }

    // THÊM METHOD MỚI ĐỂ XỬ LÝ CẢ FILTER VÀ SCROLL
    private void onSubCategorySelected(String selectedSubCategory) {
        if (isFetchingProducts) {
            Log.d(TAG, "Fetch in progress, ignoring subcategory click");
            return;
        }
        currentSelectedSubCategory = selectedSubCategory;

        // Cập nhật trạng thái selected cho UI
        for (SubCategory sub : subCategoryList) {
            sub.setSelected(sub.getName().equals(selectedSubCategory));
        }
        subCategoryAdapter.notifyDataSetChanged();

        // Scroll đến vị trí được chọn
        int selectedPosition = -1;
        for (int i = 0; i < subCategoryList.size(); i++) {
            if (subCategoryList.get(i).getName().equals(selectedSubCategory)) {
                selectedPosition = i;
                break;
            }
        }
        if (selectedPosition != -1) {
            final int position = selectedPosition;
            recyclerViewSubCategory.postDelayed(() -> smoothScrollToSubCategoryPosition(position), 100);
        }

        // Xử lý lọc sản phẩm
        if (selectedSubCategory.equals("All products")) {
            if (selectedCategory.equals("Shop All")) {
                fetchAllProductsForShopAll();
            } else {
                filterProductsBySubCategory("All products");
            }
        } else {
            if (selectedCategory.equals("Shop All")) {
                fetchAllProductsForShopAll(); // Shop All luôn dùng 1 danh sách
            } else {
                filterProductsBySubCategory(selectedSubCategory); // Category thường lọc theo sub
            }
        }
    }

    private void smoothScrollToSubCategoryPosition(int position) {
        recyclerViewSubCategory.post(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewSubCategory.getLayoutManager();
            if (layoutManager != null) {
                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                    @Override
                    protected int getHorizontalSnapPreference() {
                        return SNAP_TO_START;
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return 100f / displayMetrics.densityDpi; // Tốc độ scroll
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
    }

    private void setupBackButton() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    private void initializeLookupTable() {
        alphaLookupTable = new float[101];
        for (int i = 0; i <= 100; i++) {
            float percentage = i / 100f;
            alphaLookupTable[i] = smoothInterpolate(percentage);
        }
        if (getContext() != null) {
            backgroundColor = getResources().getColor(R.color.color_bg);
        }
        isLookupTableInitialized = true;
    }

    private float smoothInterpolate(float input) {
        return input * input * (3.0f - 2.0f * input);
    }

    private void optimizeScrolling() {
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setHasFixedSize(true);
            recyclerViewProducts.setItemAnimator(null);
            recyclerViewProducts.setItemViewCacheSize(20);
            recyclerViewProducts.setDrawingCacheEnabled(true);
            recyclerViewProducts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerViewProducts.setNestedScrollingEnabled(true);
        }
        if (recyclerViewSubCategory != null) {
            recyclerViewSubCategory.setHasFixedSize(true);
            recyclerViewSubCategory.setItemAnimator(null);
            recyclerViewSubCategory.setNestedScrollingEnabled(false);
        }
        if (toolbar != null) {
            toolbar.setContentInsetsAbsolute(0, 0);
            toolbar.setContentInsetsRelative(0, 0);
            toolbar.setPadding(0, 0, 0, 0);
        }
        if (heroSection != null) {
            heroSection.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        if (txtCollapsedTitle != null) {
            txtCollapsedTitle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    private void setupCollapsingEffect() {
        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    int totalScrollRange = appBarLayout.getTotalScrollRange();
                    if (totalScrollRange == 0) return;
                    float percentage = Math.abs(verticalOffset) / (float) totalScrollRange;
                    if (Math.abs(percentage - lastPercentage) < 0.005f) return;
                    performUltraSmoothUpdate(percentage);
                    lastPercentage = percentage;
                }
            });
        }
    }

    private void performUltraSmoothUpdate(float percentage) {
        if (!isLookupTableInitialized) return;
        int index = Math.min(100, Math.max(0, (int)(percentage * 100)));
        float smoothPercentage = alphaLookupTable[index];
        updateToolbarBackgroundFast(smoothPercentage);
        updateCollapsedTitleFast(smoothPercentage);
        updateElementsVisibilityFast(smoothPercentage);
    }

    private void updateToolbarBackgroundFast(float percentage) {
        if (toolbar == null) return;
        if (percentage > 0.3f) {
            float backgroundAlpha = Math.min(0.95f, (percentage - 0.3f) * 2.5f);
            int alpha = (int)(255 * backgroundAlpha);
            int transparentColor = (alpha << 24) | (backgroundColor & 0x00FFFFFF);
            toolbar.setBackgroundColor(transparentColor);
        } else {
            toolbar.setBackgroundColor(0x00000000);
        }
    }

    private void updateCollapsedTitleFast(float percentage) {
        if (txtCollapsedTitle == null) return;
        if (percentage > 0.7f) {
            float titleAlpha = Math.min(1f, (percentage - 0.7f) * 3.33f);
            txtCollapsedTitle.setAlpha(titleAlpha);
            txtCollapsedTitle.setVisibility(View.VISIBLE);
            float scale = 0.96f + (titleAlpha * 0.04f);
            txtCollapsedTitle.setScaleX(scale);
            txtCollapsedTitle.setScaleY(scale);
        } else {
            txtCollapsedTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void updateElementsVisibilityFast(float percentage) {
        if (heroSection != null) {
            float heroAlpha = Math.max(0f, 1f - (percentage * 1.2f));
            heroSection.setAlpha(heroAlpha);
            heroSection.setTranslationY(-percentage * 30f);
        }
        if (recyclerViewSubCategory != null) {
            if (percentage > 0.2f) {
                float subCategoryAlpha = Math.max(0f, 1f - ((percentage - 0.2f) * 4f));
                recyclerViewSubCategory.setAlpha(subCategoryAlpha);
                recyclerViewSubCategory.setVisibility(subCategoryAlpha > 0.1f ? View.VISIBLE : View.INVISIBLE);
            } else {
                recyclerViewSubCategory.setAlpha(1f);
                recyclerViewSubCategory.setVisibility(View.VISIBLE);
            }
        }
        if (sortFilterSection != null) {
            if (percentage > 0.35f) {
                float sortFilterAlpha = Math.max(0f, 1f - ((percentage - 0.35f) * 4f));
                sortFilterSection.setAlpha(sortFilterAlpha);
                sortFilterSection.setVisibility(sortFilterAlpha > 0.1f ? View.VISIBLE : View.INVISIBLE);
            } else {
                sortFilterSection.setAlpha(1f);
                sortFilterSection.setVisibility(View.VISIBLE);
            }
        }
        if (txtSubCategoryShop != null) {
            txtSubCategoryShop.setAlpha(Math.max(0f, 1f - (percentage * 1.3f)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (backgroundAnimator != null) {
            backgroundAnimator.cancel();
        }
        if (titleAnimator != null) {
            titleAnimator.cancel();
        }
        if (mainHandler != null && pendingUpdate != null) {
            mainHandler.removeCallbacks(pendingUpdate);
        }
        if (heroSection != null) {
            heroSection.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        if (txtCollapsedTitle != null) {
            txtCollapsedTitle.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        Log.d("CartBadge", "Fragment onDestroyView()");
        cleanupCartListener();
        txtCartBadge = null; // Clear view reference
    }
    // CartBadge
    /**
     * Start listening for cart changes and update badge
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
        if (sessionManager != null && txtCartBadge != null && isAdded()) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CartBadge", "Fragment onResume()");
        // Only start listener if we don't already have one and fragment is properly attached
        if (cartListener == null && isAdded() && getContext() != null &&
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
        Log.d("CartBadge", "Fragment onStop()");
        cleanupCartListener();
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