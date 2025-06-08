package com.mobile.adapters;


import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.FlashSaleProduct;
import com.mobile.models.WishlistProduct;
import com.mobile.utils.FontUtils;

import java.util.List;

public class WishlistProductAdapter extends RecyclerView.Adapter<WishlistProductAdapter.WishlistProductViewHolder> {

    private List<WishlistProduct> wishlistProductList;

    public WishlistProductAdapter(List<WishlistProduct> wishlistProductList) {
        this.wishlistProductList = wishlistProductList;
    }

    @NonNull
    @Override
    public WishlistProductAdapter.WishlistProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wishlist_list_product, parent, false);
        return new WishlistProductAdapter.WishlistProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistProductAdapter.WishlistProductViewHolder holder, int position) {
        WishlistProduct product = wishlistProductList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvOldPrice.setText(product.getOldPrice());
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvNewPrice.setText(product.getNewPrice());
        holder.tvDiscount.setText(product.getDiscount());
        holder.tvRating.setText(String.valueOf(product.getRating()));
        holder.imgProduct.setImageResource(product.getImageResId());

        // Áp dụng font Zbold cho tên sản phẩm
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvProductName);
    }

    @Override
    public int getItemCount() {
        return wishlistProductList.size();
    }

    public class WishlistProductViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgProduct;
        TextView tvProductName, tvOldPrice, tvNewPrice, tvDiscount, tvRating;

        public WishlistProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            tvNewPrice = itemView.findViewById(R.id.tvPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
