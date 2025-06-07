package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Forgot1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Forgot1Fragment extends Fragment {

    private TextView txtTitle, txtEmailPhoneLabel, txtTerm, txtPrivacy, txtBy;
    private EditText edtEmailPhone;
    private Button btnContinue;

    public Forgot1Fragment() {
        // Required empty public constructor
    }

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
        if (getArguments() != null) {
            // Retrieve parameters if necessary
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forgot1, container, false);

        // Get references to the views
        txtTitle = rootView.findViewById(R.id.txtTitle);
        txtEmailPhoneLabel = rootView.findViewById(R.id.txtEmailPhoneLabel);
        edtEmailPhone = rootView.findViewById(R.id.edtEmailPhone);
        btnContinue = rootView.findViewById(R.id.btnContinue);
        txtTerm = rootView.findViewById(R.id.txtTerm);
        txtPrivacy = rootView.findViewById(R.id.txtPrivacy);
        txtBy = rootView.findViewById(R.id.txtBy);

        // Create Typeface from font in assets
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");
        Typeface semiBoldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-SemiBold.otf");
        Typeface boldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Bold.otf");
        Typeface mediumFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Medium.otf");

        // Apply the custom fonts to the views
        txtTitle.setTypeface(boldFont); // Title - Bold
        txtEmailPhoneLabel.setTypeface(mediumFont); // Email/Phone Label - Medium
        edtEmailPhone.setTypeface(regularFont); // Email/Phone Input - Regular
        btnContinue.setTypeface(semiBoldFont); // Continue Button - SemiBold
        txtBy.setTypeface(regularFont);
        txtTerm.setTypeface(semiBoldFont);
        txtPrivacy.setTypeface(semiBoldFont);

        // Set click listener for the Continue button
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, navigate to Forgot2Fragment
                Forgot2Fragment forgot2Fragment = new Forgot2Fragment();

                // Begin the fragment transaction
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // Replace the current fragment with Forgot2Fragment
                transaction.replace(R.id.fragment_container, forgot2Fragment);
                // Optionally, add the transaction to the back stack
                transaction.addToBackStack(null);
                // Commit the transaction to perform the fragment transaction
                transaction.commit();
            }
        });

        return rootView;
    }
}
