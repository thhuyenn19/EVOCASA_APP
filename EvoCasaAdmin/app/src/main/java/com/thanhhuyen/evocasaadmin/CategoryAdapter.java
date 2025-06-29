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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "CategoryAdapter";
    private final Context context;
    private List<String> categoryIds;
    private Map<String, Category> categories;
    private Map<String, List<Product>> productsByCategory;
    private Map<String, Boolean> expandedStates;

    public CategoryAdapter(Context context) {
        this.context = context;
        this.categoryIds = new ArrayList<>();
        this.categories = new HashMap<>();
        this.productsByCategory = new HashMap<>();
        this.expandedStates = new HashMap<>();
    }

    public void updateData(Map<String, List<Product>> newProductsByCategory) {
        Log.d(TAG, "updateData: Updating with " + newProductsByCategory.size() + " categories");
        
        // Clear existing data
        productsByCategory.clear();
        categoryIds.clear();
        expandedStates.clear();

        // Load categories for the products
        FirebaseFirestore.getInstance()
            .collection("Category")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                categories.clear();
                querySnapshot.forEach(doc -> {
                    Category category = doc.toObject(Category.class);
                    category.setId(doc.getId());
                    categories.put(category.getId(), category);
                });

                // Update data
                productsByCategory.putAll(newProductsByCategory);
                categoryIds.addAll(newProductsByCategory.keySet());
                
                // Initialize expansion state
                for (String categoryId : categoryIds) {
                    expandedStates.put(categoryId, false);
                }

                Log.d(TAG, "updateData: Loaded " + categories.size() + " categories");
                notifyDataSetChanged();
            })
            .addOnFailureListener(e -> 
                Log.e(TAG, "updateData: Error loading categories: " + e.getMessage()));
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryId = categoryIds.get(position);
        Category category = categories.get(categoryId);
        List<Product> products = productsByCategory.get(categoryId);
        
        if (category == null || products == null) {
            Log.e(TAG, "onBindViewHolder: Category or products null for position " + position);
            return;
        }

        Log.d(TAG, "onBindViewHolder: Binding category " + category.getName() + 
            " with " + products.size() + " products");

        // Set category name
        holder.categoryName.setText(category.getName());

        // Set up product list
        ProductAdapter productAdapter = new ProductAdapter(context, products);
        holder.productsRecyclerView.setAdapter(productAdapter);
        holder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Handle expansion state
        boolean isExpanded = expandedStates.getOrDefault(categoryId, false);
        holder.productsRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // Rotate arrow based on expansion state
        float rotation = isExpanded ? 180f : 0f;
        holder.expandIcon.setRotation(rotation);

        // Set click listener for the header
        holder.categoryHeader.setOnClickListener(v -> {
            boolean newState = !expandedStates.getOrDefault(categoryId, false);
            expandedStates.put(categoryId, newState);
            
            // Animate the expansion
            RotateAnimation rotate = new RotateAnimation(
                rotation, newState ? 180f : 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            holder.expandIcon.startAnimation(rotate);
            
            holder.productsRecyclerView.setVisibility(newState ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return categoryIds.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View categoryHeader;
        TextView categoryName;
        ImageView expandIcon;
        RecyclerView productsRecyclerView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryHeader = itemView.findViewById(R.id.categoryHeader);
            categoryName = itemView.findViewById(R.id.categoryName);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            productsRecyclerView = itemView.findViewById(R.id.productsRecyclerView);
        }
    }
} 