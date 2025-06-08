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
import com.mobile.utils.FontUtils;
import com.mobile.adapters.CategoryAdapter;
import com.mobile.models.Category;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter adapter;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1. Gán layout cho view
        view = inflater.inflate(R.layout.fragment_shop, container, false);

        // 2. Áp dụng font cho các TextView
        applyCustomFonts();

        // 3. Gán RecyclerView cho categories
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 4. Khởi tạo danh sách categories với Shop All ở đầu
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category(R.mipmap.ic_category_shop, "Shop All"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        // 5. Gán adapter
        adapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setAdapter(adapter);

        return view;
    }

    private void applyCustomFonts() {
        // Áp dụng font Zbold cho title "All category"
        TextView txtCategoryShop = view.findViewById(R.id.txtCategoryShop);
        if (txtCategoryShop != null) {
            FontUtils.setZboldFont(getContext(), txtCategoryShop);
        }

        // Áp dụng font Regular Italic cho description
        TextView txtDescriptionShop = view.findViewById(R.id.txtDescriptionShop);
        if (txtDescriptionShop != null) {
            FontUtils.setRegularFont(getContext(), txtDescriptionShop);
            // Nếu có method setRegularItalicFont thì dùng cái đó thay vì setRegularFont
            // FontUtils.setRegularItalicFont(getContext(), txtDescriptionShop);
        }

        // Áp dụng font Zbold cho tất cả các TextView khác (bao gồm category names)
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
            // Bỏ qua các TextView đã được set font riêng
            if (textView.getId() != R.id.txtCategoryShop &&
                    textView.getId() != R.id.txtDescriptionShop) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
}