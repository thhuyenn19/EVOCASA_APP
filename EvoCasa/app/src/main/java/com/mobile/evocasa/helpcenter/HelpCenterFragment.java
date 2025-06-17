package com.mobile.evocasa.helpcenter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.adapters.FaqAdapter;
import com.mobile.evocasa.R;
import com.mobile.evocasa.profile.EditPersonalFragment;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.FaqItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HelpCenterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelpCenterFragment extends Fragment {

    private View view;
    RecyclerView recyclerView;
    FaqAdapter faqAdapter;
    private ImageView imgWishlistBack;
    List<FaqItem> faqList;

    LinearLayout policyPurchaseGroup, policyReturnGroup, policyPrivacyGroup;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HelpCenterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HelpCenterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HelpCenterFragment newInstance(String param1, String param2) {
        HelpCenterFragment fragment = new HelpCenterFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_help_center, container, false);

        //FAQ
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<FaqItem> faqList = new ArrayList<>();
        faqList.add(new FaqItem("How can I place an order on EvoCasa?",
                "You can browse products, select your preferred size/color, and tap “Add to Cart” or “Buy Now.” Then, fill in your delivery details and choose a payment method to complete your order."));
        faqList.add(new FaqItem("Does EvoCasa support returns and exchanges?",
                "Yes. EvoCasa accepts returns or exchanges within 7 days from the delivery date for items that are defective, incorrect, or damaged during shipping."));
        faqList.add(new FaqItem("Are there any fees for returns or exchanges?",
                "Returns or exchanges are free of charge if the issue is on EvoCasa’s end. For personal reasons, customers will cover the round-trip shipping fee."));
        faqList.add(new FaqItem("Does EvoCasa protect my personal information?",
                "Absolutely. We do not share any personal data with third parties without your consent."));
        faqList.add(new FaqItem("How long does delivery take?",
                "In-stock: 2–5 business days. Made-to-order: 7–14 business days."));

        faqAdapter = new FaqAdapter(requireContext(), faqList);
        recyclerView.setAdapter(faqAdapter);

        // Gán sự kiện quay lại ProfileFragment
        imgWishlistBack = view.findViewById(R.id.imgWishlistBack);
        imgWishlistBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        //Mở policy
        LinearLayout policyReturnGroup = view.findViewById(R.id.policyReturnGroup);
        policyReturnGroup.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ReturnPolicyFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

        LinearLayout policyWarrantyGroup = view.findViewById(R.id.policyWarrantyGroup);
        policyWarrantyGroup.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new WarrantyPolicyFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

        LinearLayout policyShippingGroup = view.findViewById(R.id.policyShippingGroup);
        policyShippingGroup.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ShippingPolicyFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });


        return view;
    }
}