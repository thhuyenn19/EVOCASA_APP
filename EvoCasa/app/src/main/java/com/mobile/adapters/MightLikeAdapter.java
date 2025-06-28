package com.mobile.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.evocasa.R;
import com.mobile.models.MightLike;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MightLikeAdapter extends RecyclerView.Adapter<MightLikeAdapter.MightLikeViewHolder> {

    private List<MightLike> mightLikeList;
    private OnItemClickListener itemClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserSessionManager sessionManager;
    private final boolean isWishlistMode;

    public MightLikeAdapter(List<MightLike> mightLikeList, Context context) {
        this.mightLikeList = mightLikeList;
        this.sessionManager = new UserSessionManager(context);
        this.isWishlistMode = false;
    }

    public MightLikeAdapter(List<MightLike> mightLikeList, OnFavoriteClickListener listener) {
        this.mightLikeList = mightLikeList;
        this.favoriteClickListener = listener;
        this.sessionManager = null;
        this.isWishlistMode = true;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(MightLike product, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(MightLike product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public MightLikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_products, parent, false);
        return new MightLikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MightLikeViewHolder holder, int position) {
        MightLike product = mightLikeList.get(position);

        double oldPrice = product.getPrice();
        int discount = new Random().nextInt(6) + 5;
        double newPrice = oldPrice * (1 - discount / 100.0);

        holder.txtProductName.setText(product.getName());
        holder.txtOldPrice.setText("$" + String.format("%.2f", oldPrice));
        holder.txtOldPrice.setPaintFlags(holder.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.txtDiscount.setText("-" + discount + "%");
        holder.txtPrice.setText("$" + String.format("%.2f", newPrice));

        List<String> images = product.getImageList();
        if (images != null && !images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(images.get(0))
                    .placeholder(R.mipmap.placeholder_image)
                    .error(R.mipmap.error_image)
                    .into(holder.imgProduct);
        }

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
        float fakeRating = (float) (4 + new Random().nextFloat());
        holder.txtRating.setText(String.format("%.1f", fakeRating));

        // Set icon mặc định là favourite (chưa thêm vào wishlist)
        holder.imgFavorite.setImageResource(R.drawable.ic_favourite);

        holder.imgFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                // Chuyển icon thành wishlist heart khi click
                holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                favoriteClickListener.onFavoriteClick(product, position);
            }
        });

        // Item click listener -> open product detail
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(product);
            }
        });
    }

    private void addProductToWishlist(String customerId, String productId, MightLikeViewHolder holder) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        createWishlistAndAddProduct(customerId, productId, holder);
                    } else {
                        QueryDocumentSnapshot wishlistDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String wishlistId = wishlistDoc.getId();
                        db.collection("Wishlist").document(wishlistId)
                                .update("Productid", FieldValue.arrayUnion(productId))
                                .addOnSuccessListener(aVoid -> holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart))
                                .addOnFailureListener(e -> Log.e("Wishlist", "Error adding to wishlist", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Wishlist", "Error adding to wishlist", e));
    }

    private void createWishlistAndAddProduct(String customerId, String productId, MightLikeViewHolder holder) {
        List<String> productIds = new ArrayList<>();
        productIds.add(productId);
        Map<String, Object> newWishlist = new HashMap<>();
        newWishlist.put("Customer_id", customerId);
        newWishlist.put("Productid", productIds);

        db.collection("Wishlist")
                .add(newWishlist)
                .addOnSuccessListener(documentReference -> holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart))
                .addOnFailureListener(e -> Log.e("Wishlist", "Error creating wishlist", e));
    }

    @Override
    public int getItemCount() {
        return mightLikeList.size();
    }

    public static class MightLikeViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtOldPrice, txtPrice, txtDiscount, txtRating;
        ImageView imgFavorite;

        public MightLikeViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtOldPrice = itemView.findViewById(R.id.txtOldPrice);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
        }
    }
}
