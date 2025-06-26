package com.mobile.evocasa.payment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.ShippingAddressPaymentAdapter;
import com.mobile.evocasa.R;
import com.mobile.evocasa.profile.EditShippingFragment;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShippingAddressFragment extends Fragment {

    public ShippingAddressFragment() {}
    private ShippingAddress selectedShipping = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipping_address, container, false);
        
        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);
        rvShipping.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageView imgBack = view.findViewById(R.id.imgPaymentMethodBack);

        // Danh sách địa chỉ mẫu
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || uid.isEmpty()) {
            Log.e("ShippingAddressFrag", "User not logged in");
            return view;
        }
// Nhận địa chỉ đang chọn từ MainPaymentFragment (nếu có)
        Bundle args = getArguments();
        if (args != null && args.containsKey("selectedShipping")) {
            selectedShipping = (ShippingAddress) args.getSerializable("selectedShipping");
        }

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<ShippingAddress> addresses = new ArrayList<>();
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> raw =
                            (List<Map<String,Object>>) doc.get("ShippingAddresses");
                    if (raw != null) {
                        for (Map<String,Object> m : raw) {
                            addresses.add(new ShippingAddress(
                                    (String) m.get("Name"),
                                    (String) m.get("Phone"),
                                    (String) m.get("Address"),
                                    Boolean.TRUE.equals(m.get("IsDefault"))
                            ));
                        }
                    }

                    // tạo adapter với callback edit (giữ nguyên)
                    ShippingAddressPaymentAdapter adapter =
                            new ShippingAddressPaymentAdapter(
                                    addresses,
                                    // OnEditClickListener (giữ nguyên)
                                    address -> {
                                        Bundle b = new Bundle();
                                        b.putSerializable("shippingAddress", address);
                                        EditShippingFragment f = new EditShippingFragment();
                                        f.setArguments(b);
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_container, f)
                                                .addToBackStack(null)
                                                .commit();
                                    },
                                    // OnSelectClickListener (MỚI)
                                    address -> {
                                        // 1) Gửi result
                                        Bundle result = new Bundle();
                                        result.putSerializable("selectedShipping", address);
                                        getParentFragmentManager()
                                                .setFragmentResult("select_shipping", result);
                                        // 2) Quay về MainPaymentFragment
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    },
                                    selectedShipping
                            );
                    rvShipping.setAdapter(adapter);

                    // chỉ khác: back arrow gửi result rồi pop
                    imgBack.setOnClickListener(v -> {
                        ShippingAddress sel = adapter.getSelectedAddress();
                        if (sel != null) {
                            Bundle result = new Bundle();
                            result.putSerializable("selectedShipping", sel);
                            getParentFragmentManager()
                                    .setFragmentResult("select_shipping", result);
                        }
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                })
                .addOnFailureListener(e ->
                        Log.e("ShippingAddressFrag", "Fail loading addresses", e)
                );
        AppCompatButton btnAddShipping = view.findViewById(R.id.btn_add_shipping);
        btnAddShipping.setOnClickListener(v -> {
            // Mở EditShippingFragment với form rỗng
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditShippingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}