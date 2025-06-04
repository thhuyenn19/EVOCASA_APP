package com.mobile.evocasa;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
    LinearLayout tabHome;
    LinearLayout tabShop;
    LinearLayout tabNotification;
    LinearLayout tabProfile;

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
        highlightTab(tabHome, txtHome, imgHome);
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
        tabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab(tabHome, txtHome, imgHome);
                openHomeActivity();
            }
        });

        tabShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab(tabShop, txtShop, imgShop);
                openShopActivity();
            }
        });
        tabNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab(tabNotification, txtNotification, imgNotification);
                openNotificationActivity();
            }
        });
        tabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightTab(tabProfile, txtProfile, imgProfile);
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
        tabHome = findViewById(R.id.tabHome);
        tabShop = findViewById(R.id.tabShop);
        tabNotification = findViewById(R.id.tabNotification);
        tabProfile = findViewById(R.id.tabProfile);

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
    // Khai báo biến để lưu tab đang chọn
    private LinearLayout currentTab = null;

    // Hàm này đổi màu cho tab được chọn
    private void highlightTab(LinearLayout tab, TextView txt, ImageView img) {
        // Nếu có tab cũ đang chọn thì trả về màu cũ
        if (currentTab != null && currentTab != tab) {
            if (currentTab == tabHome) {
                txtHome.setTextColor(getResources().getColor(R.color.color_5E4C3E));
                imgHome.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
            } else if (currentTab == tabShop) {
                txtShop.setTextColor(getResources().getColor(R.color.color_5E4C3E));
                imgShop.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
            } else if (currentTab == tabNotification) {
                txtNotification.setTextColor(getResources().getColor(R.color.color_5E4C3E));
                imgNotification.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
            } else if (currentTab == tabProfile) {
                txtProfile.setTextColor(getResources().getColor(R.color.color_5E4C3E));
                imgProfile.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
            }
        }
        // Đổi màu cho tab được chọn
        txt.setTextColor(getResources().getColor(R.color.color_tab_active));
        img.setColorFilter(getResources().getColor(R.color.color_tab_active));

        // Lưu tab đang chọn
        currentTab = tab;
    }
}