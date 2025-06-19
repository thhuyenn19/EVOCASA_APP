package com.mobile.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.SubCategory;
import com.mobile.utils.FontUtils;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {

    // Giao diện callback để fragment biết sub category được chọn
    public interface OnSubCategoryClickListener {
        void onSubCategorySelected(String subCategoryName);
    }

    // Giao diện callback để fragment biết và scroll tới vị trí
    public interface OnSubCategoryPositionClickListener {
        void onSubCategoryClick(int position);
    }

    private final List<SubCategory> list;
    private final OnSubCategoryClickListener listener;
    private OnSubCategoryPositionClickListener positionClickListener;

    // Setter để fragment truyền vào callback xử lý scroll
    public void setOnSubCategoryClickListener(OnSubCategoryPositionClickListener positionClickListener) {
        this.positionClickListener = positionClickListener;
    }

    public SubCategoryAdapter(List<SubCategory> list, OnSubCategoryClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubCategory;
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            txtSubCategory = itemView.findViewById(R.id.txtSubCategory);
            container = (LinearLayout) itemView;
        }
    }

    @NonNull
    @Override
    public SubCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubCategoryAdapter.ViewHolder holder, int position) {
        SubCategory subCategory = list.get(position);
        Context context = holder.itemView.getContext();
        FontUtils.setMediumFont(context, holder.txtSubCategory);

        holder.txtSubCategory.setText(subCategory.getName());

        if (subCategory.isSelected()) {
            holder.container.setBackgroundResource(R.drawable.bg_subcategory_selected);
            holder.txtSubCategory.setTextColor(Color.parseColor("#5E4C3E"));
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_orders_status_unselected);
            holder.txtSubCategory.setTextColor(Color.parseColor("#5E4C3E"));
        }

        holder.container.setOnClickListener(v -> {

            for (int i = 0; i < list.size(); i++) {
                list.get(i).setSelected(i == position);
            }
            notifyDataSetChanged();

            if (listener != null) {
                listener.onSubCategorySelected(subCategory.getName());
            }

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}