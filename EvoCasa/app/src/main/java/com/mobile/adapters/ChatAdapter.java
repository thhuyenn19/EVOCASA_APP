package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;
    private final String userId; // Để xác định tin nhắn của user

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    public ChatAdapter(List<ChatMessage> messages, String userId) {
        this.messages = messages;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new ChatViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_bot_message, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getMessage());

        // Ép kiểu itemView thành LinearLayout để sử dụng setGravity
        LinearLayout layout = (LinearLayout) holder.itemView;
        if (message.getSenderId().equals(userId)) {
            // Tin nhắn của user, hiển thị bên phải
            layout.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            // Không cần botIcon cho user, để mặc định (null là OK)
        } else {
            // Tin nhắn của bot, hiển thị bên trái
            layout.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            if (holder.botIcon != null) {
                holder.botIcon.setVisibility(View.VISIBLE); // Chỉ setVisibility nếu botIcon tồn tại
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(userId) ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView botIcon;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            botIcon = itemView.findViewById(R.id.bot_icon); // Có thể null nếu không tồn tại trong layout
        }
    }

    // Thêm tin nhắn mới
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}