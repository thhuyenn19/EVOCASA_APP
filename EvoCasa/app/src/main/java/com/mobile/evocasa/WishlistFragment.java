package com.mobile.evocasa;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.adapters.FlashSaleAdapter;
import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.adapters.WishlistProductAdapter;
import com.mobile.adapters.WishlistRcmAdapter;
import com.mobile.models.FlashSaleProduct;
import com.mobile.models.SuggestedProducts;
import com.mobile.models.WishlistProduct;
import com.mobile.models.WishlistRcm;

import java.util.ArrayList;
import java.util.List;


public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        //List product

        RecyclerView recyclerViewWishlistProduct = view.findViewById(R.id.recyclerViewWishlistProduct);
        recyclerViewWishlistProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<WishlistProduct> wishlistProductList = new ArrayList<>();
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistProductList.add(new WishlistProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));

        WishlistProductAdapter wishlistProductAdapter = new WishlistProductAdapter(wishlistProductList);
        recyclerViewWishlistProduct.setAdapter(wishlistProductAdapter);


        //Recommend//
        RecyclerView recyclerViewWishlistRcm = view.findViewById(R.id.recyclerViewWishlistRcm);

        recyclerViewWishlistRcm.setLayoutManager(new GridLayoutManager(getContext(), 2));


        List<WishlistRcm> wishlistRcmList = new ArrayList<>();
        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        wishlistRcmList.add(new WishlistRcm(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));

        WishlistRcmAdapter wishlistRcmAdapter = new WishlistRcmAdapter(wishlistRcmList);
        recyclerViewWishlistRcm.setAdapter(wishlistRcmAdapter);

        return view;
    }
}