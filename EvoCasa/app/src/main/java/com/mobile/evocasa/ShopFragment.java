package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.models.Category;
import com.mobile.utils.FontUtils;
import com.mobile.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {
    private RecyclerView recyclerViewCategories;
    private CategoryShopAdapter adapter;
    private View view;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Inflate layout
        view = inflater.inflate(R.layout.fragment_shop, container, false);

        // 2. Apply fonts
        applyCustomFonts();

        // 3. Setup RecyclerView
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);

        // Khởi tạo danh sách categories
        categoryList = new ArrayList<>();
        categoryList.add(new Category(R.mipmap.ic_category_furniture_shop, "Shop All"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        // GridLayoutManager với 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        // Item cuối chiếm 2 cột (span toàn bộ width)
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Item cuối cùng chiếm 2 cột
                return (position == categoryList.size() - 1) ? 2 : 1;
            }
        });
        recyclerViewCategories.setLayoutManager(layoutManager);

        // Thêm khoảng cách giữa các item - giảm spacing và thêm padding cho RecyclerView
        int spacingInPixels = (int) (8 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        // Thêm padding cho RecyclerView để tránh item dính vào edge
        int paddingInPixels = (int) (16 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.setPadding(paddingInPixels, 0, paddingInPixels, 0);
        recyclerViewCategories.setClipToPadding(false);

        recyclerViewCategories.setHasFixedSize(true);

        // 4. Gán adapter
        adapter = new CategoryShopAdapter(getContext(), categoryList);
        recyclerViewCategories.setAdapter(adapter);

        return view;
    }

    private void applyCustomFonts() {
        // Áp dụng font Zbold cho tiêu đề "All category"
        TextView txtCategoryShop = view.findViewById(R.id.txtCategoryShop);
        if (txtCategoryShop != null) {
            FontUtils.setZboldFont(getContext(), txtCategoryShop);
        }

        // Áp dụng font Regular (hoặc Regular Italic nếu có) cho phần mô tả
        TextView txtDescriptionShop = view.findViewById(R.id.txtDescriptionShop);
        if (txtDescriptionShop != null) {
            FontUtils.setRegularFont(getContext(), txtDescriptionShop);
            // Nếu có italic: FontUtils.setRegularItalicFont(getContext(), txtDescriptionShop);
        }

        // Áp dụng font Zbold cho các TextView còn lại
        applyZboldFontToAllTextViews(view);
    }

    private void applyZboldFontToAllTextViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyZboldFontToAllTextViews(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            // Bỏ qua các TextView đã set font riêng
            if (textView.getId() != R.id.txtCategoryShop &&
                    textView.getId() != R.id.txtDescriptionShop) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
}