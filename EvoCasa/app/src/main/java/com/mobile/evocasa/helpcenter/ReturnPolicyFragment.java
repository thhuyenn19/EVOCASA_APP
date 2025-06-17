package com.mobile.evocasa.helpcenter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReturnPolicyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReturnPolicyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReturnPolicyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReturnPolicyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReturnPolicyFragment newInstance(String param1, String param2) {
        ReturnPolicyFragment fragment = new ReturnPolicyFragment();
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
        //return inflater.inflate(R.layout.fragment_return_policy, container, false);

        View view = inflater.inflate(R.layout.fragment_return_policy, container, false);

        View btnBack = view.findViewById(R.id.btnReturnBack);
        View imgBack = view.findViewById(R.id.imgReturnBack);

        View.OnClickListener backListener = v -> {
            requireActivity().getSupportFragmentManager().popBackStack(); // hoặc onBackPressed()
        };

        btnBack.setOnClickListener(backListener);
        imgBack.setOnClickListener(backListener);

        return view;
    }
}