package com.mobile.evocasa.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.*;
import com.mobile.evocasa.R;

public class SignUp3Fragment extends Fragment {

    private static final String ARG_VERIFICATION_ID = "verificationId";
    private static final String ARG_PHONE = "phone";

    private String verificationId;
    private String phoneNumber;

    private EditText[] codeInputs;
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
        setupInputBehaviour();

        btnVerify.setOnClickListener(v -> {
            String code = getCodeFromInputs();
            if (code.length() != 6) {
                showError("Vui lòng nhập đầy đủ 6 số mã OTP");
                return;
            }
            verifyCode(code);
        });

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
                    } else if (s.length() == 0 && index > 0) {
                        codeInputs[index - 1].requestFocus();
                    }
                }
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
