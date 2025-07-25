package com.mobile.evocasa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.auth.SignInFragment;
import com.mobile.evocasa.auth.SignUp1Fragment;
import com.mobile.evocasa.onboarding.Onboarding1Activity;
import com.mobile.utils.UserSessionManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppLocale();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra trạng thái onboarding
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasShownOnboarding = prefs.getBoolean(KEY_ONBOARDING, false);
        Log.d("MainActivity", "Onboarding status: " + hasShownOnboarding);

        if (savedInstanceState == null) {
            if (!hasShownOnboarding) {
                // Nếu chưa xem onboarding → xóa session cũ
                UserSessionManager sessionManager = new UserSessionManager(this);
                sessionManager.clearSession();

                Intent intent = new Intent(this, Onboarding1Activity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Nếu có yêu cầu mở một Fragment cụ thể (SignUp / SignIn)
            String openFragment = getIntent().getStringExtra("openFragment");
            if (openFragment != null) {
                if ("SignUp".equals(openFragment)) {
                    switchToFragment(new SignUp1Fragment());
                    return;
                } else if ("SignIn".equals(openFragment)) {
                    switchToFragment(new SignInFragment());
                    return;
                }
            }

            // Sau onboarding, luôn vào NarBarActivity (dù có login hay chưa)
            startActivity(new Intent(MainActivity.this, NarBarActivity.class));
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    public void switchToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setAppLocale() {
        SharedPreferences prefs = getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "order_channel_id";
            CharSequence name = "Order Notifications";
            String description = "Notifications for order updates";

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
