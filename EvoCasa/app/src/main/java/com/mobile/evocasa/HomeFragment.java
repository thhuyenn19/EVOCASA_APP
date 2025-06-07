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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mobile.utils.FontUtils;

import com.mobile.adapters.CategoryAdapter;
import com.mobile.adapters.CollectionAdapter;
import com.mobile.adapters.FlashSaleAdapter;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.models.Category;
import com.mobile.models.Collection;
import com.mobile.models.FlashSaleProduct;
import com.mobile.models.HotProducts;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Object> categoryList;
    private CategoryAdapter adapter;
    private View view;
    //       Khi làm phần này nhớ đổi font chử cho logo với search, tham khảo bài cũ hoặc duwosi đâu
//    private void addViews() {
//        txtEvocasa = findViewById(R.id.txtEvocasa);
//        txtHome = findViewById(R.id.txtHome);
//        txtShop = findViewById(R.id.txtShop);
//        txtNotification = findViewById(R.id.txtNotification);
//        txtProfile = findViewById(R.id.txtProfile);
//        edtSearch = findViewById(R.id.edtSearch);
//        imgChat = findViewById(R.id.imgChat);
//        imgCart = findViewById(R.id.imgCart);
//        imgHome = findViewById(R.id.imgHome);
//        imgShop = findViewById(R.id.imgShop);
//        imgNotification = findViewById(R.id.imgNotification);
//        imgProfile = findViewById(R.id.imgProfile);
//        imgMic = findViewById(R.id.imgMic);
//        imgCamera = findViewById(R.id.imgCamera);
//        imgSearch = findViewById(R.id.imgSearch);
//        tabHome = findViewById(R.id.tabHome);
//        tabShop = findViewById(R.id.tabShop);
//        tabNotification = findViewById(R.id.tabNotification);
//        tabProfile = findViewById(R.id.tabProfile);
//
//        // Load custom font từ assets
//
//        Typeface fontTitle = Typeface.createFromAsset(getAssets(), "fonts/ZenOldMincho-Bold.ttf");
//        Typeface fontRegular = Typeface.createFromAsset(getAssets(), "fonts/Inter-Regular.otf");
//
//        // Áp dụng font
//        txtEvocasa.setTypeface(fontTitle);
//        txtHome.setTypeface(fontRegular);
//        txtShop.setTypeface(fontRegular);
//        txtNotification.setTypeface(fontRegular);
//        txtProfile.setTypeface(fontRegular);
//        edtSearch.setTypeface(fontRegular);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        /*Category*/
        // 1. Gán layout cho view
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // Áp dụng font cho các TextView
        applyCustomFonts();

        // 2. Gán RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 3. Khởi tạo danh sách dữ liệu
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        // 4. Gán adapter
        adapter = new CategoryAdapter(categoryList);
        recyclerView.setAdapter(adapter);

        /*FlashSale*/
        RecyclerView recyclerViewFlashSale = view.findViewById(R.id.recyclerViewFlashSale);
        recyclerViewFlashSale.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<FlashSaleProduct> flashSaleList = new ArrayList<>();
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));
        flashSaleList.add(new FlashSaleProduct(R.mipmap.ic_furniture_tevechairs, "Teve Chairs", "$109", "$69", "-37%", 4.8f));

        // Gán adapter
        FlashSaleAdapter flashSaleAdapter = new FlashSaleAdapter(flashSaleList);
        recyclerViewFlashSale.setAdapter(flashSaleAdapter);

        /* Hot Products */
        RecyclerView recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);

        // Set GridLayoutManager để hiển thị 2 cột (2 item mỗi hàng)
        recyclerViewHotProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Tạo danh sách Hot Products
        List<HotProducts> hotProductList = new ArrayList<>();
        hotProductList.add(new HotProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        hotProductList.add(new HotProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        hotProductList.add(new HotProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        hotProductList.add(new HotProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));

        // Gán adapter cho RecyclerView
        HotProductsAdapter hotProductsAdapter = new HotProductsAdapter(hotProductList);
        recyclerViewHotProducts.setAdapter(hotProductsAdapter);

        /*Collection*/
        RecyclerView recyclerViewCollections = view.findViewById(R.id.recyclerViewCollections);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCollections.setLayoutManager(layoutManager);

        List<Collection> collectionList = new ArrayList<>();
        collectionList.add(new Collection(R.mipmap.ic_sabi_collection, "Sabi Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_disc_collection, "The Disc Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_pavillon_collection, "The Pavillon Collection"));
        collectionList.add(new Collection(R.mipmap.ic_the_bromley_collection, "The Bromley Collection"));

        CollectionAdapter collectionAdapter = new CollectionAdapter(collectionList);
        recyclerViewCollections.setAdapter(collectionAdapter);

        return view;
    }

    private void applyCustomFonts() {
        // Áp dụng font Zregular cho tên sản phẩm
        TextView txtProductName = view.findViewById(R.id.txtProductName);
        if (txtProductName != null) {
            FontUtils.setZregularFont(getContext(), txtProductName);
        }

        TextView tvProductName = view.findViewById(R.id.tvProductName);
        if (tvProductName != null) {
            FontUtils.setZregularFont(getContext(), tvProductName);
        }

        // Áp dụng font Regular cho "See All"
        TextView txtSeeAll = view.findViewById(R.id.txtSeeAll);
        if (txtSeeAll != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAll);
        }
        // Áp dụng font Regular cho "See All"
        TextView txtSeeAllCollection = view.findViewById(R.id.txtSeeAllCollection);
        if (txtSeeAllCollection != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAllCollection);
        }

        TextView txtSeeAllHotProducts = view.findViewById(R.id.txtSeeAllHotProducts);
        if (txtSeeAllHotProducts != null) {
            FontUtils.setRegularFont(getContext(), txtSeeAllHotProducts);
        }


        // Áp dụng font Zbold cho tên collection
        TextView txtCollectionName = view.findViewById(R.id.txtCollectionName);
        if (txtCollectionName != null) {
            FontUtils.setZboldFont(getContext(), txtCollectionName);
        }

        // Áp dụng font Zbold cho tất cả TextView còn lại
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
            // Bỏ qua các TextView đã được set font riêng
            if (textView.getId() != R.id.txtProductName && 
                textView.getId() != R.id.tvProductName && 
                textView.getId() != R.id.txtSeeAll && 
                textView.getId() != R.id.txtSeeAllHotProducts &&
                textView.getId() != R.id.txtSeeAllCollection &&
                textView.getId() != R.id.txtCollectionName ) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
}