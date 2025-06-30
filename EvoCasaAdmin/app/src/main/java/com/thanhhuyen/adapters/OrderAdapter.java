package com.thanhhuyen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.evocasaadmin.OrderDetailActivity;
import com.thanhhuyen.models.Order;
import com.thanhhuyen.evocasaadmin.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    // ✅ Constructor truyền context rõ ràng
    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
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
        holder.tvCustomerName.setText("Customer ID: " + order.getCustomer_id().get("$oid"));

        String date = "-";
        if (order.getOrderDate() != null && order.getOrderDate().get("$date") != null) {
            // Ép kiểu Object thành String và sau đó lấy substring
            date = order.getOrderDate().get("$date").toString().substring(0, 10);
        }

        holder.tvOrderDate.setText("Order Date: " + date);
        holder.tvTotalPrice.setText("Total Price: " + order.getTotalPrice());
        holder.tvStatus.setText("Status: " + order.getStatus());

        // ✅ Mở OrderDetailActivity bằng tracking number
        View.OnClickListener viewDetailClick = v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("trackingNumber", order.getTrackingNumber());
            context.startActivity(intent);
        };

        holder.btnView.setOnClickListener(viewDetailClick);
        holder.txtViewLabel.setOnClickListener(viewDetailClick);

        // ✅ Bổ sung: click toàn bộ itemView mở OrderDetailActivity
        holder.itemView.setOnClickListener(viewDetailClick);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrackingNumber, tvCustomerName, tvOrderDate, tvTotalPrice, txtViewLabel, tvStatus;
        ImageView btnView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrackingNumber = itemView.findViewById(R.id.tvTrackingNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            txtViewLabel = itemView.findViewById(R.id.txtViewLabel);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
