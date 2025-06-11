package com.mobile.evocasa.productdetails;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.SuggestedProducts;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProductDetailPagerAdapter pagerAdapter;
    private RecyclerView recyclerViewRecommend;
    private SuggestedProductAdapter suggestedProductAdapter;
    private List<SuggestedProducts> productList;

    // Add the helper for wrap content functionality
    private WrapContentViewPager2Helper viewPagerHelper;

    // UI Elements for font setting
    private TextView txtProductName;
    private TextView txtProductPrice;
    private TextView txtRating;
    private TextView txtImageIndex;
    private TextView txtRecommendItems;
    private Button btnAddToCart;
    private Button btnBuyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);

        // Initialize FontUtils
        FontUtils.initFonts(this);

        // Áp dụng padding cho status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        initViews();

        // Set fonts for UI elements
        setFonts();

        // Ánh xạ View
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Initialize the wrap content helper
        viewPagerHelper = new WrapContentViewPager2Helper(viewPager);

        // Khởi tạo adapter và gán vào ViewPager2
        pagerAdapter = new ProductDetailPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0, false); // Mặc định tab đầu tiên được chọn

        // Setup initial height after adapter is set
        viewPagerHelper.setupInitialHeight();

        // Gán TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String title = "";
            switch (position) {
                case 0: title = "Description"; break;
                case 1: title = "Dimensions"; break;
                case 2: title = "Reviews"; break;
            }

            TextView textView = new TextView(this);
            textView.setText(title);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(16);
            // Set font using FontUtils instead of ResourcesCompat
            textView.setTypeface(FontUtils.getZregular(this));
            textView.setTextColor(ContextCompat.getColor(this, R.color.color_5E4C3E));
            tab.setCustomView(textView);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    // Use FontUtils for bold font
                    tv.setTypeface(FontUtils.getZbold(ProductDetailsActivity.this));
                    tv.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.color_5E4C3E));
                }

                // Update height when tab changes
                viewPagerHelper.updateHeight();
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    // Use FontUtils for regular font
                    tv.setTypeface(FontUtils.getZregular(ProductDetailsActivity.this));
                    tv.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.color_5E4C3E));
                }
            }

            @Override public void onTabReselected(@NonNull TabLayout.Tab tab) {}
        });

        // Fix: Apply style cho tab đầu tiên sau khi attach
        tabLayout.post(() -> {
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select();
                View view = firstTab.getCustomView();
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setTypeface(FontUtils.getZbold(ProductDetailsActivity.this));
                    tv.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.color_5E4C3E));
                }
            }
        });

        /* Suggested Products */
        recyclerViewRecommend = findViewById(R.id.recyclerViewRecommend);
        recyclerViewRecommend.setLayoutManager(new GridLayoutManager(this, 2));

        List<SuggestedProducts> productList = new ArrayList<>();
        productList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        productList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        productList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        productList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductAdapter = new SuggestedProductAdapter(productList);
        recyclerViewRecommend.setAdapter(suggestedProductAdapter);
        recyclerViewRecommend.setHasFixedSize(false);
    }

    private void initViews() {
        txtProductName = findViewById(R.id.txtProductName);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtRating = findViewById(R.id.txtRating);
        txtImageIndex = findViewById(R.id.txtImageIndex);
        txtRecommendItems = findViewById(R.id.txtRecommendItems);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
    }

    private void setFonts() {
        // Set fonts for product details
        if (txtProductName != null) {
            FontUtils.setZboldFont(this, txtProductName);
        }

        if (txtProductPrice != null) {
            FontUtils.setZboldFont(this, txtProductPrice);
        }

        if (txtRating != null) {
            FontUtils.setZregularFont(this, txtRating);
        }

        if (txtImageIndex != null) {
            FontUtils.setRegularFont(this, txtImageIndex);
        }

        if (txtRecommendItems != null) {
            FontUtils.setZboldFont(this, txtRecommendItems);
        }

        // Set fonts for buttons
        if (btnAddToCart != null) {
            FontUtils.setBoldFont(this, btnAddToCart);
        }

        if (btnBuyNow != null) {
            FontUtils.setBoldFont(this, btnBuyNow);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the helper
        if (viewPagerHelper != null) {
            viewPagerHelper.destroy();
        }
    }
}