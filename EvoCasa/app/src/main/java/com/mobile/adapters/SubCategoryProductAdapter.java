package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.SuggestedProducts;
import com.mobile.utils.FontUtils;

import java.util.List;

public class SubCategoryProductAdapter extends RecyclerView.Adapter<SubCategoryProductAdapter.SubCategoryProductViewHolder> {

    private final List<SuggestedProducts> productList;

    public SubCategoryProductAdapter(List<SuggestedProducts> productList) {
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
        SuggestedProducts product = productList.get(position);

        holder.imgProduct.setImageResource(product.getImageResId());
        holder.txtProductName.setText(product.getName());
        holder.txtOldPrice.setText(product.getOldPrice());
        holder.txtOldPrice.setPaintFlags(
                holder.txtOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        );
        holder.txtPrice.setText(product.getNewPrice());
        holder.txtDiscount.setText(product.getDiscount());
        holder.txtRating.setText(String.valueOf(product.getRating()));

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class SubCategoryProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtOldPrice, txtPrice, txtDiscount, txtRating;
        ImageView imgFavorite;

        public SubCategoryProductViewHolder(@NonNull View itemView) {
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
