package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;  // Thêm import này để sử dụng FragmentTransaction

import com.mobile.evocasa.R;

public class SignUp3Fragment extends Fragment {

    private static final String TAG = "SignUp3Fragment";

    // UI Components
    private ImageView btnBack, btnHelp;
    private TextView txtTitle, txtDescription, txtCodeLabel, btnSendAgain, txtTerm, txtPrivacy, txtDidReceive, txtBy;
    private EditText edtCode1, edtCode2, edtCode3, edtCode4, edtCode5;
    private AppCompatButton btnVerifyEmail;
    private EditText[] codeInputs;

    private Typeface regularFont;
    private Typeface boldFont;
    private Typeface mediumFont;
    private Typeface mediumitalicFont;
    private Typeface semiBoldFont;

    public SignUp3Fragment() {
        // Required empty public constructor
    }

    public static SignUp3Fragment newInstance(String param1, String param2) {
        SignUp3Fragment fragment = new SignUp3Fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCustomFonts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up3, container, false);

        initViews(view);
        setCustomFonts();
        setClickListeners();
        setupCodeInputs();

        return view;
    }

    private void loadCustomFonts() {
        try {
            if (getContext() != null) {
                regularFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Regular.otf");
                boldFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Bold.otf");
                mediumFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-Medium.otf");
                semiBoldFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-SemiBold.otf");
                mediumitalicFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Inter-MediumItalic.otf");
            }
        } catch (Exception e) {
            setDefaultTypefaceFallback();
        }
    }

    private void setDefaultTypefaceFallback() {
        regularFont = Typeface.DEFAULT;
        boldFont = Typeface.DEFAULT_BOLD;
        mediumFont = Typeface.DEFAULT;
        semiBoldFont = Typeface.DEFAULT_BOLD;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnHelp = view.findViewById(R.id.btnHelp);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtDescription = view.findViewById(R.id.txtCategoryShop);
        txtCodeLabel = view.findViewById(R.id.txtCodeLabel);
        edtCode1 = view.findViewById(R.id.edtCode1);
        edtCode2 = view.findViewById(R.id.edtCode2);
        edtCode3 = view.findViewById(R.id.edtCode3);
        edtCode4 = view.findViewById(R.id.edtCode4);
        edtCode5 = view.findViewById(R.id.edtCode5);
        btnSendAgain = view.findViewById(R.id.btnSendAgain);
        btnVerifyEmail = view.findViewById(R.id.btnVerifyEmail);
        txtTerm = view.findViewById(R.id.txtTerm);
        txtPrivacy = view.findViewById(R.id.txtPrivacy);
        txtDidReceive = view.findViewById(R.id.txtDidReceive);
        txtBy = view.findViewById(R.id.txtBy);

        codeInputs = new EditText[]{edtCode1, edtCode2, edtCode3, edtCode4, edtCode5};
    }

    private void setCustomFonts() {
        txtTitle.setTypeface(boldFont);
        txtDescription.setTypeface(mediumitalicFont);
        txtCodeLabel.setTypeface(mediumFont);
        btnSendAgain.setTypeface(semiBoldFont);
        txtTerm.setTypeface(semiBoldFont);
        txtPrivacy.setTypeface(semiBoldFont);
        txtDidReceive.setTypeface(regularFont);
        txtBy.setTypeface(regularFont);
        btnVerifyEmail.setTypeface(semiBoldFont);

        for (EditText input : codeInputs) {
            input.setTypeface(boldFont);
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> getActivity().onBackPressed());

        btnHelp.setOnClickListener(v -> openHelp());

        btnSendAgain.setOnClickListener(v -> resendVerificationCode());

        btnVerifyEmail.setOnClickListener(v -> {
            String code = getEnteredCode();
            if (validateCode(code)) {
                verifyCode(code);
                // After verification, navigate to SignUp4Fragment
                navigateToSignUp4Fragment();
            }
        });

        txtTerm.setOnClickListener(v -> openTerms());

        txtPrivacy.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void setupCodeInputs() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;
            if (codeInputs[i] != null) {
                codeInputs[i].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 1 && index < codeInputs.length - 1) {
                            if (codeInputs[index + 1] != null) {
                                codeInputs[index + 1].requestFocus();
                            }
                        }
                        if (index == codeInputs.length - 1 && s.length() == 1) {
                            String code = getEnteredCode();
                            if (code.length() == 5) {
                                // Optional: Auto verify
                                // verifyCode(code);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                codeInputs[i].setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (codeInputs[index].getText().toString().isEmpty() && index > 0) {
                            if (codeInputs[index - 1] != null) {
                                codeInputs[index - 1].requestFocus();
                                codeInputs[index - 1].getText().clear();
                            }
                        }
                    }
                    return false;
                });
            }
        }
    }

    private String getEnteredCode() {
        StringBuilder code = new StringBuilder();
        for (EditText input : codeInputs) {
            if (input != null) {
                code.append(input.getText().toString());
            }
        }
        return code.toString();
    }

    private boolean validateCode(String code) {
        if (code.length() != 5) {
            showError("Please enter a valid 5-digit code");
            return false;
        }
        if (!code.matches("\\d{5}")) {
            showError("Code must be numeric");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Log.w(TAG, "Validation error: " + message);
        if (codeInputs != null) {
            for (EditText input : codeInputs) {
                if (input != null && input.getText().toString().isEmpty()) {
                    input.requestFocus();
                    break;
                }
            }
        }
    }

    private void resendVerificationCode() {
        Log.d(TAG, "Resending verification code");
        for (EditText input : codeInputs) {
            if (input != null) {
                input.getText().clear();
            }
        }
        if (edtCode1 != null) {
            edtCode1.requestFocus();
        }
    }

    private void verifyCode(String code) {
        Log.d(TAG, "Verifying code: " + code);
        // Implement the logic to verify the code here
    }

    private void navigateToSignUp4Fragment() {
        // Create new instance of SignUp4Fragment
        SignUp4Fragment signUp4Fragment = new SignUp4Fragment();

        // Begin Fragment transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, signUp4Fragment); // Make sure you have the correct container ID
        transaction.addToBackStack(null); // Optionally add to back stack
        transaction.commit();
    }

    private void openHelp() {
        Log.d(TAG, "Opening help");
    }

    private void openTerms() {
        Log.d(TAG, "Opening terms");
    }

    private void openPrivacyPolicy() {
        Log.d(TAG, "Opening privacy policy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        codeInputs = null;
    }
}
