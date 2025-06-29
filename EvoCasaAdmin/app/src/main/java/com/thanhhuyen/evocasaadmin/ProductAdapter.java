package com.thanhhuyen.evocasaadmin;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String TAG = "ProductAdapter";
    private final Context context;
    private List<Product> products;
    private final FirebaseFirestore db;
    private final NumberFormat currencyFormatter;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.db = FirebaseFirestore.getInstance();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        // Load first image if available
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.productImage);
        }

        holder.productName.setText(product.getName());
        
        // Get category name from Firestore
        String categoryId = product.getCategoryId();
        if (categoryId != null && !categoryId.isEmpty()) {
            db.collection("categories")
                .document(categoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Category category = documentSnapshot.toObject(Category.class);
                        if (category != null) {
                            holder.productCategory.setText(category.getName());
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error loading category for product: " + product.getId(), e));
        }

        holder.productPrice.setText(currencyFormatter.format(product.getPrice()));

        // Handle edit button click
        holder.editButton.setOnClickListener(v -> {
            // TODO: Implement edit product functionality
        });

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Handle product click
            Log.d(TAG, "Product clicked: " + product.getName());
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productCategory;
        TextView productPrice;
        ImageButton editButton;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productCategory = itemView.findViewById(R.id.productCategory);
            productPrice = itemView.findViewById(R.id.productPrice);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
} 