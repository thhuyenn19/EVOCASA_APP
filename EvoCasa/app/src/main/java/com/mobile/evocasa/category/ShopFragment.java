package com.mobile.evocasa.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.CategoryFragment;
import com.mobile.evocasa.R;
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
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Shop All"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        // GridLayoutManager với 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

        // Item cuối chiếm 2 cột
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == categoryList.size() - 1) ? 2 : 1;
            }
        });
        recyclerViewCategories.setLayoutManager(layoutManager);

        // Thêm khoảng cách
        int spacingInPixels = (int) (8 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        recyclerViewCategories.setClipToPadding(false);
        recyclerViewCategories.setHasFixedSize(true);

        // 4. Gán adapter
        adapter = new CategoryShopAdapter(getContext(), categoryList, category -> {
            CategoryFragment categoryFragment = new CategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedCategory", category.getName());
            categoryFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, categoryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        recyclerViewCategories.setAdapter(adapter); // ⚠️ Quan trọng: Gắn adapter vào RecyclerView

        return view; // ⚠️ Phải return view để Fragment hoạt động đúng
    }

    private void applyCustomFonts() {
        TextView txtCategoryShop = view.findViewById(R.id.txtCategoryShop);
        if (txtCategoryShop != null) {
            FontUtils.setZboldFont(getContext(), txtCategoryShop);
        }

        TextView txtDescriptionShop = view.findViewById(R.id.txtDescriptionShop);
        if (txtDescriptionShop != null) {
            FontUtils.setLightitalicFont(getContext(), txtDescriptionShop);
        }

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
            if (textView.getId() != R.id.txtCategoryShop && textView.getId() != R.id.txtDescriptionShop) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
}
