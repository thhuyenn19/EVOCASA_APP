package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.evocasa.BlogDetailFragment;
import com.mobile.evocasa.R;
import com.mobile.models.Blog;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final List<Blog> blogList;

    public BlogAdapter(List<Blog> blogList) {
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        
        // Load image with Glide for better quality and automatic caching
        Glide.with(holder.itemView.getContext())
                .load(blog.getImageResId())
                .placeholder(R.mipmap.placeholder_image)
                .error(R.mipmap.error_image)
                .centerCrop()
                .into(holder.imgBlog);
        
        holder.txtTitle.setText(blog.getTitle());
        holder.txtDate.setText(blog.getDate());
        holder.itemView.setOnClickListener(v -> {
            FragmentTransaction transaction = ((FragmentActivity) v.getContext())
                    .getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new BlogDetailFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return blogList != null ? blogList.size() : 0;
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBlog, imgBookmark;
        TextView txtTitle, txtDate;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBlog = itemView.findViewById(R.id.img_blog);
            imgBookmark = itemView.findViewById(R.id.img_bookmark);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtDate = itemView.findViewById(R.id.txt_date);
        }
    }
}
