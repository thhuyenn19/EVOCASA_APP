package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.R;

public class PaymentMethodFragment extends Fragment {

    private LinearLayout layoutCredit, layoutMOMO, layoutInternetBanking, layoutCOD;
    private View layoutCardDetails;

    private void resetBackgrounds() {
        layoutCredit.setBackgroundResource(R.drawable.bg_border_rounded);
        layoutMOMO.setBackgroundResource(R.drawable.bg_border_rounded);
        layoutInternetBanking.setBackgroundResource(R.drawable.bg_border_rounded);
        layoutCOD.setBackgroundResource(R.drawable.bg_border_rounded);
    }

    private void hideCardDetails() {
        layoutCardDetails.setVisibility(View.GONE);
    }

    private void showCardDetails() {
        layoutCardDetails.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        // Ánh xạ View
        layoutCredit = view.findViewById(R.id.layoutCredit);
        layoutMOMO = view.findViewById(R.id.layoutMOMO);
        layoutInternetBanking = view.findViewById(R.id.layoutInternetBanking);
        layoutCOD = view.findViewById(R.id.layoutCOD);
        layoutCardDetails = view.findViewById(R.id.layoutCardDetails);

        // Handle nút back
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Credit/Debit Card chọn
        layoutCredit.setOnClickListener(v -> {
            resetBackgrounds();
            layoutCredit.setBackgroundResource(R.drawable.bg_selected_payment);
            showCardDetails();
        });

        // Momo chọn
        layoutMOMO.setOnClickListener(v -> {
            resetBackgrounds();
            layoutMOMO.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        // Internet Banking chọn
        layoutInternetBanking.setOnClickListener(v -> {
            resetBackgrounds();
            layoutInternetBanking.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        // COD chọn
        layoutCOD.setOnClickListener(v -> {
            resetBackgrounds();
            layoutCOD.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        return view;
    }
}
