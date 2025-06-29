package com.thanhhuyen.evocasaadmin;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private final FirebaseFirestore db;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public interface OnProductsLoadedListener {
        void onProductsLoaded(Map<String, List<Product>> productsByCategory);
        void onError(String error);
    }

    public void loadProducts(OnProductsLoadedListener listener) {
        Log.d(TAG, "loadProducts: Starting to load products");
        
        // First check if we have any categories
        db.collection("Category")
            .get()
            .addOnSuccessListener(categoryQuerySnapshot -> {
                Log.d(TAG, "loadProducts: Total categories found: " + categoryQuerySnapshot.size());
                
                if (categoryQuerySnapshot.isEmpty()) {
                    // No categories exist, create sample categories
                    createSampleCategories(() -> {
                        // Retry loading after creating samples
                        loadProductsWithCategories(listener);
                    });
                } else {
                    // Categories exist, proceed with normal loading
                    loadProductsWithCategories(listener);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "loadProducts: Error checking categories", e);
                listener.onError("Failed to check categories: " + e.getMessage());
            });
    }

    private void loadProductsWithCategories(OnProductsLoadedListener listener) {
        db.collection("Category")
            .orderBy("order")
            .get()
            .addOnSuccessListener(categoryQuerySnapshot -> {
                Log.d(TAG, "loadProductsWithCategories: Categories loaded, count: " + categoryQuerySnapshot.size());
                
                Map<String, List<Product>> productsByCategory = new HashMap<>();
                List<Category> categories = new ArrayList<>();

                // Process all categories
                for (QueryDocumentSnapshot document : categoryQuerySnapshot) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categories.add(category);
                    productsByCategory.put(category.getId(), new ArrayList<>());
                    Log.d(TAG, "loadProductsWithCategories: Added category: " + category.getName() + 
                        " (ID: " + category.getId() + ")");
                }

                // Then load all products
                db.collection("Product")
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnSuccessListener(productQuerySnapshot -> {
                        Log.d(TAG, "loadProductsWithCategories: Products loaded, count: " + 
                            productQuerySnapshot.size());

                        for (QueryDocumentSnapshot document : productQuerySnapshot) {
                            try {
                                Product product = document.toObject(Product.class);
                                product.setId(document.getId());
                                
                                String categoryId = product.getCategoryId();
                                if (categoryId != null && productsByCategory.containsKey(categoryId)) {
                                    productsByCategory.get(categoryId).add(product);
                                    Log.d(TAG, "loadProductsWithCategories: Added product " + 
                                        product.getName() + " to category " + categoryId);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "loadProductsWithCategories: Error processing product", e);
                            }
                        }

                        listener.onProductsLoaded(productsByCategory);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "loadProductsWithCategories: Error loading products", e);
                        listener.onError("Failed to load products: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "loadProductsWithCategories: Error loading categories", e);
                listener.onError("Failed to load categories: " + e.getMessage());
            });
    }

    private void createSampleCategories(Runnable onComplete) {
        Log.d(TAG, "createSampleCategories: Creating sample categories");

        List<Category> sampleCategories = new ArrayList<>();
        
        // Create sample categories
        Category cat1 = new Category();
        cat1.setName("Nhà phố");
        cat1.setDescription("Các dự án nhà phố");
        cat1.setOrder(1);
        cat1.setActive(true);
        cat1.setCreatedAt(System.currentTimeMillis());
        cat1.setUpdatedAt(System.currentTimeMillis());
        sampleCategories.add(cat1);

        Category cat2 = new Category();
        cat2.setName("Chung cư");
        cat2.setDescription("Các dự án chung cư");
        cat2.setOrder(2);
        cat2.setActive(true);
        cat2.setCreatedAt(System.currentTimeMillis());
        cat2.setUpdatedAt(System.currentTimeMillis());
        sampleCategories.add(cat2);

        Category cat3 = new Category();
        cat3.setName("Biệt thự");
        cat3.setDescription("Các dự án biệt thự");
        cat3.setOrder(3);
        cat3.setActive(true);
        cat3.setCreatedAt(System.currentTimeMillis());
        cat3.setUpdatedAt(System.currentTimeMillis());
        sampleCategories.add(cat3);

        // Counter for tracking completion
        final int[] completedCount = {0};
        final int totalCategories = sampleCategories.size();

        // Add categories to Firestore
        for (Category category : sampleCategories) {
            db.collection("Category")
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "createSampleCategories: Added category: " + category.getName());
                    completedCount[0]++;
                    if (completedCount[0] == totalCategories) {
                        Log.d(TAG, "createSampleCategories: All sample categories created");
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "createSampleCategories: Error adding category: " + 
                        category.getName(), e));
        }
    }
} 