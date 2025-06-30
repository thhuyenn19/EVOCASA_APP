package com.thanhhuyen.evocasaadmin;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.thanhhuyen.models.Category;
import com.thanhhuyen.models.Product;

import java.util.ArrayList;
import java.util.List;

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
        void onProductsLoaded(List<Product> products);
        void onError(String error);
    }

    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);
        void onError(String error);
    }

    public interface OnSubcategoriesLoadedListener {
        void onSubcategoriesLoaded(List<Category> subcategories);
        void onError(String error);
    }

    public interface OnProductLoadedListener {
        void onProductLoaded(Product product);
        void onError(String error);
    }

    public interface OnProductUpdateListener {
        void onSuccess();
        void onError(String error);
    }

    // Method to load only Category Parent
    public void loadCategories(OnCategoriesLoadedListener listener) {
        db.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Category category = doc.toObject(Category.class);
                        if (category != null) {
                            category.setId(doc.getId());
                            // Check if this is a parent category (no ParentCategory field)
                            if (category.getParentCategory() == null || category.getParentId() == null) {
                                categories.add(category);
                                Log.d(TAG, "Loaded parent category: " + category.getName());
                            }
                        }
                    }
                    listener.onCategoriesLoaded(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories", e);
                    listener.onError(e.getMessage());
                });
    }

    // Method to load Subcategories by Parent Category ID
    public void loadSubcategoriesByCategory(String categoryId, OnSubcategoriesLoadedListener listener) {
        db.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Category> subcategories = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Category subcategory = doc.toObject(Category.class);
                        if (subcategory != null) {
                            subcategory.setId(doc.getId());
                            // Check if this category has the specified parent
                            String parentId = subcategory.getParentId();
                            if (parentId != null && parentId.equals(categoryId)) {
                                subcategories.add(subcategory);
                                Log.d(TAG, "Loaded subcategory: " + subcategory.getName() + " for parent: " + categoryId);
                            }
                        }
                    }
                    listener.onSubcategoriesLoaded(subcategories);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load subcategories: " + e.getMessage());
                });
    }

    // Method to load Products for a given Category
    public void loadProductsBySubcategory(String subcategoryId, OnProductsLoadedListener listener) {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> products = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            String productCategoryId = product.getCategoryId();
                            if (productCategoryId != null && productCategoryId.equals(subcategoryId)) {
                                products.add(product);
                                Log.d(TAG, "Loaded product: " + product.getName() + " for category: " + subcategoryId);
                            }
                        }
                    }
                    listener.onProductsLoaded(products);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load products: " + e.getMessage());
                });
    }

    // Method to get a product by ID
    public void getProductById(String productId, OnProductLoadedListener listener) {
        db.collection("Product")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            product.setId(documentSnapshot.getId());
                            listener.onProductLoaded(product);
                        } else {
                            listener.onError("Product object is null");
                        }
                    } else {
                        listener.onError("Product not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProductById: Failed to fetch product", e);
                    listener.onError("Failed to load product: " + e.getMessage());
                });
    }

    public void updateProduct(Product product, OnProductUpdateListener listener) {
        if (product == null || product.getId() == null) {
            listener.onError("Invalid product data");
            return;
        }

        db.collection("Product")
                .document(product.getId())
                .update(
                        "price", product.getPrice(),
                        "quantity", product.getQuantity()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product updated successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating product", e);
                    listener.onError(e.getMessage());
                });
    }
}
