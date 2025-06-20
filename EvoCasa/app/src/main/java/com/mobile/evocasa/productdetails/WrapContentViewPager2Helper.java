package com.mobile.evocasa.productdetails;

import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mobile.adapters.ProductDetailPagerAdapter;

public class WrapContentViewPager2Helper {
    private ViewPager2 viewPager;
    private FragmentActivity activity;

    public WrapContentViewPager2Helper(ViewPager2 viewPager) {
        this.viewPager = viewPager;
        this.activity = (FragmentActivity) viewPager.getContext();
    }

    public void setupInitialHeight() {
        viewPager.post(() -> updateHeight());
    }

    public void updateHeight() {
        if (activity == null || viewPager == null) return;

        try {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment != null && currentFragment.getView() != null) {
                View fragmentView = currentFragment.getView();

                // Special handling for ReviewsFragment
                if (currentFragment instanceof ReviewsFragment) {
                    // Find the RecyclerView in the reviews fragment
                    RecyclerView recyclerView = fragmentView.findViewById(com.mobile.evocasa.R.id.recyclerViewReviews);
                    if (recyclerView != null && recyclerView.getAdapter() != null) {
                        // Force the RecyclerView to measure all its children
                        recyclerView.measure(
                                View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        );
                    }
                }

                // Force measure the fragment view
                fragmentView.measure(
                        View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                int targetHeight = fragmentView.getMeasuredHeight();


                ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                if (Math.abs(layoutParams.height - targetHeight) > 10) { // Only update if significant difference
                    layoutParams.height = targetHeight;
                    viewPager.setLayoutParams(layoutParams);
                    viewPager.requestLayout();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Fragment getCurrentFragment() {
        try {
            ProductDetailPagerAdapter adapter = (ProductDetailPagerAdapter) viewPager.getAdapter();
            if (adapter != null) {
                int currentItem = viewPager.getCurrentItem();
                String fragmentTag = "f" + currentItem;
                Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(fragmentTag);

                // If not found by tag, try the adapter method
                if (fragment == null) {
                    fragment = adapter.getCurrentFragment(currentItem);
                }

                return fragment;
            }
        } catch (Exception e) {
            // Fallback method
            try {
                ProductDetailPagerAdapter adapter = (ProductDetailPagerAdapter) viewPager.getAdapter();
                if (adapter != null) {
                    return adapter.getCurrentFragment(viewPager.getCurrentItem());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void destroy() {
        viewPager = null;
        activity = null;
    }
}