package com.mobile.utils;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int totalItemCount = state.getItemCount();

        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        int spanSize = layoutManager.getSpanSizeLookup().getSpanSize(position);

        if (spanSize == spanCount) {
            outRect.left = 0;
            outRect.right = 0;

            if (position > 0) {
                outRect.top = spacing;
            }
        } else {
            // Items bình thường (1 cột)
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                if (position < totalItemCount - 1) {
                    outRect.bottom = spacing;
                }
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;

                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}