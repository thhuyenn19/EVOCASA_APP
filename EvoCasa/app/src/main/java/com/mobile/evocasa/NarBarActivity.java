package com.mobile.evocasa;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NarBarActivity extends AppCompatActivity {
    TextView txtEvocasa;
    TextView txtHome;
    TextView txtShop;
    TextView txtNotification;
    TextView txtProfile;
    ImageView imgHome;
    ImageView imgShop;
    ImageView imgChat;
    ImageView imgCart;
    ImageView imgNotification;
    ImageView imgProfile;
    ImageView imgSearch;
    ImageView imgMic;
    ImageView imgCamera;
    EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nar_bar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addViews();
        addEvents();
    }
    private void addEvents() {
        imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatActivity();
            }
        });
        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCartActivity();
            }
        });
        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeActivity();
            }
        });
        txtHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeActivity();
            }
        });
        imgShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShopActivity();
            }
        });
        txtShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShopActivity();
            }
        });
        imgNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationActivity();
            }
        });
        txtNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationActivity();
            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });
        txtProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });
    }
    void openChatActivity(){
        Intent intent= new Intent(NarBarActivity.this, ChatActivity.class);
        startActivity(intent);
    }
    void openCartActivity(){
        Intent intent= new Intent(NarBarActivity.this, CartActivity.class);
        startActivity(intent);
    }

    void openHomeActivity(){
        Intent intent= new Intent(NarBarActivity.this, HomeActivity.class);
        startActivity(intent);
    }
    void openShopActivity(){
        Intent intent= new Intent(NarBarActivity.this, ShopActivity.class);
        startActivity(intent);
    }
    private void openNotificationActivity(){
        Intent intent= new Intent(NarBarActivity.this, NotificationActivity.class);
        startActivity(intent);
    }
    private void openProfileActivity(){
        Intent intent= new Intent(NarBarActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
    private void addViews() {
        txtEvocasa = findViewById(R.id.txtEvocasa);
        txtHome = findViewById(R.id.txtHome);
        txtShop = findViewById(R.id.txtShop);
        txtNotification = findViewById(R.id.txtNotification);
        txtProfile = findViewById(R.id.txtProfile);
        edtSearch = findViewById(R.id.edtSearch);
        imgChat = findViewById(R.id.imgChat);
        imgCart = findViewById(R.id.imgCart);
        imgHome = findViewById(R.id.imgHome);
        imgShop = findViewById(R.id.imgShop);
        imgNotification = findViewById(R.id.imgNotification);
        imgProfile = findViewById(R.id.imgProfile);
        imgMic = findViewById(R.id.imgMic);
        imgCamera = findViewById(R.id.imgCamera);
        imgSearch = findViewById(R.id.imgSearch);

        // Load custom font từ assets

        Typeface fontTitle = Typeface.createFromAsset(getAssets(), "fonts/ZenOldMincho-Bold.ttf");
        Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Inter-Regular.otf");

        // Áp dụng font
        txtEvocasa.setTypeface(fontTitle);
        txtHome.setTypeface(fontRegular);
        txtShop.setTypeface(fontRegular);
        txtNotification.setTypeface(fontRegular);
        txtProfile.setTypeface(fontRegular);
        edtSearch.setTypeface(fontRegular);
    }
}