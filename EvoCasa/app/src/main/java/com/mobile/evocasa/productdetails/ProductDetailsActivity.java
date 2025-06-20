package com.mobile.evocasa.productdetails;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.adapters.ProductDetailPagerAdapter;
import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProductDetailPagerAdapter pagerAdapter;
    private RecyclerView recyclerViewRecommend;
    private SuggestedProductAdapter suggestedProductAdapter;
    private List<ProductItem> productList;
    private FirebaseFirestore db;

    private WrapContentViewPager2Helper viewPagerHelper;

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

        db = FirebaseFirestore.getInstance();

        FontUtils.initFonts(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

            scale = (float) viewWidth / (float) imageWidth;

            float dy = viewHeight - imageHeight * scale;

            matrix.setScale(scale, scale);
            matrix.postTranslate(0, dy);

            imageView.setImageMatrix(matrix);
        });

        setFonts();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        btnAddToCart.setOnClickListener(v -> showAddToCartBottomSheet());
        btnBuyNow.setOnClickListener(v -> showBuyNowBottomSheet());

        viewPagerHelper = new WrapContentViewPager2Helper(viewPager);

        pagerAdapter = new ProductDetailPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0, false);
        viewPager.setOffscreenPageLimit(3);

        viewPagerHelper.setupInitialHeight();

        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

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

                if (position == 2) {
                    tabLayout.postDelayed(() -> {
                        Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                        if (reviewsFragment instanceof ReviewsFragment) {
                            ((ReviewsFragment) reviewsFragment).onFragmentVisible();
                        }
                    }, 100);
                }

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

                if (position == 2) {
                    Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                    if (reviewsFragment instanceof ReviewsFragment) {
                        ((ReviewsFragment) reviewsFragment).onFragmentVisible();
                    }
                }

                updateViewPagerHeight();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

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

        String productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            db.collection("Product").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (txtProductName != null) {
                                txtProductName.setText(documentSnapshot.getString("Name"));
                            }
                            if (txtProductPrice != null && documentSnapshot.getDouble("Price") != null) {
                                txtProductPrice.setText("$" + documentSnapshot.getDouble("Price"));
                            }
                        } else {
                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerViewRecommend = findViewById(R.id.recyclerViewRecommend);
        recyclerViewRecommend.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        suggestedProductAdapter = new SuggestedProductAdapter(productList, this);
        recyclerViewRecommend.setAdapter(suggestedProductAdapter);
        recyclerViewRecommend.setHasFixedSize(false);

        suggestedProductAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(this, ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });

        loadSuggestedProducts();
    }

    private void loadSuggestedProducts() {
        db.collection("Product")
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductItem product = new ProductItem();
                        product.setId(doc.getId());
                        product.setName(doc.getString("Name"));
                        product.setPrice(doc.getDouble("Price") != null ? doc.getDouble("Price") : 0.0);
                        product.setImage(doc.getString("Image"));
                        product.setDescription(doc.getString("Description"));
                        product.setDimensions(doc.getString("Dimensions"));
                        ProductItem.Ratings ratings = new ProductItem.Ratings();
                        Object ratingsObj = doc.get("Ratings");
                        if (ratingsObj instanceof Map) {
                            Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                            Object averageObj = ratingsMap.get("Average");
                            if (averageObj instanceof Number) {
                                ratings.setAverage(((Number) averageObj).doubleValue());
                            }
                        }
                        product.setRatings(ratings);
                        Object categoryIdObj = doc.get("category_id");
                        if (categoryIdObj instanceof Map) {
                            Map<String, Object> categoryIdMap = (Map<String, Object>) categoryIdObj;
                            product.setCategoryId(categoryIdMap);
                        }
                        productList.add(product);
                    }
                    suggestedProductAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load suggested products", Toast.LENGTH_SHORT).show();
                });
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
                ((ReviewsFragment) currentFragment).refreshReviews();
            }
            updateViewPagerHeight();
        }
    }
}