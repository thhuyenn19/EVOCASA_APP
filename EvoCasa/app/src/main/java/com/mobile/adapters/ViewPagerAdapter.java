package com.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mobile.evocasa.onboarding.Onboarding2Fragment;
import com.mobile.evocasa.onboarding.Onboarding3Fragment;
import com.mobile.evocasa.onboarding.Onboarding4Fragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Onboarding2Fragment();
            case 1:
                return new Onboarding3Fragment();
            case 2:
                return new Onboarding4Fragment();
            default:
                return new Onboarding2Fragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}