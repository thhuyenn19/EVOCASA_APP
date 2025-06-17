package com.mobile.evocasa.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;

import java.util.HashMap;
import java.util.Map;

public class SignUp3Fragment extends Fragment {

    private static final String ARG_VERIFICATION_ID = "verificationId";
    private static final String ARG_PHONE = "phone";

    private String verificationId;
    private String phoneNumber;

    private EditText edtCode;
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

        edtCode = view.findViewById(R.id.edtCode1); // hoặc gộp nhiều ô như trước
        btnVerify = view.findViewById(R.id.btnVerifyEmail);

        btnVerify.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                edtCode.setError("Nhập mã xác thực");
                return;
            }
            verifyCode(code);
        });

        return view;
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveAccount();
            } else {
                Toast.makeText(getContext(), "Mã không chính xác", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAccount() {
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> account = new HashMap<>();
        account.put("Contact", phoneNumber);
        account.put("ContactType", "Phone");
        account.put("CreatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("Account").document(uid).set(account)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    goToNextStep();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void goToNextStep() {
        SignUp4Fragment fragment = new SignUp4Fragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
