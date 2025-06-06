package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp3Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp3Fragment extends Fragment {

    private static final String TAG = "SignUp3Fragment";

    // UI Components
    private ImageView btnBack, btnHelp;
    private TextView txtTitle, txtDescription, txtCodeLabel, btnSendAgain, txtTerms, txtPrivacyPolicy;
    private EditText edtCode1, edtCode2, edtCode3, edtCode4, edtCode5;
    private AppCompatButton btnVerifyEmail;
    private EditText[] codeInputs;

    // Font variables
    private Typeface regularFont;
    private Typeface boldFont;
    private Typeface lightFont;
    private Typeface mediumFont;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUp3Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUp3Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUp3Fragment newInstance(String param1, String param2) {
        SignUp3Fragment fragment = new SignUp3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Load fonts early in lifecycle
        loadCustomFonts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up3, container, false);

        // Initialize UI components
        initViews(view);

        // Set custom fonts
        setCustomFonts();

        // Set click listeners
        setClickListeners();

        // Setup code input functionality
        setupCodeInputs();

        return view;
    }

    private void loadCustomFonts() {
        try {
            if (getContext() != null) {
                // Load different font weights
                // Thay đổi tên file font theo font thực tế trong assets/fonts/
                regularFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto_regular.ttf");
                boldFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto_bold.ttf");
                lightFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto_light.ttf");
                mediumFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto_medium.ttf");

                Log.d(TAG, "Custom fonts loaded successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading custom fonts: " + e.getMessage(), e);
            // Use default fonts as fallback
            setDefaultTypefaceFallback();
        }
    }

    private void setDefaultTypefaceFallback() {
        regularFont = Typeface.DEFAULT;
        boldFont = Typeface.DEFAULT_BOLD;
        lightFont = Typeface.DEFAULT;
        mediumFont = Typeface.DEFAULT_BOLD;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnHelp = view.findViewById(R.id.btnHelp);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtCodeLabel = view.findViewById(R.id.txtCodeLabel);
        edtCode1 = view.findViewById(R.id.edtCode1);
        edtCode2 = view.findViewById(R.id.edtCode2);
        edtCode3 = view.findViewById(R.id.edtCode3);
        edtCode4 = view.findViewById(R.id.edtCode4);
        edtCode5 = view.findViewById(R.id.edtCode5);
        btnSendAgain = view.findViewById(R.id.btnSendAgain);
        btnVerifyEmail = view.findViewById(R.id.btnVerifyEmail);
        txtTerms = view.findViewById(R.id.txtTerms);
        txtPrivacyPolicy = view.findViewById(R.id.txtPrivacyPolicy);

        // Array of code inputs for easier management
        codeInputs = new EditText[]{edtCode1, edtCode2, edtCode3, edtCode4, edtCode5};
    }

    private void setCustomFonts() {
        try {
            // Áp dụng font cho các TextView
            if (txtTitle != null && boldFont != null) {
                txtTitle.setTypeface(boldFont);
            }

            if (txtDescription != null && regularFont != null) {
                txtDescription.setTypeface(regularFont);
            }

            if (txtCodeLabel != null && mediumFont != null) {
                txtCodeLabel.setTypeface(mediumFont);
            }

            // Áp dụng font cho buttons
            if (btnVerifyEmail != null && boldFont != null) {
                btnVerifyEmail.setTypeface(boldFont);
            }

            if (btnSendAgain != null && mediumFont != null) {
                btnSendAgain.setTypeface(mediumFont);
            }

            // Áp dụng font cho Terms và Privacy Policy
            if (txtTerms != null && regularFont != null) {
                txtTerms.setTypeface(regularFont);
            }

            if (txtPrivacyPolicy != null && regularFont != null) {
                txtPrivacyPolicy.setTypeface(regularFont);
            }

            // Áp dụng font cho code inputs
            if (codeInputs != null && boldFont != null) {
                for (EditText input : codeInputs) {
                    if (input != null) {
                        input.setTypeface(boldFont);
                    }
                }
            }

            Log.d(TAG, "Custom fonts applied successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error applying custom fonts: " + e.getMessage(), e);
            setDefaultFonts();
        }
    }

    private void setDefaultFonts() {
        Log.w(TAG, "Using default fonts as fallback");

        // Fallback fonts
        if (txtTitle != null) txtTitle.setTypeface(null, Typeface.BOLD);
        if (txtDescription != null) txtDescription.setTypeface(null, Typeface.NORMAL);
        if (txtCodeLabel != null) txtCodeLabel.setTypeface(null, Typeface.NORMAL);
        if (btnVerifyEmail != null) btnVerifyEmail.setTypeface(null, Typeface.BOLD);
        if (btnSendAgain != null) btnSendAgain.setTypeface(null, Typeface.BOLD);
        if (txtTerms != null) txtTerms.setTypeface(null, Typeface.NORMAL);
        if (txtPrivacyPolicy != null) txtPrivacyPolicy.setTypeface(null, Typeface.NORMAL);

        if (codeInputs != null) {
            for (EditText input : codeInputs) {
                if (input != null) {
                    input.setTypeface(null, Typeface.BOLD);
                }
            }
        }
    }

    private void setClickListeners() {
        // Back button click
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Help button click
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                // TODO: Implement help functionality
                openHelp();
            });
        }

        // Send again button click
        if (btnSendAgain != null) {
            btnSendAgain.setOnClickListener(v -> {
                resendVerificationCode();
            });
        }

        // Verify Email button click
        if (btnVerifyEmail != null) {
            btnVerifyEmail.setOnClickListener(v -> {
                String code = getEnteredCode();
                if (validateCode(code)) {
                    verifyCode(code);
                }
            });
        }

        // Terms click
        if (txtTerms != null) {
            txtTerms.setOnClickListener(v -> {
                openTerms();
            });
        }

        // Privacy Policy click
        if (txtPrivacyPolicy != null) {
            txtPrivacyPolicy.setOnClickListener(v -> {
                openPrivacyPolicy();
            });
        }
    }

    private void setupCodeInputs() {
        if (codeInputs == null) return;

        for (int i = 0; i < codeInputs.length; i++) {
            final int index = i;

            if (codeInputs[i] != null) {
                codeInputs[i].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 1 && index < codeInputs.length - 1) {
                            // Move to next input
                            if (codeInputs[index + 1] != null) {
                                codeInputs[index + 1].requestFocus();
                            }
                        }

                        // Auto verify when all 5 digits are entered
                        if (index == codeInputs.length - 1 && s.length() == 1) {
                            String code = getEnteredCode();
                            if (code.length() == 5) {
                                // Optional: Auto verify
                                // verifyCode(code);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                // Handle backspace
                codeInputs[i].setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (codeInputs[index].getText().toString().isEmpty() && index > 0) {
                            // Move to previous input
                            if (codeInputs[index - 1] != null) {
                                codeInputs[index - 1].requestFocus();
                                codeInputs[index - 1].getText().clear();
                            }
                        }
                    }
                    return false;
                });
            }
        }
    }

    private String getEnteredCode() {
        StringBuilder code = new StringBuilder();
        if (codeInputs != null) {
            for (EditText input : codeInputs) {
                if (input != null) {
                    code.append(input.getText().toString());
                }
            }
        }
        return code.toString();
    }

    private boolean validateCode(String code) {
        if (code.length() != 5) {
            showError("Vui lòng nhập đầy đủ 5 chữ số");
            return false;
        }

        if (!code.matches("\\d{5}")) {
            showError("Mã xác thực chỉ được chứa số");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        // TODO: Show error message (Toast, Snackbar, or custom dialog)
        Log.w(TAG, "Validation error: " + message);

        // Focus on first empty field
        if (codeInputs != null) {
            for (EditText input : codeInputs) {
                if (input != null && input.getText().toString().isEmpty()) {
                    input.requestFocus();
                    break;
                }
            }
        }
    }

    private void resendVerificationCode() {
        Log.d(TAG, "Resending verification code");

        // TODO: Implement resend code functionality
        // Clear all inputs
        if (codeInputs != null) {
            for (EditText input : codeInputs) {
                if (input != null) {
                    input.getText().clear();
                }
            }
            if (edtCode1 != null) {
                edtCode1.requestFocus();
            }
        }
    }

    private void verifyCode(String code) {
        Log.d(TAG, "Verifying code: " + code);
        // TODO: Implement code verification
        // Navigate to next screen or complete registration
    }

    private void openHelp() {
        // TODO: Implement help functionality
        Log.d(TAG, "Opening help");
    }

    private void openTerms() {
        // TODO: Implement terms page navigation
        Log.d(TAG, "Opening terms");
    }

    private void openPrivacyPolicy() {
        // TODO: Implement privacy policy page navigation
        Log.d(TAG, "Opening privacy policy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        codeInputs = null;
    }
}