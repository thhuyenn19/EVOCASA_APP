package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private String selectedMethod = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        // Ánh xạ UI
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

        TextView txtTotal = view.findViewById(R.id.txtTotalValue);
        TextView txtSave  = view.findViewById(R.id.txtSavingValue);

        Bundle args = getArguments();
        if (args != null) {
            double total = args.getDouble("total", 0.0);
            double save  = args.getDouble("saving", 0.0);

            txtTotal.setText(String.format("  $%,.0f", total));   // ví dụ: "  $3,500"
            txtSave.setText(String.format("  $%,.0f", save));     // ví dụ: "  $50"
        }

        // Màu highlight và mặc định
        selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        defaultColor  = getResources().getColor(android.R.color.transparent, null);

        // Nếu có state trước, prefill và highlight
        String pre = args != null ? args.getString("paymentMethod") : null;
        if (pre != null) {
            selectedMethod = pre;
            if ("CREDIT".equals(pre)) {
                cardDetails.setVisibility(View.VISIBLE);
                eNumber.setText(args.getString("cardNumber"));
                eName  .setText(args.getString("cardName"));
                eExpiry.setText(args.getString("expiry"));
                eCvv   .setText(args.getString("cvv"));
                highlightOption(lCredit);
            } else {
                cardDetails.setVisibility(View.GONE);
                switch (pre) {
                    case "MOMO":    highlightOption(lMomo);    break;
                    case "BANKING": highlightOption(lBanking); break;
                    case "COD":     highlightOption(lCOD);     break;
                }
            }
        } else {
            cardDetails.setVisibility(View.GONE);
        }

        // Khi click chọn option → đổi màu ngay
        View.OnClickListener optClick = v -> {
            resetAllOptionsBg();
            v.setBackgroundResource(R.drawable.bg_selected_payment);
            // xác định method
            if (v == lCredit) {
                selectedMethod = "CREDIT";
                cardDetails.setVisibility(View.VISIBLE);
            } else {
                if (v == lMomo)    selectedMethod = "MOMO";
                if (v == lBanking) selectedMethod = "BANKING";
                if (v == lCOD)     selectedMethod = "COD";
                cardDetails.setVisibility(View.GONE);
            }
        };
        lCredit .setOnClickListener(optClick);
        lMomo   .setOnClickListener(optClick);
        lBanking.setOnClickListener(optClick);
        lCOD    .setOnClickListener(optClick);

        // Nút SAVE
        btnSave.setOnClickListener(v -> {
            if (selectedMethod == null) {
                Toast.makeText(getContext(),
                        "Vui lòng chọn phương thức thanh toán",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if ("CREDIT".equals(selectedMethod)) {
                if (eNumber.getText().toString().isEmpty() ||
                        eName  .getText().toString().isEmpty() ||
                        eExpiry.getText().toString().isEmpty() ||
                        eCvv   .getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),
                            "Vui lòng điền đầy đủ thông tin thẻ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Gửi result
            Bundle result = new Bundle();
            result.putString("paymentMethod", selectedMethod);
            if ("CREDIT".equals(selectedMethod)) {
                result.putString("cardNumber", eNumber.getText().toString());
                result.putString("cardName",   eName  .getText().toString());
                result.putString("expiry",     eExpiry.getText().toString());
                result.putString("cvv",        eCvv   .getText().toString());
            }
            getParentFragmentManager()
                    .setFragmentResult("select_payment_method", result);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Nút back (nếu có)
        View back = view.findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    private void resetAllOptionsBg() {
        lCredit .setBackgroundResource(R.drawable.bg_border_rounded);
        lMomo   .setBackgroundResource(R.drawable.bg_border_rounded);
        lBanking.setBackgroundResource(R.drawable.bg_border_rounded);
        lCOD    .setBackgroundResource(R.drawable.bg_border_rounded);
    }

    private void highlightOption(View option) {
        resetAllOptionsBg();
        option.setBackgroundResource(R.drawable.bg_selected_payment);
    }
}
