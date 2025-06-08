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
import com.mobile.models.WishlistRcm;
import com.mobile.utils.FontUtils;

import java.text.BreakIterator;
import java.util.List;

public class WishlistRcmAdapter extends RecyclerView.Adapter<WishlistRcmAdapter.WishlistRcmViewHolder> {

    private List<WishlistRcm> wishlistRcmList;

    public WishlistRcmAdapter(List<WishlistRcm> wishlistRcmList) {
        this.wishlistRcmList = wishlistRcmList;
    }

    @NonNull
    @Override
    public WishlistRcmAdapter.WishlistRcmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wishlist_recommendation, parent, false);
        return new WishlistRcmAdapter.WishlistRcmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistRcmAdapter.WishlistRcmViewHolder holder, int position) {
        WishlistRcm product = wishlistRcmList.get(position);

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
        return wishlistRcmList.size();
    }


    public class WishlistRcmViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView tvProductName, tvOldPrice, tvNewPrice, tvDiscount, tvRating;


        public WishlistRcmViewHolder(View itemView) {
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
