package com.mobile.evocasa.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.utils.UserSessionManager;
import com.mobile.evocasa.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private LinearLayout agentNotification;
    private FirebaseFirestore db;
    private String userId; // Sẽ lấy từ UserSessionManager
    private String adminId = null; // Chưa có, sẽ cập nhật sau
    private String chatId; // ID cuộc trò chuyện
    private UserSessionManager sessionManager;

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
            // Xử lý trường hợp chưa đăng nhập (ví dụ: chuyển hướng đến màn hình đăng nhập)
            // Ví dụ: finish(); hoặc startActivity(new Intent(this, LoginActivity.class));
        } else {
            Log.d(TAG, "User logged in with userId: " + userId);
        }

        // Handle back button click
        LinearLayout btnCartBack = findViewById(R.id.btnCartBack);
        if (btnCartBack != null) {
            btnCartBack.setOnClickListener(v -> finish());
        }

        // Handle click for txtRecommend to open ChatActivity2
        TextView txtRecommend = findViewById(R.id.txtRecommend);
        if (txtRecommend != null) {
            txtRecommend.setOnClickListener(v -> {
                Log.d(TAG, "txtRecommend clicked, starting ChatActivity2");
                Intent intent = new Intent(ChatActivity.this, ChatActivity2.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "txtRecommend not found in layout");
        }

        // Handle click for txtChatEmployee
        TextView txtChatEmployee = findViewById(R.id.txtChatEmployee);
        agentNotification = findViewById(R.id.agent_notification);
        if (txtChatEmployee != null) {
            txtChatEmployee.setOnClickListener(v -> {
                Log.d(TAG, "txtChatEmployee clicked, showing agent notification");
                if (userId != null) {
                    agentNotification.setVisibility(View.VISIBLE);

                    // Tạo chatId dựa trên userId và adminId (adminId có thể null lúc này)
                    chatId = "chat_" + userId + "_admin"; // Sử dụng "admin" tạm thời, sẽ cập nhật sau
                    startChatWithEmployee();
                } else {
                    Log.e(TAG, "Cannot start chat, userId is null");
                }
            });
        } else {
            Log.e(TAG, "txtChatEmployee not found in layout");
        }
    }

    private void startChatWithEmployee() {
        if (userId == null) return;

        // Dữ liệu cho document chats
        com.google.firebase.firestore.DocumentReference chatRef = db.collection("chats").document(chatId);
        ArrayList<String> participants = new ArrayList<>();
        participants.add(userId);
        participants.add(adminId != null ? adminId : "admin");
        chatRef.set(new HashMap<String, Object>() {{
                    put("participants", participants);
                    put("lastMessage", "A support agent has joined the conversation");
                    put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                }}).addOnSuccessListener(aVoid -> Log.d(TAG, "Chat document created"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating chat document", e));

        // Dữ liệu cho message đầu tiên
        com.google.firebase.firestore.CollectionReference messagesRef = chatRef.collection("messages");
        messagesRef.add(new HashMap<String, Object>() {{
                    put("senderId", adminId != null ? adminId : "admin");
                    put("message", "A support agent has joined the conversation");
                    put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    put("isRead", false);
                }}).addOnSuccessListener(documentReference -> Log.d(TAG, "Initial message added"))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding initial message", e));
    }

    // Phương thức để cập nhật adminId khi có phản hồi từ admin
    public void updateAdminId(String newAdminId) {
        if (newAdminId != null && !newAdminId.isEmpty()) {
            adminId = newAdminId;
            Log.d(TAG, "Admin ID updated to: " + adminId);
            // Cập nhật lại chatId nếu cần
            if (chatId != null) {
                chatId = "chat_" + userId + "_" + adminId;
                // Cập nhật document chats với adminId mới (tùy chọn)
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