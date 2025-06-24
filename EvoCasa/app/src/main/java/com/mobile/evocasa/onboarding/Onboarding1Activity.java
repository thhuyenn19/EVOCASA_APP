package com.mobile.evocasa.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.mobile.adapters.ViewPagerAdapter;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import me.relex.circleindicator.CircleIndicator;

public class Onboarding1Activity extends AppCompatActivity {

    private TextView tvSkip;
    private ViewPager viewPager;
    private CircleIndicator indicator;
    private ViewPagerAdapter viewPagerAdapter;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding1);

        initUI();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);
        indicator.setViewPager(viewPager);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position == 2) { // Trang 3 (cuối cùng)
                    tvSkip.setVisibility(View.GONE);
                } else {
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void initUI() {
        tvSkip = findViewById(R.id.tvSkip);
        viewPager = findViewById(R.id.view_pager);
        indicator = findViewById(R.id.indicator);

        tvSkip.setOnClickListener(v -> {
            // Lưu trạng thái đã xem onboarding
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ONBOARDING, true);
            editor.apply();

            Intent intent = new Intent(this, Onboarding5Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        FontUtils.initFonts(this);
        FontUtils.setRegularFont(this, tvSkip);
    }
}