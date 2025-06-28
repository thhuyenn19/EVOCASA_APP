package com.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.evocasa.R;
import com.mobile.evocasa.productdetails.ProductDetailsActivity;
import com.mobile.models.ProductItem;
import com.mobile.utils.UserSessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ViewHolder> {

    private final List<ProductItem> productList;
    private final List<String> wishlistIds = new ArrayList<>();

    private final Context context;
    private OnItemClickListener onItemClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId;
    // Interface để handle click events
    public interface OnItemClickListener {
        void onItemClick(ProductItem product);
    }

    public SearchProductAdapter(List<ProductItem> productList, Context context) {
        this.productList = productList;
        this.context = context;
        this.userId = new UserSessionManager(context).getUid();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SearchProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subcategory_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchProductAdapter.ViewHolder holder, int position) {
        ProductItem product = productList.get(position);

        holder.txtProductName.setText(product.getName());

        holder.txtPrice.setText("$" + String.format(Locale.US, "%.0f", product.getPrice()));


        if (product.getRatings() != null && product.getRatings().getAverage() != null) {
            holder.txtRating.setText(String.format("%.1f", product.getRatings().getAverage()));
        } else {
            holder.txtRating.setText("5.0");
        }

        Glide.with(context)
                .load(product.getFirstImage())
                .placeholder(R.mipmap.ic_lighting_brasslamp)
                .error(R.mipmap.ic_lighting_brasslamp)
                .into(holder.imgProduct);

        if (userId != null && holder.imgFavorite != null) {
            checkWishlistStatus(userId, product.getId(), holder);
        } else if (holder.imgFavorite != null) {
            holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
        }

        holder.imgFavorite.setOnClickListener(v -> {
            if (userId != null) {
                toggleWishlist(userId, product.getId(), holder);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });
    }

    private void checkWishlistStatus(String customerId, String productId, ViewHolder holder) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isInWishlist = false;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        List<String> productIds = (List<String>) document.get("Productid");
                        if (productIds != null && productIds.contains(productId)) {
                            isInWishlist = true;
                            break;
                        }
                    }
                    holder.imgFavorite.setImageResource(isInWishlist ? R.drawable.ic_wishlist_heart : R.drawable.ic_favourite);
                })
                .addOnFailureListener(e -> holder.imgFavorite.setImageResource(R.drawable.ic_favourite));
    }

    private void toggleWishlist(String customerId, String productId, ViewHolder holder) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        createWishlistAndAddProduct(customerId, productId, holder);
                    } else {
                        QueryDocumentSnapshot wishlistDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String wishlistId = wishlistDoc.getId();
                        List<String> productIds = (List<String>) wishlistDoc.get("Productid");
                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }
                        if (productIds.contains(productId)) {
                            removeProductFromWishlist(wishlistId, productId, holder);
                        } else {
                            addProductToWishlist(wishlistId, productId, holder);
                        }
                    }
                });
    }

    private void createWishlistAndAddProduct(String customerId, String productId, ViewHolder holder) {
        List<String> productIds = new ArrayList<>();
        productIds.add(productId);
        var wishlistData = new java.util.HashMap<String, Object>();
        wishlistData.put("Customer_id", customerId);
        wishlistData.put("Productid", productIds);

        db.collection("Wishlist")
                .add(wishlistData)
                .addOnSuccessListener(documentReference ->
                        holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart)
                );
    }

    private void addProductToWishlist(String wishlistId, String productId, ViewHolder holder) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds == null) productIds = new ArrayList<>();
                        if (!productIds.contains(productId)) {
                            productIds.add(productId);
                            db.collection("Wishlist").document(wishlistId)
                                    .update("Productid", productIds)
                                    .addOnSuccessListener(unused ->
                                            holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart)
                                    );
                        }
                    }
                });
    }

    private void removeProductFromWishlist(String wishlistId, String productId, ViewHolder holder) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds != null && productIds.contains(productId)) {
                            productIds.remove(productId);
                            db.collection("Wishlist").document(wishlistId)
                                    .update("Productid", productIds)
                                    .addOnSuccessListener(unused ->
                                            holder.imgFavorite.setImageResource(R.drawable.ic_favourite)
                                    );
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgFavorite;
        TextView txtProductName, txtPrice, txtRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}
