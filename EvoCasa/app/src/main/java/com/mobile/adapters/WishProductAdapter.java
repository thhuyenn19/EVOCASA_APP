package com.mobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.mobile.evocasa.R;
import com.mobile.models.WishProduct;
import com.mobile.utils.FontUtils;

import java.util.List;
import java.util.Random;

public class WishProductAdapter extends RecyclerView.Adapter<WishProductAdapter.WishProductViewHolder> {

    private List<WishProduct> wishProductList;
    private OnItemClickListener onItemClickListener;
    private String currentTab = "all";

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public WishProductAdapter(List<WishProduct> wishProductList, OnItemClickListener listener) {
        this.wishProductList = wishProductList;
        this.onItemClickListener = listener;
    }

    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }

    @NonNull
    @Override
    public WishProductAdapter.WishProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wish_product, parent, false);
        return new WishProductAdapter.WishProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishProductAdapter.WishProductViewHolder holder, int position) {
        WishProduct product = wishProductList.get(position);

        // Lấy giá gốc từ Firebase
        double Price = product.getPrice();

        // Format và hiển thị
        holder.txtProductName.setText(product.getName());
        holder.txtPrice.setText("$" + String.format("%.2f", Price));
        holder.txtRating.setText(String.valueOf(product.getRating()));

        // Load ảnh đầu tiên từ danh sách ảnh
        Log.d("IMAGE_TEST", "Ảnh đầu tiên: " + product.getFirstImage());
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

        // Nếu rating không có trong Firestore, thì tạo tạm số ngẫu nhiên
        float fakeRating = (float) (3 + new Random().nextFloat() * 2); // từ 3.0 đến 5.0
        holder.txtRating.setText(String.format("%.1f", fakeRating));

        // Set icon cho heart (đã trong wishlist)
        holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);

        // Click để remove khỏi wishlist
        holder.imgFavorite.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });

        // ✅ Xử lý Flash Sale badge theo tab
        if ("sale".equalsIgnoreCase(currentTab)) {
            holder.tvFlashSale.setVisibility(View.VISIBLE);
        } else {
            holder.tvFlashSale.setVisibility(View.GONE);
        }

        // ✅ Xử lý lớp phủ "Out of Stock" theo tab
        if ("outOfStock".equalsIgnoreCase(currentTab)) {
            holder.overlayOutOfStock.setVisibility(View.VISIBLE);
        } else {
            holder.overlayOutOfStock.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return wishProductList.size();
    }

    public class WishProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtProductName, txtPrice, txtRating;
        ImageView imgFavorite;
        View overlayOutOfStock;
        TextView tvFlashSale;

        public WishProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            overlayOutOfStock = itemView.findViewById(R.id.overlayOutOfStock);
            tvFlashSale = itemView.findViewById(R.id.tvFlashSale);
        }
    }
}

