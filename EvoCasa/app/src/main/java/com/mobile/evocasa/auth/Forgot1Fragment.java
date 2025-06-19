package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.mobile.evocasa.R;

import java.util.concurrent.TimeUnit;

public class Forgot1Fragment extends Fragment {

    private TextView txtTitle, txtEmailPhoneLabel, txtTerm, txtPrivacy, txtBy;
    private EditText edtEmailPhone;
    private Button btnContinue;
    private ImageView btnBack;
    private FirebaseAuth mAuth;

    public Forgot1Fragment() {}

    public static Forgot1Fragment newInstance(String param1, String param2) {
        Forgot1Fragment fragment = new Forgot1Fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot1, container, false);

        // Ánh xạ view
        txtTitle = rootView.findViewById(R.id.txtTitle);
        txtEmailPhoneLabel = rootView.findViewById(R.id.txtEmailPhoneLabel);
        edtEmailPhone = rootView.findViewById(R.id.edtEmailPhone);
        btnContinue = rootView.findViewById(R.id.btnContinue);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtBy = rootView.findViewById(R.id.txtBy);
        btnBack = rootView.findViewById(R.id.btnBack); // 🔹 gán nút back

        mAuth = FirebaseAuth.getInstance();
        edtEmailPhone.setInputType(InputType.TYPE_CLASS_PHONE);

        // Set font
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");
        Typeface semiBoldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface boldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Bold.otf");
        Typeface mediumFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Medium.otf");

        txtTitle.setTypeface(boldFont);
        txtEmailPhoneLabel.setTypeface(mediumFont);
        edtEmailPhone.setTypeface(regularFont);
        btnContinue.setTypeface(semiBoldFont);
        txtBy.setTypeface(regularFont);
        txtTerm.setTypeface(semiBoldFont);
        txtPrivacy.setTypeface(semiBoldFont);


        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Xử lý tiếp tục
        btnContinue.setOnClickListener(v -> {
            btnContinue.setEnabled(false);
            String phone = edtEmailPhone.getText().toString().trim();

            if (!isValidVietnamPhone(phone)) {
                edtEmailPhone.setError("Invalid phone number (10 digits, starts with 0)");
                btnContinue.setEnabled(true);
                return;
            }

            String formattedPhone = "+84" + phone.substring(1);
            checkIfPhoneExists(formattedPhone);
        });

        return rootView;
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
                            sendOtp(fullPhoneNumber);
                        } else {
                            checkInAccountCollection(fullPhoneNumber);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to check phone", Toast.LENGTH_SHORT).show();
                        btnContinue.setEnabled(true);
                    }
                });
    }

    private void checkInAccountCollection(String fullPhoneNumber) {
        FirebaseFirestore.getInstance()
                .collection("Account")
                .whereEqualTo("Phone", fullPhoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            sendOtp(fullPhoneNumber);
                        } else {
                            edtEmailPhone.setError("Phone number not found");
                            btnContinue.setEnabled(true);
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
                public void onVerificationCompleted(PhoneAuthCredential credential) {
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

                    Forgot2Fragment fragment = Forgot2Fragment.newInstance(verificationId, edtEmailPhone.getText().toString());
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    btnContinue.setEnabled(true);
                }
            };
}
