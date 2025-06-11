package com.mobile.evocasa.profile;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditShippingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditShippingFragment extends Fragment {

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

    public EditShippingFragment() {
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
    public static EditShippingFragment newInstance(String param1, String param2) {
        EditShippingFragment fragment = new EditShippingFragment();
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

        // Gán sự kiện quay lại ProfilDetailsFragment
        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        imgProfileDetailsBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileDetailFragment())
                    .addToBackStack(null)
                    .commit();
        });


        //Address
        EditText edtAddressShip = view.findViewById(R.id.edtAddressShip);
        edtAddressShip.setOnClickListener(v -> {
            // Chuyển sang EditAddressFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditAddressFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;

    }
}