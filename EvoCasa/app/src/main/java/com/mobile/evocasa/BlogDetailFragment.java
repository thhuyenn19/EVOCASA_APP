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
import com.mobile.models.Blog;
import com.mobile.utils.ImageUtils;

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

        // Resize blog detail images
        ImageView imgBlogDetail1 = view.findViewById(R.id.img_blog_detail_1);
        ImageView imgBlogDetail2 = view.findViewById(R.id.img_blog_detail_2);
        ImageView imgBlogDetail3 = view.findViewById(R.id.img_blog_detail_3);
        ImageView imgBlogDetail4 = view.findViewById(R.id.img_blog_detail_4);
        ImageView imgBlogDetail5 = view.findViewById(R.id.img_blog_detail_5);

        Bitmap resizedBitmap1 = ImageUtils.getResizedBitmap(requireContext(), R.mipmap.ic_blogdetail16, 360, 200);
        Bitmap resizedBitmap2 = ImageUtils.getResizedBitmap(requireContext(), R.mipmap.ic_blogdetail11, 360, 200);
        Bitmap resizedBitmap3 = ImageUtils.getResizedBitmap(requireContext(), R.mipmap.ic_blogdetail13, 360, 200);
        Bitmap resizedBitmap4 = ImageUtils.getResizedBitmap(requireContext(), R.mipmap.ic_blogdetail14, 360, 200);

        imgBlogDetail1.setImageBitmap(resizedBitmap1);
        imgBlogDetail2.setImageBitmap(resizedBitmap2);
        imgBlogDetail3.setImageBitmap(resizedBitmap3);
        imgBlogDetail4.setImageBitmap(resizedBitmap4);

        // Xử lý nút back
        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new BlogFragment())
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }
}
