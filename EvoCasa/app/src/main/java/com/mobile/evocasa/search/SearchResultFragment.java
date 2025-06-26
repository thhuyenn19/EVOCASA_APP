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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.models.ProductItem;
import com.mobile.adapters.SearchProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private EditText edtSearch;
    private ImageView imgSearch, btnBack;
    private RecyclerView recyclerView;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                v.performClick(); // ✅ thêm dòng này

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

        return view;
    }

    private void searchSimilarProducts(String keyword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ProductItem> matchedProducts = new ArrayList<>();

        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String lowerKeyword = keyword.toLowerCase();

                    for (DocumentSnapshot doc : querySnapshot) {
                        ProductItem product = doc.toObject(ProductItem.class);
                        if (product == null) continue;

                        String name = product.getName() != null ? product.getName().toLowerCase() : "";
                        String subCat = product.getSubCategory() != null ? product.getSubCategory().toLowerCase() : "";
                        String mainCat = product.getMainCategory() != null ? product.getMainCategory().toLowerCase() : "";

                        // Mức độ khớp ưu tiên: tên chứa keyword → subcat khớp → maincat khớp
                        boolean matchKeyword = name.contains(lowerKeyword);
                        boolean matchSubCategory = subCat.contains(lowerKeyword) || lowerKeyword.contains(subCat);
                        boolean matchMainCategory = mainCat.contains(lowerKeyword) || lowerKeyword.contains(mainCat);

                        if (matchKeyword || matchSubCategory || matchMainCategory) {
                            product.setId(doc.getId());
                            matchedProducts.add(product);
                        }
                    }

                    if (matchedProducts.isEmpty()) {
                        Toast.makeText(getContext(), "Không tìm thấy sản phẩm phù hợp", Toast.LENGTH_SHORT).show();
                    }

                    recyclerView.setAdapter(new SearchProductAdapter(matchedProducts));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e("SearchResult", "Lỗi tìm kiếm: ", e);
                });
    }

}
