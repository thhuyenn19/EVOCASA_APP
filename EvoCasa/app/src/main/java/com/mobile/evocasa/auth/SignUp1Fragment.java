package com.mobile.evocasa.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.widget.AppCompatButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.evocasa.NarBarActivity;

import java.util.*;

public class SignUp1Fragment extends Fragment {

    private AppCompatButton btnContinueEmailPhone, btnContinueFacebook, btnContinueGoogle, btnSignIn;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up1, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // Font
        Typeface medium = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Medium.otf");
        Typeface semiBold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-SemiBold.otf");

        // Views
        btnContinueEmailPhone = view.findViewById(R.id.btnContinueEmailPhone);
        btnContinueFacebook = view.findViewById(R.id.btnContinueFacebook);
        btnContinueGoogle = view.findViewById(R.id.btnContinueGoogle);
        btnSignIn = view.findViewById(R.id.btnSignIn);

        btnContinueEmailPhone.setTypeface(medium);
        btnContinueFacebook.setTypeface(medium);
        btnContinueGoogle.setTypeface(medium);
        btnSignIn.setTypeface(semiBold);

        // Phone signup
        btnContinueEmailPhone.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SignUp2Fragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Sign in
        btnSignIn.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SignInFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Google launcher
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

        // Google signup
        btnContinueGoogle.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), gso);

            // Force choose account again
            client.signOut().addOnCompleteListener(task -> {
                googleLauncher.launch(client.getSignInIntent());
            });
        });


        // Facebook signup
        btnContinueFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override public void onSuccess(LoginResult loginResult) {
                    AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                    firebaseAuth.signInWithCredential(credential)
                            .addOnSuccessListener(authResult -> handlePostAuth(authResult.getUser()))
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Facebook Auth Failed", Toast.LENGTH_SHORT).show());
                }
                @Override public void onCancel() {}
                @Override public void onError(FacebookException error) {
                    Toast.makeText(getContext(), "Facebook Error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void firebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> handlePostAuth(authResult.getUser()))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Google Auth Failed", Toast.LENGTH_SHORT).show());
    }

    private void handlePostAuth(FirebaseUser user) {
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if account already exists
        db.collection("Account").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Đã có → xử lý như đăng nhập
                        startActivity(new Intent(getActivity(), NarBarActivity.class));
                        requireActivity().finish();
                    } else {
                        // Mới → tạo dữ liệu
                        String name = user.getDisplayName() != null ? user.getDisplayName() : "User_" + UUID.randomUUID().toString().substring(0, 5);
                        String email = user.getEmail() != null ? user.getEmail() : "";
                        String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";

                        Map<String, Object> account = new HashMap<>();
                        account.put("Contact", email.isEmpty() ? phone : email);
                        account.put("ContactType", email.isEmpty() ? "Phone" : "Email");
                        account.put("Name", name);
                        account.put("Password", ""); // no password for social sign in

                        Map<String, Object> customer = new HashMap<>();
                        customer.put("Name", name);
                        customer.put("Phone", phone);
                        customer.put("Mail", email);
                        customer.put("DOB", null);
                        customer.put("Address", null);
                        customer.put("Gender", "");
                        customer.put("Image", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                        customer.put("CreatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        customer.put("Cart", new ArrayList<>());
                        customer.put("Notification", new ArrayList<>());
                        customer.put("Voucher", new ArrayList<>());

                        db.collection("Account").document(uid).set(account)
                                .addOnSuccessListener(aVoid -> {
                                    db.collection("Customers").document(uid).set(customer)
                                            .addOnSuccessListener(doc2 -> {
                                                Toast.makeText(getContext(), "Signed Up Successfully!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getActivity(), NarBarActivity.class));
                                                requireActivity().finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed saving customer", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed saving account", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking user", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
