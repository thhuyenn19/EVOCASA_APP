package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.SubCategoryAdapter;
import com.mobile.adapters.SubCategoryProductAdapter;
import com.mobile.models.SubCategory;
import com.mobile.models.SuggestedProducts;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerViewSubCategory;
    private RecyclerView recyclerViewProducts;
    private SubCategoryAdapter subCategoryAdapter;
    private SubCategoryProductAdapter productAdapter;
    private List<SubCategory> subCategoryList;
    private List<SuggestedProducts> allProducts;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewSubCategory = view.findViewById(R.id.recyclerViewSubCategory);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);

        setupSubCategories();
        setupProducts();
    }

    private void setupSubCategories() {
        subCategoryList = new ArrayList<>();
        String[] subCategoryNames = {"All products", "Seating", "Tables", "Casegoods"};

        for (int i = 0; i < subCategoryNames.length; i++) {
            subCategoryList.add(new SubCategory(subCategoryNames[i], i == 0));
        }

        subCategoryAdapter = new SubCategoryAdapter(subCategoryList, selected -> filterProductsBySubCategory(selected));
        recyclerViewSubCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSubCategory.setAdapter(subCategoryAdapter);
    }

    private void setupProducts() {
        allProducts = new ArrayList<>();
        // Dummy data
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        allProducts.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));

        productAdapter = new SubCategoryProductAdapter(allProducts);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void filterProductsBySubCategory(String selectedCategory) {
        List<SuggestedProducts> filteredList = new ArrayList<>();

        if (selectedCategory.equals("All products")) {
            filteredList.addAll(allProducts);
        } else {
            for (SuggestedProducts product : allProducts) {
                if (product.getName().toLowerCase().contains(selectedCategory.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }

        productAdapter = new SubCategoryProductAdapter(filteredList);
        recyclerViewProducts.setAdapter(productAdapter);
    }
}