package com.mobile.evocasa.payment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.evocasa.R;
import com.mobile.evocasa.payment.ShippingAddressFragment;
import com.mobile.utils.FontUtils;

public class EditAddressPaymentFragment extends Fragment {

    private View view;
    private ImageView imgProfileDetailsBack;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public EditAddressPaymentFragment() {
    }

    public static EditAddressPaymentFragment newInstance(String param1, String param2) {
        EditAddressPaymentFragment fragment = new EditAddressPaymentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_address, container, false);

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        imgProfileDetailsBack.setOnClickListener(v -> {
            // Tạo và hiển thị custom dialog
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.custom_exit_dialog);
            dialog.setCancelable(true); // Hoặc false nếu bạn không muốn người dùng bấm ra ngoài để đóng

            // Ánh xạ các nút trong custom_exit_dialog (ví dụ: Confirm và Cancel)
            Button btnExit = dialog.findViewById(R.id.btn_exit);
            Button btnSave = dialog.findViewById(R.id.btn_save);
            ImageView btnExitIcon = dialog.findViewById(R.id.btn_close_icon);

            btnExit.setOnClickListener(confirmView -> {
                // Xử lý khi người dùng chọn xác nhận (ví dụ: thoát Fragment, hoặc thoát Activity)
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EditShippingPaymentFragment())
                        .addToBackStack(null)
                        .commit();

                dialog.dismiss();
            });

            btnExitIcon.setOnClickListener(view -> {
                dialog.dismiss(); // chỉ đóng dialog
            });


            btnSave.setOnClickListener(cancelView -> {
                // Đóng dialog nếu người dùng huỷ
                dialog.dismiss();
            });

            dialog.show();

            // Cài đặt lại kích thước và nền trong suốt cho dialog
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        });


        return view;
    }
}