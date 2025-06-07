package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.R;

public class SignUp4Fragment extends Fragment {

    private EditText edtPassword, edtConfirmPassword;
    private ImageView btnBack, btnHelp, btnTogglePassword, btnToggleConfirmPassword;
    private ProgressBar progressBar;
    private LinearLayout passwordCriteriaLayout;
    private View checkLength, checkNumber, checkSpecialChar, checkUpperCase;
    private AppCompatButton btnCreateAccount;
    private TextView txtTitle, txtTerm, txtPrivacy, txtBy, txtPassword, txtTypePassword;

    private boolean hasLength = false;
    private boolean hasNumber = false;
    private boolean hasSpecialChar = false;
    private boolean hasUpperCase = false;
    private boolean passwordsMatch = false;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private Typeface fontRegular, fontBold, fontMedium, fontSemiBold;

    public SignUp4Fragment() {}

    public static SignUp4Fragment newInstance() {
        return new SignUp4Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCustomFonts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFonts();
        setupListeners();
        updateCreateAccountButton();
    }

    private void loadCustomFonts() {
        try {
            fontRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");
            fontBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Bold.otf");
            fontMedium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Medium.otf");
            fontSemiBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        } catch (Exception e) {
            fontRegular = Typeface.DEFAULT;
            fontBold = Typeface.DEFAULT_BOLD;
            fontMedium = Typeface.DEFAULT;
            fontSemiBold = Typeface.DEFAULT_BOLD;
        }
    }

    private void initViews(View view) {
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnBack = view.findViewById(R.id.btnBack);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = view.findViewById(R.id.btnToggleConfirmPassword);
        btnCreateAccount = view.findViewById(R.id.btnResetPassword);
        progressBar = view.findViewById(R.id.progressBar);
        passwordCriteriaLayout = view.findViewById(R.id.passwordCriteriaLayout);
        checkLength = view.findViewById(R.id.checkLength);
        checkNumber = view.findViewById(R.id.checkNumber);
        checkSpecialChar = view.findViewById(R.id.checkSpecialChar);
        checkUpperCase = view.findViewById(R.id.checkUpperCase);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtTerm = view.findViewById(R.id.txtTerm);
        txtPrivacy = view.findViewById(R.id.txtPrivacy);
        txtBy = view.findViewById(R.id.txtBy);
        txtPassword = view.findViewById(R.id.txtPassword);
        txtTypePassword = view.findViewById(R.id.txtTypePassword);
    }

    private void setupFonts() {
        if (fontRegular != null && fontBold != null && fontMedium != null && fontSemiBold != null) {
            txtTitle.setTypeface(fontBold);
            edtPassword.setTypeface(fontRegular);
            edtConfirmPassword.setTypeface(fontRegular);
            btnCreateAccount.setTypeface(fontSemiBold);
            txtTerm.setTypeface(fontSemiBold);
            txtPrivacy.setTypeface(fontSemiBold);
            txtBy.setTypeface(fontRegular);
            txtPassword.setTypeface(fontMedium);
            txtTypePassword.setTypeface(fontMedium);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnHelp.setOnClickListener(v -> Toast.makeText(getContext(), "Help clicked", Toast.LENGTH_SHORT).show());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                checkPasswordsMatch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordsMatch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Add OnFocusChangeListener to show/hide password criteria
        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Show progress bar and password criteria when focused
                progressBar.setVisibility(View.VISIBLE);
                passwordCriteriaLayout.setVisibility(View.VISIBLE);
            } else {
                // Hide progress bar and password criteria when focus is lost
                progressBar.setVisibility(View.GONE);
                passwordCriteriaLayout.setVisibility(View.GONE);
            }
        });

        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());

        txtTerm.setOnClickListener(v -> openTerms());
        txtPrivacy.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
    }

    private void validatePassword(String password) {
        hasLength = password.length() >= 8;
        updateCheckIndicator(checkLength, hasLength);

        hasNumber = password.matches(".*\\d.*");
        updateCheckIndicator(checkNumber, hasNumber);

        hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        updateCheckIndicator(checkSpecialChar, hasSpecialChar);

        hasUpperCase = password.matches(".*[A-Z].*");
        updateCheckIndicator(checkUpperCase, hasUpperCase);

        updateProgressBar();
        updateCreateAccountButton();
    }

    private void updateCheckIndicator(View indicator, boolean isValid) {
        indicator.setSelected(isValid);
    }

    private void updateProgressBar() {
        int progress = 0;
        if (hasLength) progress++;
        if (hasNumber) progress++;
        if (hasSpecialChar) progress++;
        if (hasUpperCase) progress++;

        progressBar.setProgress(progress);
    }

    private void checkPasswordsMatch() {
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        passwordsMatch = !password.isEmpty() && password.equals(confirmPassword);
        updateCreateAccountButton();
    }

    private void updateCreateAccountButton() {
        boolean allValid = hasLength && hasNumber && hasSpecialChar && hasUpperCase && passwordsMatch;
        btnCreateAccount.setEnabled(allValid);

        if (allValid) {
            btnCreateAccount.setAlpha(1.0f);
            btnCreateAccount.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.green, null));
        } else {
            btnCreateAccount.setAlpha(0.5f);
            btnCreateAccount.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.gray_light, null));
        }
    }

    private void handleCreateAccount() {
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(hasLength && hasNumber && hasSpecialChar && hasUpperCase)) {
            Toast.makeText(getContext(), "Password does not meet all requirements", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to SignUp5Fragment
        navigateToSignUp5Fragment();
    }

    private void navigateToSignUp5Fragment() {
        // Navigate to the next fragment (SignUp5Fragment)
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SignUp5Fragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openTerms() {
        // TODO: Implement opening the Terms and Conditions page
        Toast.makeText(getContext(), "Opening Terms", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        // TODO: Implement opening the Privacy Policy page
        Toast.makeText(getContext(), "Opening Privacy Policy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        edtPassword = null;
        edtConfirmPassword = null;
        btnBack = null;
        btnHelp = null;
        btnTogglePassword = null;
        btnToggleConfirmPassword = null;
        progressBar = null;
        passwordCriteriaLayout = null;
        checkLength = null;
        checkNumber = null;
        checkSpecialChar = null;
        checkUpperCase = null;
        btnCreateAccount = null;
        txtTitle = null;
        txtTerm = null;
        txtPrivacy = null;
        txtBy = null;
        txtPassword = null;
        txtTypePassword = null;
    }
}
