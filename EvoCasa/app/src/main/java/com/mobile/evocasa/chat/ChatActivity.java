package com.mobile.evocasa.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.ChatAdapter;
import com.mobile.models.ChatMessage;
import com.mobile.utils.UserSessionManager;
import com.mobile.evocasa.R;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private FirebaseFirestore db;
    private String userId;
    private String adminId = null;
    private String chatId;
    private UserSessionManager sessionManager;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> messages;
    private EditText edtTypeMessage;
    private ImageView imgSend;
    private boolean isFirstTimeChat = true; // Flag để kiểm tra lần đầu chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo UserSessionManager
        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getUid();
        if (userId == null) {
            Log.w(TAG, "User not logged in, userId is null");
            finish();
        } else {
            Log.d(TAG, "User logged in with userId: " + userId);
        }

        // Khởi tạo RecyclerView
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Cuộn đến tin nhắn mới nhất
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Handle back button click
        LinearLayout btnCartBack = findViewById(R.id.btnCartBack);
        if (btnCartBack != null) {
            btnCartBack.setOnClickListener(v -> finish());
        }

        // Handle click for txtRecommend
        TextView txtRecommend = findViewById(R.id.txtRecommend);
        if (txtRecommend != null) {
            txtRecommend.setOnClickListener(v -> {
                Log.d(TAG, "txtRecommend clicked, starting ChatActivity2");
                Intent intent = new Intent(ChatActivity.this, ChatActivity2.class);
                startActivity(intent);
            });
        }

        // Handle click for txtChatEmployee
        TextView txtChatEmployee = findViewById(R.id.txtChatEmployee);
        if (txtChatEmployee != null) {
            txtChatEmployee.setOnClickListener(v -> {
                Log.d(TAG, "txtChatEmployee clicked");
                if (userId != null) {
                    chatId = "chat_" + userId + "_admin";
                    startChatWithEmployee();
                    // Hiển thị thông báo support agent joined
                    if (isFirstTimeChat) {
                        showSupportAgentJoinedMessage();
                        isFirstTimeChat = false;
                    }
                    listenForMessages();
                } else {
                    Log.e(TAG, "Cannot start chat, userId is null");
                }
            });
        }

        // Handle send button
        edtTypeMessage = findViewById(R.id.edtTypeMessage);
        imgSend = findViewById(R.id.imgSend);
        if (imgSend != null) {
            imgSend.setOnClickListener(v -> {
                String messageText = edtTypeMessage.getText().toString().trim();
                if (!messageText.isEmpty() && userId != null && chatId != null) {
                    sendMessage(messageText, userId);
                    edtTypeMessage.setText("");
                }
            });
        }
    }

    private void startChatWithEmployee() {
        if (userId == null) return;

        com.google.firebase.firestore.DocumentReference chatRef = db.collection("chats").document(chatId);
        ArrayList<String> participants = new ArrayList<>();
        participants.add(userId);
        participants.add(adminId != null ? adminId : "admin");

        // Kiểm tra nếu chat đã tồn tại, chỉ tạo nếu chưa có
        chatRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                chatRef.set(new java.util.HashMap<String, Object>() {{
                    put("participants", participants);
                    put("lastMessage", "");
                    put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                }}).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat document created");
                }).addOnFailureListener(e -> Log.e(TAG, "Error creating chat document", e));
            } else {
                Log.d(TAG, "Chat already exists, skipping creation");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error checking chat document", e));
    }

    private void sendMessage(String messageText, String senderId) {
        if (messageText.isEmpty() || userId == null || chatId == null) return;

        com.google.firebase.firestore.CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
        messagesRef.add(new java.util.HashMap<String, Object>() {{
            put("senderId", senderId);
            put("message", messageText);
            put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            put("isRead", false);
        }}).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "Message sent");
            // Xóa phần thêm tin nhắn trực tiếp vào adapter để tránh duplicate
            // Tin nhắn sẽ được thêm thông qua listenForMessages()
        }).addOnFailureListener(e -> Log.e(TAG, "Error sending message", e));
    }

    private void listenForMessages() {
        if (chatId == null) return;
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String message = dc.getDocument().getString("message");
                            String senderId = dc.getDocument().getString("senderId");
                            Date timestamp = dc.getDocument().getDate("timestamp");
                            Boolean isReadObj = dc.getDocument().getBoolean("isRead");
                            boolean isRead = isReadObj != null ? isReadObj : false;

                            ChatMessage chatMessage = new ChatMessage(message, senderId, timestamp, isRead);
                            chatAdapter.addMessage(chatMessage);
                            recyclerViewChat.scrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    private void showSupportAgentJoinedMessage() {
        // Tạo một tin nhắn hệ thống
        ChatMessage systemMessage = new ChatMessage(
                "A support agent has joined the conversation",
                "system",
                new Date(),
                true
        );
        chatAdapter.addMessage(systemMessage);
        recyclerViewChat.scrollToPosition(messages.size() - 1);
    }

    public void updateAdminId(String newAdminId) {
        if (newAdminId != null && !newAdminId.isEmpty()) {
            adminId = newAdminId;
            Log.d(TAG, "Admin ID updated to: " + adminId);
            if (chatId != null) {
                chatId = "chat_" + userId + "_" + adminId;
                com.google.firebase.firestore.DocumentReference chatRef = db.collection("chats").document(chatId);
                ArrayList<String> updatedParticipants = new ArrayList<>();
                updatedParticipants.add(userId);
                updatedParticipants.add(adminId);
                chatRef.update("participants", updatedParticipants)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat participants updated"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating chat participants", e));
            }
        }
    }
}