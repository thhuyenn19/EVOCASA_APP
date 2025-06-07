package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobile.evocasa.R;

public class SignIn1Fragment extends Fragment {

    private TextView txtTitle, txtEmailPhoneLabel, txtPassword, txtTerm, txtPrivacy, txtForgotPassword, txtBy;
    private EditText edtEmailPhone, edtPassword;
    private Button btnContinue;

    public SignIn1Fragment() {
        // Required empty public constructor
    }

    public static SignIn1Fragment newInstance(String param1, String param2) {
        SignIn1Fragment fragment = new SignIn1Fragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sign_in1, container, false);

        // Get references to the views
        txtTitle = rootView.findViewById(R.id.txtTitle);
        txtEmailPhoneLabel = rootView.findViewById(R.id.txtEmailPhoneLabel);
        txtPassword = rootView.findViewById(R.id.txtPassword);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtForgotPassword = rootView.findViewById(R.id.txtForgotPassword);
        txtBy = rootView.findViewById(R.id.txtBy);

        edtEmailPhone = rootView.findViewById(R.id.edtEmailPhone);
        edtPassword = rootView.findViewById(R.id.edtPassword);

        btnContinue = rootView.findViewById(R.id.btnContinue);

        // Create Typeface from font in assets
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");
        Typeface semiBoldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface boldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Bold.otf");
        Typeface mediumFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Medium.otf");

        // Apply the custom fonts to the views
        txtTitle.setTypeface(boldFont); // Title - Bold
        txtEmailPhoneLabel.setTypeface( mediumFont); // Email/Phone Label - Medium
        txtPassword.setTypeface( mediumFont); // Password Label - Medium
        txtTerm.setTypeface(semiBoldFont); // Terms - Medium
        txtPrivacy.setTypeface(semiBoldFont); // Privacy Policy - Medium
        txtForgotPassword.setTypeface(semiBoldFont); // Forgot Password - SemiBold
        txtBy.setTypeface(regularFont);
        edtEmailPhone.setTypeface(regularFont); // Email/Phone Input - Medium
        edtPassword.setTypeface(regularFont); // Password Input - Medium

        btnContinue.setTypeface(semiBoldFont); // Continue Button - Black

        return rootView;
    }
}
