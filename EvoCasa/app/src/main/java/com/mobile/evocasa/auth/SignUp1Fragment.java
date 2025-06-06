package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp1Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SignUp1Fragment() {
        // Required empty public constructor
    }

    public static SignUp1Fragment newInstance(String param1, String param2) {
        SignUp1Fragment fragment = new SignUp1Fragment();
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
        View view = inflater.inflate(R.layout.fragment_sign_up1, container, false);

        // Load custom font from assets/fonts
        Typeface regular = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Regular.otf");
        Typeface bold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Bold.otf");

//        // Assign the custom fonts to TextViews and Buttons
//        TextView tvHeader = view.findViewById(R.id.tvHeader);
//        TextView tvDesc = view.findViewById(R.id.tvDesc);

        AppCompatButton btnEmail = view.findViewById(R.id.btnContinueEmailPhone);
        AppCompatButton btnFacebook = view.findViewById(R.id.btnContinueFacebook);
        AppCompatButton btnGoogle = view.findViewById(R.id.btnContinueGoogle);

//        // Set typefaces
//        if (tvHeader != null) tvHeader.setTypeface(bold);
//        if (tvDesc != null) tvDesc.setTypeface(regular);
//        if (btnEmail != null) btnEmail.setTypeface(bold);
//        if (btnFacebook != null) btnFacebook.setTypeface(bold);
//        if (btnGoogle != null) btnGoogle.setTypeface(bold);

        return view;
    }
}
