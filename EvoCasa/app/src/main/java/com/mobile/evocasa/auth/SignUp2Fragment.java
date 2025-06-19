package com.mobile.evocasa.auth;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.evocasa.R;

import java.util.concurrent.TimeUnit;

public class SignUp2Fragment extends Fragment {

    private EditText edtPhone;
    private AppCompatButton btnContinue;
    private FirebaseAuth mAuth;
    private ImageView btnBack;

    public SignUp2Fragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up2, container, false);

        edtPhone = view.findViewById(R.id.edtEmailPhone);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnBack = view.findViewById(R.id.btnBack);
        mAuth = FirebaseAuth.getInstance();

        // Cài đặt input type để hiện bàn phím số
        edtPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        btnContinue.setOnClickListener(v -> {
            btnContinue.setEnabled(false);

            String phone = edtPhone.getText().toString().trim();
            if (!isValidVietnamPhone(phone)) {
                edtPhone.setError("Invalid phone number (10 digits, starts with 0)");
                btnContinue.setEnabled(true);
                return;
            }

            String formattedPhone = "+84" + phone.substring(1);
            checkIfPhoneExists(formattedPhone);
        });

        return view;
    }

    private boolean isValidVietnamPhone(String phone) {
        return phone.length() == 10 && phone.startsWith("0") && phone.matches("\\d{10}");
    }

    private void checkIfPhoneExists(String fullPhoneNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Customers")
                .whereEqualTo("Phone", fullPhoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            edtPhone.setError("Phone number already exists");
                            btnContinue.setEnabled(true);
                        } else {
                            sendOtp(fullPhoneNumber);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to check phone", Toast.LENGTH_SHORT).show();
                        btnContinue.setEnabled(true);
                    }
                });
    }

    private void sendOtp(String fullPhoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential credential) {
                    Toast.makeText(getContext(), "Auto verification", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(getContext(), "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnContinue.setEnabled(true);
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Toast.makeText(getContext(), "Verification code sent", Toast.LENGTH_SHORT).show();
                    SignUp3Fragment fragment = SignUp3Fragment.newInstance(verificationId, edtPhone.getText().toString());
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    btnContinue.setEnabled(true);
                }
            };
}
