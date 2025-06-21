package com.mobile.evocasa.payment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.BottomNavFragment;
import com.mobile.evocasa.NarBarActivity;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

public class FinishPaymentFragment extends Fragment {
    private LinearLayout tabHome, tabShop, tabNotification, tabProfile;
    private TextView txtHome, txtShop, txtNotification, txtProfile;
    private ImageView imgHome, imgShop, imgNotification, imgProfile;

    private Typeface fontSelected, fontRegular;
    private TextView txtThanks;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finish_payment, container, false);

        // Inflate bottom nav layout vào container trong layout

        // Set custom fonts
        FontUtils.setZboldFont(requireContext(), view.findViewById(R.id.txtTitle));
        FontUtils.setBoldFont(requireContext(), view.findViewById(R.id.txtPaymentSuccessfulTitle));
        FontUtils.setMediumitalicFont(requireContext(), view.findViewById(R.id.txtPaymentSuccessfulThanks));

        // Nút quay lại
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            BottomNavFragment bottomNav = (BottomNavFragment)
                    getParentFragmentManager().findFragmentById(R.id.bottom_nav_container);

            if (bottomNav != null) {
                bottomNav.selectTab(0); // Highlight lại tab Home + chuyển fragment
            }
        });

        // Nút Track Orders (nếu cần implement thêm)
        view.findViewById(R.id.btnTrackOrders).setOnClickListener(v -> {
            // TODO: mở TrackOrdersFragment nếu có
        });

        view.findViewById(R.id.btnBackShop).setOnClickListener(v -> {
            BottomNavFragment bottomNav = (BottomNavFragment)
                    getParentFragmentManager().findFragmentById(R.id.bottom_nav_container);

            if (bottomNav != null) {
                bottomNav.selectTab(0); // Highlight lại tab Home + chuyển fragment
            }
        });

        txtThanks = view.findViewById(R.id.txtPaymentSuccessfulThanks);

        loadCustomerName();

        return view;
    }

    private void loadCustomerName() {
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || uid.isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("Name");
                        if (name != null && !name.isEmpty()) {
                            String msg = "Thank you " + name + ", your order has been placed.";
                            txtThanks.setText(msg);
                        }
                    }
                });
    }


    private void startNavActivity(int tabPos) {
        Intent intent = new Intent(requireContext(), NarBarActivity.class);
        intent.putExtra("tab_pos", tabPos);
        startActivity(intent);
    }
}
