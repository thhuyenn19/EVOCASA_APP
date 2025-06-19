package com.mobile.evocasa.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
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
    private TextView txtDontHave;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private ImageView btnBack;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up1, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // Font
        Typeface medium = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Medium.otf");
        Typeface black = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-Black.otf");
        Typeface semiBold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Inter-SemiBold.otf");

        // Views
        btnContinueEmailPhone = view.findViewById(R.id.btnContinueEmailPhone);
        btnContinueFacebook = view.findViewById(R.id.btnContinueFacebook);
        btnContinueGoogle = view.findViewById(R.id.btnContinueGoogle);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnBack = view.findViewById(R.id.btnBack);
        txtDontHave=view.findViewById(R.id.txtDontHave);

        btnContinueEmailPhone.setTypeface(medium);
        btnContinueFacebook.setTypeface(medium);
        btnContinueGoogle.setTypeface(medium);
        btnSignIn.setTypeface(black);
        txtDontHave.setTypeface(semiBold);

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
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
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
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
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
        db.collection("Account").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Welcome back! Logged in successfully.", Toast.LENGTH_SHORT).show();

                        new com.mobile.utils.UserSessionManager(requireContext()).saveUid(uid);
                        startActivity(new Intent(getActivity(), NarBarActivity.class));
                        requireActivity().finish();
                    } else {
                        createNewUser(user, uid, db);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewUser(FirebaseUser user, String uid, FirebaseFirestore db) {
        String name = user.getDisplayName() != null ? user.getDisplayName() : "User_" + UUID.randomUUID().toString().substring(0, 5);
        String email = user.getEmail() != null ? user.getEmail() : "";
        String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";

        // Xác định loại đăng ký dựa trên provider
        String contactType = "Email";
        String contact = email;

        // Kiểm tra provider để xác định ContactType chính xác
        for (UserInfo userInfo : user.getProviderData()) {
            String providerId = userInfo.getProviderId();
            if (providerId.equals("facebook.com") || providerId.equals("google.com")) {
                contactType = "Email";
                contact = email.isEmpty() ? "social_user_" + uid.substring(0, 8) : email;
                break;
            } else if (providerId.equals("phone")) {
                contactType = "Phone";
                contact = phone;
                break;
            }
        }


        if (contact.isEmpty()) {
            contact = name;
        }

        Map<String, Object> account = new HashMap<>();
        account.put("Contact", contact);
        account.put("ContactType", contactType);
        account.put("Name", name);
        account.put("Password", "");

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

        // Tạo tài khoản mới
        db.collection("Account").document(uid).set(account)
                .addOnSuccessListener(aVoid -> {
                    db.collection("Customers").document(uid).set(customer)
                            .addOnSuccessListener(doc -> {
                                new com.mobile.utils.UserSessionManager(requireContext()).saveUid(uid);

                                Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), NarBarActivity.class));
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed saving customer", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed saving account", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}