package com.mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mobile.evocasa.productdetails.ProductDetailsActivity;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubCategoryProductAdapter extends RecyclerView.Adapter<SubCategoryProductAdapter.SubCategoryProductViewHolder> {

    private final List<ProductItem> productList;
    private final DecimalFormat decimalFormat;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final UserSessionManager sessionManager;
    private OnItemClickListener onItemClickListener;

    public SubCategoryProductAdapter(List<ProductItem> productList, Context context) {
        this.productList = productList;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        this.decimalFormat = new DecimalFormat("0.0", symbols);
        decimalFormat.setGroupingUsed(false);
        this.sessionManager = new UserSessionManager(context);
    }

    // Interface cho click listener
    public interface OnItemClickListener {
        void onItemClick(ProductItem product);
    }

    // Phương thức để set click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SubCategoryProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subcategory_product, parent, false);
        return new SubCategoryProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubCategoryProductViewHolder holder, int position) {
        ProductItem product = productList.get(position);

        // Load first image from Image array using Glide
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String[] images = product.getImage().replaceAll("[\\[\\]\"]", "").split(",");
            if (images.length > 0) {
                Glide.with(holder.itemView.getContext())
                        .load(images[0].trim())
                        .placeholder(R.mipmap.ic_lighting_brasslamp)
                        .into(holder.imgProduct);
            }
        }

        holder.txtProductName.setText(product.getName());
        holder.txtPrice.setText("$" + product.getPrice());

        // Hiển thị rating nếu có, nếu null thì hiện 5.0
        Double averageRating = product.getRatings().getAverage();
        if (averageRating != null) {
            holder.ratingLayout.setVisibility(View.VISIBLE);
            holder.txtRating.setText(decimalFormat.format(averageRating));
        } else {
            holder.ratingLayout.setVisibility(View.VISIBLE);
            holder.txtRating.setText("5.0");
        }

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);

        // Kiểm tra xem sản phẩm đã trong wishlist chưa
        String customerId = sessionManager.getUid();
        if (customerId != null) {
            checkWishlistStatus(customerId, product.getId(), holder);
        } else {
            holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
        }

        // Xử lý sự kiện nhấn vào icon yêu thích - Toggle wishlist
        holder.imgFavorite.setOnClickListener(v -> {
            String customerIdClick = sessionManager.getUid();
            if (customerIdClick != null) {
                toggleWishlist(customerIdClick, product.getId(), holder);
            } else {
                Toast.makeText(holder.itemView.getContext(), "Please sign in to add to wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện nhấn vào item để mở ProductDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            } else {
                // Fallback: Open ProductDetailsActivity directly
                Intent intent = new Intent(holder.itemView.getContext(), ProductDetailsActivity.class);
                intent.putExtra("productId", product.getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    private void checkWishlistStatus(String customerId, String productId, SubCategoryProductViewHolder holder) {
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

                    if (isInWishlist) {
                        holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                    } else {
                        holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                    }
                })
                .addOnFailureListener(e -> {
                    holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                });
    }

    private void toggleWishlist(String customerId, String productId, SubCategoryProductViewHolder holder) {
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

    private void createWishlistAndAddProduct(String customerId, String productId, SubCategoryProductViewHolder holder) {
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
                    Toast.makeText(holder.itemView.getContext(), "Failed to create wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void addProductToWishlist(String wishlistId, String productId, SubCategoryProductViewHolder holder) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds == null) {
                            productIds = new ArrayList<>();
                        }

                        if (!productIds.contains(productId)) {
                            productIds.add(productId);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Productid", productIds);

                            db.collection("Wishlist").document(wishlistId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                                        Toast.makeText(holder.itemView.getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(holder.itemView.getContext(), "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to retrieve wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeProductFromWishlist(String wishlistId, String productId, SubCategoryProductViewHolder holder) {
        db.collection("Wishlist").document(wishlistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> productIds = (List<String>) documentSnapshot.get("Productid");
                        if (productIds != null && productIds.contains(productId)) {
                            productIds.remove(productId);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Productid", productIds);

                            db.collection("Wishlist").document(wishlistId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        holder.imgFavorite.setImageResource(R.drawable.ic_favourite);
                                        Toast.makeText(holder.itemView.getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(holder.itemView.getContext(), "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to retrieve wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class SubCategoryProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtPrice, txtRating;
        ImageView imgFavorite;
        LinearLayout ratingLayout;

        public SubCategoryProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            ratingLayout = itemView.findViewById(R.id.ratingLayout);
        }
    }
}