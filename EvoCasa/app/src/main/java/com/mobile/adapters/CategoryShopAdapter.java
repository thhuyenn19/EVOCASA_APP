package com.mobile.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.Category;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.List;

public class CategoryShopAdapter extends RecyclerView.Adapter<CategoryShopAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private final Context context;
    private final OnCategoryClickListener listener;

    public CategoryShopAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        // Item cuối dùng layout khác
        return (position == categoryList.size() - 1) ? 1 : 0;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_category_shop_last : R.layout.item_category_shop;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Gán font Zbold
        FontUtils.setZboldFont(context, holder.txtCategoryName);
        holder.txtCategoryName.setGravity(Gravity.CENTER);


        String categoryName = category.getName();
        if (position != categoryList.size() - 1) {
            categoryName = formatCategoryText(categoryName);
        }

        holder.txtCategoryName.setText(categoryName);


        int paddingLeft = 0;
        if ("Furniture".equals(category.getName()) || "Lighting".equals(category.getName())) {
            paddingLeft = (int) (9 * context.getResources().getDisplayMetrics().density);
        }
        holder.txtCategoryName.setPadding(paddingLeft, 0, 0, 0);

        // Click event
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }


    private String formatCategoryText(String text) {
        if (text.contains(" ")) {
            String[] words = text.split(" ");
            if (words.length == 2) {
                return words[0] + "\n" + words[1];
            } else if (words.length > 2) {
                StringBuilder firstLine = new StringBuilder();
                StringBuilder secondLine = new StringBuilder();
                int mid = words.length / 2;
                for (int i = 0; i < mid; i++) {
                    if (i > 0) firstLine.append(" ");
                    firstLine.append(words[i]);
                }
                for (int i = mid; i < words.length; i++) {
                    if (i > mid) secondLine.append(" ");
                    secondLine.append(words[i]);
                }
                return firstLine + "\n" + secondLine;
            } else {
                return words[0] + "\n" + text.substring(words[0].length()).trim();
            }
        }
        return text;
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

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
}
