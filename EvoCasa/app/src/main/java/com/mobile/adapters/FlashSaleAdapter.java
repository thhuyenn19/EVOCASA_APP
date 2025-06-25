package com.mobile.adapters;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.models.FlashSaleProduct;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private List<FlashSaleProduct> flashSaleList;
    private OnItemClickListener itemClickListener;

    public FlashSaleAdapter(List<FlashSaleProduct> flashSaleList) {
        this.flashSaleList = flashSaleList;
    }

    public interface OnItemClickListener {
        void onItemClick(FlashSaleProduct product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashsale, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        FlashSaleProduct product = flashSaleList.get(position);

        double oldPrice = product.getPrice();
        int discount = new Random().nextInt(41) + 10;
        double newPrice = oldPrice * (1 - discount / 100.0);
        float rating = (float) (3 + new Random().nextFloat() * 2); // Random từ 3.0 – 5.0

        holder.tvProductName.setText(product.getName());
        holder.tvOldPrice.setText("$" + String.format("%.2f", oldPrice));
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvPrice.setText("$" + String.format("%.2f", newPrice));
        holder.tvDiscount.setText("-" + discount + "%");
        holder.tvTagDiscountTop.setText("-" + discount + "%");
        holder.tvRating.setText(String.format("%.1f", rating));

        Log.d("IMAGE_TEST", "Ảnh đầu tiên: " + product.getFirstImage());

        List<String> images = product.getImageList();
        if (images != null && !images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(images.get(0))
                    .placeholder(R.mipmap.placeholder_image)
                    .error(R.mipmap.error_image)
                    .into(holder.imgProduct);
        }

        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvProductName);

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashSaleList.size();
    }

    public static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView tvProductName, tvOldPrice, tvPrice, tvDiscount, tvRating, tvTagDiscountTop;
        ImageView imgFavorite;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.txtProductName);
            tvOldPrice = itemView.findViewById(R.id.txtOldPrice);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDiscount = itemView.findViewById(R.id.txtDiscount);
            tvRating = itemView.findViewById(R.id.txtRating);
            tvTagDiscountTop = itemView.findViewById(R.id.tvTagDiscountTop);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
        }
    }
}
