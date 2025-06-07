package com.mobile.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.FlashSaleProduct;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.List;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private List<FlashSaleProduct> flashSaleList;

    public FlashSaleAdapter(List<FlashSaleProduct> flashSaleList) {
        this.flashSaleList = flashSaleList;
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashsale, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        FlashSaleProduct product = flashSaleList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvOldPrice.setText(product.getOldPrice());
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvNewPrice.setText(product.getNewPrice());
        holder.tvDiscount.setText(product.getDiscount());
        holder.tvRating.setText(String.valueOf(product.getRating()));
        holder.imgProduct.setImageResource(product.getImageResId());

        // ✅ Áp dụng font Zbold cho tên sản phẩm
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvProductName);
    }

    @Override
    public int getItemCount() {
        return flashSaleList.size();
    }

    public static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView tvProductName, tvOldPrice, tvNewPrice, tvDiscount, tvRating;

        public FlashSaleViewHolder(@NonNull View itemView) {
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
