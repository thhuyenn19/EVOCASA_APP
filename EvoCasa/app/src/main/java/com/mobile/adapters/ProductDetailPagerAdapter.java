package com.mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mobile.evocasa.productdetails.DescriptionFragment;
import com.mobile.evocasa.productdetails.DimensionsFragment;
import com.mobile.evocasa.productdetails.ReviewsFragment;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailPagerAdapter extends FragmentStateAdapter {

    private Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public ProductDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new DescriptionFragment();
                break;
            case 1:
                fragment = new DimensionsFragment();
                break;
            case 2:
                fragment = new ReviewsFragment();
                break;
            default:
                fragment = new DescriptionFragment();
                break;
        }

        // Store fragment reference
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Fragment getCurrentFragment(int position) {
        return fragmentMap.get(position);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        fragmentMap.clear();
    }
}