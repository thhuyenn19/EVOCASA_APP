package com.thanhhuyen.evocasaadmin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "CategoryAdapter";
    private final Context context;
    private List<Category> categories;
    private final FirebaseFirestore db;
    private Map<String, Boolean> expandedStates;
    private Map<String, List<Product>> productsByCategory;

    public CategoryAdapter(Context context) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.expandedStates = new HashMap<>();
        this.productsByCategory = new HashMap<>();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        // Initialize expansion states for all categories
        for (Category category : categories) {
            if (!expandedStates.containsKey(category.getId())) {
                expandedStates.put(category.getId(), false);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        if (category == null) {
            Log.e(TAG, "onBindViewHolder: Category null for position " + position);
            return;
        }

        Log.d(TAG, "Binding category: " + category.getName() + " with ID: " + category.getId());

        // Set category name
        holder.categoryName.setText(category.getName());

        // Set up expansion state
        boolean isExpanded = expandedStates.getOrDefault(category.getId(), false);
        holder.productsRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // Rotate arrow based on expansion state
        float rotation = isExpanded ? 180f : 0f;
        holder.expandIcon.setRotation(rotation);

        // Set up products RecyclerView
        Log.d(TAG, "Setting up RecyclerView for category: " + category.getName());
        holder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<Product> products = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());

// Nếu adapter đã tồn tại → cập nhật dữ liệu
        ProductAdapter adapter = (ProductAdapter) holder.productsRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateProducts(products);
        } else {
            adapter = new ProductAdapter(context, products);
            holder.productsRecyclerView.setAdapter(adapter);
        }


        // Handle expand icon click
        holder.expandIcon.setOnClickListener(v -> {
            Log.d(TAG, "Expand icon clicked for category: " + category.getName() + " (ID: " + category.getId() + ")");
            boolean newState = !expandedStates.getOrDefault(category.getId(), false);
            expandedStates.put(category.getId(), newState);

            // Animate the arrow rotation
            RotateAnimation rotate = new RotateAnimation(
                rotation, newState ? 180f : 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            holder.expandIcon.startAnimation(rotate);

            // If expanding and no products loaded yet, load them
            if (newState && !productsByCategory.containsKey(category.getId())) {
                Log.d(TAG, "Loading products for category: " + category.getName());
                loadProductsForCategory(category.getId(), holder.productsRecyclerView);
            }

            // Show/hide products
            holder.productsRecyclerView.setVisibility(newState ? View.VISIBLE : View.GONE);
        });
        Log.d(TAG, "Final product count for category " + category.getName() + ": " + productsByCategory.getOrDefault(category.getId(), new ArrayList<>()).size());
    }

    private void loadProductsForCategory(String categoryId, RecyclerView recyclerView) {
        Log.d(TAG, "Starting to load products for category ID: " + categoryId);
        
        // Check if products are already loaded
        if (productsByCategory.containsKey(categoryId)) {
            Log.d(TAG, "Products already loaded for category " + categoryId);
            return;
        }

        Log.d(TAG, "Making Firestore query for products with category_id: " + categoryId);
        db.collection("Product")
            .get()  // First get all products to debug
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Firestore query successful. Found " + querySnapshot.size() + " total products");
                
                List<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        Log.d(TAG, "Processing product document: " + doc.getId());
                        Map<String, Object> data = doc.getData();
                        Log.d(TAG, "Raw product data: " + (data != null ? data.toString() : "null"));
                        
                        // Debug category_id field specifically
                        Object categoryIdObj = data != null ? data.get("category_id") : null;
                        Log.d(TAG, "Category ID in document: " + (categoryIdObj != null ? categoryIdObj.toString() : "null"));
                        
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            String productCategoryId = product.getCategoryId();
                            Log.d(TAG, "Mapped product: " + product.getName() + 
                                ", Category ID from getter: " + productCategoryId);
                            
                            if (categoryId.equals(productCategoryId)) {
                                product.setId(doc.getId());
                                products.add(product);
                                Log.d(TAG, "Added product to list: " + product.getName());
                            }
                        } else {
                            Log.e(TAG, "Failed to convert document to Product: " + doc.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing product document: " + doc.getId(), e);
                        e.printStackTrace();
                    }
                }
                
                Log.d(TAG, "Successfully filtered " + products.size() + " products for category " + categoryId);
                productsByCategory.put(categoryId, products);
                
                // Update RecyclerView on the main thread
                recyclerView.post(() -> {
                    ProductAdapter adapter = (ProductAdapter) recyclerView.getAdapter();
                    if (adapter != null) {
                        Log.d(TAG, "Updating ProductAdapter with " + products.size() + " products");
                        adapter.updateProducts(products);
                    } else {
                        Log.e(TAG, "ProductAdapter is null for category " + categoryId);
                        // Create new adapter if null
                        adapter = new ProductAdapter(context, products);
                        recyclerView.setAdapter(adapter);
                        Log.d(TAG, "Created new ProductAdapter for category " + categoryId);
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading products for category " + categoryId, e);
                Log.e(TAG, "Error details: " + e.getMessage());
                e.printStackTrace();
            });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView expandIcon;
        RecyclerView productsRecyclerView;
        View categoryHeader;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            productsRecyclerView = itemView.findViewById(R.id.productsRecyclerView);
            categoryHeader = itemView.findViewById(R.id.categoryHeader);
        }
    }
} 