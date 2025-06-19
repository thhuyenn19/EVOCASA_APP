package com.mobile.adapters;

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

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(int position); // Định nghĩa sự kiện click vào bất kỳ item nào
    }

    public WishProductAdapter(List<WishProduct> wishProductList,  OnItemClickListener listener) {
        this.wishProductList = wishProductList;
        this.onItemClickListener = listener;
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
        float fakeRating = (float) (4 + new Random().nextFloat() * 2); // từ 3.0 đến 5.0
        holder.txtRating.setText(String.format("%.1f", fakeRating));

        // Thêm sự kiện click vào icon yêu thích (cập nhật tất cả)
        holder.imgFavorite.setImageResource(R.drawable.ic_wishlist_heart);  // Set default icon (ic_wish_heart)

        holder.imgFavorite.setOnClickListener(v -> {
            // Chuyển icon thành "favourite" khi click
            for (WishProduct p : wishProductList) {
                // Không cần set trạng thái "yêu thích" trong đối tượng WishProduct
                // Cập nhật lại giao diện với ic_favourite
            }
            // Thông báo cho RecyclerView cập nhật lại UI (cập nhật tất cả các icon)
            notifyDataSetChanged();  // Cập nhật lại tất cả các item

            // Gọi listener để xử lý thêm hành động khác (xóa sản phẩm khỏi wishlist hoặc lưu vào favourites)
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishProductList.size();
    }

    public class WishProductViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgProduct;
        TextView txtProductName, txtPrice, txtRating;
        ImageView imgFavorite;


        public WishProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtRating = itemView.findViewById(R.id.txtRating);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
        }
    }
}









//BÀI CŨ
//package com.mobile.adapters;
//
//import android.graphics.Paint;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.imageview.ShapeableImageView;
//import com.mobile.evocasa.R;
//import com.mobile.models.FlashSaleProduct;
//import com.mobile.models.WishProduct;
//import com.mobile.utils.FontUtils;
//
//import java.util.List;
//
//public class WishProductAdapter extends RecyclerView.Adapter<WishProductAdapter.WishProductViewHolder> {
//
//    private List<WishProduct> wishProductList;
//
//    public WishProductAdapter(List<WishProduct> wishProductList) {
//        this.wishProductList = wishProductList;
//    }
//
//
//    @NonNull
//    @Override
//    public WishProductAdapter.WishProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wish_product, parent, false);
//        return new WishProductAdapter.WishProductViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull WishProductAdapter.WishProductViewHolder holder, int position) {
//        WishProduct product = wishProductList.get(position);
//
//        holder.tvProductName.setText(product.getName());
//        holder.tvOldPrice.setText(product.getOldPrice());
//        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//        holder.tvNewPrice.setText(product.getNewPrice());
//        holder.tvDiscount.setText(product.getDiscount());
//        holder.tvRating.setText(String.valueOf(product.getRating()));
//        holder.imgProduct.setImageResource(product.getImageResId());
//
//        // ✅ Áp dụng font Zbold cho tên sản phẩm
//        FontUtils.setZboldFont(holder.itemView.getContext(), holder.tvProductName);
//    }
//
//    @Override
//    public int getItemCount() {
//        return wishProductList.size();
//    }
//
//    public class WishProductViewHolder extends RecyclerView.ViewHolder {
//
//        ShapeableImageView imgProduct;
//        TextView tvProductName, tvOldPrice, tvNewPrice, tvDiscount, tvRating;
//
//        public WishProductViewHolder(View view) {
//            super(view);
//            imgProduct = itemView.findViewById(R.id.imgProduct);
//            tvProductName = itemView.findViewById(R.id.tvProductName);
//            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
//            tvNewPrice = itemView.findViewById(R.id.tvPrice);
//            tvDiscount = itemView.findViewById(R.id.tvDiscount);
//            tvRating = itemView.findViewById(R.id.tvRating);
//        }
//    }
//}
