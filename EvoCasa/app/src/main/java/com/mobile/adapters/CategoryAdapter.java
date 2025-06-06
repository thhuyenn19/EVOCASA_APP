package com.mobile.adapters;

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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category item = categoryList.get(position);
        holder.txtCategoryName.setText(item.getName());
        holder.imgCategory.setImageResource(item.getImageResId());
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
