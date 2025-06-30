package com.thanhhuyen.evocasaadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.adapters.CategoryAdapter;
import com.thanhhuyen.models.Category;
import com.thanhhuyen.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private static final String TAG = "ProductActivity";

    private RecyclerView categoriesRecyclerView;
    private ProgressBar loadingIndicator;
    private CategoryAdapter categoryAdapter;
    private FirebaseManager firebaseManager;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        initializeViews();

        // Load Category Parent
        loadCategories();
    }

    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this);
        categoryAdapter.setOnCategoryClickListener(this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadCategories() {
        loadingIndicator.setVisibility(View.VISIBLE);
        firebaseManager.loadCategories(new FirebaseManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                loadingIndicator.setVisibility(View.GONE);
                if (categories.isEmpty()) {
                    Toast.makeText(ProductActivity.this, "No categories found", Toast.LENGTH_SHORT).show();
                } else {
                    categoryList.clear();
                    categoryList.addAll(categories);
                    categoryAdapter.setCategories(categories);
                }
            }

            @Override
            public void onError(String error) {
                loadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Error loading categories: " + error);
                Toast.makeText(ProductActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        // This method will be called when either a parent category or subcategory is clicked
        Log.d(TAG, "Category clicked: " + category.getName());
    }
}
