package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp2Fragment extends Fragment {

    // UI Components
    private ImageView btnBack, btnHelp;
    private TextView txtTitle, txtEmailPhoneLabel, txtTerms, txtPrivacyPolicy;
    private EditText edtEmailPhone;
    private AppCompatButton btnContinue;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUp2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUp2Fragment.
     */
    // TODO: Rename and change types and number of parameters
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
        txtTerms = view.findViewById(R.id.txtTerms);
        txtPrivacyPolicy = view.findViewById(R.id.txtPrivacyPolicy);
    }

    private void setCustomFonts() {
        try {
            // Load custom font from assets folder
            // Thay đổi tên font theo font bạn có trong assets/fonts/
            Typeface customFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Regular.otf");
            Typeface customFontBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Regular.otf");

            if (customFont != null && customFontBold != null) {
                txtTitle.setTypeface(customFontBold);
                txtEmailPhoneLabel.setTypeface(customFont);
                edtEmailPhone.setTypeface(customFont);
                btnContinue.setTypeface(customFontBold);
                txtTerms.setTypeface(customFontBold);
                txtPrivacyPolicy.setTypeface(customFontBold);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default fonts if custom fonts are not available
            setDefaultFonts();
        }
    }

    private void setDefaultFonts() {
        // Fallback fonts
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtEmailPhoneLabel.setTypeface(null, Typeface.NORMAL);
        edtEmailPhone.setTypeface(null, Typeface.NORMAL);
        btnContinue.setTypeface(null, Typeface.BOLD);
        txtTerms.setTypeface(null, Typeface.BOLD);
        txtPrivacyPolicy.setTypeface(null, Typeface.BOLD);
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
                // TODO: Proceed to next step
                proceedToNextStep(emailPhone);
            }
        });

        // Terms click
        txtTerms.setOnClickListener(v -> {
            // TODO: Open Terms page
            openTerms();
        });

        // Privacy Policy click
        txtPrivacyPolicy.setOnClickListener(v -> {
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
        // TODO: Implement navigation to next fragment or API call
        // Example: Navigate to SignUp3Fragment or verify email/phone
    }

    private void openTerms() {
        // TODO: Implement terms page navigation
    }

    private void openPrivacyPolicy() {
        // TODO: Implement privacy policy page navigation
    }
}