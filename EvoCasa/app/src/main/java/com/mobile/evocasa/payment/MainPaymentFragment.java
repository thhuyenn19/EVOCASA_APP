package com.mobile.evocasa.payment;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.NarBarActivity;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

public class MainPaymentFragment extends Fragment {

    private View currentSelectedOption = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_payment, container, false);

        // Set font và underline cho các TextView cần thiết
        FontUtils.applyFont(view.findViewById(R.id.txtShippingAddress), requireContext(), R.font.inter);

        TextView txtVoucher = view.findViewById(R.id.txtVoucher);
        txtVoucher.setPaintFlags(txtVoucher.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView txtEditInfor = view.findViewById(R.id.txtEditInfor);
        txtEditInfor.setPaintFlags(txtEditInfor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Chuyển sang fragment chọn phương thức thanh toán
        TextView txtSeeAllPayment = view.findViewById(R.id.txtSeeAllPayment);
        txtSeeAllPayment.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PaymentMethodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Chuyển sang fragment chọn edit Shipping Address
        txtEditInfor.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ShippingAddressFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Nút Checkout
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NarBarActivity.class);
            intent.putExtra("tab_pos", 4);
            startActivity(intent);
            requireActivity().finish();
        });

        // Ánh xạ các lựa chọn thanh toán
        LinearLayout optionCOD = view.findViewById(R.id.optionCOD);
        LinearLayout optionBanking = view.findViewById(R.id.optionBanking);
        LinearLayout optionMomo = view.findViewById(R.id.optionMomo);
        LinearLayout optionCredit = view.findViewById(R.id.optionCredit);

        int selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        int defaultColor = getResources().getColor(android.R.color.transparent, null);

        // Xử lý click chọn phương thức thanh toán
        View.OnClickListener paymentClickListener = v -> {
            if (currentSelectedOption == v) {
                // Nhấn lại để bỏ chọn
                resetOption((ViewGroup) currentSelectedOption, defaultColor);
                currentSelectedOption = null;
            } else {
                // Reset lựa chọn cũ nếu có
                if (currentSelectedOption != null) {
                    resetOption((ViewGroup) currentSelectedOption, defaultColor);
                }

                // Chọn mới
                currentSelectedOption = v;
                v.setBackgroundColor(selectedColor);
                setTextStyleInViewGroup((ViewGroup) v, Typeface.BOLD);
            }
        };

        optionCOD.setOnClickListener(paymentClickListener);
        optionBanking.setOnClickListener(paymentClickListener);
        optionMomo.setOnClickListener(paymentClickListener);
        optionCredit.setOnClickListener(paymentClickListener);

        // Reset font và background ban đầu để tránh bị bold sẵn
        resetOption(optionCOD, defaultColor);
        resetOption(optionBanking, defaultColor);
        resetOption(optionMomo, defaultColor);
        resetOption(optionCredit, defaultColor);

        return view;
    }

    private void resetOption(ViewGroup group, int backgroundColor) {
        group.setBackgroundColor(backgroundColor);
        setTextStyleInViewGroup(group, Typeface.NORMAL);
    }

    private void setTextStyleInViewGroup(ViewGroup group, int style) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                Typeface original = tv.getTypeface();
                if (original != null) {
                    tv.setTypeface(Typeface.create(original, style));
                } else {
                    tv.setTypeface(null, style);
                }
            } else if (child instanceof ViewGroup) {
                setTextStyleInViewGroup((ViewGroup) child, style);
            }
        }
    }
}
