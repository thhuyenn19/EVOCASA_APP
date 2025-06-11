package com.mobile.evocasa;

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

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

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

        // Handle back button click
        LinearLayout btnCartBack = findViewById(R.id.btnCartBack);
        if (btnCartBack != null) {
            btnCartBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Close the current activity
                }
            });
        }

        // Handle click for txtRecommend to open ChatActivity2
        TextView txtRecommend = findViewById(R.id.txtRecommend);
        if (txtRecommend != null) {
            Log.d(TAG, "txtRecommend found, setting click listener");
            txtRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "txtRecommend clicked, starting ChatActivity2");
                    Intent intent = new Intent(ChatActivity.this, ChatActivity2.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "txtRecommend not found in layout");
        }
    }
}