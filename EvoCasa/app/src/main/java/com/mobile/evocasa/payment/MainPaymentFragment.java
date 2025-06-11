package com.mobile.evocasa.payment;

import android.content.Intent;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_payment, container, false);

        // Ánh xạ và set font
        FontUtils.applyFont(view.findViewById(R.id.txtShippingAddress), requireContext(), R.font.inter);
        // (các dòng applyFont khác giữ nguyên như bạn đang có)

        // Sự kiện điều hướng
        TextView txtSeeAllPayment = view.findViewById(R.id.txtSeeAllPayment);
        txtSeeAllPayment.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PaymentMethodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Sự kiện nút Checkout
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NarBarActivity.class);
            intent.putExtra("tab_pos", 4);
            startActivity(intent);
            requireActivity().finish();
        });

        LinearLayout optionCOD = view.findViewById(R.id.optionCOD);
        LinearLayout optionBanking = view.findViewById(R.id.optionBanking);
        LinearLayout optionMomo = view.findViewById(R.id.optionMomo);
        LinearLayout optionCredit = view.findViewById(R.id.optionCredit);

        int selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        int defaultColor = getResources().getColor(android.R.color.transparent, null);

        View.OnClickListener paymentClickListener = v -> {
            optionCOD.setBackgroundColor(defaultColor);
            optionBanking.setBackgroundColor(defaultColor);
            optionMomo.setBackgroundColor(defaultColor);
            optionCredit.setBackgroundColor(defaultColor);

            v.setBackgroundColor(selectedColor);
        };

        optionCOD.setOnClickListener(paymentClickListener);
        optionBanking.setOnClickListener(paymentClickListener);
        optionMomo.setOnClickListener(paymentClickListener);
        optionCredit.setOnClickListener(paymentClickListener);

        return view;
    }
}
