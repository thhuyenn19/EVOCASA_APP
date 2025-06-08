package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.BlogAdapter;
import com.mobile.models.Blog;

import java.util.ArrayList;
import java.util.List;

public class BlogDetailFragment extends Fragment {
    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList;

    public BlogDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog_detail, container, false);
        recyclerView = view.findViewById(R.id.recycler_blog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        blogList = new ArrayList<>();
        blogList.add(new Blog("5 Interior Design Trends Of 2024 | Sofa Trend", "16/05/2025", R.mipmap.ic_blog1));
        blogList.add(new Blog("Minimalist Living Room Ideas", "14/05/2025", R.mipmap.ic_blog1));
        blogList.add(new Blog("Top Colors To Refresh Your Space", "13/05/2025", R.mipmap.ic_blog1));

        blogAdapter = new BlogAdapter(blogList);
        recyclerView.setAdapter(blogAdapter);

        // Xử lý nút back
        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new BlogFragment())
                    .commit();
        });
        return view;
    }
}
