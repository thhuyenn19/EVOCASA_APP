package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mobile.evocasa.R;

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

        // Create Typeface from font in assets
        Typeface interMedium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Medium.otf");
        Typeface interBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Bold.otf");
        Typeface interSemiBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface interBlack = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Black.otf");
        Typeface interRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");

        // Apply the custom font to the views
        txtSignIn.setTypeface(interBold); // Inter-Bold
        txtDescription.setTypeface(interMedium); // Inter-Medium
        txtOrWith.setTypeface(interSemiBold); // Inter-Medium
        txtDontHave.setTypeface(interMedium); // Inter-Medium
        txtTerm.setTypeface(interSemiBold); // Inter-SemiBold
        txtPrivacy.setTypeface(interSemiBold); // Inter-SemiBold
        txtView.setTypeface(interMedium); // Inter-Medium
        txtBy.setTypeface(interRegular);

        btnContinueEmailPhoneSignIn.setTypeface(interMedium); // Inter-Medium
        btnContinueFacebook.setTypeface(interMedium); // Inter-Medium
        btnContinueGoogle.setTypeface(interMedium); // Inter-Medium
        btnSignUp.setTypeface(interBlack); // Inter-Black

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

        return rootView;
    }
}
