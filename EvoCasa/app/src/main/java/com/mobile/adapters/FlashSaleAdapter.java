package com.mobile.adapters;

import android.content.Context;
import android.graphics.Paint;
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
import com.mobile.models.FlashSaleProduct;
import com.mobile.utils.BehaviorLogger;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private List<FlashSaleProduct> flashSaleList;
    private OnItemClickListener itemClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserSessionManager sessionManager;
    private final boolean isWishlistMode;

    // Constructor for HomeFragment (with wishlist functionality)
    public FlashSaleAdapter(List<FlashSaleProduct> flashSaleList, Context context) {
        this.flashSaleList = flashSaleList;
        this.sessionManager = new UserSessionManager(context);
        this.isWishlistMode = false;
    }

    // Constructor for WishlistFragment
    public FlashSaleAdapter(List<FlashSaleProduct> flashSaleList, OnFavoriteClickListener listener) {
        this.flashSaleList = flashSaleList;
        this.favoriteClickListener = listener;
        this.sessionManager = null;
        this.isWishlistMode = true;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FlashSaleProduct product, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(FlashSaleProduct product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashsale, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        FlashSaleProduct product = flashSaleList.get(position);

        holder.tvProductName.setText(product.getName());
        double originalPrice = product.getPrice();
        String formattedOldPrice = originalPrice % 1 == 0 ? 
            String.format("$%.0f", originalPrice) : 
            String.format("$%.1f", originalPrice);
        holder.tvOldPrice.setText(formattedOldPrice);
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Calculate discounted price (30% off for flash sale)
        double discountedPrice = product.getPrice() * 0.7;
        String formattedPrice = discountedPrice % 1 == 0 ? 
            String.format("$%.0f", discountedPrice) : 
            String.format("$%.1f", discountedPrice);
        holder.tvPrice.setText(formattedPrice);
        holder.tvDiscount.setText("-30%");
        holder.tvTagDiscountTop.setText("-30%");

        // Load first image using the new helper method
        String firstImage = product.getFirstImage();
        if (firstImage != null) {
            Glide.with(holder.itemView.getContext())
                    .load(firstImage)
                    .placeholder(R.mipmap.placeholder_image)
                    .error(R.mipmap.error_image)
                    .into(holder.imgProduct);
        }

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvProductName);

        if (isWishlistMode) {
            // WishlistFragment mode
            holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
            holder.imgFavorite.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                    favoriteClickListener.onFavoriteClick(product, holder.getAdapterPosition());
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

    private void checkWishlistStatus(String customerId, String productId, FlashSaleViewHolder holder) {
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

    private void toggleWishlist(String customerId, String productId, FlashSaleViewHolder holder) {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", customerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Create new wishlist and add product
                        createWishlistAndAddProduct(customerId, productId, holder);
                    } else {
                        // Check if product is already in wishlist
                        QueryDocumentSnapshot wishlistDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        String wishlistId = wishlistDoc.getId();
                        List<String> productIds = (List<String>) wishlistDoc.get("Productid");

                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }

                        if (productIds.contains(productId)) {
                            // Product is in wishlist -> Remove
                            removeProductFromWishlist(wishlistId, productId, holder);
                        } else {
                            // Product is not in wishlist -> Add
                            addProductToWishlist(wishlistId, productId, holder);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to check wishlist", Toast.LENGTH_SHORT).show();
                });
        String uid = new UserSessionManager(holder.itemView.getContext()).getUid();
        BehaviorLogger.record(
                uid,
                productId,
                "wishlist",      // Action type: "wishlist"
                "home_page",     // Page: "home_page"
                null              // No additional data
        );
    }

    private void createWishlistAndAddProduct(String customerId, String productId, FlashSaleViewHolder holder) {
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

    private void addProductToWishlist(String wishlistId, String productId, FlashSaleViewHolder holder) {
        db.collection("Wishlist")
                .document(wishlistId)
                .update("Productid", FieldValue.arrayUnion(productId))
                .addOnSuccessListener(aVoid -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                    Toast.makeText(holder.itemView.getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                    String uid = new UserSessionManager(holder.itemView.getContext()).getUid();
                    BehaviorLogger.record(
                            uid,
                            productId,
                            "wishlist",      // Action type: "wishlist"
                            "home_page",     // Page: "home_page"
                            null              // No additional data
                    );
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeProductFromWishlist(String wishlistId, String productId, FlashSaleViewHolder holder) {
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
        return flashSaleList.size();
    }

    public static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView tvProductName, tvOldPrice, tvPrice, tvDiscount;
        ImageView imgFavorite;
        TextView tvTagDiscountTop;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.txtProductName);
            tvOldPrice = itemView.findViewById(R.id.txtOldPrice);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvDiscount = itemView.findViewById(R.id.txtDiscount);
            tvTagDiscountTop = itemView.findViewById(R.id.tvTagDiscountTop);
        }
    }
}
