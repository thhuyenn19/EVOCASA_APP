package com.mobile.evocasa.onboarding;

import android.content.Intent;
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
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mobile.adapters.ViewPagerAdapter;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import me.relex.circleindicator.CircleIndicator;

public class Onboarding1Activity extends AppCompatActivity {

    private TextView tvSkip;
    private ViewPager viewPager;
    private CircleIndicator indicator;

    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding1);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        initUI();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);

        indicator.setViewPager(viewPager);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2){
                    tvSkip.setVisibility(View.GONE);
                }
                else {
                    tvSkip.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void initUI() {
        tvSkip = findViewById(R.id.tvSkip);
        viewPager = findViewById(R.id.view_pager);
        indicator = findViewById(R.id.indicator);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Onboarding1Activity.this, Onboarding5Activity.class);
                startActivity(intent);
                finish(); //không quay lại màn hình onboarding1 khi nhấn back
            }
        });



//        tvSkip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               viewPager.setCurrentItem(2);
////                if (viewPager.getCurrentItem() <2 ){
////                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
////                }
//            }
//        });

        FontUtils.initFonts(this);

        FontUtils.setRegularFont(this, tvSkip);

    }
}