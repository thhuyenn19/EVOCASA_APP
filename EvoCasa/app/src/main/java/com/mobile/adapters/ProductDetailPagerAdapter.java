package com.mobile.adapters;

import android.os.Bundle;
import android.util.Log;

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
    private String description;
    private String dimensions;
    private String reviewsJson;
    private String productId;


    public ProductDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setProductData(String description, String dimensions, String reviewsJson, String productId) {
        this.description = description;
        this.dimensions = dimensions;
        this.reviewsJson = reviewsJson;
        this.productId = productId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                fragment = new DescriptionFragment();
                args.putString(DescriptionFragment.ARG_DESCRIPTION, description);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created DescriptionFragment with description: " + description);
                break;
            case 1:
                fragment = new DimensionsFragment();
                args.putString(DimensionsFragment.ARG_DIMENSIONS, dimensions);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created DimensionsFragment with dimensions: " + dimensions);
                break;
            case 2:
                fragment = new ReviewsFragment();
                args.putString(ReviewsFragment.ARG_REVIEWS_JSON, reviewsJson);
                args.putString(ReviewsFragment.ARG_PRODUCT_ID, productId);
                fragment.setArguments(args);
                break;
            default:
                fragment = new DescriptionFragment();
                args.putString(DescriptionFragment.ARG_DESCRIPTION, description);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created default DescriptionFragment with description: " + description);
                break;
        }

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