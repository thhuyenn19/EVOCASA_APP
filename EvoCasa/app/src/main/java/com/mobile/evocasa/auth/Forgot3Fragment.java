package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.*;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;

import java.util.*;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Forgot3Fragment extends Fragment {

    private EditText edtPassword, edtConfirmPassword;
    private ImageView btnBack, btnHelp, btnTogglePassword, btnToggleConfirmPassword;
    private ProgressBar progressBar;
    private LinearLayout passwordCriteriaLayout;
    private View checkLength, checkNumber, checkSpecialChar, checkUpperCase;
    private AppCompatButton btnResetPassword;
    private TextView txtTitle, txtTerm, txtPrivacy, txtBy, txtPassword, txtTypePassword;

    private boolean hasLength = false;
    private boolean hasNumber = false;
    private boolean hasSpecialChar = false;
    private boolean hasUpperCase = false;
    private boolean passwordsMatch = false;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private Typeface fontRegular, fontBold, fontMedium, fontSemiBold;

    public Forgot3Fragment() {}

    public static Forgot3Fragment newInstance() {
        return new Forgot3Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCustomFonts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupFonts();
        setupListeners();
        updateResetPasswordButton();
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
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
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
        txtTitle.setTypeface(fontBold);
        edtPassword.setTypeface(fontRegular);
        edtConfirmPassword.setTypeface(fontRegular);
        btnResetPassword.setTypeface(fontSemiBold);
        txtTerm.setTypeface(fontSemiBold);
        txtPrivacy.setTypeface(fontSemiBold);
        txtBy.setTypeface(fontRegular);
        txtPassword.setTypeface(fontMedium);
        txtTypePassword.setTypeface(fontMedium);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnHelp.setOnClickListener(v -> Toast.makeText(getContext(), "Help clicked", Toast.LENGTH_SHORT).show());
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            progressBar.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            passwordCriteriaLayout.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

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

        btnResetPassword.setOnClickListener(v -> handleResetPassword());
        txtTerm.setOnClickListener(v -> openTerms());
        txtPrivacy.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void togglePasswordVisibility() {
        edtPassword.setTransformationMethod(isPasswordVisible ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
        btnTogglePassword.setImageResource(isPasswordVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye_on);
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        edtConfirmPassword.setTransformationMethod(isConfirmPasswordVisible ? PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
        btnToggleConfirmPassword.setImageResource(isConfirmPasswordVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye_on);
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
    }

    private void validatePassword(String password) {
        hasLength = password.length() >= 8;
        hasNumber = password.matches(".*\\d.*");
        hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        hasUpperCase = password.matches(".*[A-Z].*");

        updateCheckIndicator(checkLength, hasLength);
        updateCheckIndicator(checkNumber, hasNumber);
        updateCheckIndicator(checkSpecialChar, hasSpecialChar);
        updateCheckIndicator(checkUpperCase, hasUpperCase);
        updateProgressBar();
        updateResetPasswordButton();
    }

    private void updateCheckIndicator(View view, boolean isValid) {
        view.setSelected(isValid);
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
        String confirm = edtConfirmPassword.getText().toString();
        passwordsMatch = !password.isEmpty() && password.equals(confirm);
        updateResetPasswordButton();
    }

    private void updateResetPasswordButton() {
        boolean enabled = hasLength && hasNumber && hasSpecialChar && hasUpperCase && passwordsMatch;
        btnResetPassword.setEnabled(enabled);
        btnResetPassword.setAlpha(enabled ? 1f : 0.5f);
    }

    private void handleResetPassword() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = edtPassword.getText().toString();
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Account").document(currentUser.getUid())
                .update("Password", hashedPassword)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Password reset successfully", Toast.LENGTH_SHORT).show();
                    navigateToForgot4Fragment();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void navigateToForgot4Fragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new Forgot4Fragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openTerms() {
        Toast.makeText(getContext(), "Opening Terms", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        Toast.makeText(getContext(), "Opening Privacy Policy", Toast.LENGTH_SHORT).show();
    }
}
