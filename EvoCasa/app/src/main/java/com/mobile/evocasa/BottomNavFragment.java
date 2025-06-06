package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BottomNavFragment extends Fragment {

    private LinearLayout tabHome, tabShop, tabNotification, tabProfile;
    private TextView txtHome, txtShop, txtNotification, txtProfile;
    private ImageView imgHome, imgShop, imgNotification, imgProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_nav, container, false);

        tabHome = view.findViewById(R.id.tabHome);
        tabShop = view.findViewById(R.id.tabShop);
        tabNotification = view.findViewById(R.id.tabNotification);
        tabProfile = view.findViewById(R.id.tabProfile);

        txtHome = view.findViewById(R.id.txtHome);
        txtShop = view.findViewById(R.id.txtShop);
        txtNotification = view.findViewById(R.id.txtNotification);
        txtProfile = view.findViewById(R.id.txtProfile);

        imgHome = view.findViewById(R.id.imgHome);
        imgShop = view.findViewById(R.id.imgShop);
        imgNotification = view.findViewById(R.id.imgNotification);
        imgProfile = view.findViewById(R.id.imgProfile);

        tabHome.setOnClickListener(v -> selectTab(0));
        tabShop.setOnClickListener(v -> selectTab(1));
        tabNotification.setOnClickListener(v -> selectTab(2));
        tabProfile.setOnClickListener(v -> selectTab(3));

        // Hiện tab Home mặc định
        selectTab(0);

        return view;
    }

    private void selectTab(int pos) {
        // Đổi màu tab active
        highlightTab(pos);

        // Gửi event lên Activity
        if (getActivity() instanceof OnBottomNavSelectedListener) {
            ((OnBottomNavSelectedListener) getActivity()).onBottomNavSelected(pos);
        }
    }

    private void highlightTab(int pos) {
        // Reset tất cả về màu mặc định
        txtHome.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        imgHome.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
        txtShop.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        imgShop.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
        txtNotification.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        imgNotification.setColorFilter(getResources().getColor(R.color.color_5E4C3E));
        txtProfile.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        imgProfile.setColorFilter(getResources().getColor(R.color.color_5E4C3E));

        // Tab được chọn dùng màu nổi bật
        int activeColor = getResources().getColor(R.color.color_tab_active);
        switch (pos) {
            case 0:
                txtHome.setTextColor(activeColor);
                imgHome.setColorFilter(activeColor);
                break;
            case 1:
                txtShop.setTextColor(activeColor);
                imgShop.setColorFilter(activeColor);
                break;
            case 2:
                txtNotification.setTextColor(activeColor);
                imgNotification.setColorFilter(activeColor);
                break;
            case 3:
                txtProfile.setTextColor(activeColor);
                imgProfile.setColorFilter(activeColor);
                break;
        }
    }

    public interface OnBottomNavSelectedListener {
        void onBottomNavSelected(int position);
    }
}
