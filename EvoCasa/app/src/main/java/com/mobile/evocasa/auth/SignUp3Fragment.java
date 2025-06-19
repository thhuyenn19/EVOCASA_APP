package com.mobile.evocasa.auth;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.mobile.evocasa.R;

import java.util.concurrent.TimeUnit;

public class SignUp3Fragment extends Fragment {

    private static final String ARG_VERIFICATION_ID = "verificationId";
    private static final String ARG_PHONE = "phone";

    private String verificationId;
    private String phoneNumber;

    private EditText[] codeInputs;
    private Button btnSendAgain;
    private ImageView btnBack;
    private AppCompatButton btnVerify;
    private FirebaseAuth mAuth;

    public SignUp3Fragment() {}

    public static SignUp3Fragment newInstance(String verificationId, String phoneNumber) {
        SignUp3Fragment fragment = new SignUp3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_VERIFICATION_ID, verificationId);
        args.putString(ARG_PHONE, phoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            verificationId = getArguments().getString(ARG_VERIFICATION_ID);
            phoneNumber = getArguments().getString(ARG_PHONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up3, container, false);

        codeInputs = new EditText[]{
                view.findViewById(R.id.edtCode1),
                view.findViewById(R.id.edtCode2),
                view.findViewById(R.id.edtCode3),
                view.findViewById(R.id.edtCode4),
                view.findViewById(R.id.edtCode5),
                view.findViewById(R.id.edtCode6)
        };

        btnVerify = view.findViewById(R.id.btnVerifyEmail);
        btnSendAgain = view.findViewById(R.id.btnSendAgain);
        btnBack = view.findViewById(R.id.btnBack);

        setupInputBehaviour();

        btnVerify.setOnClickListener(v -> {
            String code = getCodeFromInputs();
            if (code.length() != 6) {
                showError("Vui lòng nhập đầy đủ 6 số mã OTP");
                return;
            }
            verifyCode(code);
        });

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnSendAgain.setOnClickListener(v -> resendOTP());

        return view;
    }

    private void setupInputBehaviour() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;

            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < codeInputs.length - 1) {
                        codeInputs[index + 1].requestFocus();
                    }
                }
            });

            codeInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_DEL &&
                        codeInputs[index].getText().toString().isEmpty() &&
                        index > 0) {
                    codeInputs[index - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
    }

    private String getCodeFromInputs() {
        StringBuilder codeBuilder = new StringBuilder();
        for (EditText edt : codeInputs) {
            codeBuilder.append(edt.getText().toString().trim());
        }
        return codeBuilder.toString();
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                clearError();
                goToNextStep();
            } else {
                showError("Mã không chính xác");
            }
        });
    }

    private void goToNextStep() {
        SignUp4Fragment fragment = new SignUp4Fragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void resendOTP() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto verification if available
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getContext(), "Không thể gửi lại mã: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        Toast.makeText(getContext(), "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        for (EditText edt : codeInputs) {
            edt.setBackgroundResource(R.drawable.code_input_error_background);
        }
    }

    private void clearError() {
        for (EditText edt : codeInputs) {
            edt.setBackgroundResource(R.drawable.code_input_background);
        }
    }
}
