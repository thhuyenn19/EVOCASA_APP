package com.mobile.evocasa.productdetails;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

public class WrapContentViewPager2Helper {

    private ViewPager2 viewPager2;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    public WrapContentViewPager2Helper(@NonNull ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
        init();
    }

    private void init() {
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Delay to ensure fragment view is ready
                viewPager2.post(() -> adjustHeight());
            }
        };
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);
    }

    private void adjustHeight() {
        try {
            FragmentActivity activity = null;
            Context context = viewPager2.getContext();
            if (context instanceof FragmentActivity) {
                activity = (FragmentActivity) context;
            }

            if (activity != null) {
                Fragment currentFragment = activity.getSupportFragmentManager()
                        .findFragmentByTag("f" + viewPager2.getCurrentItem());

                if (currentFragment != null && currentFragment.getView() != null) {
                    View fragmentView = currentFragment.getView();

                    // Measure the fragment view
                    fragmentView.measure(
                            View.MeasureSpec.makeMeasureSpec(viewPager2.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    int height = fragmentView.getMeasuredHeight();

                    if (height > 0) {
                        ViewGroup.LayoutParams params = viewPager2.getLayoutParams();
                        if (params.height != height) {
                            params.height = height;
                            viewPager2.setLayoutParams(params);
                            viewPager2.requestLayout();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Handle any potential exceptions silently
        }
    }

    // Method to manually trigger height adjustment
    public void updateHeight() {
        viewPager2.post(() -> adjustHeight());
    }

    // Method to set up initial height after adapter is set
    public void setupInitialHeight() {
        viewPager2.post(() -> {
            // Wait a bit more for fragments to be created
            viewPager2.postDelayed(() -> adjustHeight(), 100);
        });
    }

    // Clean up when no longer needed
    public void destroy() {
        if (pageChangeCallback != null) {
            viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
        }
    }
}