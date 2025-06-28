package com.mobile.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import java.text.DecimalFormat;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.CartProduct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartViewHolder> {

    public interface OnProductCheckedChangeListener {
        void onCheckedChanged(List<CartProduct> selectedProducts);
        void onCartUpdated();
        void onQuantityChanged(CartProduct product, int newQuantity);
        void onProductRemoved(String productId);
    }

    private final List<CartProduct> productList;
    private final OnProductCheckedChangeListener listener;

    public CartProductAdapter(List<CartProduct> productList, OnProductCheckedChangeListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartProduct product = productList.get(position);

        holder.tvTitle.setText(product.getName());
        holder.tvPrice.setText("$" + String.format(Locale.US, "%.0f", product.getPrice()));
//        holder.tvPrice.setText("$" + String.format("%.2f", product.getPrice()));
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));
        String imageUrl = product.getFirstImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_cart_product)
                    .error(R.mipmap.ic_cart_product)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.mipmap.ic_cart_product);
        }

        // Set checkbox state without triggering listener
        holder.checkboxSelect.setOnCheckedChangeListener(null);
        holder.checkboxSelect.setChecked(product.isSelected());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.checkboxSelect.setButtonTintList(null);
        }

        // Set checkbox listener
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setSelected(isChecked);

            // Notify về danh sách selected products
            List<CartProduct> selected = new ArrayList<>();
            for (CartProduct p : productList) {
                if (p.isSelected()) selected.add(p);
            }
            listener.onCheckedChanged(selected);
        });

        // Plus button
        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = product.getQuantity() + 1;
            product.setQuantity(newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            listener.onQuantityChanged(product, newQuantity);
            listener.onCartUpdated();
        });

        // Minus button
        holder.btnMinus.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                int newQuantity = product.getQuantity() - 1;
                product.setQuantity(newQuantity);
                holder.tvQuantity.setText(String.valueOf(newQuantity));
                listener.onQuantityChanged(product, newQuantity);
                listener.onCartUpdated();
            } else {
                // Remove product when quantity becomes 0
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    String productId = product.getId();
                    productList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    if (currentPosition < productList.size()) {
                        notifyItemRangeChanged(currentPosition, productList.size() - currentPosition);
                    }
                    listener.onProductRemoved(productId);
                    listener.onCartUpdated();
                }
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                String productId = product.getId();
                productList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                if (currentPosition < productList.size()) {
                    notifyItemRangeChanged(currentPosition, productList.size() - currentPosition);
                }
                listener.onProductRemoved(productId);
                listener.onCartUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvQuantity;
        CheckBox checkboxSelect;
        ShapeableImageView imgProduct;
        ImageView btnPlus, btnMinus, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}