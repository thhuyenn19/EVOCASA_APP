package com.mobile.evocasa.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.mobile.utils.UserSessionManager;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.NarBarActivity;
import com.mobile.evocasa.R;

import java.util.Objects;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignIn1Fragment extends Fragment {

    private TextView txtTitle, txtEmailPhoneLabel, txtPassword, txtTerm, txtPrivacy, txtForgotPassword, txtBy;
    private EditText edtEmailPhone, edtPassword;
    private ImageView btnTogglePassword;
    private AppCompatButton btnContinue;
    private boolean isPasswordVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in1, container, false);

        txtTitle = rootView.findViewById(R.id.txtTitle);
        txtEmailPhoneLabel = rootView.findViewById(R.id.txtEmailPhoneLabel);
        txtPassword = rootView.findViewById(R.id.txtPassword);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtForgotPassword = rootView.findViewById(R.id.btnForgotPassword);
        txtBy = rootView.findViewById(R.id.txtBy);

        edtEmailPhone = rootView.findViewById(R.id.edtEmailPhone);
        edtPassword = rootView.findViewById(R.id.edtPassword);
        edtEmailPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        btnTogglePassword = rootView.findViewById(R.id.btnTogglePassword);
        btnContinue = rootView.findViewById(R.id.btnContinue);

        Typeface regularFont = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/Inter-Regular.otf");
        Typeface semiBoldFont = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface boldFont = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/Inter-Bold.otf");
        Typeface mediumFont = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/Inter-Medium.otf");

        txtTitle.setTypeface(boldFont);
        txtEmailPhoneLabel.setTypeface(mediumFont);
        txtPassword.setTypeface(mediumFont);
        txtTerm.setTypeface(semiBoldFont);
        txtPrivacy.setTypeface(semiBoldFont);
        txtForgotPassword.setTypeface(semiBoldFont);
        txtBy.setTypeface(regularFont);
        edtEmailPhone.setTypeface(regularFont);
        edtPassword.setTypeface(regularFont);
        btnContinue.setTypeface(semiBoldFont);

        txtForgotPassword.setPaintFlags(txtForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtForgotPassword.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new Forgot1Fragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_eye_on);
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.getText().length());
        });

        btnContinue.setOnClickListener(v -> {
            String phone = edtEmailPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedPhone = phone.startsWith("0") ? "+84" + phone.substring(1) : phone;

            FirebaseFirestore.getInstance().collection("Account")
                    .whereEqualTo("Contact", formattedPhone)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            Toast.makeText(getContext(), "Account not found", Toast.LENGTH_SHORT).show();
                        } else {
                            DocumentSnapshot doc = query.getDocuments().get(0);
                            String storedHash = Objects.requireNonNull(doc.getString("Password"));
                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);

                            if (result.verified) {
                                // Lưu UID bằng UserSessionManager
                                String uid = doc.getId();
                                UserSessionManager sessionManager = new UserSessionManager(requireContext());
                                sessionManager.saveUid(uid);

                                // Mở màn hình chính
                                Intent intent = new Intent(getActivity(), NarBarActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            } else {
                                Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show());
        });
        return rootView;
    }
}
