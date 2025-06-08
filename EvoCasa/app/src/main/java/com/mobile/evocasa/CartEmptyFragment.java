package com.mobile.evocasa;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.utils.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartEmptyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartEmptyFragment extends Fragment {

    private View view;

    private ImageView imgCartBack;

    private Button btnBackShop;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CartEmptyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartEmptyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartEmptyFragment newInstance(String param1, String param2) {
        CartEmptyFragment fragment = new CartEmptyFragment();
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
        view = inflater.inflate(R.layout.fragment_cart_empty, container, false);

        // Gán sự kiện quay lại HomeFragment
        imgCartBack = view.findViewById(R.id.imgCartBack);
        imgCartBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Gán sự kiện quay lại HomeFragment
        btnBackShop = view.findViewById(R.id.btnBackShop);
        btnBackShop.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        //set font//
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

        TextView txtCartEmptyTitle = view.findViewById(R.id.txtCartEmptyTitle);
        FontUtils.setBoldFont(requireContext(),txtCartEmptyTitle);

        TextView txtCartDescription = view.findViewById(R.id.txtCartDescription);
        FontUtils.setMediumitalicFont(requireContext(),txtCartDescription);

        return view;
    }
}