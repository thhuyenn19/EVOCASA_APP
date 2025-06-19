package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;

import java.util.List;

public class SubCategoryProductAdapter extends RecyclerView.Adapter<SubCategoryProductAdapter.SubCategoryProductViewHolder> {

    private final List<ProductItem> productList;

    public SubCategoryProductAdapter(List<ProductItem> productList) {
        this.productList = productList;
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
        holder.txtRating.setText(String.valueOf(product.getRating() != 0.0 ? product.getRating() : 0.0));

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class SubCategoryProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtPrice, txtRating;
        ImageView imgFavorite;

        public SubCategoryProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
        }

    }
}