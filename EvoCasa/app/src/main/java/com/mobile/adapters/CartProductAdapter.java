package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.CartProduct;
import com.mobile.utils.FontUtils;

import java.util.List;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartViewHolder> {

    private List<CartProduct> productList;

    public CartProductAdapter(List<CartProduct> productList) {
        this.productList = productList;
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
        holder.checkboxSelect.setChecked(product.isSelected());
        holder.imgProduct.setImageResource(product.getImageResId());
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(String.format("$%,.0f", product.getPrice()));
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setSelected(isChecked);
        });

        holder.btnPlus.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            holder.tvQuantity.setText(String.valueOf(product.getQuantity()));
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                holder.tvQuantity.setText(String.valueOf(product.getQuantity()));
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            productList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, productList.size());
        });

        //set font
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvTitle);
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvPrice);
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvQuantity);

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxSelect;
        ShapeableImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity;
        ImageView btnMinus, btnPlus, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

