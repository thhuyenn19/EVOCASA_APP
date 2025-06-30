package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thanhhuyen.utils.AdminSessionManager;
import com.thanhhuyen.utils.FontUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText edtUserName, edtPassword;
    private TextView txtWelcome, txtSignIn, txtUserName, txtPassword, txtRememberMe;
    private ImageView btnTogglePassword;
    private CheckBox cbRememberMe;
    private AppCompatButton btnLogIn;
    private boolean isPasswordVisible = false;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_ID = "employeeid";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Khởi tạo các view
        initializeViews();

        // Xử lý Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });

        // Khôi phục thông tin đăng nhập
        restoreLoginInformation();

        // Xử lý toggle password visibility
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


        // Xử lý nút Login
        btnLogIn.setOnClickListener(v -> {
            String employeeid = edtUserName.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (employeeid.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Admin")
                    .whereEqualTo("employeeid", employeeid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean isFound = false;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String storedPassword = doc.getString("Password");
                            String status = doc.getString("Status");
                            String fullName = doc.getString("FullName");


                            if (storedPassword != null && storedPassword.equals(password)) {
                                if ("Active".equalsIgnoreCase(status)) {
                                    isFound = true;
                                    AdminSessionManager session = new AdminSessionManager(this);
                                    session.saveAdmin(employeeid, fullName);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                } else {
                                    Toast.makeText(this, "Tài khoản đang bị vô hiệu hóa", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }

                        if (!isFound) {
                            Toast.makeText(this, "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        txtWelcome.setTypeface(FontUtils.getItalic(this));
        txtUserName.setTypeface(FontUtils.getRegular(this));
        txtPassword.setTypeface(FontUtils.getRegular(this));
        edtPassword.setTypeface(FontUtils.getRegular(this));
        edtUserName.setTypeface(FontUtils.getRegular(this));
        txtSignIn.setTypeface(FontUtils.getZblack(this));
        btnLogIn.setTypeface(FontUtils.getBold(this));
        txtRememberMe.setTypeface(FontUtils.getRegular(this));

    }

    private void initializeViews() {
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogIn = findViewById(R.id.btnLogIn);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtSignIn = findViewById(R.id.txtSignIn);
        txtRememberMe=findViewById(R.id.txtRememberMe);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cbRememberMe.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#5E4C3E")));
        }
    }


    private void saveLoginInformation(String employeeid, String password, boolean isRemember) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ID, employeeid);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER, isRemember);
        editor.apply();
    }

    private void restoreLoginInformation() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isRemember = prefs.getBoolean(KEY_REMEMBER, false);
        if (isRemember) {
            String savedId = prefs.getString(KEY_ID, "");
            String savedPassword = prefs.getString(KEY_PASSWORD, "");
            edtUserName.setText(savedId);
            edtPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String employeeid = edtUserName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        boolean isRemember = cbRememberMe.isChecked();
        if (isRemember) {
            saveLoginInformation(employeeid, password, isRemember);
        } else {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        }
    }

}