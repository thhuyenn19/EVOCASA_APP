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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;
    private final String userId; // Để xác định tin nhắn của user

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_SYSTEM = 3; // Thêm type cho system message

    public ChatAdapter(List<ChatMessage> messages, String userId) {
        this.messages = messages;
        this.userId = userId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_USER:
                View userView = inflater.inflate(R.layout.item_user_message, parent, false);
                return new UserMessageViewHolder(userView);
            case VIEW_TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_system_message, parent, false);
                return new SystemMessageViewHolder(systemView);
            default: // VIEW_TYPE_BOT
                View botView = inflater.inflate(R.layout.item_bot_message, parent, false);
                return new BotMessageViewHolder(botView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        String senderId = message.getSenderId();

        if ("system".equals(senderId)) {
            return VIEW_TYPE_SYSTEM;
        } else if (senderId.equals(userId)) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }
    }

    // ViewHolder cho tin nhắn của user
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
        }
    }

    // ViewHolder cho tin nhắn của bot/admin
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView botIcon;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            botIcon = itemView.findViewById(R.id.bot_icon);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            if (botIcon != null) {
                botIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    // ViewHolder cho system message
    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView systemMessageText;

        SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            systemMessageText = itemView.findViewById(R.id.txtSuggestedForYou);
        }

        void bind(ChatMessage message) {
            systemMessageText.setText(message.getMessage());
        }
    }

    // Thêm tin nhắn mới
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}