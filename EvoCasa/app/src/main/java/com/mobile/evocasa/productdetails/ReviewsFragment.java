package com.mobile.evocasa.productdetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public ReviewsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerViewReviews = view.findViewById(R.id.recyclerViewReviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewList = getSampleReviews(); // tạo danh sách mẫu

        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        recyclerViewReviews.setAdapter(reviewAdapter);

        return view;
    }

    private List<Review> getSampleReviews() {
        List<Review> list = new ArrayList<>();
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Alexandre", "Good quality and very fast delivery.", "25 - 01 - 2025", R.mipmap.sample_avt, 5));
        list.add(new Review("Maria", "The design is beautiful, I love it!", "05 - 03 - 2025",R.mipmap.sample_avt, 4));
        list.add(new Review("John", "A bit expensive, but worth it overall.", "10 - 05 - 2025", R.mipmap.sample_avt, 4));
        return list;
    }
}