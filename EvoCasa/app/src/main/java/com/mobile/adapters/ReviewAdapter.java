package com.mobile.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);
        Log.d("ReviewAdapter", "Name: " + review.getName());
        Log.d("ReviewAdapter", "Comment: " + review.getComment());
        Log.d("ReviewAdapter", "Date: " + review.getCreatedAt());
        Log.d("ReviewAdapter", "Rating: " + review.getRating());
        holder.txtReviewerName.setText(review.getName());
        holder.txtReviewComment.setText(review.getComment());
        holder.txtReviewDate.setText(review.getCreatedAt());
        holder.imgAvatar.setImageResource(review.getAvatarRes());

        // Hiển thị số sao đánh giá
        holder.layoutStars.removeAllViews();
        for (int i = 0; i < review.getRating(); i++) {
            ImageView star = new ImageView(context);
            star.setImageResource(R.drawable.ic_star_yellow); // vector/icon star vàng
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
            params.setMargins(2, 0, 2, 0);
            star.setLayoutParams(params);
            holder.layoutStars.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtReviewerName, txtReviewComment, txtReviewDate;
        LinearLayout layoutStars;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtReviewerName = itemView.findViewById(R.id.txtReviewerName);
            txtReviewComment = itemView.findViewById(R.id.txtReviewComment);
            txtReviewDate = itemView.findViewById(R.id.txtReviewDate);
            layoutStars = itemView.findViewById(R.id.layoutStars);
        }
    }
}
