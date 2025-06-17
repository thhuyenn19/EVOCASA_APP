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

public class SignUp4Fragment extends Fragment {

    private EditText edtPassword, edtConfirmPassword;
    private ImageView btnBack, btnHelp, btnTogglePassword, btnToggleConfirmPassword;
    private ProgressBar progressBar;
    private LinearLayout passwordCriteriaLayout;
    private View checkLength, checkNumber, checkSpecialChar, checkUpperCase;
    private AppCompatButton btnCreateAccount;
    private TextView txtTitle, txtTerm, txtPrivacy, txtBy, txtPassword, txtTypePassword;

    private boolean hasLength = false, hasNumber = false, hasSpecialChar = false, hasUpperCase = false, passwordsMatch = false;
    private boolean isPasswordVisible = false, isConfirmPasswordVisible = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            if (getActivity() != null) getActivity().onBackPressed();
        });

        btnHelp.setOnClickListener(v -> Toast.makeText(getContext(), "Help clicked", Toast.LENGTH_SHORT).show());
        btnTogglePassword.setOnClickListener(v -> toggleVisibility(edtPassword, btnTogglePassword));
        btnToggleConfirmPassword.setOnClickListener(v -> toggleVisibility(edtConfirmPassword, btnToggleConfirmPassword));

        edtPassword.addTextChangedListener(new PasswordWatcher());
        edtConfirmPassword.addTextChangedListener(new PasswordWatcher());

        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            passwordCriteriaLayout.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            progressBar.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
        txtTerm.setOnClickListener(v -> openTerms());
        txtPrivacy.setOnClickListener(v -> openPrivacyPolicy());
    }

    private void toggleVisibility(EditText editText, ImageView toggleBtn) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleBtn.setImageResource(R.drawable.ic_eye_on);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleBtn.setImageResource(R.drawable.ic_eye_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private class PasswordWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            validatePassword(edtPassword.getText().toString());
            checkPasswordsMatch();
        }
        @Override public void afterTextChanged(Editable s) {}
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

    private void updateCheckIndicator(View view, boolean valid) {
        view.setSelected(valid);
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
        String p1 = edtPassword.getText().toString();
        String p2 = edtConfirmPassword.getText().toString();
        passwordsMatch = !p1.isEmpty() && p1.equals(p2);
        updateCreateAccountButton();
    }

    private void updateCreateAccountButton() {
        boolean enabled = hasLength && hasNumber && hasSpecialChar && hasUpperCase && passwordsMatch;
        btnCreateAccount.setEnabled(enabled);
        btnCreateAccount.setAlpha(enabled ? 1f : 0.5f);
    }

    private void handleCreateAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = edtPassword.getText().toString();
        String uid = currentUser.getUid();
        String phone = currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "";

        // Hash password with bcrypt
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        // Account document (for authentication only)
        Map<String, Object> account = new HashMap<>();
        account.put("Contact", phone);
        account.put("ContactType", "Phone");
        account.put("Name", ""); // Chưa nhập nên để trống
        account.put("Password", hashedPassword);


        // Customers document (for personal info)
        Map<String, Object> customer = new HashMap<>();
        customer.put("Name", "");
        customer.put("Phone", phone);
        customer.put("Mail", "");
        customer.put("DOB", null);
        customer.put("Address", null);
        customer.put("Gender", "");
        customer.put("Image", "");
        customer.put("CreatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        customer.put("Cart", new ArrayList<>());
        customer.put("Notification", new ArrayList<>());
        customer.put("Voucher", new ArrayList<>());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Save Account
        db.collection("Account").document(uid).set(account)
                .addOnSuccessListener(aVoid -> {
                    // Step 2: Save Customers
                    db.collection("Customers").document(uid).set(customer)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(getContext(), "SignUp Successfully!", Toast.LENGTH_SHORT).show();
                                navigateToSignUp5Fragment();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error Customers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error Account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }


    private void navigateToSignUp5Fragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SignUp5Fragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openTerms() {
        Toast.makeText(getContext(), "Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        Toast.makeText(getContext(), "Chính sách bảo mật", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
