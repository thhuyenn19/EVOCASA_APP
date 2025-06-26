package com.mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.evocasa.R;
import com.mobile.evocasa.productdetails.ProductDetailsActivity;
import com.mobile.models.ProductItem;

import java.util.List;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ViewHolder> {

    private final List<ProductItem> productList;
    private final Context context;

    public SearchProductAdapter(List<ProductItem> productList) {
        this.productList = productList;
        this.context = null; // Nếu bạn cần context, hãy truyền vào từ constructor
    }

    public SearchProductAdapter(List<ProductItem> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchProductAdapter.ViewHolder holder, int position) {
        ProductItem product = productList.get(position);
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(String.format("%.0f₫", product.getPrice() != null ? product.getPrice() : 0));

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImage())
                    .placeholder(R.mipmap.ic_lighting_brasslamp)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.mipmap.ic_lighting_brasslamp);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
        }
    }
}
