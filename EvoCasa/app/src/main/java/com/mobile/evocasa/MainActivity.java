package com.mobile.evocasa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.auth.SignUp1Fragment;
import com.mobile.utils.UserSessionManager;
import com.mobile.evocasa.auth.SignInFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // QUAN TRỌNG: Gọi setAppLocale() TRƯỚC super.onCreate()
        setAppLocale();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            UserSessionManager sessionManager = new UserSessionManager(this);

            if (sessionManager.isLoggedIn()) {
                // Đã đăng nhập → Mở luôn màn hình chính
                startActivity(new Intent(MainActivity.this, NarBarActivity.class));
                finish(); // Đóng MainActivity để không quay lại được
            } else {
                // Chưa đăng nhập → hiện SignInFragment
                SignInFragment signInFragment = new SignInFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, signInFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        String openFragment = getIntent().getStringExtra("openFragment");

        if ("SignUp".equals(openFragment)) {
            switchToFragment(new SignUp1Fragment());
            return;
        } else if ("SignIn".equals(openFragment)) {
            switchToFragment(new SignInFragment());
            return;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "en"); // Mặc định LUÔN là tiếng Anh

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    // Thêm phương thức switchToFragment
    public void switchToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setAppLocale() {
        SharedPreferences prefs = getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "en"); // Mặc định tiếng Anh

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);
    }
}