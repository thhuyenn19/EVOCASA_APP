package com.mobile.evocasa.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobile.evocasa.MainActivity;
import com.mobile.evocasa.NarBarActivity;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

public class Onboarding5Activity extends AppCompatActivity {

    private TextView txtViewOnboarding5;
    private TextView txtView3;
    private TextView txtHaveAccount;
    private Button btnCreateAccountOnboarding5;
    private Button btnLogIn;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding5);

        txtViewOnboarding5 = findViewById(R.id.txtViewOnboarding5);
        txtView3 = findViewById(R.id.txtView3);
        txtHaveAccount = findViewById(R.id.txtHaveAccount);
        btnCreateAccountOnboarding5 = findViewById(R.id.btnCreateAccountOnboarding5);
        btnLogIn = findViewById(R.id.btnLogIn);

        // Gán font
        FontUtils.setZblackFont(this, txtViewOnboarding5);
        FontUtils.setItalicFont(this, txtView3);
        FontUtils.setRegularFont(this, txtHaveAccount);
        FontUtils.setBoldFont(this, btnCreateAccountOnboarding5);
        FontUtils.setMediumFont(this, btnLogIn);

        // Xử lý nút Create Account
        btnCreateAccountOnboarding5.setOnClickListener(v -> {
            // Lưu trạng thái đã xem onboarding
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ONBOARDING, true);
            editor.apply();

            // Chuyển đến màn hình đăng ký (SignUp1Fragment)
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("openFragment", "SignUp");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Xử lý nút Log In
        btnLogIn.setOnClickListener(v -> {
            // Lưu trạng thái đã xem onboarding
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ONBOARDING, true);
            editor.apply();

            // Chuyển đến màn hình đăng nhập (SignInFragment)
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("openFragment", "SignIn");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}