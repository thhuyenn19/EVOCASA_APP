package com.mobile.evocasa.productdetails;

import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.SuggestedProducts;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
        ImageView imageView = findViewById(R.id.imgProduct);
        imageView.post(() -> {
            Drawable drawable = imageView.getDrawable();
            if (drawable == null) return;

            float scale;
            Matrix matrix = new Matrix();

            int imageWidth = drawable.getIntrinsicWidth();
            int imageHeight = drawable.getIntrinsicHeight();
            int viewWidth = imageView.getWidth();
            int viewHeight = imageView.getHeight();

            // Scale theo chiều rộng
            scale = (float) viewWidth / (float) imageWidth;

            // Dịch ảnh lên để phần dưới được giữ lại
            float dy = viewHeight - imageHeight * scale;

            matrix.setScale(scale, scale);
            matrix.postTranslate(0, dy); // Dịch theo chiều dọc

            imageView.setImageMatrix(matrix);
        });

        // Set fonts for UI elements
        setFonts();

        // Ánh xạ View
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        btnAddToCart.setOnClickListener(v -> showAddToCartBottomSheet());
        btnBuyNow.setOnClickListener(v -> showBuyNowBottomSheet());

        // Initialize the wrap content helper
        viewPagerHelper = new WrapContentViewPager2Helper(viewPager);

        // Khởi tạo adapter và gán vào ViewPager2
        pagerAdapter = new ProductDetailPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0, false); // Mặc định tab đầu tiên được chọn
        viewPager.setOffscreenPageLimit(3); // Giữ tất cả fragments trong memory

        // Setup initial height after adapter is set
        viewPagerHelper.setupInitialHeight();

        // Giữ tất cả fragment để chuyển mượt hơn
        viewPager.setOffscreenPageLimit(3);

        // Tắt hiệu ứng kéo vượt quá => Khi vuốt nhanh, ViewPager2 có hiệu ứng overscroll (lò xo hoặc sáng góc),có thể gây cảm giác lag hoặc không mượt.
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

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
                    tv.setTypeface(FontUtils.getZbold(ProductDetailsActivity.this));
                    tv.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.color_5E4C3E));
                }

                int position = tab.getPosition();

                // Special handling for Reviews tab
                if (position == 2) { // Reviews tab
                    // Force refresh reviews fragment
                    tabLayout.postDelayed(() -> {
                        Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                        if (reviewsFragment instanceof ReviewsFragment) {
                            ((ReviewsFragment) reviewsFragment).onFragmentVisible();
                        }
                    }, 100);
                }

                // Force update height multiple times
                tabLayout.postDelayed(() -> updateViewPagerHeight(), 50);
                tabLayout.postDelayed(() -> updateViewPagerHeight(), 200);
                tabLayout.postDelayed(() -> updateViewPagerHeight(), 400);
                tabLayout.postDelayed(() -> updateViewPagerHeight(), 600);
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setTypeface(FontUtils.getZregular(ProductDetailsActivity.this));
                    tv.setTextColor(ContextCompat.getColor(ProductDetailsActivity.this, R.color.color_5E4C3E));
                }
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                int position = tab.getPosition();

                // Special handling for Reviews tab
                if (position == 2) { // Reviews tab
                    Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                    if (reviewsFragment instanceof ReviewsFragment) {
                        ((ReviewsFragment) reviewsFragment).onFragmentVisible();
                    }
                }

                // Force refresh when tab is reselected
                updateViewPagerHeight();
            }
        });

        // 🔧 Force height update every time tab changes (fix mất item review)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Multiple attempts to update height with delays
                viewPager.postDelayed(() -> {
                    if (viewPagerHelper != null) {
                        viewPagerHelper.updateHeight();
                    }
                }, 50);

                viewPager.postDelayed(() -> {
                    if (viewPagerHelper != null) {
                        viewPagerHelper.updateHeight();
                    }
                }, 150);

                viewPager.postDelayed(() -> {
                    if (viewPagerHelper != null) {
                        viewPagerHelper.updateHeight();
                    }
                }, 300);
            }
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

    private void showBuyNowBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.product_buy_now, null);
        bottomSheetDialog.setContentView(view);

        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        ImageButton btnIncrease = view.findViewById(R.id.btnIncrease);
        ImageButton btnDecrease = view.findViewById(R.id.btnDecrease);

        final int[] quantity = {1};
        final int[] selectedOptionIndex = {-1};

        txtQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(v -> {
            quantity[0]++;
            txtQuantity.setText(String.valueOf(quantity[0]));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        ImageView img1 = view.findViewById(R.id.imgAvatar1);
        ImageView img2 = view.findViewById(R.id.imgAvatar2);
        ImageView img3 = view.findViewById(R.id.imgAvatar3);
        List<ImageView> avatarOptions = Arrays.asList(img1, img2, img3);

        for (int i = 0; i < avatarOptions.size(); i++) {
            int finalI = i;
            avatarOptions.get(i).setOnClickListener(v -> {
                for (ImageView avatar : avatarOptions) {
                    avatar.setBackground(null);
                }
                avatarOptions.get(finalI).setBackgroundResource(R.drawable.bg_option_selected);
                selectedOptionIndex[0] = finalI;
            });
        }

        Button btnBuyNow = view.findViewById(R.id.btnBuyNow);
        btnBuyNow.setOnClickListener(v -> {
            if (selectedOptionIndex[0] == -1) {
                Toast.makeText(this, "Please select an option before buying!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đặt mua " + quantity[0] + " sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showAddToCartBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.product_add_to_cart, null);
        bottomSheetDialog.setContentView(view);

        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        ImageButton btnIncrease = view.findViewById(R.id.btnIncrease);
        ImageButton btnDecrease = view.findViewById(R.id.btnDecrease);

        final int[] quantity = {1};
        final int[] selectedOptionIndex = {-1};

        txtQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(v -> {
            quantity[0]++;
            txtQuantity.setText(String.valueOf(quantity[0]));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        ImageView img1 = view.findViewById(R.id.imgAvatar1);
        ImageView img2 = view.findViewById(R.id.imgAvatar2);
        ImageView img3 = view.findViewById(R.id.imgAvatar3);
        List<ImageView> avatarOptions = Arrays.asList(img1, img2, img3);

        for (int i = 0; i < avatarOptions.size(); i++) {
            int finalI = i;
            avatarOptions.get(i).setOnClickListener(v -> {
                for (ImageView avatar : avatarOptions) {
                    avatar.setBackground(null);
                }
                avatarOptions.get(finalI).setBackgroundResource(R.drawable.bg_option_selected);
                selectedOptionIndex[0] = finalI;
            });
        }

        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> {
            if (selectedOptionIndex[0] == -1) {
                Toast.makeText(this, "Please select an option before adding to cart!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Added " + quantity[0] + " item(s) to cart", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
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

    public void updateViewPagerHeight() {
        if (viewPagerHelper != null) {
            viewPagerHelper.updateHeight();
        }
    }
    public void forceRefreshCurrentFragment() {
        if (pagerAdapter != null) {
            int currentItem = viewPager.getCurrentItem();
            Fragment currentFragment = pagerAdapter.getCurrentFragment(currentItem);
            if (currentFragment instanceof ReviewsFragment) {
                // Force recreate the reviews
                ((ReviewsFragment) currentFragment).refreshReviews();
            }
            updateViewPagerHeight();
        }
    }
}