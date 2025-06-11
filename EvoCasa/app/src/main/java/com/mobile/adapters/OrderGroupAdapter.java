package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.utils.FontUtils;

import java.util.List;

public class OrderGroupAdapter extends RecyclerView.Adapter<OrderGroupAdapter.ViewHolder> {

    private List<OrderGroup> orderGroups;

    public OrderGroupAdapter(List<OrderGroup> orderGroups) {
        this.orderGroups = orderGroups;
    }
    public void updateData(List<OrderGroup> newList) {
        this.orderGroups.clear();
        this.orderGroups.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemContainer, btnViewMoreContainer;
        TextView btnViewMore, txtTotal;
        Button btnAction;
        ImageView iconArrow;

        public ViewHolder(View view) {
            super(view);
            itemContainer = view.findViewById(R.id.itemContainer);
            btnViewMoreContainer = view.findViewById(R.id.btnViewMoreContainer);
            btnViewMore = view.findViewById(R.id.btnViewMore);
            iconArrow = view.findViewById(R.id.iconArrow);
            txtTotal = view.findViewById(R.id.txtTotal);
            btnAction = view.findViewById(R.id.btnAction);
        }
    }

    @NonNull
    @Override
    public OrderGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderGroupAdapter.ViewHolder holder, int position) {
        OrderGroup group = orderGroups.get(position);
        Context context = holder.itemView.getContext();
        holder.itemContainer.removeAllViews();

        List<OrderItem> items = group.getItems();
        // 1. Tính tổng full trước (không phụ thuộc expand)
        int totalPrice = 0;
        for (OrderItem item : items) {
            totalPrice += item.getPrice() * item.getQuantity();
        }

// 2. Chỉ hiển thị 1 hoặc toàn bộ sản phẩm
        int showCount = group.isExpanded() ? items.size() : Math.min(1, items.size());
        for (int i = 0; i < showCount; i++) {
            OrderItem item = items.get(i);

            View productView = LayoutInflater.from(context)
                    .inflate(R.layout.item_order_product, holder.itemContainer, false);

            ImageView img = productView.findViewById(R.id.imgProduct);
            TextView title = productView.findViewById(R.id.txtTitle);
            TextView price = productView.findViewById(R.id.txtPrice);
            TextView qty = productView.findViewById(R.id.txtQuantity);

            img.setImageResource(item.getImageResId());
            title.setText(item.getTitle());
            price.setText("$" + item.getPrice());
            qty.setText("Quantity: " + item.getQuantity());

            FontUtils.setZboldFont(context, title);
            FontUtils.setZboldFont(context, price);
            FontUtils.setRegularFont(context, qty);

            holder.itemContainer.addView(productView);
        }

// 3. Gán total dựa trên toàn bộ items
        holder.txtTotal.setText("Total (" + items.size() + " items): $" + totalPrice);



        // Show or hide "View More"
        if (items.size() > showCount) {
            holder.btnViewMoreContainer.setVisibility(View.VISIBLE);
            holder.btnViewMore.setText("View More");
            FontUtils.setMediumFont(holder.itemView.getContext(), holder.btnViewMore);
            holder.iconArrow.setRotation(90); // Hướng xuống

            holder.btnViewMore.setOnClickListener(v -> {
                group.setExpanded(true);
                notifyItemChanged(position);
            });
        } else {
            holder.btnViewMoreContainer.setVisibility(View.GONE);
        }




        // Set action button based on order status
        String status = group.getStatus();
        switch (status) {
            case "Pending":
            case "Pick Up":
            case "In Transit":
                holder.btnAction.setText("Track Order");
                holder.btnAction.setEnabled(true);
                break;
            case "Review":
                holder.btnAction.setText("Rate +200");
                holder.btnAction.setEnabled(true);
                break;
            case "Completed":
                holder.btnAction.setText("Buy Again");
                holder.btnAction.setEnabled(true);
                break;
            case "Cancelled":
                holder.btnAction.setText("View Order");
                holder.btnAction.setEnabled(false);
                break;
            default:
                holder.btnAction.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return orderGroups.size();
    }
}
