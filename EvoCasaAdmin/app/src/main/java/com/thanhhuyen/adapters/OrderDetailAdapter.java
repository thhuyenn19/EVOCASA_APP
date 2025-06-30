package com.thanhhuyen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.evocasaadmin.OrderDetailActivity;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.evocasaadmin.R;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private List<Order> orderList;
    private Context context;

    // ✅ Constructor
    public OrderDetailAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        Order order = orderList.get(position);

        // ✅ Binding data to item_order_detail.xml with safe null checks
        holder.tv_order_id.setText("OrderID: " + (order.getOrderId() != null ? order.getOrderId() : "N/A"));
        holder.tv_total_price.setText("Amount: $" + order.getTotalPrice());
        holder.tv_order_date.setText("Date: " + order.getFormattedOrderDate());
        holder.tv_payment_method.setText("Payment: " + order.getPaymentMethod());
        holder.tv_status.setText("Status: " + order.getStatus());

        // ✅ Click item to open OrderDetailActivity using tracking number
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            // Use tracking number instead of order_id since OrderDetailActivity expects tracking number
            String trackingNumber = order.getTrackingNumber();
            if (trackingNumber != null && !trackingNumber.isEmpty()) {
                intent.putExtra("trackingNumber", trackingNumber);
                context.startActivity(intent);
            } else {
                // Fallback: try using orderId if tracking number is not available
                intent.putExtra("trackingNumber", order.getOrderId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tv_order_id, tv_total_price, tv_order_date, tv_payment_method, tv_status;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_order_id = itemView.findViewById(R.id.tv_order_id);
            tv_total_price = itemView.findViewById(R.id.tv_total_price);
            tv_order_date = itemView.findViewById(R.id.tv_order_date);
            tv_payment_method = itemView.findViewById(R.id.tv_payment_method);
            tv_status = itemView.findViewById(R.id.tv_status);
        }
    }
}