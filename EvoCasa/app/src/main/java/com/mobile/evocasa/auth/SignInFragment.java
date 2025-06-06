package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment {

    private TextView txtSignIn, txtDescription, txtOrWith, txtDontHave, txtTerm, txtPrivacy, txtView;
    private Button btnContinueEmailPhoneSignIn, btnContinueFacebook, btnContinueGoogle, btnSignUp;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve parameters if necessary
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Lấy tham chiếu đến các TextView và Button
        txtSignIn = rootView.findViewById(R.id.txtSignIn);
        txtDescription = rootView.findViewById(R.id.txtDescription);
        txtOrWith = rootView.findViewById(R.id.txtOrWith);
        txtDontHave = rootView.findViewById(R.id.txtDontHave);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtView = rootView.findViewById(R.id.txtView);

        btnContinueEmailPhoneSignIn = rootView.findViewById(R.id.btnContinueEmailPhoneSignIn);
        btnContinueFacebook = rootView.findViewById(R.id.btnContinueFacebook);
        btnContinueGoogle = rootView.findViewById(R.id.btnContinueGoogle);
        btnSignUp = rootView.findViewById(R.id.btnSignUp);

        // Tạo Typeface từ font trong assets
        Typeface customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/my_custom_font.ttf");

        // Áp dụng font cho các TextView và Button
        txtSignIn.setTypeface(customFont);
        txtDescription.setTypeface(customFont);
        txtOrWith.setTypeface(customFont);
        txtDontHave.setTypeface(customFont);
        txtTerm.setTypeface(customFont);
        txtPrivacy.setTypeface(customFont);
        txtView.setTypeface(customFont);

        btnContinueEmailPhoneSignIn.setTypeface(customFont);
        btnContinueFacebook.setTypeface(customFont);
        btnContinueGoogle.setTypeface(customFont);
        btnSignUp.setTypeface(customFont);

        return rootView;
    }
}
