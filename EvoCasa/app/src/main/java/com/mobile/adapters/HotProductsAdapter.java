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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.HotProducts;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.List;
import java.util.Random;

public class HotProductsAdapter extends RecyclerView.Adapter<HotProductsAdapter.HotProductViewHolder> {

    private List<HotProducts> hotProductList;
    private OnFavoriteClickListener favoriteClickListener;
    private OnItemClickListener itemClickListener;

    public HotProductsAdapter(List<HotProducts> hotProductList, OnFavoriteClickListener listener) {
        this.hotProductList = hotProductList;
        this.favoriteClickListener = listener;
    }

    public HotProductsAdapter(List<HotProducts> hotProductList) {
        this.hotProductList = hotProductList;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(HotProducts product, int position);
    }

    // NEW interface for item clicks
    public interface OnItemClickListener {
        void onItemClick(HotProducts product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
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

        // Lấy giá gốc từ Firebase
        double oldPrice = product.getPrice();

        // Random giảm giá từ 5% đến 10%
        int discount = new Random().nextInt(6) + 5;  // giảm giá từ 5-10%

        // Tính giá mới sau khi giảm
        double newPrice = oldPrice * (1 - discount / 100.0);

        // Format và hiển thị
        holder.txtProductName.setText(product.getName());
        holder.txtOldPrice.setText("$" + String.format("%.2f", oldPrice));
        holder.txtOldPrice.setPaintFlags(holder.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.txtDiscount.setText("-" + discount + "%");
        holder.txtPrice.setText("$" + String.format("%.2f", newPrice));
        holder.txtRating.setText(String.valueOf(product.getRating()));

        // Load ảnh đầu tiên từ danh sách ảnh
        Log.d("HOT_PRODUCT", "Ảnh đầu tiên: " + product.getFirstImage());

        List<String> images = product.getImageList();
        if (images != null && !images.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(images.get(0))
                    .placeholder(R.mipmap.placeholder_image)
                    .error(R.mipmap.error_image)
                    .into(holder.imgProduct);
        }

        // Font đậm cho tên sản phẩm
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
        float fakeRating = (float) (4 + new Random().nextFloat() * 1); // từ 4.0 đến 5.0
        holder.txtRating.setText(String.format("%.1f", fakeRating));

        // Set icon mặc định là favourite (chưa thêm vào wishlist)
        holder.imgFavorite.setImageResource(R.drawable.ic_favourite);

        holder.imgFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                // Chuyển icon thành wishlist heart khi click
                holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);
                favoriteClickListener.onFavoriteClick(product, position);
            }
        });

        // Item click listener -> open product detail
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotProductList.size();
    }

    public void removeAt(int position) {
        hotProductList.remove(position);
        notifyItemRemoved(position);
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








//package com.mobile.adapters;
//
//import android.graphics.Paint;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.google.android.material.imageview.ShapeableImageView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.mobile.models.HotProducts;
//import com.mobile.evocasa.R;
//import com.mobile.utils.FontUtils;
//
//import java.util.List;
//import java.util.Random;
//
//public class HotProductsAdapter extends RecyclerView.Adapter<HotProductsAdapter.HotProductViewHolder> {
//
//    private List<HotProducts> hotProductList;
//
//    private OnFavoriteClickListener favoriteClickListener;
//
//    public HotProductsAdapter(List<HotProducts> hotProductList, OnFavoriteClickListener listener) {
//        this.hotProductList = hotProductList;
//        this.favoriteClickListener = listener;
//    }
//
//    public HotProductsAdapter(List<HotProducts> hotProductList) {
//        this.hotProductList = hotProductList;
//    }
//
//
//    public interface OnFavoriteClickListener {
//        void onFavoriteClick(HotProducts product, int position);
//    }
//
//
//    @NonNull
//    @Override
//    public HotProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_hot_products, parent, false);
//        return new HotProductViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull HotProductViewHolder holder, int position) {
//        HotProducts product = hotProductList.get(position);
//
//        // Lấy giá gốc từ Firebase
//        double oldPrice = product.getPrice();
//
//        // Random giảm giá từ 5% đến 10%
//        int discount = new Random().nextInt(6) + 5;  // giảm giá từ 5-10%
//
//        // Tính giá mới sau khi giảm
//        double newPrice = oldPrice * (1 - discount / 100.0);
//
//        // Format và hiển thị
//        holder.txtProductName.setText(product.getName());
//        holder.txtOldPrice.setText("$" + String.format("%.2f", oldPrice));
//        holder.txtOldPrice.setPaintFlags(holder.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//        holder.txtDiscount.setText("-" + discount + "%");
//        holder.txtPrice.setText("$" + String.format("%.2f", newPrice));
//        holder.txtRating.setText(String.valueOf(product.getRating()));
//
//        // Load ảnh đầu tiên từ danh sách ảnh
//        Log.d("HOT_PRODUCT", "Ảnh đầu tiên: " + product.getFirstImage());
//
//        List<String> images = product.getImageList();
//        if (images != null && !images.isEmpty()) {
//            Glide.with(holder.itemView.getContext())
//                    .load(images.get(0))
//                    .placeholder(R.mipmap.placeholder_image)
//                    .error(R.mipmap.error_image)
//                    .into(holder.imgProduct);
//        }
//
//        // Font đậm cho tên sản phẩm
//        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtProductName);
//        float fakeRating = (float) (4 + new Random().nextFloat() * 1); // từ 4.0 đến 5.0
//        holder.txtRating.setText(String.format("%.1f", fakeRating));
//
//
//        holder.imgFavorite.setImageResource(R.drawable.ic_favourite); // mặc định
//        holder.imgFavorite.setOnClickListener(v -> {
//            if (favoriteClickListener != null) {
//                holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart); // chuyển icon
//                favoriteClickListener.onFavoriteClick(product, position);
//            }
//        });
//
//
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return hotProductList.size();
//    }
//
//    public void removeAt(int position) {
//        hotProductList.remove(position);
//        notifyItemRemoved(position);
//    }
//
//
//    public static class HotProductViewHolder extends RecyclerView.ViewHolder {
//        ShapeableImageView imgProduct;
//        TextView txtProductName, txtOldPrice, txtPrice, txtDiscount, txtRating;
//        ImageView imgFavorite;
//
//        public HotProductViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imgProduct = itemView.findViewById(R.id.imgProduct);
//            txtProductName = itemView.findViewById(R.id.txtProductName);
//            txtOldPrice = itemView.findViewById(R.id.txtOldPrice);
//            txtPrice = itemView.findViewById(R.id.txtPrice);
//            txtDiscount = itemView.findViewById(R.id.txtDiscount);
//            txtRating = itemView.findViewById(R.id.txtRating);
//            imgFavorite = itemView.findViewById(R.id.imgFavorite);
//        }
//    }
//}
