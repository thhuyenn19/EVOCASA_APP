package com.thanhhuyen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.models.Order;
import com.thanhhuyen.evocasaadmin.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvTrackingNumber.setText("Order Tracking Number: " + order.getTrackingNumber());
        holder.tvCustomerName.setText("Customer: " + order.getCustomerName());

        String date = "-";
        if (order.getOrderDate() != null && order.getOrderDate().get("$date") != null) {
            date = order.getOrderDate().get("$date").substring(0, 10);
        }
        holder.tvOrderDate.setText("Order Date: " + date);
        holder.tvTotalPrice.setText("Total Price: " + order.getTotalPrice());
        holder.tvStatus.setText("Status: " + order.getStatus());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrackingNumber, tvCustomerName, tvOrderDate, tvTotalPrice, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrackingNumber = itemView.findViewById(R.id.tvTrackingNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}