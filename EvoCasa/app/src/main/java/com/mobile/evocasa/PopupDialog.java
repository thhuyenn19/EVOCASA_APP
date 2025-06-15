package com.mobile.evocasa;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PopupDialog extends DialogFragment {

    public interface OnShopClickListener {
        void onShopClick();
    }

    private OnShopClickListener shopClickListener;

    public void setOnShopClickListener(OnShopClickListener listener) {
        this.shopClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Bỏ title
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        // Cho phép bấm ra ngoài để đóng
        setCancelable(true);

        // Inflate layout
        View view = inflater.inflate(R.layout.popup_new_collection, container, false);

        // Đóng khi bấm vào nút đóng
        view.findViewById(R.id.imgPopupClose).setOnClickListener(v -> dismiss());

        // Xử lý click vào btnShopNow
        view.findViewById(R.id.btnShopNow).setOnClickListener(v -> {
            if (shopClickListener != null) {
                shopClickListener.onShopClick();
            }
            dismiss(); // Đóng popup sau khi click
        });

        // Tự động đóng sau 10 giây
        new Handler().postDelayed(this::dismiss, 10_000);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();

            // Bỏ background mặc định
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);

            // Làm mờ nền phía sau
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.6f;

            // Set kích thước cố định theo dp (chuyển sang pixel)
            float density = getResources().getDisplayMetrics().density;
            int widthPx = (int) (330 * density);
            int heightPx = (int) (440 * density);

            params.width = widthPx;
            params.height = heightPx;

            window.setAttributes(params);
        }
    }
}