package com.mobile.evocasa;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.utils.FontUtils;

import java.util.Arrays;
import java.util.List;


public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;

    private TextView btnAll, btnSale, btnLowStock, btnOutOfStock;
    private List<TextView> allTabs;

    private ImageView imgWishlistBack;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);



//        /* Wishlist Products */
//        RecyclerView recyclerViewWishlistProduct = view.findViewById(R.id.recyclerViewSuggestedProducts);
//
//        recyclerViewWishlistProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
//
//        List<WishlistProduct> wishlistProductList = new ArrayList<>();
//        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//
//        // Gán adapter cho RecyclerView
//        WishlistProductAdapter wishlistProductAdapter = new WishlistProductAdapter(wishlistProductList);
//        recyclerViewWishlistProduct.setAdapter(wishlistProductAdapter);
//
//
//
//        /* Wishlist Recommend Products */
//        RecyclerView recyclerViewWishlistRcm = view.findViewById(R.id.recyclerViewSuggestedProducts);
//
//        recyclerViewWishlistRcm.setLayoutManager(new GridLayoutManager(getContext(), 2));
//
//        List<WishlistRcm> wishlistRcmList = new ArrayList<>();
//        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
//
//        // Gán adapter cho RecyclerView
//        WishlistRcmAdapter wishlistRcmAdapter = new WishlistRcmAdapter(wishlistRcmList);
//        recyclerViewWishlistRcm.setAdapter(wishlistRcmAdapter);






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