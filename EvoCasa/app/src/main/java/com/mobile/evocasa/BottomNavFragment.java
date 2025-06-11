package com.mobile.evocasa;

import android.graphics.Typeface;
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

    private Typeface fontSelected, fontRegular;
    private int selectedTab = 0;

    public static BottomNavFragment newInstance(int selectedTab) {
        BottomNavFragment fragment = new BottomNavFragment();
        Bundle args = new Bundle();
        args.putInt("selected_tab", selectedTab);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_nav, container, false);

        if (getArguments() != null) {
            selectedTab = getArguments().getInt("selected_tab", 0);
        }

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

        fontSelected = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-SemiBold.otf");
        fontRegular = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Regular.otf");

        tabHome.setOnClickListener(v -> selectTab(0));
        tabShop.setOnClickListener(v -> selectTab(1));
        tabNotification.setOnClickListener(v -> selectTab(2));
        tabProfile.setOnClickListener(v -> selectTab(3));

        // Chỉ highlight và gọi callback nếu là tab 0–3
        if (selectedTab < 4) {
            selectTab(selectedTab);
        }

        return view;
    }

    public void selectTab(int pos) {
        highlightTab(pos);
        if (getActivity() instanceof OnBottomNavSelectedListener) {
            ((OnBottomNavSelectedListener) getActivity()).onBottomNavSelected(pos);
        }
    }

    private void highlightTab(int pos) {
        txtHome.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        txtHome.setTypeface(fontRegular);
        imgHome.setColorFilter(getResources().getColor(R.color.color_5E4C3E));

        txtShop.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        txtShop.setTypeface(fontRegular);
        imgShop.setColorFilter(getResources().getColor(R.color.color_5E4C3E));

        txtNotification.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        txtNotification.setTypeface(fontRegular);
        imgNotification.setColorFilter(getResources().getColor(R.color.color_5E4C3E));

        txtProfile.setTextColor(getResources().getColor(R.color.color_5E4C3E));
        txtProfile.setTypeface(fontRegular);
        imgProfile.setColorFilter(getResources().getColor(R.color.color_5E4C3E));

        int activeColor = getResources().getColor(R.color.color_tab_active);
        switch (pos) {
            case 0:
                txtHome.setTextColor(activeColor);
                txtHome.setTypeface(fontSelected);
                imgHome.setColorFilter(activeColor);
                break;
            case 1:
                txtShop.setTextColor(activeColor);
                txtShop.setTypeface(fontSelected);
                imgShop.setColorFilter(activeColor);
                break;
            case 2:
                txtNotification.setTextColor(activeColor);
                txtNotification.setTypeface(fontSelected);
                imgNotification.setColorFilter(activeColor);
                break;
            case 3:
                txtProfile.setTextColor(activeColor);
                txtProfile.setTypeface(fontSelected);
                imgProfile.setColorFilter(activeColor);
                break;
        }
    }

    public interface OnBottomNavSelectedListener {
        void onBottomNavSelected(int position);
    }
}
