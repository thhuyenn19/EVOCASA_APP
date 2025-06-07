package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;

import com.mobile.evocasa.R;

public class SignUp2Fragment extends Fragment {

    // UI Components
    private ImageView btnBack, btnHelp;
    private TextView txtTitle, txtEmailPhoneLabel, txtTerm, txtPrivacy, txtBy;
    private EditText edtEmailPhone;
    private AppCompatButton btnContinue;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SignUp2Fragment() {
        // Required empty public constructor
    }

    public static SignUp2Fragment newInstance(String param1, String param2) {
        SignUp2Fragment fragment = new SignUp2Fragment();
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
        View view = inflater.inflate(R.layout.fragment_sign_up2, container, false);

        // Initialize UI components
        initViews(view);

        // Set custom fonts
        setCustomFonts();

        // Set click listeners
        setClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnHelp = view.findViewById(R.id.btnHelp);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtEmailPhoneLabel = view.findViewById(R.id.txtEmailPhoneLabel);
        edtEmailPhone = view.findViewById(R.id.edtEmailPhone);
        btnContinue = view.findViewById(R.id.btnContinue);
        txtTerm = view.findViewById(R.id.txtTerm);
        txtPrivacy = view.findViewById(R.id.txtPrivacy);
        txtBy = view.findViewById(R.id.txtBy);
    }

    private void setCustomFonts() {
        try {
            // Load custom fonts from assets/fonts
            Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Bold.otf");
            Typeface medium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Medium.otf");
            Typeface semiBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-SemiBold.otf");
            Typeface regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Regular.otf");

            if (bold != null && medium != null && semiBold != null && regular != null) {
                // Set typefaces for TextViews and Buttons
                txtTitle.setTypeface(bold); // Title - Bold
                txtEmailPhoneLabel.setTypeface(medium); // Email/Phone Label - Medium
                txtTerm.setTypeface(semiBold); // Term - SemiBold
                txtPrivacy.setTypeface(semiBold); // Privacy - SemiBold
                txtBy.setTypeface(regular); // "By Using" - Regular
                btnContinue.setTypeface(semiBold); // Continue Button - SemiBold
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default fonts if custom fonts are not available
            setDefaultFonts();
        }
    }

    private void setDefaultFonts() {
        // Fallback fonts if custom fonts are not available
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtEmailPhoneLabel.setTypeface(null, Typeface.NORMAL);
        txtTerm.setTypeface(null, Typeface.BOLD);
        txtPrivacy.setTypeface(null, Typeface.BOLD);
        txtBy.setTypeface(null, Typeface.NORMAL);
        btnContinue.setTypeface(null, Typeface.BOLD);
    }

    private void setClickListeners() {
        // Back button click
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Help button click
        btnHelp.setOnClickListener(v -> {
            // TODO: Implement help functionality
            // showHelpDialog() or navigate to help screen
        });

        // Continue button click
        btnContinue.setOnClickListener(v -> {
            String emailPhone = edtEmailPhone.getText().toString().trim();
            if (validateInput(emailPhone)) {
                // Proceed to next step
                proceedToNextStep(emailPhone);
            }
        });

        // Terms click
        txtTerm.setOnClickListener(v -> {
            // TODO: Open Terms page
            openTerms();
        });

        // Privacy Policy click
        txtPrivacy.setOnClickListener(v -> {
            // TODO: Open Privacy Policy page
            openPrivacyPolicy();
        });
    }

    private boolean validateInput(String input) {
        if (input.isEmpty()) {
            edtEmailPhone.setError("Please enter email or phone number");
            edtEmailPhone.requestFocus();
            return false;
        }

        // Basic validation - you can enhance this
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() &&
                !android.util.Patterns.PHONE.matcher(input).matches()) {
            edtEmailPhone.setError("Please enter a valid email or phone number");
            edtEmailPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void proceedToNextStep(String emailPhone) {
        // Navigate to SignUp3Fragment
        SignUp3Fragment signUp3Fragment = new SignUp3Fragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, signUp3Fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openTerms() {
        // TODO: Implement terms page navigation
    }

    private void openPrivacyPolicy() {
        // TODO: Implement privacy policy page navigation
    }
}
