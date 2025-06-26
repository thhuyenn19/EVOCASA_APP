package com.mobile.evocasa.productdetails;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.adapters.ImagePagerAdapter;
import com.mobile.adapters.ProductDetailPagerAdapter;
import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.R;
import com.mobile.evocasa.payment.PaymentActivity;
import com.mobile.models.CartProduct;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProductDetailPagerAdapter pagerAdapter;
    private RecyclerView recyclerViewRecommend;
    private LinearLayout btnBack;
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
    private ViewPager2 imageViewPager;
    private List<String> imageUrls;
    private ImageButton btnFavorite, btnShare;
    private LinearLayout ratingLayout;
    private UserSessionManager sessionManager;
    private String productId;
    private DecimalFormat decimalFormat;
    private String productDescription;
    private String productDimensions;
    private ProductItem productItem;
    private ListenerRegistration cartListener;
    private TextView txtCartBadge;
    ImageView imgCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);

        db = FirebaseFirestore.getInstance();
        sessionManager = new UserSessionManager(this);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        imgCart = findViewById(R.id.imgCart);

        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                Intent intent = new Intent(ProductDetailsActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        FontUtils.initFonts(this);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        decimalFormat = new DecimalFormat("0.0", symbols);
        decimalFormat.setGroupingUsed(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setFonts();

        // Start cart badge listener
        startCartBadgeListener();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        imageViewPager = findViewById(R.id.imgProductViewPager);

        btnAddToCart.setOnClickListener(v -> showAddToCartBottomSheet());
        btnBuyNow.setOnClickListener(v -> {
            if (productItem != null) {
                showBuyNowBottomSheet();
            } else {
                Toast.makeText(this, "Product data not loaded yet", Toast.LENGTH_SHORT).show();
                Log.w("ProductDetails", "Attempted Buy Now before productItem is loaded");
            }
        });

        viewPagerHelper = new WrapContentViewPager2Helper(viewPager);
        pagerAdapter = new ProductDetailPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0, false);
        viewPager.setOffscreenPageLimit(3);
        viewPagerHelper.setupInitialHeight();
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("wishlistChanged", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        btnShare.setOnClickListener(v -> shareProductWithNameLink());

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
                if (position == 2 && productItem != null) {
                    tabLayout.postDelayed(() -> {
                        Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                        if (reviewsFragment instanceof ReviewsFragment) {
                            ((ReviewsFragment) reviewsFragment).refreshReviews();
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
                if (position == 2 && productItem != null) {
                    Fragment reviewsFragment = pagerAdapter.getCurrentFragment(2);
                    if (reviewsFragment instanceof ReviewsFragment) {
                        ((ReviewsFragment) reviewsFragment).refreshReviews();
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
                    tv.setTypeface(FontUtils.getZbold(this));
                    tv.setTextColor(ContextCompat.getColor(this, R.color.color_5E4C3E));
                }
            }
        });

        productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            db.collection("Product").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            txtProductName.setText(documentSnapshot.getString("Name"));
                            Double price = documentSnapshot.getDouble("Price");
                            if (price != null) {
                                if (price % 1 == 0) {
                                    txtProductPrice.setText("$" + String.format(Locale.US, "%.0f", price));
                                } else {
                                    txtProductPrice.setText("$" + String.format(Locale.US, "%.2f", price));
                                }
                            } else {
                                txtProductPrice.setText("$0");
                            }

                            productDescription = documentSnapshot.getString("Description");
                            productDimensions = documentSnapshot.getString("Dimension");
                            productItem = documentSnapshot.toObject(ProductItem.class);
                            Log.d("ProductDetails", "Loaded ProductItem: " + (productItem != null));

                            if (productItem != null) {
                                productItem.setId(productId);
                                if (productItem.getRatings() == null) {
                                    productItem.setRatings(new ProductItem.Ratings());
                                }
                                Object ratingsObj = documentSnapshot.get("Ratings");
                                if (ratingsObj instanceof Map) {
                                    Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                                    Object averageObj = ratingsMap.get("Average");
                                    if (averageObj instanceof Number) {
                                        productItem.getRatings().setAverage(((Number) averageObj).doubleValue());
                                    }
                                    Object detailsObj = ratingsMap.get("Details");
                                    if (detailsObj instanceof List) {
                                        List<ProductItem.Ratings.Detail> detailList = new ArrayList<>();
                                        List<Map<String, Object>> rawDetails = (List<Map<String, Object>>) detailsObj;
                                        for (Map<String, Object> detailMap : rawDetails) {
                                            ProductItem.Ratings.Detail detail = new ProductItem.Ratings.Detail();
                                            detail.setReviewId((String) detailMap.get("ReviewId"));
                                            detail.setComment((String) detailMap.get("Comment"));
                                            detail.setCustomerName((String) detailMap.get("CustomerName"));
                                            detail.setCreatedAt((String) detailMap.get("CreatedAt"));
                                            Object ratingVal = detailMap.get("Rating");
                                            if (ratingVal instanceof Number) {
                                                detail.setRating(((Number) ratingVal).intValue());
                                            }
                                            detailList.add(detail);
                                        }
                                        productItem.getRatings().setDetails(detailList);
                                    }
                                }

                                Log.d("ProductDetails", "ProductItem Ratings Average: " + (productItem.getRatings() != null ? productItem.getRatings().getAverage() : "null"));

                                viewPager.getAdapter().notifyDataSetChanged();
                                pagerAdapter.setProductData(productDescription, productDimensions, productItem);
                                viewPager.setAdapter(pagerAdapter);
                                viewPager.setCurrentItem(0, false);

                                String imageJson = documentSnapshot.getString("Image");
                                if (imageJson != null) {
                                    try {
                                        Gson gson = new Gson();
                                        Type listType = new TypeToken<List<String>>() {}.getType();
                                        imageUrls = gson.fromJson(imageJson, listType);
                                        setupImageViewPager();
                                    } catch (Exception e) {
                                        Log.e("ProductDetails", "Failed to load images: " + e.getMessage());
                                        Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                if (productItem.getRatings() != null && productItem.getRatings().getAverage() != null) {
                                    ratingLayout.setVisibility(View.VISIBLE);
                                    txtRating.setText(decimalFormat.format(productItem.getRatings().getAverage()));
                                } else {
                                    ratingLayout.setVisibility(View.VISIBLE);
                                    txtRating.setText("0.0");
                                }
                                checkWishlistStatus();
                            } else {
                                Log.w("ProductDetails", "Failed to parse ProductItem");
                                Toast.makeText(this, "Failed to load product data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("ProductDetails", "Product not found for ID: " + productId);
                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProductDetails", "Failed to load product details: " + e.getMessage());
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

    private void shareProductWithNameLink() {
        if (productItem == null || productItem.getName() == null) {
            Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encodedName = URLEncoder.encode(productItem.getName(), "UTF-8");

            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://evocasa.com/product?name=" + encodedName)) // dùng tên
                    .setDomainUriPrefix("https://evocasa.page.link")
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener(shortLink -> {
                        Uri shortUri = shortLink.getShortLink();
                        String shareText = productItem.getName() + "\n" + shortUri;

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                        startActivity(Intent.createChooser(shareIntent, "Share product via"));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to generate link", Toast.LENGTH_SHORT).show();
                        Log.e("DynamicLink", "Error: " + e.getMessage());
                    });

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Encoding error", Toast.LENGTH_SHORT).show();
            Log.e("DynamicLink", "Encoding error: " + e.getMessage());
        }
    }


    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("wishlistChanged", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    private void setupImageViewPager() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImagePagerAdapter imageAdapter = new ImagePagerAdapter(imageUrls);
            imageViewPager.setAdapter(imageAdapter);
            imageViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (txtImageIndex != null) {
                        txtImageIndex.setText((position + 1) + "/" + imageUrls.size());
                    }
                }
            });
            txtImageIndex.setText("1/" + imageUrls.size());
        } else {
            txtImageIndex.setText("0/0");
        }
    }

    private void loadSuggestedProducts() {
        db.collection("Product")
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductItem product = doc.toObject(ProductItem.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            if (product.getRatings() == null) {
                                product.setRatings(new ProductItem.Ratings());
                            }
                            Object ratingsObj = doc.get("Ratings");
                            if (ratingsObj instanceof Map) {
                                Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                                Object averageObj = ratingsMap.get("Average");
                                if (averageObj instanceof Number) {
                                    product.getRatings().setAverage(((Number) averageObj).doubleValue());
                                }
                            }

                            // Parse Image field
                            String imageJson = doc.getString("Image");
                            if (imageJson != null) {
                                try {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<String>>() {}.getType();
                                    List<String> images = gson.fromJson(imageJson, listType);
                                    if (images != null && !images.isEmpty()) {
                                        product.setImage(String.join(",", images)); // Store as comma-separated string
                                    }
                                } catch (Exception e) {
                                    Log.e("ProductDetails", "Failed to parse images for suggested product: " + e.getMessage());
                                }
                            }

                            // Explicitly set Name and Price
                            String name = doc.getString("Name");
                            Double price = doc.getDouble("Price");
                            if (name != null) {
                                product.setName(name); // Ensure Name is set
                            }
                            if (price != null) {
                                product.setPrice(price); // Ensure Price is set
                            }

                            // Log to debug
                            Log.d("ProductDetails", "Loaded Suggested Product: Name=" + product.getName() + ", Price=" + product.getPrice() + ", Image=" + product.getImage());

                            productList.add(product);
                        }
                    }
                    suggestedProductAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load suggested products", Toast.LENGTH_SHORT).show();
                });
    }

    private void showBuyNowBottomSheet() {
        String customerId = sessionManager.getUid();
        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "Please sign in to purchase", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.product_buy_now, null);
        bottomSheetDialog.setContentView(view);

        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        ImageButton btnIncrease = view.findViewById(R.id.btnIncrease);
        ImageButton btnDecrease = view.findViewById(R.id.btnDecrease);

        final int[] quantity = {1};
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

        Button btnBuyNow = view.findViewById(R.id.btnBuyNow);
        btnBuyNow.setOnClickListener(v -> {
            if (productItem == null) {
                Toast.makeText(this, "Product data not available", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                CartProduct cartProduct = new CartProduct();
                cartProduct.setId(productItem.getId() != null ? productItem.getId() : "");
                cartProduct.setName(productItem.getName() != null ? productItem.getName() : "Sản phẩm không tên");
                cartProduct.setPrice(productItem.getPrice() != null ? productItem.getPrice() : 0.0);
                cartProduct.setQuantity(quantity[0]);

                List<String> imageUrls = new ArrayList<>();
                String imageRaw = productItem.getImage();
                if (imageRaw != null && !imageRaw.isEmpty()) {
                    try {
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        imageUrls = new Gson().fromJson(imageRaw, listType);
                    } catch (Exception e) {
                        Log.e("BuyNow", "Failed to parse image JSON: " + e.getMessage());
                    }
                }
                cartProduct.setImageUrls(imageUrls);

                List<CartProduct> cartList = new ArrayList<>();
                cartList.add(cartProduct);
                String cartJson = new Gson().toJson(cartList);

                Log.d("BuyNowDebug", "Sending cart: " + cartJson);

                Intent intent = new Intent(ProductDetailsActivity.this, PaymentActivity.class);
                intent.putExtra("cartPayment", cartJson);
                startActivity(intent);
                bottomSheetDialog.dismiss();

            } catch (Exception ex) {
                Log.e("BuyNow", "Error preparing product for purchase: " + ex.getMessage(), ex);
                Toast.makeText(this, "Đã xảy ra lỗi khi xử lý sản phẩm", Toast.LENGTH_SHORT).show();
            }
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

        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnAddToCart.setOnClickListener(v -> {
            String customerId = sessionManager.getUid();
            if (customerId != null && productItem != null) {
                String productId = productItem.getId();
                db.collection("Customers")
                        .document(customerId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                                boolean productExists = false;
                                int existingIndex = -1;

                                if (cartList != null) {
                                    for (int i = 0; i < cartList.size(); i++) {
                                        Map<String, Object> item = cartList.get(i);
                                        if (item.get("productId") != null && item.get("productId").equals(productId)) {
                                            productExists = true;
                                            existingIndex = i;
                                            break;
                                        }
                                    }
                                }

                                if (productExists && existingIndex >= 0) {
                                    // Update existing cart item quantity
                                    Map<String, Object> updatedCart = new HashMap<>();
                                    updatedCart.put("productId", productId);
                                    int newQuantity = ((Number) cartList.get(existingIndex).get("cartQuantity")).intValue() + quantity[0];
                                    updatedCart.put("cartQuantity", newQuantity);

                                    List<Map<String, Object>> updatedCartList = new ArrayList<>(cartList);
                                    updatedCartList.set(existingIndex, updatedCart);

                                    db.collection("Customers")
                                            .document(customerId)
                                            .update("Cart", updatedCartList)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Increased quantity by " + quantity[0] + " item(s) in cart", Toast.LENGTH_SHORT).show();
                                                bottomSheetDialog.dismiss();
                                                refreshCartBadge(); // Update cart badge
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProductDetails", "Failed to update cart quantity: " + e.getMessage());
                                                Toast.makeText(this, "Failed to update cart quantity", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Add new cart item
                                    Map<String, Object> newCartItem = new HashMap<>();
                                    newCartItem.put("productId", productId);
                                    newCartItem.put("cartQuantity", quantity[0]);

                                    db.collection("Customers")
                                            .document(customerId)
                                            .update("Cart", FieldValue.arrayUnion(newCartItem))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Added " + quantity[0] + " item(s) to cart", Toast.LENGTH_SHORT).show();
                                                bottomSheetDialog.dismiss();
                                                refreshCartBadge(); // Update cart badge
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProductDetails", "Failed to add to cart: " + e.getMessage());
                                                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Log.w("ProductDetails", "Customer document not found for ID: " + customerId);
                                Toast.makeText(this, "Customer data not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProductDetails", "Failed to check cart: " + e.getMessage());
                            Toast.makeText(this, "Failed to check cart", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
            }
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
        btnFavorite = findViewById(R.id.btnFavorite);
        ratingLayout = findViewById(R.id.linearLayoutRating);
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
        cleanupCartListener();
    }

    public void updateViewPagerHeight() {
        if (viewPagerHelper != null) {
            viewPagerHelper.updateHeight();
        }
    }

    public void forceRefreshCurrentFragment() {
        if (pagerAdapter != null) {
            int position = viewPager.getCurrentItem();
            Fragment currentFragment = pagerAdapter.getCurrentFragment(position);
            if (currentFragment instanceof ReviewsFragment) {
                ((ReviewsFragment) currentFragment).refreshReviews();
            }
            updateViewPagerHeight();
        }
    }

    private void checkWishlistStatus() {
        String customerId = sessionManager.getUid();
        if (customerId != null && productId != null) {
            db.collection("Wishlist")
                    .whereEqualTo("Customer_id", customerId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            List<String> productIds = (List<String>) document.get("Productid");
                            if (productIds != null && productIds.contains(productId)) {
                                btnFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                                break;
                            } else {
                                btnFavorite.setImageResource(R.drawable.ic_favourite);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        btnFavorite.setImageResource(R.drawable.ic_favourite);
                    });
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favourite);
        }

        btnFavorite.setOnClickListener(v -> {
            String customerIdClick = sessionManager.getUid();
            if (customerIdClick != null && productId != null) {
                toggleWishlist(customerIdClick, productId);
            } else {
                Toast.makeText(this, "Please sign in to add to wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWishlist(String customerId, String productId) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        createWishlistAndAddProduct(customerId, productId);
                    } else {
                        DocumentSnapshot wishlistDoc = querySnapshot.getDocuments().get(0);
                        String wishlistId = wishlistDoc.getId();
                        List<String> productIds = (List<String>) wishlistDoc.get("Productid");

                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }

                        if (productIds.contains(productId)) {
                            removeProductFromWishlist(wishlistId, productId);
                        } else {
                            addProductToWishlist(wishlistId, productId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void createWishlistAndAddProduct(String customerId, String productId) {
        Map<String, Object> wishlistData = new HashMap<>();
        wishlistData.put("Customer_id", customerId);
        wishlistData.put("CreatedAt", FieldValue.serverTimestamp());
        List<String> productIds = new ArrayList<>();
        productIds.add(productId);
        wishlistData.put("Productid", productIds);

        db.collection("Wishlist")
                .add(wishlistData)
                .addOnSuccessListener(documentReference -> {
                    btnFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                    Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void addProductToWishlist(String wishlistId, String productId) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }

                        if (!productIds.contains(productId)) {
                            productIds.add(productId);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Productid", productIds);

                            db.collection("Wishlist").document(wishlistId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        btnFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                                        Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeProductFromWishlist(String wishlistId, String productId) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds != null && productIds.contains(productId)) {
                            productIds.remove(productId);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Productid", productIds);

                            db.collection("Wishlist").document(wishlistId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        btnFavorite.setImageResource(R.drawable.ic_favourite);
                                        Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void startCartBadgeListener() {
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "User not logged in, hiding badge");
            if (txtCartBadge != null) {
                txtCartBadge.setVisibility(View.GONE);
            }
            return;
        }

        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (isFinishing() || isDestroyed()) {
                        Log.d("CartBadge", "Activity finishing or destroyed, skipping");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
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
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        if (txtCartBadge == null) {
            Log.w("CartBadge", "Cart badge view is null, cannot update");
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            if (isFinishing() || isDestroyed() || txtCartBadge == null) {
                Log.d("CartBadge", "Activity finishing or destroyed, skipping");
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

    private void updateCartBadge(int totalQuantity) {
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (isFinishing() || isDestroyed() || txtCartBadge == null) {
                Log.d("CartBadge", "Activity finishing or destroyed, skipping");
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

    public void refreshCartBadge() {
        if (isFinishing() || isDestroyed()) {
            Log.d("CartBadge", "Activity finishing or destroyed, skipping");
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "Cannot refresh badge - user not logged in");
            safeUpdateCartBadge(0);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isFinishing() || isDestroyed()) return;

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
                    if (isFinishing() || isDestroyed()) return;
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    safeUpdateCartBadge(0);
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CartBadge", "Activity onStart()");
        if (sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CartBadge", "Activity onResume()");
        if (cartListener == null && sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CartBadge", "Activity onStop()");
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