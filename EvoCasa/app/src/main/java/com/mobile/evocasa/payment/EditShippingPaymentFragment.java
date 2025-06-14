package com.mobile.evocasa.payment;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.mobile.evocasa.R;
import com.mobile.evocasa.payment.EditAddressPaymentFragment;
import com.mobile.evocasa.payment.ShippingAddressFragment;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;

public class EditShippingPaymentFragment extends Fragment {

    private View view;

    private ImageView imgProfileDetailsBack;

    private Button btnProfileDetailsBack;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditShippingPaymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditShippingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditShippingPaymentFragment newInstance(String param1, String param2) {
        EditShippingPaymentFragment fragment = new EditShippingPaymentFragment();
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

        if (getArguments() != null) {
            ShippingAddress address = (ShippingAddress) getArguments().getSerializable("shippingAddress");
            // TODO: Hiển thị address lên giao diện để chỉnh sửa
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_edit_shipping, container, false);

        view = inflater.inflate(R.layout.fragment_edit_shipping, container, false);

        //set font
        TextView txtTitleShip = view.findViewById(R.id.txtTitleShip);
        FontUtils.setZboldFont(requireContext(), txtTitleShip);

//        // Gán sự kiện quay lại ProfilDetailsFragment
//        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
//        imgProfileDetailsBack.setOnClickListener(v -> {
//            requireActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ProfileDetailFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });


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
                        .replace(R.id.fragment_container, new ShippingAddressFragment())
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


        //Address
        EditText edtAddressShip = view.findViewById(R.id.edtAddressShip);
        edtAddressShip.setOnClickListener(v -> {
            // Chuyển sang EditAddressFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditAddressPaymentFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;

    }
}