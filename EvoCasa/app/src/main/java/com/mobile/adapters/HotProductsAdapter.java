package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.HotProducts;
import com.mobile.evocasa.R;

import java.util.List;

public class HotProductsAdapter extends RecyclerView.Adapter<HotProductsAdapter.HotProductViewHolder> {

    private List<HotProducts> hotProductList;

    public HotProductsAdapter(List<HotProducts> hotProductList) {
        this.hotProductList = hotProductList;
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

        holder.imgProduct.setImageResource(product.getImageResId());
        holder.txtProductName.setText(product.getName());
        holder.txtOldPrice.setText(product.getOldPrice());
        holder.txtOldPrice.setPaintFlags(
                holder.txtOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        );
        holder.txtPrice.setText(product.getNewPrice());
        holder.txtDiscount.setText(product.getDiscount());
        holder.txtRating.setText(String.valueOf(product.getRating()));
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
