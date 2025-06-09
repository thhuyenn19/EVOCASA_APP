package com.mobile.evocasa.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

public class SignInFragment extends Fragment {

    private TextView txtSignIn, txtDescription, txtOrWith, txtDontHave, txtTerm, txtPrivacy, txtView, txtBy;
    private Button btnContinueEmailPhoneSignIn, btnContinueFacebook, btnContinueGoogle, btnSignUp;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Get references to the views
        txtSignIn = rootView.findViewById(R.id.txtSignIn);
        txtDescription = rootView.findViewById(R.id.txtDescription);
        txtOrWith = rootView.findViewById(R.id.txtOrWith);
        txtDontHave = rootView.findViewById(R.id.txtDontHave);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtView = rootView.findViewById(R.id.txtView);
        txtBy = rootView.findViewById(R.id.txtBy);

        btnContinueEmailPhoneSignIn = rootView.findViewById(R.id.btnContinueEmailPhoneSignIn);
        btnContinueFacebook = rootView.findViewById(R.id.btnContinueFacebook);
        btnContinueGoogle = rootView.findViewById(R.id.btnContinueGoogle);
        btnSignUp = rootView.findViewById(R.id.btnSignUp);

        // Apply fonts using FontUtils
        txtSignIn.setTypeface(FontUtils.getBold(getContext()));
        txtDescription.setTypeface(FontUtils.getMediumitalic(getContext()));
        txtOrWith.setTypeface(FontUtils.getSemiBold(getContext()));
        txtDontHave.setTypeface(FontUtils.getMedium(getContext()));
        txtTerm.setTypeface(FontUtils.getSemiBold(getContext()));
        txtPrivacy.setTypeface(FontUtils.getSemiBold(getContext()));
        txtView.setTypeface(FontUtils.getMedium(getContext()));
        txtBy.setTypeface(FontUtils.getRegular(getContext()));

        btnContinueEmailPhoneSignIn.setTypeface(FontUtils.getMedium(getContext()));
        btnContinueFacebook.setTypeface(FontUtils.getMedium(getContext()));
        btnContinueGoogle.setTypeface(FontUtils.getMedium(getContext()));
        btnSignUp.setTypeface(FontUtils.getBlack(getContext()));

        // Set onClickListener for btnContinueEmailPhoneSignIn to navigate to SignIn1Fragment
        btnContinueEmailPhoneSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the new fragment instance
                SignIn1Fragment signIn1Fragment = new SignIn1Fragment();

                // Begin the fragment transaction
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // Replace the current fragment with SignIn1Fragment
                transaction.replace(R.id.fragment_container, signIn1Fragment);
                // Optionally, add the transaction to the back stack
                transaction.addToBackStack(null);
                // Commit the transaction to perform the fragment transaction
                transaction.commit();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một instance của SignUp1Fragment
                SignUp1Fragment signUp1Fragment = new SignUp1Fragment();

                // Bắt đầu một giao dịch Fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // Thay thế fragment hiện tại bằng SignUp1Fragment
                transaction.replace(R.id.fragment_container, signUp1Fragment);
                // Thêm giao dịch vào back stack (nếu muốn quay lại trước đó)
                transaction.addToBackStack(null);
                // Cam kết giao dịch fragment
                transaction.commit();
            }
        });

        return rootView;
    }
}