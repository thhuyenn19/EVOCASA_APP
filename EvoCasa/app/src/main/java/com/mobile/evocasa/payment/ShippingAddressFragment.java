package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.ShippingAddressPaymentAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.ShippingAddress;

import java.util.ArrayList;
import java.util.List;

public class ShippingAddressFragment extends Fragment {

    public ShippingAddressFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipping_address, container, false);

        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);
        rvShipping.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageView imgPaymentMethodBack = view.findViewById(R.id.imgPaymentMethodBack); // 🔁 từ layout
        imgPaymentMethodBack.setOnClickListener(v -> {
            // Quay lại MainPaymentFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MainPaymentFragment())
                    .commit();
        });

        // Danh sách địa chỉ mẫu
        List<ShippingAddress> addresses = new ArrayList<>();
        addresses.add(new ShippingAddress("John Anthony", "(+84) 123 456 789",
                "669 Do Muoi, Linh Xuan, Thu Duc, HCMC, Vietnam", true));
        addresses.add(new ShippingAddress("Jessica Nguyen", "(+84) 456 789 123",
                "Thuan Giao, Thuan An, Binh Duong, Vietnam", false));

        // Adapter sử dụng RadioButton để chọn
        ShippingAddressPaymentAdapter adapter = new ShippingAddressPaymentAdapter(addresses, address -> {
            // Khi nhấn Edit → mở EditShippingPaymentFragment
            Bundle bundle = new Bundle();
            bundle.putSerializable("shippingAddress", address);
            EditShippingPaymentFragment fragment = new EditShippingPaymentFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvShipping.setAdapter(adapter);

        return view;
    }
}
