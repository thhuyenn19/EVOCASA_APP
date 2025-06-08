package com.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mobile.evocasa.productdetails.DescriptionFragment;
import com.mobile.evocasa.productdetails.DimensionsFragment;
import com.mobile.evocasa.productdetails.ReviewsFragment;

public class ProductDetailPagerAdapter extends FragmentStateAdapter {
    public ProductDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new DimensionsFragment();
            case 2:
                return new ReviewsFragment();
            default:
                return new DescriptionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

