package com.thanhhuyen.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.thanhhuyen.evocasaadmin.FirebaseManager;
import com.thanhhuyen.evocasaadmin.ProductDetailActivity;
import com.thanhhuyen.evocasaadmin.R;
import com.thanhhuyen.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String TAG = "ProductAdapter";
    private final Context context;
    private List<Product> products;
    private final FirebaseManager firebaseManager;
    private final Map<String, FirebaseManager.OnProductChangeListener> productListeners;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.firebaseManager = FirebaseManager.getInstance();
        this.productListeners = new HashMap<>();
        setupProductListeners();
    }

    private void setupProductListeners() {
        // Remove old listeners
        productListeners.clear();

        // Set up new listeners for each product
        for (Product product : products) {
            if (product.getId() != null && !productListeners.containsKey(product.getId())) {
                FirebaseManager.OnProductChangeListener listener = new FirebaseManager.OnProductChangeListener() {
                    @Override
                    public void onProductChanged(Product updatedProduct) {
                        // Update the product in the list
                        int position = findProductPosition(updatedProduct.getId());
                        if (position != -1) {
                            products.set(position, updatedProduct);
                            notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error listening to product changes: " + error);
                    }
                };

                productListeners.put(product.getId(), listener);
                firebaseManager.listenToProductChanges(product.getId(), listener);
            }
        }
    }

    private int findProductPosition(String productId) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(productId)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_list, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        Log.d(TAG, "Binding product at position " + position + ": " + product.getName());

        // Set product name
        holder.productName.setText(product.getName());

        // Set product quantity
        holder.productQuantity.setText(String.format("Quantity: %d", product.getQuantity()));

        // Load first image if available
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0);
            Log.d(TAG, "Loading image for " + product.getName() + ": " + imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.productImage);
        } else {
            Log.d(TAG, "No image available for product: " + product.getName());
            holder.productImage.setImageResource(android.R.color.darker_gray);
        }

        // Set click listeners
        View.OnClickListener clickListener = v -> {
            Log.d(TAG, "Product clicked: " + product.getName() + " (ID: " + product.getId() + ")");
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            context.startActivity(intent);
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.productImage.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        Log.d(TAG, "Updating products list with " + (newProducts != null ? newProducts.size() : 0) + " products");
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        setupProductListeners(); // Set up listeners for new products
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productQuantity;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productQuantity = itemView.findViewById(R.id.productQuantity);
        }
    }
} 