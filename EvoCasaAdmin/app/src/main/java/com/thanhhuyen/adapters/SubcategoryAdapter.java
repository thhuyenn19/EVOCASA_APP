package com.thanhhuyen.adapters;

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

import com.thanhhuyen.evocasaadmin.R;
import com.thanhhuyen.models.Category;
import com.thanhhuyen.models.Product;
import com.thanhhuyen.evocasaadmin.FirebaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubcategoryAdapter extends RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder> {
    private static final String TAG = "SubcategoryAdapter";
    private final Context context;
    private List<Category> subcategories;
    private final OnSubcategoryClickListener listener;
    private Map<String, Boolean> expandedStates;
    private Map<String, List<Product>> productsByCategory;
    private FirebaseManager firebaseManager;

    // Interface for handling subcategory clicks
    public interface OnSubcategoryClickListener {
        void onSubcategoryClick(Category subcategory);
    }

    public SubcategoryAdapter(Context context, List<Category> subcategories, OnSubcategoryClickListener listener) {
        this.context = context;
        this.subcategories = subcategories;
        this.listener = listener;
        this.expandedStates = new HashMap<>();
        this.productsByCategory = new HashMap<>();
        this.firebaseManager = FirebaseManager.getInstance();
    }

    @NonNull
    @Override
    public SubcategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subcategory, parent, false);
        return new SubcategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubcategoryViewHolder holder, int position) {
        Category subcategory = subcategories.get(position);
        if (subcategory != null) {
            holder.subcategoryName.setText(subcategory.getName());

            // Set up expansion state
            boolean isExpanded = expandedStates.getOrDefault(subcategory.getId(), false);
            holder.productsRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Rotate arrow based on expansion state
            float rotation = isExpanded ? 180f : 0f;
            holder.expandIcon.setRotation(rotation);

            // Set up products RecyclerView
            if (holder.productsRecyclerView.getLayoutManager() == null) {
                holder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            }

            // Create or update products adapter
            ProductAdapter productAdapter;
            List<Product> products = productsByCategory.getOrDefault(subcategory.getId(), new ArrayList<>());
            
            if (holder.productsRecyclerView.getAdapter() == null) {
                productAdapter = new ProductAdapter(context, products);
                holder.productsRecyclerView.setAdapter(productAdapter);
            } else {
                productAdapter = (ProductAdapter) holder.productsRecyclerView.getAdapter();
                productAdapter.updateProducts(products);
            }

            // Handle expand icon click
            holder.expandIcon.setOnClickListener(v -> {
                boolean newState = !expandedStates.getOrDefault(subcategory.getId(), false);
                expandedStates.put(subcategory.getId(), newState);

                // Animate the arrow rotation
                RotateAnimation rotate = new RotateAnimation(
                        rotation, newState ? 180f : 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotate.setDuration(300);
                rotate.setFillAfter(true);
                holder.expandIcon.startAnimation(rotate);

                if (newState) {
                    // Load products when expanding
                    loadProductsForCategory(subcategory.getId(), holder.productsRecyclerView);
                }

                // Show/hide products
                holder.productsRecyclerView.setVisibility(newState ? View.VISIBLE : View.GONE);
            });

            // Handle subcategory click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubcategoryClick(subcategory);
                }
            });
        }
    }

    private void loadProductsForCategory(String categoryId, RecyclerView recyclerView) {
        Log.d(TAG, "Loading products for category ID: " + categoryId);
        
        // Check if products are already loaded
        if (productsByCategory.containsKey(categoryId) && !productsByCategory.get(categoryId).isEmpty()) {
            Log.d(TAG, "Products already loaded for category " + categoryId);
            return;
        }

        firebaseManager.loadProductsBySubcategory(categoryId, new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                Log.d(TAG, "Loaded " + products.size() + " products for category " + categoryId);
                productsByCategory.put(categoryId, products);
                
                // Update RecyclerView on the main thread
                recyclerView.post(() -> {
                    ProductAdapter adapter = (ProductAdapter) recyclerView.getAdapter();
                    if (adapter != null) {
                        adapter.updateProducts(products);
                    } else {
                        adapter = new ProductAdapter(context, products);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading products for category " + categoryId + ": " + error);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subcategories != null ? subcategories.size() : 0;
    }

    public void updateSubcategories(List<Category> newSubcategories) {
        this.subcategories = newSubcategories;
        notifyDataSetChanged();
    }

    static class SubcategoryViewHolder extends RecyclerView.ViewHolder {
        TextView subcategoryName;
        ImageView expandIcon;
        RecyclerView productsRecyclerView;

        SubcategoryViewHolder(View itemView) {
            super(itemView);
            subcategoryName = itemView.findViewById(R.id.subcategoryName);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            productsRecyclerView = itemView.findViewById(R.id.productsRecyclerView);
        }
    }
}
