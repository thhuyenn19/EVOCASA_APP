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
import com.google.firebase.firestore.ListenerRegistration;
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
    private boolean isFirstTimeChat = true;
    private boolean isChatWithEmployeeActive = false; // Trạng thái chat với nhân viên
    private ListenerRegistration messageListener; // Để quản lý listener
    private long chatSessionId; // ID phiên chat để tạo chatId unique

    // UI elements
    private TextView txtChatEmployee;
    private LinearLayout replyOptionsContainer;
    private LinearLayout inputContainer;

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

        // Khởi tạo UI elements
        initializeViews();

        // Khởi tạo RecyclerView
        initializeRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Hiển thị giao diện ban đầu
        showInitialInterface();
    }

    private void initializeViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        edtTypeMessage = findViewById(R.id.edtTypeMessage);
        imgSend = findViewById(R.id.imgSend);
        txtChatEmployee = findViewById(R.id.txtChatEmployee);
        replyOptionsContainer = findViewById(R.id.reply_options_container);
        inputContainer = findViewById(R.id.input_container);
    }

    private void initializeRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
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
        if (txtChatEmployee != null) {
            txtChatEmployee.setOnClickListener(v -> {
                if (isChatWithEmployeeActive) {
                    // Kết thúc chat và quay về giao diện ban đầu
                    endChatWithEmployee();
                } else {
                    // Bắt đầu chat với nhân viên
                    startChatWithEmployee();
                }
            });
        }

        // Handle send button
        if (imgSend != null) {
            imgSend.setOnClickListener(v -> {
                String messageText = edtTypeMessage.getText().toString().trim();
                if (!messageText.isEmpty() && userId != null && chatId != null && isChatWithEmployeeActive) {
                    sendMessage(messageText, userId);
                    edtTypeMessage.setText("");
                }
            });
        }
    }

    private void showInitialInterface() {
        // Hiển thị giao diện ban đầu với các tùy chọn
        if (replyOptionsContainer != null) {
            replyOptionsContainer.setVisibility(View.VISIBLE);
        }
        // Input container vẫn hiển thị nhưng không hoạt động
        if (inputContainer != null) {
            inputContainer.setVisibility(View.VISIBLE);
        }
        if (txtChatEmployee != null) {
            txtChatEmployee.setText(R.string.title_chat_with_employee);
        }

        // Clear messages
        messages.clear();
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }

        isChatWithEmployeeActive = false;
    }

    private void showChatInterface() {
        // Hiển thị giao diện chat - ẩn reply options
        if (replyOptionsContainer != null) {
            replyOptionsContainer.setVisibility(View.GONE);
        }
        // Input container vẫn hiển thị và hoạt động
        if (inputContainer != null) {
            inputContainer.setVisibility(View.VISIBLE);
        }
        if (txtChatEmployee != null) {
            txtChatEmployee.setText("End Chat");
        }

        isChatWithEmployeeActive = true;
    }

    private void startChatWithEmployee() {
        if (userId == null) return;

        Log.d(TAG, "Starting chat with employee");

        // Tạo session ID unique cho mỗi lần chat
        chatSessionId = System.currentTimeMillis();
        chatId = "chat_" + userId + "_admin_" + chatSessionId;

        // Chuyển sang giao diện chat
        showChatInterface();

        // Tạo chat document trong Firestore
        createChatDocument();

        // Hiển thị thông báo support agent joined
        showSupportAgentJoinedMessage();

        // Bắt đầu lắng nghe tin nhắn
        listenForMessages();

        isFirstTimeChat = false;
    }

    private void endChatWithEmployee() {
        Log.d(TAG, "Ending chat with employee");

        // Dừng lắng nghe tin nhắn
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }

        // Quay về giao diện ban đầu
        showInitialInterface();

        // Reset chat variables
        chatId = null;
        chatSessionId = 0;
    }

    private void createChatDocument() {
        com.google.firebase.firestore.DocumentReference chatRef = db.collection("Chats").document(chatId);
        ArrayList<String> participants = new ArrayList<>();
        participants.add(userId);
        participants.add(adminId != null ? adminId : "admin");

        chatRef.set(new java.util.HashMap<String, Object>() {{
            put("participants", participants);
            put("lastMessage", "");
            put("sessionId", chatSessionId);
            put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        }}).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Chat document created with session ID: " + chatSessionId);
        }).addOnFailureListener(e -> Log.e(TAG, "Error creating chat document", e));
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
        }).addOnFailureListener(e -> Log.e(TAG, "Error sending message", e));
    }

    private void listenForMessages() {
        if (chatId == null) return;

        // Remove existing listener if any
        if (messageListener != null) {
            messageListener.remove();
        }

        messageListener = db.collection("chats").document(chatId).collection("messages")
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
            if (chatId != null && isChatWithEmployeeActive) {
                // Update the current chat session with new admin ID
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup listener when activity is destroyed
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}