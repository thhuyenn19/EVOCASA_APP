package com.mobile.evocasa;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.adapters.WishProductAdapter;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.HotProducts;
import com.mobile.models.WishProduct;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;

    private TextView btnAll, btnSale, btnLowStock, btnOutOfStock;
    private List<TextView> allTabs;

    private ImageView imgWishlistBack;

    private WishProductAdapter wishProductAdapter;
    private FirebaseFirestore db;
    private List<Integer> imageList;
    private List<WishProduct> wishProductList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishProductList = new ArrayList<>();
        wishProductAdapter = new WishProductAdapter(wishProductList);
        recyclerViewWishProduct.setAdapter(wishProductAdapter);

        // Gọi hàm load từ Firestore
        loadWishProduct();


        //set font//
        TextView txtViewRcm = view.findViewById(R.id.txtViewRcm);
        FontUtils.setZboldFont(requireContext(), txtViewRcm);

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

        TextView tvSortBy = view.findViewById(R.id.tvSortBy);
        FontUtils.setMediumFont(requireContext(), tvSortBy);

        TextView btnAll = view.findViewById(R.id.btnAll);
        FontUtils.setMediumFont(requireContext(), btnAll);

        TextView btnSale = view.findViewById(R.id.btnSale);
        FontUtils.setMediumFont(requireContext(), btnSale);

        TextView btnLowStock = view.findViewById(R.id.btnLowStock);
        FontUtils.setMediumFont(requireContext(), btnLowStock);

        TextView btnOutOfStock = view.findViewById(R.id.btnOutOfStock);
        FontUtils.setMediumFont(requireContext(), btnOutOfStock);


        //Chọn các option lọc
        // Danh sách tất cả tab
        allTabs = Arrays.asList(btnAll, btnSale, btnLowStock, btnOutOfStock);

        // Chọn mặc định tab All
        setActiveTab(btnAll);

        // Gán sự kiện cho các tab
        for (TextView tab : allTabs) {
            tab.setOnClickListener(v -> {
                setActiveTab(tab);
                // TODO: xử lý lọc sản phẩm tương ứng tại đây nếu cần
            });
        }

        // Gán sự kiện quay lại ProfileFragment
        imgWishlistBack = view.findViewById(R.id.imgWishlistBack);
        imgWishlistBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadWishProduct() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    wishProductList.clear();

                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
                    Collections.shuffle(allDocs); // 🔀 random

                    int limit = Math.min(6, allDocs.size()); // lấy 6 sản phẩm
                    for (int i = 0; i < limit; i++) {
                        WishProduct product = allDocs.get(i).toObject(WishProduct.class);
                        wishProductList.add(product);
                    }

                    wishProductAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
                });
    }

    private void setActiveTab(TextView selectedTab) {
        for (TextView tab : allTabs) {
            if (tab == selectedTab) {
                tab.setBackgroundResource(R.drawable.filter_button_selector_choose);
            } else {
                tab.setBackgroundResource(R.drawable.filter_button_selector);
            }
        }
    }
}










//BÀI CŨ
//package com.mobile.evocasa;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.mobile.adapters.SuggestedProductAdapter;
//import com.mobile.adapters.WishProductAdapter;
//import com.mobile.evocasa.profile.ProfileFragment;
//import com.mobile.models.SuggestedProducts;
//import com.mobile.models.WishProduct;
//import com.mobile.utils.FontUtils;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//
//public class WishlistFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private View view;
//
//    private TextView btnAll, btnSale, btnLowStock, btnOutOfStock;
//    private List<TextView> allTabs;
//
//    private ImageView imgWishlistBack;
//
//    private WishProductAdapter adapter;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        view = inflater.inflate(R.layout.fragment_wishlist, container, false);
//
//
//        /* Favourite */
//        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
//        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        List<WishProduct> wishProductList = new ArrayList<>();
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishProductList.add(new WishProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        // Gán adapter
//        WishProductAdapter wishProductAdapter = new WishProductAdapter(wishProductList);
//        recyclerViewWishProduct.setAdapter(wishProductAdapter);
//
//
//
//
//
////        /* Suggest */
////        RecyclerView recyclerViewSuggestedProducts = view.findViewById(R.id.recyclerViewSuggestedProducts);
////        recyclerViewSuggestedProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
////        List<SuggestedProducts> suggestedProductsList = new ArrayList<>();
////        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
////        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
////        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
////        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
////        // Gán adapter cho RecyclerView
////        SuggestedProductAdapter suggestedProductsAdapter = new SuggestedProductAdapter(suggestedProductsList);
////        recyclerViewSuggestedProducts.setAdapter(suggestedProductsAdapter);
//
//
//
//
//        //set font//
//        TextView txtViewRcm = view.findViewById(R.id.txtViewRcm);
//        FontUtils.setZboldFont(requireContext(), txtViewRcm);
//
//        TextView txtTitle = view.findViewById(R.id.txtTitle);
//        FontUtils.setZboldFont(requireContext(), txtTitle);
//
//        TextView tvSortBy = view.findViewById(R.id.tvSortBy);
//        FontUtils.setMediumFont(requireContext(), tvSortBy);
//
//        TextView btnAll = view.findViewById(R.id.btnAll);
//        FontUtils.setMediumFont(requireContext(), btnAll);
//
//        TextView btnSale = view.findViewById(R.id.btnSale);
//        FontUtils.setMediumFont(requireContext(), btnSale);
//
//        TextView btnLowStock = view.findViewById(R.id.btnLowStock);
//        FontUtils.setMediumFont(requireContext(), btnLowStock);
//
//        TextView btnOutOfStock = view.findViewById(R.id.btnOutOfStock);
//        FontUtils.setMediumFont(requireContext(), btnOutOfStock);
//
//
//        //Chọn các option lọc
//        // Danh sách tất cả tab
//        allTabs = Arrays.asList(btnAll, btnSale, btnLowStock, btnOutOfStock);
//
//        // Chọn mặc định tab All
//        setActiveTab(btnAll);
//
//        // Gán sự kiện cho các tab
//        for (TextView tab : allTabs) {
//            tab.setOnClickListener(v -> {
//                setActiveTab(tab);
//                // TODO: xử lý lọc sản phẩm tương ứng tại đây nếu cần
//            });
//        }
//
//        // Gán sự kiện quay lại ProfileFragment
//        imgWishlistBack = view.findViewById(R.id.imgWishlistBack);
//        imgWishlistBack.setOnClickListener(v -> {
//            requireActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ProfileFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        return view;
//    }
//
//    private void setActiveTab(TextView selectedTab) {
//        for (TextView tab : allTabs) {
//            if (tab == selectedTab) {
//                tab.setBackgroundResource(R.drawable.filter_button_selector_choose);
//            } else {
//                tab.setBackgroundResource(R.drawable.filter_button_selector);
//            }
//        }
//    }
//}