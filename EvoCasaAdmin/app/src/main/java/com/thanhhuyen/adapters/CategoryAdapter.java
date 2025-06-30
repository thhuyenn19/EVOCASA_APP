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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.thanhhuyen.models.Product;
import com.thanhhuyen.evocasaadmin.R;
import com.thanhhuyen.models.Category;
import com.thanhhuyen.evocasaadmin.FirebaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "CategoryAdapter";
    private final Context context;
    private List<Category> parentCategories;
    private final FirebaseFirestore db;
    private Map<String, Boolean> expandedStates;
    private Map<String, List<Product>> productsByCategory;
    private Map<String, List<Category>> subcategoriesByParent;
    private List<Category> allCategories;
    private OnCategoryClickListener listener;
    private FirebaseManager firebaseManager;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context) {
        this.context = context;
        this.parentCategories = new ArrayList<>();
        this.allCategories = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.expandedStates = new HashMap<>();
        this.productsByCategory = new HashMap<>();
        this.subcategoriesByParent = new HashMap<>();
        this.firebaseManager = FirebaseManager.getInstance();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.allCategories = categories;
        Log.d(TAG, "Setting categories, total: " + categories.size());

        // Organize categories by parent
        subcategoriesByParent.clear();
        parentCategories.clear();

        for (Category category : categories) {
            String parentId = category.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                // This is a parent category
                parentCategories.add(category);
                Log.d(TAG, "Added parent category: " + category.getName() + " (ID: " + category.getId() + ")");
            } else {
                // This is a subcategory
                if (!subcategoriesByParent.containsKey(parentId)) {
                    subcategoriesByParent.put(parentId, new ArrayList<>());
                }
                subcategoriesByParent.get(parentId).add(category);
                Log.d(TAG, "Added subcategory: " + category.getName() + " to parent: " + parentId);
            }
        }

        // Initialize expansion states
        expandedStates.clear();
        for (Category category : categories) {
            expandedStates.put(category.getId(), false);
        }

        notifyDataSetChanged();
        Log.d(TAG, "Categories set: " + parentCategories.size() + " parent categories");
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category parentCategory = parentCategories.get(position);
        if (parentCategory == null) {
            Log.e(TAG, "onBindViewHolder: Category null for position " + position);
            return;
        }

        Log.d(TAG, "Binding parent category: " + parentCategory.getName() + " with ID: " + parentCategory.getId());

        // Set category name
        holder.categoryName.setText(parentCategory.getName());

        // Set up expansion state
        boolean isExpanded = expandedStates.getOrDefault(parentCategory.getId(), false);
        holder.subcategoriesRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Rotate arrow based on expansion state
        float rotation = isExpanded ? 180f : 0f;
        holder.expandIcon.setRotation(rotation);

        // Set up subcategories RecyclerView
        if (holder.subcategoriesRecyclerView.getLayoutManager() == null) {
            holder.subcategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }

        // Get subcategories for this parent
        List<Category> subcategories = subcategoriesByParent.getOrDefault(parentCategory.getId(), new ArrayList<>());
        Log.d(TAG, "Found " + subcategories.size() + " subcategories for parent: " + parentCategory.getName());

        // Create or update subcategories adapter
        SubcategoryAdapter subcategoryAdapter;
        if (holder.subcategoriesRecyclerView.getAdapter() == null) {
            subcategoryAdapter = new SubcategoryAdapter(context, subcategories, subcategory -> {
                if (listener != null) {
                    listener.onCategoryClick(subcategory);
                }
            });
            holder.subcategoriesRecyclerView.setAdapter(subcategoryAdapter);
        } else {
            subcategoryAdapter = (SubcategoryAdapter) holder.subcategoriesRecyclerView.getAdapter();
            subcategoryAdapter.updateSubcategories(subcategories);
        }

        // Handle expand icon click
        holder.expandIcon.setOnClickListener(v -> {
            Log.d(TAG, "Expand icon clicked for category: " + parentCategory.getName());
            boolean newState = !expandedStates.getOrDefault(parentCategory.getId(), false);
            expandedStates.put(parentCategory.getId(), newState);

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
                // Load subcategories when expanding
                loadSubcategories(parentCategory.getId(), holder.subcategoriesRecyclerView);
            }

            // Show/hide subcategories
            holder.subcategoriesRecyclerView.setVisibility(newState ? View.VISIBLE : View.GONE);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(parentCategory);
            }
        });
    }

    private void loadSubcategories(String parentId, RecyclerView recyclerView) {
        Log.d(TAG, "Loading subcategories for parent ID: " + parentId);
        
        // Check if subcategories are already loaded
        if (subcategoriesByParent.containsKey(parentId) && !subcategoriesByParent.get(parentId).isEmpty()) {
            Log.d(TAG, "Subcategories already loaded for parent " + parentId);
            return;
        }

        firebaseManager.loadSubcategoriesByCategory(parentId, new FirebaseManager.OnSubcategoriesLoadedListener() {
            @Override
            public void onSubcategoriesLoaded(List<Category> subcategories) {
                Log.d(TAG, "Loaded " + subcategories.size() + " subcategories for parent " + parentId);
                subcategoriesByParent.put(parentId, subcategories);
                
                // Update RecyclerView on the main thread
                recyclerView.post(() -> {
                    SubcategoryAdapter adapter = (SubcategoryAdapter) recyclerView.getAdapter();
                    if (adapter != null) {
                        adapter.updateSubcategories(subcategories);
                    } else {
                        adapter = new SubcategoryAdapter(context, subcategories, subcategory -> {
                            if (listener != null) {
                                listener.onCategoryClick(subcategory);
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading subcategories for parent " + parentId + ": " + error);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parentCategories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView expandIcon;
        RecyclerView subcategoriesRecyclerView;
        View categoryHeader;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            subcategoriesRecyclerView = itemView.findViewById(R.id.subcategoriesRecyclerView);
            categoryHeader = itemView.findViewById(R.id.categoryHeader);
        }
    }

    private class SubcategoryAdapter extends RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder> {
        private final Context context;
        private List<Category> subcategories;
        private final OnCategoryClickListener listener;

        SubcategoryAdapter(Context context, List<Category> subcategories, OnCategoryClickListener listener) {
            this.context = context;
            this.subcategories = subcategories;
            this.listener = listener;
        }

        public void updateSubcategories(List<Category> newSubcategories) {
            this.subcategories = newSubcategories;
            notifyDataSetChanged();
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
            Log.d(TAG, "Binding subcategory: " + subcategory.getName());

            // Set subcategory name
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

                // If expanding and no products loaded yet, load them
                if (newState && !productsByCategory.containsKey(subcategory.getId())) {
                    loadProductsForCategory(subcategory.getId(), holder.productsRecyclerView);
                }

                // Show/hide products
                holder.productsRecyclerView.setVisibility(newState ? View.VISIBLE : View.GONE);
            });

            // Handle click on subcategory
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(subcategory);
                }
            });
        }

        @Override
        public int getItemCount() {
            return subcategories.size();
        }

        class SubcategoryViewHolder extends RecyclerView.ViewHolder {
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

    private void loadProductsForCategory(String categoryId, RecyclerView recyclerView) {
        Log.d(TAG, "Starting to load products for category ID: " + categoryId);

        // Check if products are already loaded
        if (productsByCategory.containsKey(categoryId)) {
            Log.d(TAG, "Products already loaded for category " + categoryId);
            return;
        }

        Log.d(TAG, "Making Firestore query for products with category_id: " + categoryId);
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Firestore query successful. Found " + querySnapshot.size() + " total products");

                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                String productCategoryId = product.getCategoryId();
                                if (categoryId.equals(productCategoryId)) {
                                    product.setId(doc.getId());
                                    products.add(product);
                                    Log.d(TAG, "Added product: " + product.getName());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing product document: " + doc.getId(), e);
                        }
                    }

                    Log.d(TAG, "Found " + products.size() + " products for category " + categoryId);
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
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products for category " + categoryId, e);
                });
    }
}
