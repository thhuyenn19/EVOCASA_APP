package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.Category;
import com.mobile.evocasa.R;

import java.util.List;

public class CategoryShopAdapter extends RecyclerView.Adapter<CategoryShopAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private final Context context;

    public CategoryShopAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_shop, parent, false);

        // Tính chiều rộng mỗi item theo số cột (2 cột)
        int screenWidth = parent.getResources().getDisplayMetrics().widthPixels;
        int spacingPx = (int) (17 * 2 * parent.getResources().getDisplayMetrics().density); // spacing giữa các item
        int paddingPx = (int) (16 * 2 * parent.getResources().getDisplayMetrics().density); // padding của RecyclerView
        int itemWidth = (screenWidth - spacingPx - paddingPx) / 2;

        // Đặt chiều cao cố định cho hình chữ nhật (ví dụ: 3/4 chiều rộng)
        int itemHeight = (int) (itemWidth * 0.75f);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemWidth, itemHeight);
        view.setLayoutParams(layoutParams);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Format text để xuống dòng nếu có 2 từ trở lên (trừ item cuối)
        String categoryName = category.getName();
        if (position != categoryList.size() - 1) { // Không phải item cuối
            String[] words = categoryName.split(" ");
            if (words.length >= 2) {
                // Nếu có 2 từ trở lên, xuống dòng sau từ đầu tiên
                StringBuilder formattedName = new StringBuilder();
                formattedName.append(words[0]).append("\n");
                for (int i = 1; i < words.length; i++) {
                    if (i > 1) formattedName.append(" ");
                    formattedName.append(words[i]);
                }
                categoryName = formattedName.toString();
            }
        }

        holder.txtCategoryName.setText(categoryName);
        holder.imgCategory.setImageResource(category.getImageResId());

        // Xử lý item cuối cùng chiếm 2 cột
        if (position == categoryList.size() - 1) {
            int screenWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
            int paddingPx = (int) (17 * 2 * holder.itemView.getResources().getDisplayMetrics().density);
            int spacingPx = (int) (8 * holder.itemView.getResources().getDisplayMetrics().density);

            int fullWidth = screenWidth - paddingPx - spacingPx;

            // Giảm chiều rộng ví dụ 32dp (theo density)
            int reduceWidthPx = (int) (14.5 * holder.itemView.getResources().getDisplayMetrics().density);
            int newWidth = fullWidth - reduceWidthPx;

            int itemHeight = (int) (newWidth * 0.75f / 2);

            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(newWidth, itemHeight);
            // Đặt margin 2 bên đều, mỗi bên giảm một nửa reduceWidthPx
            int marginSide = reduceWidthPx / 2;
            layoutParams.setMargins(marginSide, 0, marginSide, 0);

            holder.itemView.setLayoutParams(layoutParams);
        }

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        ImageView imgCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgCategory = itemView.findViewById(R.id.imgCategory);
        }
    }
}