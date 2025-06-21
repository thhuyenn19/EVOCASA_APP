package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.ShippingAddressPaymentAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || uid.isEmpty()) {
            Log.e("ShippingAddressFrag", "User not logged in");
            return view;
        }

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<ShippingAddress> addresses = new ArrayList<>();

                    // Firestore lưu array of maps dưới key "ShippingAddresses"
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> raw =
                            (List<Map<String,Object>>) doc.get("ShippingAddresses");

                    if (raw != null) {
                        for (Map<String,Object> m : raw) {
                            String name    = (String) m.get("Name");
                            String phone   = (String) m.get("Phone");
                            String addrStr = (String) m.get("Address");
                            Boolean def    = (Boolean) m.get("IsDefault");

                            addresses.add(new ShippingAddress(
                                    name    != null ? name    : "",
                                    phone   != null ? phone   : "",
                                    addrStr != null ? addrStr : "",
                                    Boolean.TRUE.equals(def)
                            ));
                        }
                    }

                    ShippingAddressPaymentAdapter adapter =
                            new ShippingAddressPaymentAdapter(addresses, address -> {
                                // khi chọn 1 địa chỉ → mở EditShippingPaymentFragment (giữ nguyên logic)
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("shippingAddress", address);
                                EditShippingPaymentFragment frag = new EditShippingPaymentFragment();
                                frag.setArguments(bundle);
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                    rvShipping.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Log.e("ShippingAddressFrag", "Fail loading addresses", e)
                );
        // ————————————————————————————————————————————————

        return view;
    }
}