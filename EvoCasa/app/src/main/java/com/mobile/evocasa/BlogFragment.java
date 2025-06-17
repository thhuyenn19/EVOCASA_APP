package com.mobile.evocasa;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.BlogAdapter;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.Blog;
import com.mobile.utils.ImageUtils;

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
        blogList.add(new Blog("5 Interior Design Trends Of 2024 | Sofa Trend", "16/05/2025", R.mipmap.ic_blog1));
        blogList.add(new Blog("How Can You Mix And Match Wood Tones In Your Home – With Confidence", "14/05/2025", R.mipmap.ic_blog2));
        blogList.add(new Blog("The Art Of Home Decor: Elevating Spaces With Timeless Artwork", "13/05/2025", R.mipmap.ic_blog3));
        blogList.add(new Blog("Redefining Comfort Through Natural Design", "10/05/2025", R.mipmap.ic_blog4));
        blogList.add(new Blog("The Charm Of Decorating With Vintage Decor", "29/04/2025", R.mipmap.ic_blog5));
        blogList.add(new Blog("Top Colors to Refresh Your Space", "25/04/2025", R.mipmap.ic_blog6));
        blogList.add(new Blog("Refresh Your Living Space By Top Warm Colors", "20/04/2025", R.mipmap.ic_blog7));

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

        // Xử lý nút back
        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();
        });

        // Add click listeners for featured blog
        View cardFeaturedBlog = view.findViewById(R.id.cardFeaturedBlog);
        ImageView imgFeaturedBlog = view.findViewById(R.id.imgFeaturedBlog);

        // Resize featured blog image
        Bitmap resizedFeaturedBitmap = ImageUtils.getResizedBitmap(
            requireContext(),
            R.mipmap.ic_featuredblog,
            360, // width in dp
            180  // height in dp
        );
        imgFeaturedBlog.setImageBitmap(resizedFeaturedBitmap);

        View.OnClickListener navigateToDetail = v -> { 
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new BlogDetailFragment())
                .addToBackStack(null)
                .commit();
        };

        cardFeaturedBlog.setOnClickListener(navigateToDetail);
        imgFeaturedBlog.setOnClickListener(navigateToDetail);

        return view;
    }
}
