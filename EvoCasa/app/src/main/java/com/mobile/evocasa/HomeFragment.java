package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    //       Khi làm phần này nhớ đổi font chử cho logo với search, tham khảo bài cũ hoặc duwosi đâu
//    private void addViews() {
//        txtEvocasa = findViewById(R.id.txtEvocasa);
//        txtHome = findViewById(R.id.txtHome);
//        txtShop = findViewById(R.id.txtShop);
//        txtNotification = findViewById(R.id.txtNotification);
//        txtProfile = findViewById(R.id.txtProfile);
//        edtSearch = findViewById(R.id.edtSearch);
//        imgChat = findViewById(R.id.imgChat);
//        imgCart = findViewById(R.id.imgCart);
//        imgHome = findViewById(R.id.imgHome);
//        imgShop = findViewById(R.id.imgShop);
//        imgNotification = findViewById(R.id.imgNotification);
//        imgProfile = findViewById(R.id.imgProfile);
//        imgMic = findViewById(R.id.imgMic);
//        imgCamera = findViewById(R.id.imgCamera);
//        imgSearch = findViewById(R.id.imgSearch);
//        tabHome = findViewById(R.id.tabHome);
//        tabShop = findViewById(R.id.tabShop);
//        tabNotification = findViewById(R.id.tabNotification);
//        tabProfile = findViewById(R.id.tabProfile);
//
//        // Load custom font từ assets
//
//        Typeface fontTitle = Typeface.createFromAsset(getAssets(), "fonts/ZenOldMincho-Bold.ttf");
//        Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Inter-Regular.otf");
//
//        // Áp dụng font
//        txtEvocasa.setTypeface(fontTitle);
//        txtHome.setTypeface(fontRegular);
//        txtShop.setTypeface(fontRegular);
//        txtNotification.setTypeface(fontRegular);
//        txtProfile.setTypeface(fontRegular);
//        edtSearch.setTypeface(fontRegular);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
