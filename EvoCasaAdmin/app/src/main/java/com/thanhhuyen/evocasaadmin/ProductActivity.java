package com.thanhhuyen.evocasaadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "ProductActivity";
    private RecyclerView categoriesRecyclerView;
    private ProgressBar loadingIndicator;
    private CategoryAdapter categoryAdapter;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Log.d(TAG, "onCreate: Initializing ProductActivity");

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        initializeViews();

        // Load data
        loadData();
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Setting up views");
        
        // Find views
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        ImageButton backButton = findViewById(R.id.backButton);

        // Setup RecyclerView
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Setup back button
        backButton.setOnClickListener(v -> finish());
    }

    private void loadData() {
        Log.d(TAG, "loadData: Starting to load data");
        loadingIndicator.setVisibility(View.VISIBLE);

        firebaseManager.loadProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(Map<String, List<Product>> productsByCategory) {
                Log.d(TAG, "onProductsLoaded: Products loaded successfully. Categories count: " 
                    + productsByCategory.size());
                
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (productsByCategory.isEmpty()) {
                        Log.d(TAG, "onProductsLoaded: No products found");
                        Toast.makeText(ProductActivity.this, 
                            "No products found", Toast.LENGTH_SHORT).show();
                    } else {
                        categoryAdapter.updateData(productsByCategory);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: Failed to load products: " + error);
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(ProductActivity.this, 
                        "Error loading products: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 