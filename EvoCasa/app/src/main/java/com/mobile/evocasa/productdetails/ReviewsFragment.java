package com.mobile.evocasa.productdetails;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.adapters.ReviewAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;
import com.mobile.models.Review;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerViewReviews;
    public static final String ARG_RATINGS_JSON = "ratings_json";
    public static final String ARG_REVIEWS_JSON = "reviews_json";
    public static final String ARG_PRODUCT_ID = "product_id";
    public static final String ARG_AVERAGE_RATING = "average_rating";

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private View rootView;

    public ReviewsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerViewReviews = rootView.findViewById(R.id.recyclerViewReviews);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        recyclerViewReviews.setLayoutManager(layoutManager);
        recyclerViewReviews.setNestedScrollingEnabled(false);
        recyclerViewReviews.setHasFixedSize(false);

        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        loadReviewsFromArgs();

        return rootView;
    }

    private void loadReviewsFromArgs() {
        Bundle args = getArguments();
        if (args == null) return;

        TextView txtRating = rootView.findViewById(R.id.txtRating);
        TextView txtCount = rootView.findViewById(R.id.txtCount);
        TextView txtNoReviews = rootView.findViewById(R.id.txtNoReviews);

        // Ưu tiên sử dụng ARG_RATINGS_JSON (toàn bộ Ratings object)
        if (args.containsKey(ARG_RATINGS_JSON)) {
            String ratingsJson = args.getString(ARG_RATINGS_JSON);
            parseRatingsJson(ratingsJson, txtRating, txtCount, txtNoReviews);
        }
        // Fallback: sử dụng cách cũ (ARG_REVIEWS_JSON + ARG_AVERAGE_RATING)
        else {
            parseOldFormat(args, txtRating, txtCount, txtNoReviews);
        }
    }

    private void parseRatingsJson(String ratingsJson, TextView txtRating, TextView txtCount, TextView txtNoReviews) {
        try {
            Gson gson = new Gson();
            ProductItem.Ratings ratings = gson.fromJson(ratingsJson, ProductItem.Ratings.class);

            if (ratings != null) {
                // Set average rating
                double avg = ratings.getAverage() != null ? ratings.getAverage() : 0.0;
                Log.d("ReviewsFragment", "Parsed average: " + avg); // Debug log
                if (txtRating != null) {
                    if (avg > 0) {
                        txtRating.setText(String.format("%.1f", avg));
                    } else {
                        txtRating.setText("Not rated yet");
                    }
                }

                // Load reviews
                List<ProductItem.Ratings.Detail> detailList = ratings.getDetails();
                reviewList.clear();

                if (detailList != null && !detailList.isEmpty()) {
                    for (ProductItem.Ratings.Detail d : detailList) {
                        Log.d("PARSE", "Name: " + d.getCustomerName());
                        Log.d("PARSE", "Comment: " + d.getComment());
                        Log.d("PARSE", "Date: " + d.getCreatedAt());
                        Log.d("PARSE", "Rating: " + d.getRating());

                        reviewList.add(new Review(
                                d.getCustomerName(),
                                d.getComment(),
                                d.getCreatedAt(),
                                R.mipmap.sample_avt,
                                d.getRating()
                        ));
                    }

                    reviewAdapter.notifyDataSetChanged();
                    txtCount.setText("(" + reviewList.size() + (reviewList.size() == 1 ? " review)" : " reviews)"));
                    txtNoReviews.setVisibility(View.GONE);
                    recyclerViewReviews.setVisibility(View.VISIBLE);
                } else {
                    // No reviews
                    txtCount.setText("(0 reviews)");
                    txtNoReviews.setVisibility(View.VISIBLE);
                    recyclerViewReviews.setVisibility(View.GONE);
                }
            } else {
                // Ratings object is null
                setNoReviewsState(txtRating, txtCount, txtNoReviews);
            }
        } catch (Exception e) {
            Log.e("ReviewsFragment", "Error parsing ratings JSON", e);
            setNoReviewsState(txtRating, txtCount, txtNoReviews);
        }
    }

    private void parseOldFormat(Bundle args, TextView txtRating, TextView txtCount, TextView txtNoReviews) {
        // ----- 1. Rating Number -----
        if (txtRating != null) {
            if (args.containsKey(ARG_AVERAGE_RATING)) {
                double avg = args.getDouble(ARG_AVERAGE_RATING, 0.0);
                if (avg > 0) {
                    txtRating.setText(String.format("%.1f", avg));
                } else {
                    txtRating.setText("Not rated yet");
                }
            } else {
                txtRating.setText("Not rated yet");
            }
        }

        // ----- 2. Load Review List -----
        if (args.containsKey(ARG_REVIEWS_JSON)) {
            String json = args.getString(ARG_REVIEWS_JSON);

            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ProductItem.Ratings.Detail>>() {}.getType();
                List<ProductItem.Ratings.Detail> detailList = gson.fromJson(json, listType);

                reviewList.clear();

                if (detailList != null && !detailList.isEmpty()) {
                    for (ProductItem.Ratings.Detail d : detailList) {
                        Log.d("PARSE", "Name: " + d.getCustomerName());
                        Log.d("PARSE", "Comment: " + d.getComment());
                        Log.d("PARSE", "Date: " + d.getCreatedAt());
                        Log.d("PARSE", "Rating: " + d.getRating());

                        reviewList.add(new Review(
                                d.getCustomerName(),
                                d.getComment(),
                                d.getCreatedAt(),
                                R.mipmap.sample_avt,
                                d.getRating()
                        ));
                    }

                    reviewAdapter.notifyDataSetChanged();
                    txtCount.setText("(" + reviewList.size() + (reviewList.size() == 1 ? " review)" : " reviews)"));
                    txtNoReviews.setVisibility(View.GONE);
                    recyclerViewReviews.setVisibility(View.VISIBLE);
                } else {
                    // No reviews
                    txtCount.setText("(0 reviews)");
                    txtNoReviews.setVisibility(View.VISIBLE);
                    recyclerViewReviews.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("ReviewsFragment", "Error parsing reviews JSON", e);
                setNoReviewsState(txtRating, txtCount, txtNoReviews);
            }
        } else {
            // No reviews passed
            txtCount.setText("(0 reviews)");
            txtNoReviews.setVisibility(View.VISIBLE);
            recyclerViewReviews.setVisibility(View.GONE);
        }
    }

    private void setNoReviewsState(TextView txtRating, TextView txtCount, TextView txtNoReviews) {
        if (txtRating != null) {
            txtRating.setText("Not rated yet");
        }
        if (txtCount != null) {
            txtCount.setText("(0 reviews)");
        }
        if (txtNoReviews != null) {
            txtNoReviews.setVisibility(View.VISIBLE);
        }
        if (recyclerViewReviews != null) {
            recyclerViewReviews.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateViewPagerHeight();
            }
        });

        view.post(() -> {
            updateViewPagerHeight();
            view.postDelayed(() -> updateViewPagerHeight(), 100);
            view.postDelayed(() -> updateViewPagerHeight(), 300);
        });
    }

    private void updateViewPagerHeight() {
        if (getActivity() instanceof ProductDetailsActivity) {
            ((ProductDetailsActivity) getActivity()).updateViewPagerHeight();
        }
    }

    public void onFragmentVisible() {
        loadReviewsFromArgs(); // hoặc bất cứ hàm cập nhật giao diện nào bạn cần
    }

    public void refreshReviews() {
        loadReviewsFromArgs(); // Gọi lại hàm để cập nhật danh sách review
    }

    // Utility method để tạo fragment với toàn bộ Ratings object
    public static ReviewsFragment newInstance(String ratingsJson, String productId) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RATINGS_JSON, ratingsJson);
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    // Utility method để tạo fragment với cách cũ (backward compatibility)
    public static ReviewsFragment newInstance(String reviewsJson, double averageRating, String productId) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REVIEWS_JSON, reviewsJson);
        args.putDouble(ARG_AVERAGE_RATING, averageRating);
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }
}