package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.R;

public class PaymentMethodFragment extends Fragment {


    private LinearLayout lCredit, lMomo, lBanking, lCOD;
    private View cardDetails;
    private EditText eNumber, eName, eExpiry, eCvv;
    private Button btnSave;
    private int selectedColor, defaultColor;


    private void resetBackgrounds() {
        lCredit.setBackgroundResource(R.drawable.bg_border_rounded);
        lMomo.setBackgroundResource(R.drawable.bg_border_rounded);
        lBanking.setBackgroundResource(R.drawable.bg_border_rounded);
        lCOD.setBackgroundResource(R.drawable.bg_border_rounded);
    }

    private void hideCardDetails() {
        cardDetails.setVisibility(View.GONE);
    }

    private void showCardDetails() {
        cardDetails.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        // Ánh xạ View
        lCredit   = view.findViewById(R.id.layoutCredit);
        lMomo     = view.findViewById(R.id.layoutMOMO);
        lBanking  = view.findViewById(R.id.layoutInternetBanking);
        lCOD      = view.findViewById(R.id.layoutCOD);
        cardDetails = view.findViewById(R.id.layoutCardDetails);

        eNumber = cardDetails.findViewById(R.id.etCardNumber);
        eName   = cardDetails.findViewById(R.id.etCardName);
        eExpiry = cardDetails.findViewById(R.id.etExpiry);
        eCvv    = cardDetails.findViewById(R.id.etCVV);
        btnSave = view.findViewById(R.id.btnSaveInfor);

        selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        defaultColor  = getResources().getColor(android.R.color.transparent, null);

        Bundle args = getArguments();
        String pre = args != null ? args.getString("paymentMethod") : null;
        if ("CREDIT".equals(pre)) {
            cardDetails.setVisibility(View.VISIBLE);
            eNumber.setText(args.getString("cardNumber"));
            eName  .setText(args.getString("cardName"));
            eExpiry.setText(args.getString("expiry"));
            eCvv   .setText(args.getString("cvv"));
            lCredit.setBackgroundColor(selectedColor);
        } else {
            cardDetails.setVisibility(View.GONE);
            if ("MOMO".equals(pre))    lMomo   .setBackgroundColor(selectedColor);
            if ("BANKING".equals(pre)) lBanking.setBackgroundColor(selectedColor);
            if ("COD".equals(pre))     lCOD    .setBackgroundColor(selectedColor);
        }
        View.OnClickListener optClick = v -> {
            resetAllOptionsBg();
            v.setBackgroundColor(selectedColor);
            cardDetails.setVisibility(v == lCredit ? View.VISIBLE : View.GONE);
        };
        lCredit  .setOnClickListener(optClick);
        lMomo    .setOnClickListener(optClick);
        lBanking .setOnClickListener(optClick);
        lCOD     .setOnClickListener(optClick);

        // ← ADD: nút Save
        btnSave.setOnClickListener(v -> {
            String method;
            if (cardDetails.getVisibility() == View.VISIBLE) {
                // validate
                if (eNumber.getText().toString().isEmpty() ||
                        eName  .getText().toString().isEmpty() ||
                        eExpiry.getText().toString().isEmpty() ||
                        eCvv   .getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),
                            "Vui lòng điền đầy đủ thông tin thẻ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                method = "CREDIT";
            } else if (lMomo.isPressed())       method = "MOMO";
            else if (lBanking.isPressed())    method = "BANKING";
            else if (lCOD.isPressed())        method = "COD";
            else                               method = null;

            Bundle result = new Bundle();
            result.putString("paymentMethod", method);
            if ("CREDIT".equals(method)) {
                result.putString("cardNumber", eNumber.getText().toString());
                result.putString("cardName",   eName  .getText().toString());
                result.putString("expiry",     eExpiry.getText().toString());
                result.putString("cvv",        eCvv   .getText().toString());
            }
            getParentFragmentManager()
                    .setFragmentResult("select_payment_method", result);
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        // Handle nút back
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Credit/Debit Card chọn
        lCredit.setOnClickListener(v -> {
            resetAllOptionsBg();
            lCredit.setBackgroundResource(R.drawable.bg_selected_payment);
            cardDetails.setVisibility(View.VISIBLE);
        });

        // Momo chọn
        lMomo.setOnClickListener(v -> {
            resetBackgrounds();
            lMomo.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        // Internet Banking chọn
        lBanking.setOnClickListener(v -> {
            resetBackgrounds();
            lBanking.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        // COD chọn
        lCOD.setOnClickListener(v -> {
            resetBackgrounds();
            lCOD.setBackgroundResource(R.drawable.bg_selected_payment);
            hideCardDetails();
        });

        return view;
    }
    private void resetAllOptionsBg() {
        lCredit .setBackgroundColor(defaultColor);
        lMomo   .setBackgroundColor(defaultColor);
        lBanking.setBackgroundColor(defaultColor);
        lCOD    .setBackgroundColor(defaultColor);
    }

}
