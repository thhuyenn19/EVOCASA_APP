package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.mobile.utils.UserSessionManager;
import com.mobile.evocasa.auth.SignInFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    }
}