package com.mobile.evocasa.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.adapters.ProfileInfoAdapter;
import com.mobile.adapters.ShippingAddressAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.ProfileInfo;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;


public class ProfileDetailFragment extends Fragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        applyCustomFonts(view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_profile_info);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ProfileInfo> data = new ArrayList<>();
        data.add(new ProfileInfo("Email", "john123456@gmail.com", R.drawable.ic_email));
        data.add(new ProfileInfo("Phone", "(+84) 123 456 789", R.drawable.ic_phone));
        data.add(new ProfileInfo("Birthday", "01/02/2003", R.drawable.ic_birthday));
        data.add(new ProfileInfo("Location", "HCMC, Viet Nam", R.drawable.ic_location));

        ProfileInfoAdapter adapter = new ProfileInfoAdapter(data);
        rv.setAdapter(adapter);
        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);
        rvShipping.setLayoutManager(new LinearLayoutManager(getContext()));

// Danh sách giả định
        List<ShippingAddress> addresses = new ArrayList<>();
        addresses.add(new ShippingAddress(
                "John Anthony", "(+84) 123 456 789",
                "669 Do Muoi, Linh Xuan, Thu Duc, HCMC, Vietnam", true));
        addresses.add(new ShippingAddress(
                "Jessica Nguyen", "(+84) 456 789 123",
                "Thuan Giao, Thuan An, Binh Duong, Vietnam", false));

//        ShippingAddressAdapter shipadapter = new ShippingAddressAdapter(addresses);
//        rvShipping.setAdapter(shipadapter);

        ShippingAddressAdapter shipadapter = new ShippingAddressAdapter(addresses, address -> {
            // Mở EditShippingFragment và truyền dữ liệu qua Bundle
            Bundle bundle = new Bundle();
            bundle.putSerializable("shippingAddress", address);

            EditShippingFragment fragment = new EditShippingFragment();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvShipping.setAdapter(shipadapter);


        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack(); // Quay lại fragment trước (ProfileFragment)
        });


        //Mở edit infor
        TextView txtEdit = view.findViewById(R.id.txtEdit);
        txtEdit.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditPersonalFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

        ImageView imgEditProfile = view.findViewById(R.id.imgEditProfile);
        imgEditProfile.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditPersonalFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

    }

    private void applyCustomFonts(View view) {
        TextView txtName = view.findViewById(R.id.txtName);
        if (txtName != null) {
            FontUtils.setZboldFont(getContext(), txtName);
        }
        TextView txtPersonalInformation = view.findViewById(R.id.txtPersonalInformation);
        TextView txtShippingAddress = view.findViewById(R.id.txtShippingAddress);
        TextView txtEdit= view.findViewById(R.id.txtEdit);
        if (txtPersonalInformation != null) {
            FontUtils.setZblackFont(getContext(), txtPersonalInformation);
        }
        if (txtShippingAddress != null) {
            FontUtils.setZblackFont(getContext(), txtShippingAddress);
        }
        if (txtEdit != null) {
            FontUtils.setZblackFont(getContext(), txtEdit);
        }

    }
}