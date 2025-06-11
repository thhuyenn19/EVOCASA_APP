package com.mobile.evocasa.productdetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.ReviewAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private View rootView;

    public ReviewsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerViewReviews = rootView.findViewById(R.id.recyclerViewReviews);

        // Create a custom LinearLayoutManager to fix height issues
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable vertical scrolling
            }
        };

        recyclerViewReviews.setLayoutManager(layoutManager);

        // Disable nested scrolling
        recyclerViewReviews.setNestedScrollingEnabled(false);

        // Set has fixed size to false for dynamic content
        recyclerViewReviews.setHasFixedSize(false);

        // Initialize data and adapter
        reviewList = getSampleReviews();
        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add ViewTreeObserver to detect when layout is complete
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateViewPagerHeight();
            }
        });

        // Force multiple layout updates with delays
        view.post(() -> {
            updateViewPagerHeight();

            // Additional delayed updates to ensure proper rendering
            view.postDelayed(() -> updateViewPagerHeight(), 100);
            view.postDelayed(() -> updateViewPagerHeight(), 300);
            view.postDelayed(() -> updateViewPagerHeight(), 500);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Force update height when fragment becomes visible again
        if (rootView != null) {
            rootView.post(() -> {
                refreshReviews();
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && rootView != null) {
            // Fragment is visible, update height
            rootView.post(() -> {
                refreshReviews();
            });
        }
    }

    // For newer API levels (replace setUserVisibleHint)
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && rootView != null) {
            rootView.post(() -> {
                refreshReviews();
            });
        }
    }

    private void updateViewPagerHeight() {
        if (getActivity() instanceof ProductDetailsActivity) {
            ((ProductDetailsActivity) getActivity()).updateViewPagerHeight();
        }
    }

    public void refreshReviews() {
        if (reviewAdapter != null && recyclerViewReviews != null) {
            // Force adapter to recreate views
            reviewAdapter.notifyDataSetChanged();

            // Force RecyclerView to remeasure
            recyclerViewReviews.post(() -> {
                recyclerViewReviews.invalidate();
                recyclerViewReviews.requestLayout();
                updateViewPagerHeight();
            });
        }
    }

    // Method to force refresh when tab is selected
    public void onFragmentVisible() {
        if (rootView != null && reviewAdapter != null) {
            rootView.post(() -> {
                // Force complete refresh
                reviewAdapter.notifyDataSetChanged();
                recyclerViewReviews.invalidate();
                recyclerViewReviews.requestLayout();

                // Multiple height updates
                updateViewPagerHeight();
                rootView.postDelayed(() -> updateViewPagerHeight(), 50);
                rootView.postDelayed(() -> updateViewPagerHeight(), 150);
            });
        }
    }

    private List<Review> getSampleReviews() {
        List<Review> list = new ArrayList<>();
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Maria", "The design is beautiful, I love it!", "05 - 03 - 2025", R.mipmap.sample_avt, 4));
        list.add(new Review("John", "A bit expensive, but worth it overall.", "10 - 05 - 2025", R.mipmap.sample_avt, 4));
        list.add(new Review("Sarah", "Perfect for my living room!", "15 - 02 - 2025", R.mipmap.sample_avt, 5));
        return list;
    }
}