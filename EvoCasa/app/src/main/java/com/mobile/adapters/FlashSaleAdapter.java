package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.FlashSaleProduct;
import com.mobile.evocasa.R;

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
        holder.imgProduct.setImageResource(product.getImageResId());
        holder.txtName.setText(product.getName());
        holder.txtOldPrice.setText(product.getOldPrice());
        holder.txtNewPrice.setText(product.getNewPrice());
        holder.txtDiscount.setText(product.getDiscount());
        holder.txtRating.setText(String.valueOf(product.getRating()));
    }

    @Override
    public int getItemCount() {
        return flashSaleList.size();
    }

    public static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtOldPrice, txtNewPrice, txtDiscount, txtRating;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.tvProductName);
            txtOldPrice = itemView.findViewById(R.id.tvOldPrice);
            txtNewPrice = itemView.findViewById(R.id.tvPrice);
            txtDiscount = itemView.findViewById(R.id.tvDiscount);
            txtRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
