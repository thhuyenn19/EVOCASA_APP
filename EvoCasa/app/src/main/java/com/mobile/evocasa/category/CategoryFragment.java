package com.mobile.evocasa.category;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.mobile.evocasa.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.mobile.adapters.SubCategoryAdapter;
import com.mobile.adapters.SubCategoryProductAdapter;
import com.mobile.models.SubCategory;
import com.mobile.models.SuggestedProducts;

import java.util.ArrayList;
import java.util.List;
import com.mobile.utils.FontUtils;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerViewSubCategory;
    private RecyclerView recyclerViewProducts;
    private SubCategoryAdapter subCategoryAdapter;
    private SubCategoryProductAdapter productAdapter;
    private List<SubCategory> subCategoryList;
    private List<SuggestedProducts> allProducts;

    // Views cho hiệu ứng cuộn
    private AppBarLayout appBarLayout;
    private TextView txtCollapsedTitle, tvShortBy;
    private TextView txtSubCategoryShop;
    private FrameLayout topBarContainer;
    private View heroSection;
    private View sortFilterSection;
    private Toolbar toolbar;

    // Biến để tránh animation giật
    private ValueAnimator backgroundAnimator;
    private ValueAnimator titleAnimator;
    private boolean isAnimating = false;
    private float lastPercentage = -1f;

    // Optimization cho slow scroll
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 8; // Tăng lên 120fps
    private Runnable pendingUpdate;
    private android.os.Handler mainHandler;

    // Pre-calculated values để tránh tính toán trong scroll
    private int backgroundColor;
    private float[] alphaLookupTable;
    private boolean isLookupTableInitialized = false;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo handler
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        // Khởi tạo views
        recyclerViewSubCategory = view.findViewById(R.id.recyclerViewSubCategory);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        appBarLayout = view.findViewById(R.id.appBarLayout);
        toolbar = view.findViewById(R.id.toolbar);
        tvShortBy = view.findViewById(R.id.tvSortBy);
        txtCollapsedTitle = view.findViewById(R.id.txtCollapsedTitle);
        txtSubCategoryShop = view.findViewById(R.id.txtSubCategoryShop);
        topBarContainer = view.findViewById(R.id.topBarContainer);
        heroSection = view.findViewById(R.id.heroSection);
        sortFilterSection = view.findViewById(R.id.sortFilterSection);
        FontUtils.setZboldFont(requireContext(), view.findViewById(R.id.txtSubCategoryShop));
        FontUtils.setMediumFont(requireContext(), view.findViewById(R.id.tvSortBy));

        setupSubCategories();
        setupProducts();
        setupCollapsingEffect();
        optimizeScrolling();
        initializeLookupTable();
    }

    private void initializeLookupTable() {
        // Pre-calculate alpha values để tránh tính toán runtime
        alphaLookupTable = new float[101]; // 0-100%
        for (int i = 0; i <= 100; i++) {
            float percentage = i / 100f;
            alphaLookupTable[i] = smoothInterpolate(percentage);
        }

        // Cache background color
        if (getContext() != null) {
            backgroundColor = getResources().getColor(R.color.color_bg);
        }

        isLookupTableInitialized = true;
    }

    private void setupSubCategories() {
        subCategoryList = new ArrayList<>();
        String[] subCategoryNames = {"All products", "Seating", "Tables", "Casegoods"};

        for (int i = 0; i < subCategoryNames.length; i++) {
            subCategoryList.add(new SubCategory(subCategoryNames[i], i == 0));
        }

        subCategoryAdapter = new SubCategoryAdapter(subCategoryList, selected -> filterProductsBySubCategory(selected));
        recyclerViewSubCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSubCategory.setAdapter(subCategoryAdapter);
    }

    private void setupProducts() {
        allProducts = new ArrayList<>();
        // Dummy data
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));

        productAdapter = new SubCategoryProductAdapter(allProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void optimizeScrolling() {
        // Tối ưu RecyclerView để scroll mượt hơn
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setHasFixedSize(true);
            recyclerViewProducts.setItemAnimator(null); // Tắt animation default
            recyclerViewProducts.setDrawingCacheEnabled(true);
            recyclerViewProducts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            // Thêm smooth scrolling
            recyclerViewProducts.setNestedScrollingEnabled(true);
        }

        if (recyclerViewSubCategory != null) {
            recyclerViewSubCategory.setHasFixedSize(true);
            recyclerViewSubCategory.setItemAnimator(null);
            recyclerViewSubCategory.setNestedScrollingEnabled(false); // Tránh conflict
        }

        // Tối ưu toolbar
        if (toolbar != null) {
            toolbar.setContentInsetsAbsolute(0, 0);
            toolbar.setContentInsetsRelative(0, 0);
            toolbar.setPadding(0, 0, 0, 0);
        }

        // Force hardware acceleration cho các view quan trọng
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

                    // Chỉ update khi thay đổi đáng kể
                    if (Math.abs(percentage - lastPercentage) < 0.002f) return;

                    // Immediate update cho slow scroll - không dùng Handler
                    performUltraSmoothUpdate(percentage);
                    lastPercentage = percentage;
                }
            });
        }
    }

    private void performUltraSmoothUpdate(float percentage) {
        if (!isLookupTableInitialized) return;

        // Sử dụng lookup table thay vì tính toán
        int index = Math.min(100, Math.max(0, (int)(percentage * 100)));
        float smoothPercentage = alphaLookupTable[index];

        // Inline updates để tránh method call overhead
        updateToolbarBackgroundFast(smoothPercentage);
        updateCollapsedTitleFast(smoothPercentage);
        updateElementsVisibilityFast(smoothPercentage);
    }

    private float smoothInterpolate(float input) {
        // Sử dụng cubic interpolation để làm mượt
        return input * input * (3.0f - 2.0f * input);
    }

    private void updateToolbarBackgroundFast(float percentage) {
        if (toolbar == null) return;

        // Fast calculation với pre-cached color
        if (percentage > 0.3f) {
            float backgroundAlpha = Math.min(0.95f, (percentage - 0.3f) * 2.5f); // Pre-calculated multiplier
            int alpha = (int)(255 * backgroundAlpha);
            int transparentColor = (alpha << 24) | (backgroundColor & 0x00FFFFFF);
            toolbar.setBackgroundColor(transparentColor);
        } else {
            toolbar.setBackgroundColor(0x00000000); // Transparent
        }
    }

    private void updateCollapsedTitleFast(float percentage) {
        if (txtCollapsedTitle == null) return;

        if (percentage > 0.7f) {
            float titleAlpha = Math.min(1f, (percentage - 0.7f) * 3.33f); // Pre-calculated
            txtCollapsedTitle.setAlpha(titleAlpha);
            txtCollapsedTitle.setVisibility(View.VISIBLE);

            // Micro scale effect
            float scale = 0.96f + (titleAlpha * 0.04f);
            txtCollapsedTitle.setScaleX(scale);
            txtCollapsedTitle.setScaleY(scale);
        } else {
            txtCollapsedTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void updateElementsVisibilityFast(float percentage) {
        // Hero section
        if (heroSection != null) {
            float heroAlpha = Math.max(0f, 1f - (percentage * 1.2f));
            heroSection.setAlpha(heroAlpha);
            heroSection.setTranslationY(-percentage * 30f); // Reduced translation
        }

        // SubCategory RecyclerView
        if (recyclerViewSubCategory != null) {
            if (percentage > 0.2f) {
                float subCategoryAlpha = Math.max(0f, 1f - ((percentage - 0.2f) * 4f));
                recyclerViewSubCategory.setAlpha(subCategoryAlpha);
                recyclerViewSubCategory.setVisibility(subCategoryAlpha > 0.1f ? View.VISIBLE : View.GONE);
            } else {
                recyclerViewSubCategory.setAlpha(1f);
                recyclerViewSubCategory.setVisibility(View.VISIBLE);
            }
        }

        // Sort Filter Section
        if (sortFilterSection != null) {
            if (percentage > 0.35f) {
                float sortFilterAlpha = Math.max(0f, 1f - ((percentage - 0.35f) * 4f));
                sortFilterSection.setAlpha(sortFilterAlpha);
                sortFilterSection.setVisibility(sortFilterAlpha > 0.1f ? View.VISIBLE : View.GONE);
            } else {
                sortFilterSection.setAlpha(1f);
                sortFilterSection.setVisibility(View.VISIBLE);
            }
        }

        // Original title
        if (txtSubCategoryShop != null) {
            txtSubCategoryShop.setAlpha(Math.max(0f, 1f - (percentage * 1.3f)));
        }
    }

    // Easing function để làm animation mượt hơn
    private float easeOutQuart(float t) {
        return 1f - (float)Math.pow(1f - Math.min(1f, t), 4);
    }

    private void filterProductsBySubCategory(String selectedCategory) {
        List<SuggestedProducts> filteredList = new ArrayList<>();

        if (selectedCategory.equals("All products")) {
            filteredList.addAll(allProducts);
        } else {
            for (SuggestedProducts product : allProducts) {
                if (product.getName().toLowerCase().contains(selectedCategory.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }

        productAdapter = new SubCategoryProductAdapter(filteredList);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cleanup animators để tránh memory leak
        if (backgroundAnimator != null) {
            backgroundAnimator.cancel();
        }
        if (titleAnimator != null) {
            titleAnimator.cancel();
        }

        // Cleanup handler
        if (mainHandler != null && pendingUpdate != null) {
            mainHandler.removeCallbacks(pendingUpdate);
        }

        // Reset layer types
        if (heroSection != null) {
            heroSection.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        if (txtCollapsedTitle != null) {
            txtCollapsedTitle.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }
}