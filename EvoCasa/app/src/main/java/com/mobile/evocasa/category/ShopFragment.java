package com.mobile.evocasa.category;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.Category;
import com.mobile.utils.FontUtils;
import com.mobile.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";
    private RecyclerView recyclerViewCategories;
    private CategoryShopAdapter adapter;
    private View view;
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private Map<String, String> categoryNameToIdMap;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shop, container, false);
        db = FirebaseFirestore.getInstance();
        categoryNameToIdMap = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());

        applyCustomFonts();
        fetchCategoryIds();

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        categoryList = new ArrayList<>();
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Shop All"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == categoryList.size() - 1) ? 2 : 1;
            }
        });
        recyclerViewCategories.setLayoutManager(layoutManager);

        int spacingInPixels = (int) (17 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        recyclerViewCategories.setClipToPadding(false);
        recyclerViewCategories.setHasFixedSize(true);

        adapter = new CategoryShopAdapter(getContext(), categoryList, category -> {
            CategoryFragment categoryFragment = new CategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedCategory", category.getName());
            if (!category.getName().equals("Shop All")) {
                String categoryId = categoryNameToIdMap.get(category.getName().toLowerCase());
                if (categoryId != null) {
                    bundle.putString("categoryId", categoryId);
                    Log.d(TAG, "Navigating to CategoryFragment with category: " + category.getName() + ", ID: " + categoryId);
                } else {
                    Log.w(TAG, "No category ID found for: " + category.getName());
                }
            } else {
                Log.d(TAG, "Navigating to CategoryFragment for Shop All");
            }
            categoryFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, categoryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        recyclerViewCategories.setAdapter(adapter);
    }

    private void fetchCategoryIds() {
        db.collection("Category")
                .whereEqualTo("ParentCategory", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        String id = doc.getId();
                        if (name != null) {
                            categoryNameToIdMap.put(name.toLowerCase(), id);
                            Log.d(TAG, "Mapped category: " + name + " to ID: " + id);
                        }
                    }
                    Log.d(TAG, "Category ID mapping: " + categoryNameToIdMap.toString());
                    // Setup RecyclerView after IDs are fetched
                    handler.post(this::setupRecyclerView);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch category IDs: ", e);
                    // Setup RecyclerView even if query fails to ensure UI loads
                    handler.post(this::setupRecyclerView);
                });
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