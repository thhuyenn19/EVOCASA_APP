package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NotificationItem> items;
    private final Context context;

    public NotificationAdapter(Context context, List<NotificationItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NotificationItem.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).txtHeader.setText(item.getHeaderTitle());
        } else {
            NotificationViewHolder vh = (NotificationViewHolder) holder;
            vh.imgIcon.setImageResource(item.getIconResId());
            vh.txtTitle.setText(item.getTitle());
            vh.txtMessage.setText(item.getMessage());
            vh.txtTime.setText(item.getTime());
             // Làm mờ nếu đã đọc
            float alpha = item.isRead() ? 0.7f : 1.0f;
            vh.itemView.setAlpha(alpha);

            // Thêm background color khi đã đọc
            if (item.isRead()) {
                vh.bgNotiMessage.setBackgroundColor(0x26D9CDB6); // Mã màu #D9CDB6
            } else {
                vh.bgNotiMessage.setBackgroundColor(0x00000000); // Transparent khi chưa đọc
            }
        }
    }

    public void markAllAsRead() {
        for (NotificationItem item : items) {
            if (item.getType() == NotificationItem.TYPE_NOTIFICATION) {
                item.setRead(true);
            }
        }
        notifyDataSetChanged();
    }

    // ViewHolder cho Header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHeader = itemView.findViewById(R.id.txtHeader);
        }
    }

    // ViewHolder cho Notification
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtTitle, txtMessage, txtTime;
        View bgNotiMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtTitle = itemView.findViewById(R.id.txtNotiTitle);
            txtMessage = itemView.findViewById(R.id.txtNotiMessage);
            txtTime = itemView.findViewById(R.id.txtNotiTime);
            bgNotiMessage = itemView.findViewById(R.id.bgNotiMessage);
        }
    }
}
