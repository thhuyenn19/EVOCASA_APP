package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.BlogAdapter;
import com.mobile.models.Blog;

import java.util.ArrayList;
import java.util.List;

public class BlogFragment extends Fragment {

    private RecyclerView recyclerView;
    private BlogAdapter blogAdapter;
    private List<Blog> blogList;

    public BlogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        recyclerView = view.findViewById(R.id.recycler_blog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        blogList = new ArrayList<>();
        blogList.add(new Blog("5 Interior Design Trends Of 2024 | Sofa Trend", "16/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Minimalist Living Room Ideas", "14/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "13/05/2025", R.mipmap.ic_featuredblog));

        blogAdapter = new BlogAdapter(blogList);
        recyclerView.setAdapter(blogAdapter);

        // Tab selection logic
        View tabRecent = view.findViewById(R.id.tab_recent);
        View tabRecommended = view.findViewById(R.id.tab_recommended);
        View tabPopular = view.findViewById(R.id.tab_popular);

        View[] tabs = {tabRecent, tabRecommended, tabPopular};

        // Mặc định chọn tab Recent
        tabRecent.setSelected(true);

        for (View tab : tabs) {
            tab.setOnClickListener(v -> {
                for (View t : tabs) t.setSelected(false);
                v.setSelected(true);
                // TODO: Thay đổi dữ liệu blog theo tab nếu cần
            });
        }

        return view;
    }
}
