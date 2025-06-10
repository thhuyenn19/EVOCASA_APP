package com.mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.OrderStatus;
import com.mobile.utils.FontUtils;

import java.util.List;

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.ViewHolder> {
    public interface OnStatusClickListener {
        void onStatusSelected(String status);
    }


    private final List<OrderStatus> list;

    private final OnStatusClickListener listener;

    public OrderStatusAdapter(List<OrderStatus> list, OnStatusClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderStatus;
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            container = (LinearLayout) itemView;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderStatus status = list.get(position);
        holder.txtOrderStatus.setText(status.getTitle());

        if (status.isSelected()) {
            holder.container.setBackgroundResource(R.drawable.bg_orders_status_selected);
            holder.txtOrderStatus.setTextColor(Color.WHITE);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_orders_status_unselected);
            holder.txtOrderStatus.setTextColor(Color.parseColor("#5E4C3E"));
        }
        FontUtils.setMediumFont(holder.itemView.getContext(), holder.txtOrderStatus);
        holder.container.setOnClickListener(v -> {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setSelected(i == position);
            }
            notifyDataSetChanged();

            if (listener != null) {
                listener.onStatusSelected(status.getTitle());
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}