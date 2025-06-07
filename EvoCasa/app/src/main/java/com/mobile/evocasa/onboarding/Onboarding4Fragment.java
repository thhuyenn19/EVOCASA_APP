package com.mobile.evocasa.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Onboarding4Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Onboarding4Fragment extends Fragment {

    private Button btn_lets_start;
    private View mView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Onboarding4Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Onboarding4Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Onboarding4Fragment newInstance(String param1, String param2) {
        Onboarding4Fragment fragment = new Onboarding4Fragment();
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
        //return inflater.inflate(R.layout.fragment_onboarding4, container, false);

        mView = inflater.inflate(R.layout.fragment_onboarding4, container, false);

        btn_lets_start = mView.findViewById(R.id.btn_lets_start);
        btn_lets_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), Onboarding5Activity.class);
                getActivity().startActivity(intent);
            }
        });

        return mView;
    }
}