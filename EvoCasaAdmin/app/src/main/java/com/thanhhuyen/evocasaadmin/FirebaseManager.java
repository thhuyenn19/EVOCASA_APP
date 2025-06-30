package com.thanhhuyen.evocasaadmin;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.thanhhuyen.models.Category;
import com.thanhhuyen.models.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private final FirebaseFirestore db;

    // List of sample category names to be removed
    private final List<String> SAMPLE_CATEGORY_NAMES = Arrays.asList("Nhà phố", "Chung cư", "Biệt thự");

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

    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);
        void onError(String error);
    }

    private void deleteSampleCategories(Runnable onComplete) {
        db.collection("Category")
                .whereIn("name", SAMPLE_CATEGORY_NAMES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        int[] deletedCount = {0};
                        int totalToDelete = querySnapshot.size();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            db.collection("Category")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        deletedCount[0]++;
                                        Log.d(TAG, "deleteSampleCategories: Deleted sample category: " + document.get("name"));
                                        if (deletedCount[0] == totalToDelete) {
                                            Log.d(TAG, "deleteSampleCategories: All sample categories deleted");
                                            onComplete.run();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "deleteSampleCategories: Error deleting category", e));
                        }
                    } else {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteSampleCategories: Error querying sample categories", e);
                    onComplete.run();
                });
    }

    public void loadProducts(OnProductsLoadedListener listener) {
        Log.d(TAG, "loadProducts: Starting to load products");
        loadProductsWithCategories(listener);
    }

    private void loadProductsWithCategories(OnProductsLoadedListener listener) {
        db.collection("Category")
                .orderBy("order")
                .get()
                .addOnSuccessListener(categoryQuerySnapshot -> {
                    Log.d(TAG, "loadProductsWithCategories: Categories loaded, count: " + categoryQuerySnapshot.size());

                    Map<String, List<Product>> productsByCategory = new HashMap<>();
                    List<Category> categories = new ArrayList<>();

                    for (QueryDocumentSnapshot document : categoryQuerySnapshot) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        categories.add(category);
                        productsByCategory.put(category.getId(), new ArrayList<>());
                        Log.d(TAG, "loadProductsWithCategories: Added category: " + category.getName() +
                                " (ID: " + category.getId() + ")");
                    }

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

    public void loadCategories(OnCategoriesLoadedListener listener) {
        deleteSampleCategories(() -> {
            db.collection("Category")
//                    .orderBy("order")  // Sort by order field
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Category> categories = new ArrayList<>();

                        Log.d(TAG, "Checking raw data from Firestore:");
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Log.d(TAG, "Raw data: " + doc.getData());  // ✅ Kiểm tra dữ liệu gốc

                            Category category = doc.toObject(Category.class);
                            if (category != null) {
                                category.setId(doc.getId());
                                categories.add(category);
                                Log.d(TAG, "loadCategories: Loaded category: " + category.getName());
                            }
                        }

                        Log.d(TAG, "loadCategories: Total categories loaded: " + categories.size());
                        listener.onCategoriesLoaded(categories);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "loadCategories: Error loading categories", e);
                        listener.onError(e.getMessage());
                    });
        });
    }
}
