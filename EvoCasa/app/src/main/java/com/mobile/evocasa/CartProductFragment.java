package com.mobile.evocasa;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartProductFragment extends Fragment {

    private boolean hasProduct = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CartProductFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartProductFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartProductFragment newInstance(String param1, String param2) {
        CartProductFragment fragment = new CartProductFragment();
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
        return inflater.inflate(R.layout.fragment_cart_product, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kiểm tra nếu có sản phẩm → ẩn navigation bar
        if (hasProduct) {
            hideBottomNav();
        } else {
            showBottomNav();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Khi thoát fragment thì hiện lại navigation
        showBottomNav();
    }

    private void hideBottomNav() {
        View nav = requireActivity().findViewById(R.id.bottom_nav_container);
        if (nav != null) nav.setVisibility(View.GONE);
    }

    private void showBottomNav() {
        View nav = requireActivity().findViewById(R.id.bottom_nav_container);
        if (nav != null) nav.setVisibility(View.VISIBLE);
    }
}