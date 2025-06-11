package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.mobile.evocasa.R;

public class SignUp1Fragment extends Fragment {

    private TextView txtCreate, txtDescription, txtOrWith, txtDontHave, txtTerm, txtPrivacy, txtBy;
    private AppCompatButton btnContinueEmailPhone, btnContinueFacebook, btnContinueGoogle, btnSignIn;

    public SignUp1Fragment() {
        // Required empty public constructor
    }

    public static SignUp1Fragment newInstance(String param1, String param2) {
        SignUp1Fragment fragment = new SignUp1Fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve parameters if necessary
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up1, container, false);

        // Load custom fonts from assets/fonts
        Typeface bold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Bold.otf");
        Typeface medium = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Medium.otf");
        Typeface semiBold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface black = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Black.otf");
        Typeface italic = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Italic.otf");
        Typeface regular = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Regular.otf");

        // Get references to the views
        txtCreate = view.findViewById(R.id.txtCreate);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtOrWith = view.findViewById(R.id.txtOrWith);
        txtDontHave = view.findViewById(R.id.txtDontHave);
        txtTerm = view.findViewById(R.id.txtTerm);
        txtPrivacy = view.findViewById(R.id.txtPrivacy);
        txtBy = view.findViewById(R.id.txtBy);

        btnContinueEmailPhone = view.findViewById(R.id.btnContinueEmailPhone);
        btnContinueFacebook = view.findViewById(R.id.btnContinueFacebook);
        btnContinueGoogle = view.findViewById(R.id.btnContinueGoogle);
        btnSignIn = view.findViewById(R.id.btnSignIn);

        // Set typefaces for TextViews and Buttons
        txtCreate.setTypeface(bold); // Title - Bold
        txtDescription.setTypeface(medium); // Description - Medium Italic
        txtDescription.setTypeface(italic); // Applying italic style
        txtOrWith.setTypeface(semiBold); // "Or With" - SemiBold
        txtDontHave.setTypeface(medium); // "Already have an account?" - SemiBold
        txtTerm.setTypeface(semiBold); // Terms - SemiBold
        txtPrivacy.setTypeface(semiBold); // Privacy - SemiBold
        txtBy.setTypeface(regular);
        btnContinueEmailPhone.setTypeface(medium); // Continue with Email/Phone - Medium
        btnContinueFacebook.setTypeface(medium); // Continue with Facebook - Medium
        btnContinueGoogle.setTypeface(medium); // Continue with Google - Medium
        btnSignIn.setTypeface(black); // SignUp button - Black

        // Set onClickListener for btnContinueEmailPhone to navigate to SignUp2Fragment
        btnContinueEmailPhone.setOnClickListener(v -> {
            // Create the new fragment instance
            SignUp2Fragment signUp2Fragment = new SignUp2Fragment();

            // Begin the fragment transaction
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // Replace the current fragment with SignUp2Fragment
            transaction.replace(R.id.fragment_container, signUp2Fragment);
            // Optionally, add the transaction to the back stack
            transaction.addToBackStack(null);
            // Commit the transaction to perform the fragment transaction
            transaction.commit();
        });
        btnSignIn.setOnClickListener(v -> {
            // Tạo một instance của SignInFragment
            SignInFragment signInFragment = new SignInFragment();

            // Bắt đầu một giao dịch Fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // Thay thế fragment hiện tại bằng SignInFragment
            transaction.replace(R.id.fragment_container, signInFragment);
            // Thêm giao dịch vào back stack (nếu muốn quay lại trước đó)
            transaction.addToBackStack(null);
            // Cam kết giao dịch fragment
            transaction.commit();
        });



        return view;
    }
}
