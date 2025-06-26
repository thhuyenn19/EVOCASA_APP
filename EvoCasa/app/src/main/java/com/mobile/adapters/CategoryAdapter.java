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
import com.mobile.utils.FontUtils;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.txtCategoryName.setText(category.getName());
        holder.imgCategory.setImageResource(category.getImageResId());

        // Apply Zbold font to category name
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtCategoryName);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName;
        ImageView imgCategory;

        public CategoryViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgCategory = itemView.findViewById(R.id.imgCategory);

            // Set click listener on the entire item view
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(categoryList.get(getAdapterPosition()));
                }
            });
        }
    }
}