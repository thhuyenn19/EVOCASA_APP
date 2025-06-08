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

public class SuggestedProductAdapter extends RecyclerView.Adapter<SuggestedProductAdapter.SuggestedProductViewHolder>{
    private List<SuggestedProducts> suggestedProductList;

    public SuggestedProductAdapter(List<SuggestedProducts> suggestedProductList) {
        this.suggestedProductList = suggestedProductList;
    }

    @NonNull
    @Override
    public SuggestedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_product, parent, false);
        return new SuggestedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedProductViewHolder holder, int position) {
        SuggestedProducts product = suggestedProductList.get(position);

        holder.imgProduct.setImageResource(product.getImageResId());
        holder.txtProductName.setText(product.getName());
        holder.txtOldPrice.setText(product.getOldPrice());
        holder.txtOldPrice.setPaintFlags(
                holder.txtOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        );
        holder.txtPrice.setText(product.getNewPrice());
        holder.txtDiscount.setText(product.getDiscount());
        holder.txtRating.setText(String.valueOf(product.getRating()));

        // ✅ Áp dụng font Zbold cho tên sản phẩm
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
    }

    @Override
    public int getItemCount() {
        return suggestedProductList.size();
    }

    public static class SuggestedProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtOldPrice, txtPrice, txtDiscount, txtRating;
        ImageView imgFavorite;

        public SuggestedProductViewHolder(@NonNull View itemView) {
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
