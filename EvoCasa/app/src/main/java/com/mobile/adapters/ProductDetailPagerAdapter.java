package com.mobile.adapters;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.gson.Gson;
import com.mobile.evocasa.productdetails.DescriptionFragment;
import com.mobile.evocasa.productdetails.DimensionsFragment;
import com.mobile.evocasa.productdetails.ReviewsFragment;
import com.mobile.models.ProductItem;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailPagerAdapter extends FragmentStateAdapter {

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private String description;
    private String dimensions;
    private ProductItem productItem;
    private String productId;

    public ProductDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setProductData(String description, String dimensions, ProductItem productItem) {
        this.description = description != null ? description : "";
        this.dimensions = dimensions != null ? dimensions : "";
        this.productItem = productItem;
        this.productId = productItem != null ? productItem.getId() : null;
        if (productItem != null && productItem.getRatings() == null) {
            productItem.setRatings(new ProductItem.Ratings()); // Khởi tạo Ratings nếu null
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                fragment = new DescriptionFragment();
                args.putString(DescriptionFragment.ARG_DESCRIPTION, description);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created DescriptionFragment with description: " + description);
                break;
            case 1:
                fragment = new DimensionsFragment();
                args.putString(DimensionsFragment.ARG_DIMENSIONS, dimensions);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created DimensionsFragment with dimensions: " + dimensions);
                break;
            case 2:
                fragment = new ReviewsFragment();
                if (productItem != null && productItem.getRatings() != null) {
                    Gson gson = new Gson();
                    String ratingsJson = gson.toJson(productItem.getRatings()); // Truyền toàn bộ Ratings
                    args.putString(ReviewsFragment.ARG_RATINGS_JSON, ratingsJson);
                    args.putString(ReviewsFragment.ARG_PRODUCT_ID, productId);
                    Log.d("ProductDetailPagerAdapter", "Created ReviewsFragment with ratingsJson: " + ratingsJson);
                } else {
                    args.putString(ReviewsFragment.ARG_RATINGS_JSON, "{}"); // Giá trị mặc định
                    args.putString(ReviewsFragment.ARG_PRODUCT_ID, productId);
                    Log.d("ProductDetailPagerAdapter", "Created ReviewsFragment with empty ratingsJson");
                }
                fragment.setArguments(args);
                break;
            default:
                fragment = new DescriptionFragment();
                args.putString(DescriptionFragment.ARG_DESCRIPTION, description);
                fragment.setArguments(args);
                Log.d("ProductDetailPagerAdapter", "Created default DescriptionFragment with description: " + description);
                break;
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Fragment getCurrentFragment(int position) {
        return fragmentMap.get(position);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        fragmentMap.clear();
    }
}