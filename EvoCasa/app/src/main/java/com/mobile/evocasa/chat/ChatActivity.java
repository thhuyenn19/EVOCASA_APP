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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final String CHAT_API_URL = "https://chat-service-408656799826.us-central1.run.app/chat";

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
    private boolean isChatWithEmployeeActive = false;
    private boolean isChatbotActive = false; // Track chatbot state
    private ListenerRegistration messageListener;
    private long chatSessionId;
    private OkHttpClient httpClient;

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

        // Initialize HTTP client
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

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

        // Setup reply options click listeners
        setupReplyOptionsClickListeners();

        // Start with chatbot interface and send hello message
        startChatbotInterface();
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
                    // End employee chat and go back to chatbot
                    endChatWithEmployee();
                } else {
                    // Start chat with employee
                    startChatWithEmployee();
                }
            });
        }

        // Handle send button
        if (imgSend != null) {
            imgSend.setOnClickListener(v -> {
                String messageText = edtTypeMessage.getText().toString().trim();
                if (!messageText.isEmpty() && userId != null) {
                    sendUserMessage(messageText);
                    edtTypeMessage.setText("");
                }
            });
        }
    }

    private void setupReplyOptionsClickListeners() {
        if (replyOptionsContainer != null) {
            // Get all TextViews in reply options container
            for (int i = 0; i < replyOptionsContainer.getChildCount(); i++) {
                View child = replyOptionsContainer.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    textView.setOnClickListener(v -> {
                        String messageText = textView.getText().toString();
                        if (!messageText.isEmpty()) {
                            // Send the clicked message
                            sendUserMessage(messageText);
                            // Hide reply options after clicking
                            replyOptionsContainer.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }

    private void startChatbotInterface() {
        Log.d(TAG, "Starting chatbot interface");

        // Show initial interface
        if (replyOptionsContainer != null) {
            replyOptionsContainer.setVisibility(View.VISIBLE);
        }
        if (inputContainer != null) {
            inputContainer.setVisibility(View.VISIBLE);
        }
        if (txtChatEmployee != null) {
            txtChatEmployee.setText(R.string.title_chat_with_employee);
        }

        // Set chatbot active and employee inactive
        isChatbotActive = true;
        isChatWithEmployeeActive = false;

        // Update adapter to use chatbot icon
        if (chatAdapter != null) {
            chatAdapter.setChatMode(ChatAdapter.CHAT_MODE_CHATBOT);
        }

        // Send hello message to chatbot
        sendChatbotHelloMessage();
    }

    private void startChatWithEmployee() {
        if (userId == null) return;

        Log.d(TAG, "Starting chat with employee");

        // Create session ID unique for each chat
        chatSessionId = System.currentTimeMillis();
        chatId = "chat_" + userId + "_admin_" + chatSessionId;

        // Switch to employee chat interface
        showEmployeeChatInterface();

        // Create chat document in Firestore
        createChatDocument();

        // Show support agent joined message
        showSupportAgentJoinedMessage();

        // Start listening for messages
        listenForMessages();

        // Set states
        isChatWithEmployeeActive = true;
        isChatbotActive = false;
        isFirstTimeChat = false;
    }

    private void showEmployeeChatInterface() {
        // Hide reply options
        if (replyOptionsContainer != null) {
            replyOptionsContainer.setVisibility(View.GONE);
        }
        // Keep input container visible and active
        if (inputContainer != null) {
            inputContainer.setVisibility(View.VISIBLE);
        }
        if (txtChatEmployee != null) {
            txtChatEmployee.setText("End Chat");
        }

        // Update adapter to use support icon
        if (chatAdapter != null) {
            chatAdapter.setChatMode(ChatAdapter.CHAT_MODE_EMPLOYEE);
        }
    }

    private void endChatWithEmployee() {
        Log.d(TAG, "Ending chat with employee");

        // Stop listening for messages
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }

        // Clear messages and go back to chatbot interface
        messages.clear();
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }

        // Reset chat variables
        chatId = null;
        chatSessionId = 0;

        // Go back to chatbot interface
        startChatbotInterface();
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

    private void sendUserMessage(String messageText) {
        if (messageText.isEmpty() || userId == null) return;

        // Add user message to UI immediately
        ChatMessage userMessage = new ChatMessage(messageText, userId, new Date(), true);
        chatAdapter.addMessage(userMessage);
        recyclerViewChat.scrollToPosition(messages.size() - 1);

        if (isChatWithEmployeeActive) {
            // Send to Firestore for employee chat
            if (chatId != null) {
                sendMessageToFirestore(messageText, userId);
            }
        } else if (isChatbotActive) {
            // Send to chatbot API
            sendMessageToChatbot(messageText);
        }
    }

    private void sendMessageToFirestore(String messageText, String senderId) {
        if (messageText.isEmpty() || userId == null || chatId == null) return;

        com.google.firebase.firestore.CollectionReference messagesRef = db.collection("Chats").document(chatId).collection("messages");
        messagesRef.add(new java.util.HashMap<String, Object>() {{
            put("senderId", senderId);
            put("message", messageText);
            put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            put("isRead", false);
        }}).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "Message sent to Firestore");
        }).addOnFailureListener(e -> Log.e(TAG, "Error sending message to Firestore", e));
    }

    private void sendMessageToChatbot(String message) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("query", message); // Đây là trường gửi tin nhắn

            // Tạo RequestBody để gửi nội dung JSON
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonBody.toString()  // Chuyển đổi JSON thành String
            );

            // Tạo một request HTTP POST với URL API
            Request request = new Request.Builder()
                    .url(CHAT_API_URL)   // URL của API chatbot
                    .post(body)          // Đảm bảo phương thức POST được sử dụng
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Gửi yêu cầu POST
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send message to chatbot", e);
                    runOnUiThread(() -> {
                        // Hiển thị lỗi khi kết nối không thành công
                        ChatMessage errorMessage = new ChatMessage(
                                "Sorry, I'm having trouble connecting. Please try again later.",
                                "bot",
                                new Date(),
                                true
                        );
                        chatAdapter.addMessage(errorMessage);
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String botReply = jsonResponse.optString("answer", "I'm sorry, I didn't understand that.");

                            runOnUiThread(() -> {
                                ChatMessage botMessage = new ChatMessage(botReply, "bot", new Date(), true);
                                chatAdapter.addMessage(botMessage);
                                recyclerViewChat.scrollToPosition(messages.size() - 1);
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing chatbot response", e);
                            runOnUiThread(() -> {
                                ChatMessage errorMessage = new ChatMessage(
                                        "Sorry, I'm experiencing some technical difficulties. Please try again.",
                                        "bot",
                                        new Date(),
                                        true
                                );
                                chatAdapter.addMessage(errorMessage);
                                recyclerViewChat.scrollToPosition(messages.size() - 1);
                            });
                        }
                    } else {
                        Log.e(TAG, "Chatbot API error: " + response.code());
                        runOnUiThread(() -> {
                            ChatMessage errorMessage = new ChatMessage(
                                    "Sorry, I'm experiencing some technical difficulties. Please try again.",
                                    "bot",
                                    new Date(),
                                    true
                            );
                            chatAdapter.addMessage(errorMessage);
                            recyclerViewChat.scrollToPosition(messages.size() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for chatbot request", e);
        }
    }


    private void sendChatbotHelloMessage() {
        // Send initial hello message to chatbot
        sendMessageToChatbot("Hello");
    }

    private void listenForMessages() {
        if (chatId == null) return;

        // Remove existing listener if any
        if (messageListener != null) {
            messageListener.remove();
        }

        messageListener = db.collection("Chats").document(chatId).collection("messages")
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

                            // Only add messages from Firestore that aren't from current user or bot
                            // (to avoid duplicates since we add user and bot messages directly to UI)
                            if (!senderId.equals(userId) && !senderId.equals("bot")) {
                                ChatMessage chatMessage = new ChatMessage(message, senderId, timestamp, isRead);
                                chatAdapter.addMessage(chatMessage);
                                recyclerViewChat.scrollToPosition(messages.size() - 1);
                            }
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
                com.google.firebase.firestore.DocumentReference chatRef = db.collection("Chats").document(chatId);
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