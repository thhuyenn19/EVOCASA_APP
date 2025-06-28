package com.mobile.evocasa.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;
import com.mobile.adapters.SearchProductAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchResultFragment extends Fragment {

    private EditText edtSearch;
    private ImageView imgSearch, btnBack;
    private RecyclerView recyclerView;
    private SearchProductAdapter adapter;
    private List<ProductItem> matchedProducts = new ArrayList<>();

    public static SearchResultFragment newInstance(String keyword, boolean b) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        btnBack = view.findViewById(R.id.imgBack);
        edtSearch = view.findViewById(R.id.edtSearch);
        imgSearch = view.findViewById(R.id.imgSearch);
        recyclerView = view.findViewById(R.id.recyclerSearchProduct);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 👉 chuyển thành grid 2 cột

        if (getArguments() != null) {
            String keyword = getArguments().getString("keyword", "");
            edtSearch.setText(keyword);
            searchSimilarProducts(keyword);
        }

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        imgSearch.setOnClickListener(v -> {
            String newKeyword = edtSearch.getText().toString().trim();
            if (!newKeyword.isEmpty()) {
                searchSimilarProducts(newKeyword);
            }
        });

        edtSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick(); // thêm dòng này

                // chuyển về SearchProgressFragment
                String currentKeyword = edtSearch.getText().toString().trim();
                Bundle args = new Bundle();
                args.putString("keywordFromSearchResult", currentKeyword);

                SearchProgressFragment fragment = new SearchProgressFragment();
                fragment.setArguments(args);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
            return false;
        });
        adapter = new SearchProductAdapter(matchedProducts, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void searchSimilarProducts(String keyword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        matchedProducts.clear();

        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String lowerKeyword = keyword.toLowerCase();
                    List<ProductItem> exactMatches = new ArrayList<>();
                    List<ProductItem> partialMatches = new ArrayList<>();
                    List<ProductItem> descriptionMatches = new ArrayList<>();
                    List<ProductItem> fuzzyMatches = new ArrayList<>();
                    List<ProductItem> remainingProducts = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        ProductItem product = doc.toObject(ProductItem.class);
                        if (product == null) continue;

                        String name = product.getName() != null ? product.getName().toLowerCase() : "";
                        String subCat = product.getSubCategory() != null ? product.getSubCategory().toLowerCase() : "";
                        String mainCat = product.getMainCategory() != null ? product.getMainCategory().toLowerCase() : "";
                        String description = product.getDescription() != null ? product.getDescription().toLowerCase() : "";

                        product.setId(doc.getId());

                        // Mức 1: Khớp chính xác tên sản phẩm
                        if (name.equals(lowerKeyword)) {
                            exactMatches.add(product);
                            continue;
                        }

                        // Mức 2: Khớp một phần tên, category
                        if (name.contains(lowerKeyword) || subCat.contains(lowerKeyword) || mainCat.contains(lowerKeyword)) {
                            partialMatches.add(product);
                            continue;
                        }

                        // Mức 3: Khớp trong description
                        if (description.contains(lowerKeyword)) {
                            descriptionMatches.add(product);
                            continue;
                        }

                        // Mức 4: Tìm kiếm mờ (fuzzy search) - khớp từng từ
                        String[] keywordParts = lowerKeyword.split("\\s+");
                        boolean hasFuzzyMatch = false;

                        for (String part : keywordParts) {
                            if (part.length() >= 2) { // Chỉ tìm từ có ít nhất 2 ký tự
                                if (name.contains(part) || subCat.contains(part) ||
                                        mainCat.contains(part) || description.contains(part)) {
                                    hasFuzzyMatch = true;
                                    break;
                                }
                            }
                        }

                        if (hasFuzzyMatch) {
                            fuzzyMatches.add(product);
                        } else {
                            // Nếu không khớp với 4 mức độ trên thì thêm vào danh sách sản phẩm còn lại
                            remainingProducts.add(product);
                        }
                    }

                    // Kết hợp kết quả theo thứ tự ưu tiên
                    matchedProducts.addAll(exactMatches);
                    matchedProducts.addAll(partialMatches);
                    matchedProducts.addAll(descriptionMatches);
                    matchedProducts.addAll(fuzzyMatches);
//                    matchedProducts.addAll(categoryMatches);
                    // Thêm tất cả sản phẩm còn lại vào cuối
//                    matchedProducts.addAll(remainingProducts);

                    // Loại bỏ duplicate nếu có
                    Set<String> addedIds = new HashSet<>();
                    List<ProductItem> uniqueProducts = new ArrayList<>();
                    for (ProductItem product : matchedProducts) {
                        if (!addedIds.contains(product.getId())) {
                            addedIds.add(product.getId());
                            uniqueProducts.add(product);
                        }
                    }
                    matchedProducts.clear();
                    matchedProducts.addAll(uniqueProducts);

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e("SearchResult", "Lỗi tìm kiếm: ", e);
                });
    }
}