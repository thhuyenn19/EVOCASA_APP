package com.thanhhuyen.evocasaadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.thanhhuyen.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private ImageView productImage;
    private TextView productName;
    private TextView productDescription;
    private TextView productCategory;
    private TextView productOrigin;
    private TextView productDimension;
    private TextView productQuantity;
    private TextView productPrice;
    private ImageButton editButton;
    private ImageButton viewButton;
    private View btnBack;

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Log.d(TAG, "onCreate: Starting ProductDetailActivity");

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        initializeViews();

        // Get product ID from intent
        String productId = getIntent().getStringExtra("product_id");
        Log.d(TAG, "Product ID from intent: " + productId);

        if (productId != null && !productId.isEmpty()) {
            loadProductDetails(productId);
        } else {
            Log.e(TAG, "No product ID provided in intent");
            Toast.makeText(this, "Error: Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Finding views by ID");

        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productDescription = findViewById(R.id.productDescription);
        productCategory = findViewById(R.id.productCategory);
        productOrigin = findViewById(R.id.productOrigin);
        productDimension = findViewById(R.id.productDimension);
        productQuantity = findViewById(R.id.productQuantityBadge);
        productPrice = findViewById(R.id.productPrice);
        editButton = findViewById(R.id.editButton);
        viewButton = findViewById(R.id.viewButton);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack == null) {
            Log.e(TAG, "Back button view not found!");
        }

        // Set click listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });
        }

        editButton.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked");
            Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        if (viewButton != null) {
            viewButton.setVisibility(View.GONE); // Hide view button in detail view
        }
    }

    private void loadProductDetails(String productId) {
        Log.d(TAG, "loadProductDetails: Loading details for product ID: " + productId);

        firebaseManager.getProductById(productId, new FirebaseManager.OnProductLoadedListener() {
            @Override
            public void onProductLoaded(Product product) {
                Log.d(TAG, "onProductLoaded: Product loaded successfully");
                if (product != null) {
                    runOnUiThread(() -> updateUI(product));
                } else {
                    Log.e(TAG, "onProductLoaded: Product is null");
                    runOnUiThread(() -> {
                        Toast.makeText(ProductDetailActivity.this,
                                "Error: Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: Failed to load product: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(ProductDetailActivity.this,
                            "Error loading product: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void updateUI(Product product) {
        Log.d(TAG, "updateUI: Updating UI with product data: " + product.getName());

        // Set product name
        productName.setText(product.getName());

        // Set product description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            productDescription.setText(product.getDescription());
            productDescription.setVisibility(View.VISIBLE);
        } else {
            productDescription.setVisibility(View.GONE);
        }

        // Set category
        productCategory.setText(product.getCategoryId());

        // Set origin
        if (product.getOrigin() != null && !product.getOrigin().isEmpty()) {
            productOrigin.setText(product.getOrigin());
            productOrigin.setVisibility(View.VISIBLE);
        } else {
            productOrigin.setVisibility(View.GONE);
        }

        // Set dimension
        if (product.getDimension() != null && !product.getDimension().isEmpty()) {
            productDimension.setText(product.getDimension());
            productDimension.setVisibility(View.VISIBLE);
        } else {
            productDimension.setVisibility(View.GONE);
        }

        // Set quantity
        productQuantity.setText("In Stock: " + product.getQuantity());

        // Format and set price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        productPrice.setText(currencyFormat.format(product.getPrice()));

        // Load first image if available
        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0);
            Log.d(TAG, "Loading product image: " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(productImage);
        } else {
            Log.d(TAG, "No image available for product");
            productImage.setImageResource(android.R.color.darker_gray);
        }
    }
} 