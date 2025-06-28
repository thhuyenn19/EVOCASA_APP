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
import com.mobile.models.HotProducts;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HotProductsAdapter extends RecyclerView.Adapter<HotProductsAdapter.HotProductViewHolder> {

    private List<HotProducts> hotProductList;
    private OnItemClickListener itemClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserSessionManager sessionManager;
    private final boolean isWishlistMode;

    // Constructor for HomeFragment (with wishlist functionality)
    public HotProductsAdapter(List<HotProducts> hotProductList, Context context) {
        this.hotProductList = hotProductList;
        this.sessionManager = new UserSessionManager(context);
        this.isWishlistMode = false;
    }

    // Constructor for WishlistFragment
    public HotProductsAdapter(List<HotProducts> hotProductList, OnFavoriteClickListener listener) {
        this.hotProductList = hotProductList;
        this.favoriteClickListener = listener;
        this.sessionManager = null;
        this.isWishlistMode = true;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(HotProducts product, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(HotProducts product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public HotProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_products, parent, false);
        return new HotProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotProductViewHolder holder, int position) {
        HotProducts product = hotProductList.get(position);

        // Lấy giá gốc từ Firebase
        double oldPrice = product.getPrice();

        // Random giảm giá từ 5% đến 10%
        int discount = new Random().nextInt(6) + 5;  // giảm giá từ 5-10%

        // Tính giá mới sau khi giảm
        double newPrice = oldPrice * (1 - discount / 100.0);

        // Format và hiển thị
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
        float fakeRating = (float) (4 + new Random().nextFloat() * 1); // từ 4.0 đến 5.0
        holder.txtRating.setText(String.format("%.1f", fakeRating));

        if (isWishlistMode) {
            // WishlistFragment mode
            holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
            holder.imgFavorite.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                    favoriteClickListener.onFavoriteClick(product, position);
                }
            });
        } else {
            // HomeFragment mode
            String customerId = sessionManager.getUid();
            if (customerId != null) {
                checkWishlistStatus(customerId, product.getId(), holder);
            } else {
                holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
            }

            holder.imgFavorite.setOnClickListener(v -> {
                String customerIdClick = sessionManager.getUid();
                if (customerIdClick != null) {
                    toggleWishlist(customerIdClick, product.getId(), holder);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Please sign in to add to wishlist", Toast.LENGTH_SHORT).show();
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(product);
            }
        });
    }

    private void checkWishlistStatus(String customerId, String productId, HotProductViewHolder holder) {
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
                    holder.imgFavorite.setImageResource(isInWishlist ? 
                            R.drawable.ic_wishlist_heart : R.drawable.ic_favourite);
                })
                .addOnFailureListener(e -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                });
    }

    private void toggleWishlist(String customerId, String productId, HotProductViewHolder holder) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Tạo wishlist mới và thêm sản phẩm
                        createWishlistAndAddProduct(customerId, productId, holder);
                    } else {
                        // Kiểm tra sản phẩm đã có trong wishlist chưa
                        QueryDocumentSnapshot wishlistDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String wishlistId = wishlistDoc.getId();
                        List<String> productIds = (List<String>) wishlistDoc.get("Productid");

                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }

                        if (productIds.contains(productId)) {
                            // Sản phẩm đã có trong wishlist -> Remove
                            removeProductFromWishlist(wishlistId, productId, holder);
                        } else {
                            // Sản phẩm chưa có trong wishlist -> Add
                            addProductToWishlist(wishlistId, productId, holder);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to check wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void createWishlistAndAddProduct(String customerId, String productId, HotProductViewHolder holder) {
        Map<String, Object> wishlistData = new HashMap<>();
        wishlistData.put("Customer_id", customerId);
        wishlistData.put("CreatedAt", FieldValue.serverTimestamp());
        List<String> productIds = new ArrayList<>();
        productIds.add(productId);
        wishlistData.put("Productid", productIds);

        db.collection("Wishlist")
                .add(wishlistData)
                .addOnSuccessListener(documentReference -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                    Toast.makeText(holder.itemView.getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void addProductToWishlist(String wishlistId, String productId, HotProductViewHolder holder) {
        db.collection("Wishlist")
                .document(wishlistId)
                .update("Productid", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(aVoid -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                    Toast.makeText(holder.itemView.getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeProductFromWishlist(String wishlistId, String productId, HotProductViewHolder holder) {
        db.collection("Wishlist")
                .document(wishlistId)
                .update("Productid", FieldValue.arrayRemove(productId))
                .addOnSuccessListener(aVoid -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                    Toast.makeText(holder.itemView.getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return hotProductList.size();
    }

    public static class HotProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtOldPrice, txtPrice, txtDiscount, txtRating;
        ImageView imgFavorite;

        public HotProductViewHolder(@NonNull View itemView) {
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
