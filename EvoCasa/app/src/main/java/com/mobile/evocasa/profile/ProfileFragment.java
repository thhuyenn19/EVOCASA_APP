package com.mobile.evocasa.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.BlogFragment;
import com.mobile.evocasa.ChatActivity;
import com.mobile.evocasa.OrdersFragment;
import com.mobile.evocasa.R;
import com.mobile.evocasa.WishlistFragment;
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

        ImageView imgAvatar = view.findViewById(R.id.img_avatar);
        ImageButton btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        imgAvatar.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileDetailFragment())
                    .addToBackStack(null)
                    .commit();
        });
        btnEditAvatar.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileDetailFragment())
                    .addToBackStack(null)
                    .commit();
        });

        LinearLayout containerSeeAll = view.findViewById(R.id.containerSeeAll);
        containerSeeAll.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .addToBackStack(null)
                    .commit();
        });
        LinearLayout itemPending = view.findViewById(R.id.containerPending);     // chỉnh ID theo bạn
        LinearLayout itemPickup = view.findViewById(R.id.containerPickup);
        LinearLayout itemTransit = view.findViewById(R.id.containerInTransit);
        LinearLayout itemReview = view.findViewById(R.id.containerReview);

        View.OnClickListener goToOrders = v -> {
            String status = "";

            if (v.getId() == R.id.containerPending) status = "Pending";
            else if (v.getId() == R.id.containerPickup) status = "Pick Up";
            else if (v.getId() == R.id.containerInTransit) status = "In Transit";
            else if (v.getId() == R.id.containerReview) status = "Review";

            OrdersFragment ordersFragment = new OrdersFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedStatus", status);
            ordersFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, ordersFragment)
                    .addToBackStack(null)
                    .commit();
        };

        itemPending.setOnClickListener(goToOrders);
        itemPickup.setOnClickListener(goToOrders);
        itemTransit.setOnClickListener(goToOrders);
        itemReview.setOnClickListener(goToOrders);

        // Handle click for txtChat and icChat to open ChatActivity
        TextView txtChat = view.findViewById(R.id.txtChat);
        ImageView icChat = view.findViewById(R.id.icChat);

        View.OnClickListener openChatActivity = v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            startActivity(intent);
        };

        if (txtChat != null) {
            txtChat.setOnClickListener(openChatActivity);
        }
        if (icChat != null) {
            icChat.setOnClickListener(openChatActivity);
        }

        return view;
    }
    private void applyCustomFonts(View view) {
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtOrders = view.findViewById(R.id.txtOrders);
        TextView txtMyFeatures = view.findViewById(R.id.txtMyFeatures);
        TextView txtSupport = view.findViewById(R.id.txtSupport);
        TextView txtSuggestedForYou = view.findViewById(R.id.txtSuggestedForYou);

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
        if (txtSuggestedForYou != null) {
            FontUtils.setZregularFont(getContext(), txtSuggestedForYou);
        }

        int[] textViewIds = {
                R.id.txtSeeAll,
                R.id.txtViewMore,
                R.id.txtPending,
                R.id.txtPickUp,
                R.id.txtTransit,
                R.id.txtReview,
                R.id.txtWishlist,
                R.id.txtVouchers,
                R.id.txtCoin,
                R.id.txtFlashSale,
                R.id.txtHelpCenter,
                R.id.txtChat,
                R.id.txtEvoCasaBlog,
                R.id.txtLogOut
        };

        for (int id : textViewIds) {
            TextView textView = view.findViewById(id);
            if (textView != null) {
                FontUtils.setRegularFont(getContext(), textView);
            }
        }

    }
}
