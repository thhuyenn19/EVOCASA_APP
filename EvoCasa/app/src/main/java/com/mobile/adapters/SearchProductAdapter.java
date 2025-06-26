package com.mobile.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;

import java.text.DecimalFormat;
import java.util.List;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ViewHolder> {

    private final List<ProductItem> productList;
    private final Context context;
    private OnItemClickListener onItemClickListener;

    // Interface để handle click events
    public interface OnItemClickListener {
        void onItemClick(ProductItem product);
    }

    public SearchProductAdapter(List<ProductItem> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    // Method để set click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SearchProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subcategory_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchProductAdapter.ViewHolder holder, int position) {
        ProductItem product = productList.get(position);

        holder.txtProductName.setText(product.getName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.txtPrice.setText("$"+product.getPrice());

        if (product.getRatings() != null && product.getRatings().getAverage() != null) {
            holder.txtRating.setText(String.format("%.1f", product.getRatings().getAverage()));
        } else {
            holder.txtRating.setText("5.0");
        }

        Glide.with(context)
                .load(product.getFirstImage())
                .placeholder(R.mipmap.ic_lighting_brasslamp)
                .error(R.mipmap.ic_lighting_brasslamp)
                .into(holder.imgProduct);

        // Set click listener cho item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgFavorite;
        TextView txtProductName, txtPrice, txtRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}