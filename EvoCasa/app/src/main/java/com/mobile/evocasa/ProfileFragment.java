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

import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.models.SuggestedProducts;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private TextView txtName;
    private RecyclerView recyclerView;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        applyCustomFonts(view);
        /* Suggested Products */
        RecyclerView recyclerViewSuggestedProducts = view.findViewById(R.id.recyclerViewSuggestedProducts);

        recyclerViewSuggestedProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));


        List<SuggestedProducts> suggestedProductsList = new ArrayList<>();
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));

        // Gán adapter cho RecyclerView
       SuggestedProductAdapter suggestedProductsAdapter = new SuggestedProductAdapter(suggestedProductsList);
        recyclerViewSuggestedProducts.setAdapter(suggestedProductsAdapter);

        // Sự kiện mở BlogFragment khi bấm vào txtEvoCasaBlog
        View txtEvoCasaBlog = view.findViewById(R.id.txtEvoCasaBlog);
        txtEvoCasaBlog.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new BlogFragment())
                .addToBackStack(null)
                .commit();
        });

        //Mở WishlistFragment khi bấm vào txtWishlist
        View txtWishlist = view.findViewById(R.id.txtWishlist);
        txtWishlist.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new WishlistFragment())
                .addToBackStack(null)
                .commit();

        });
    return view;
    }
    private void applyCustomFonts(View view) {
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtOrders = view.findViewById(R.id.txtOrders);
        TextView txtMyFeatures = view.findViewById(R.id.txtMyFeatures);
        TextView txtSupport = view.findViewById(R.id.txtSupport);

        if (txtName != null) {
            FontUtils.setZboldFont(getContext(), txtName);
        }
        if (txtOrders != null) {
            FontUtils.setZblackFont(getContext(), txtOrders);
        }
        if (txtMyFeatures != null) {
            FontUtils.setZblackFont(getContext(), txtMyFeatures);
        }
        if (txtSupport != null) {
            FontUtils.setZblackFont(getContext(), txtSupport);
        }

        TextView txtMarkAllRead = view.findViewById(R.id.txtMarkAllRead);
        if (txtMarkAllRead != null) {
            FontUtils.setRegularFont(getContext(), txtMarkAllRead);
        }
    }
}
