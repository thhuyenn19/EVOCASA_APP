package com.mobile.evocasa.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.evocasa.NarBarActivity;
import com.mobile.utils.FontUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {

    private TextView txtSignIn, txtDescription, txtOrWith, txtDontHave, txtTerm, txtPrivacy, txtView, txtBy;
    private Button btnContinueEmailPhoneSignIn, btnSignUp;
    private LinearLayout btnContinueFacebook, btnContinueGoogle;

    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> googleLauncher;

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

        // Initialize Firebase Auth and Facebook CallbackManager
        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
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
        txtDontHave.setTypeface(FontUtils.getSemiBold(getContext()));
        txtTerm.setTypeface(FontUtils.getSemiBold(getContext()));
        txtPrivacy.setTypeface(FontUtils.getSemiBold(getContext()));
        txtView.setTypeface(FontUtils.getMedium(getContext()));
        txtBy.setTypeface(FontUtils.getRegular(getContext()));

        btnContinueEmailPhoneSignIn.setTypeface(FontUtils.getMedium(getContext()));

        // Set font cho Facebook button (tìm TextView bên trong LinearLayout)
        TextView txtFacebook = btnContinueFacebook.findViewById(R.id.txtfacebook);
        if (txtFacebook != null) {
            txtFacebook.setTypeface(FontUtils.getMedium(getContext()));
        }

        // Set font cho Google button (tìm TextView bên trong LinearLayout)
        TextView txtGoogle = btnContinueGoogle.findViewById(R.id.txtgoogle);
        if (txtGoogle != null) {
            txtGoogle.setTypeface(FontUtils.getMedium(getContext()));
        }

        btnSignUp.setTypeface(FontUtils.getBlack(getContext()));

        // Initialize Google launcher
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseGoogleAuth(account);
                    } catch (ApiException e) {
                        Toast.makeText(getContext(), "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Set onClickListener for btnContinueEmailPhoneSignIn to navigate to SignIn1Fragment
        btnContinueEmailPhoneSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the new fragment instance
                SignIn1Fragment signIn1Fragment = new SignIn1Fragment();

                // Begin the fragment transaction
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                // Replace the current fragment with SignIn1Fragment
                transaction.replace(R.id.fragment_container, signIn1Fragment);
                // Optionally, add the transaction to the back stack
                transaction.addToBackStack(null);
                // Commit the transaction to perform the fragment transaction
                transaction.commit();
            }
        });

        // Set onClickListener for Facebook button (LinearLayout)
        btnContinueFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Facebook login implementation
                LoginManager.getInstance().logInWithReadPermissions(SignInFragment.this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                        firebaseAuth.signInWithCredential(credential)
                                .addOnSuccessListener(authResult -> handlePostAuth(authResult.getUser()))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Facebook Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getContext(), "Facebook Login Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(getContext(), "Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Set onClickListener for Google button (LinearLayout)
        btnContinueGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google login implementation
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), gso);

                // Force choose account again
                client.signOut().addOnCompleteListener(task -> {
                    googleLauncher.launch(client.getSignInIntent());
                });
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một instance của SignUp1Fragment
                SignUp1Fragment signUp1Fragment = new SignUp1Fragment();

                // Bắt đầu một giao dịch Fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
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

    private void firebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> handlePostAuth(authResult.getUser()))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Google Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handlePostAuth(FirebaseUser user) {
        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Account").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User exists, proceed with login
                        Toast.makeText(getContext(), "Welcome back! Logged in successfully.", Toast.LENGTH_SHORT).show();

                        // Save user session
                        new com.mobile.utils.UserSessionManager(requireContext()).saveUid(uid);

                        // Navigate to main activity
                        startActivity(new Intent(getActivity(), NarBarActivity.class));
                        requireActivity().finish();
                    } else {
                        // User doesn't exist, show message to sign up first
                        Toast.makeText(getContext(), "Account not found. Please sign up first.", Toast.LENGTH_LONG).show();

                        // Sign out from Firebase to prevent confusion
                        firebaseAuth.signOut();

                        // Navigate to sign up fragment
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new SignUp1Fragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}