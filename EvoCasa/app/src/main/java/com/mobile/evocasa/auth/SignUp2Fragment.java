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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mobile.evocasa.R;

import java.util.concurrent.TimeUnit;

public class SignUp2Fragment extends Fragment {

    private EditText edtPhone;
    private AppCompatButton btnContinue;
    private FirebaseAuth mAuth;

    public SignUp2Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up2, container, false);

        edtPhone = view.findViewById(R.id.edtEmailPhone);  // giữ ID cũ để không cần sửa layout
        btnContinue = view.findViewById(R.id.btnContinue);
        mAuth = FirebaseAuth.getInstance();

        btnContinue.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();

            if (!isValidVietnamPhone(phone)) {
                edtPhone.setError("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)");
                return;
            }

            String formattedPhone = "+84" + phone.substring(1); // Chuyển 090xxxxxxx → +8490xxxxxxx
            sendOtp(formattedPhone);
        });

        return view;
    }

    private boolean isValidVietnamPhone(String phone) {
        return phone.length() == 10 && phone.startsWith("0") && phone.matches("\\d{10}");
    }

    private void sendOtp(String fullPhoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential credential) {
            // Auto verification nếu có thể
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getContext(), "Lỗi gửi OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            // Gửi sang SignUp3Fragment
            SignUp3Fragment fragment = SignUp3Fragment.newInstance(verificationId, edtPhone.getText().toString());
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    };
}
