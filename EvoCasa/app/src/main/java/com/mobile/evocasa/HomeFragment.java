package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mobile.adapters.CategoryAdapter;
import com.mobile.utils.FontUtils;

import com.mobile.adapters.BannerPagerAdapter;
import com.mobile.adapters.CollectionAdapter;
import com.mobile.adapters.FlashSaleAdapter;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.models.Category;
import com.mobile.models.Collection;
import com.mobile.models.FlashSaleProduct;
import com.mobile.models.HotProducts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView recyclerViewCategories;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private List<HotProducts> hotProductList;
    private HotProductsAdapter hotProductsAdapter;

    private View view;
    private List<Integer> imageList;

    private ImageView imgCart;

    private ViewPager2 viewPagerBanner;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (viewPagerBanner.getCurrentItem() + 1) % imageList.size();
                viewPagerBanner.setCurrentItem(nextItem, false); // <--- Tắt hiệu ứng trượt ngang
                sliderHandler.postDelayed(this, 4000);
            }
        };


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

        // 3. Danh sách dữ liệu
        db = FirebaseFirestore.getInstance();
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        loadCategories(); // gọi hàm để lấy dữ liệu từ Firestore

        // Lấy giờ hiện tại
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

// Thiết lập khung giờ Flash Sale (ví dụ 9h – 21h)
        boolean isFlashSaleTime = (hour >= 9 && hour < 2);

        /* FlashSale */
        RecyclerView recyclerViewFlashSale = view.findViewById(R.id.recyclerViewFlashSale);
        recyclerViewFlashSale.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<FlashSaleProduct> flashSaleList = new ArrayList<>();
        FlashSaleAdapter flashSaleAdapter = new FlashSaleAdapter(flashSaleList);

        recyclerViewFlashSale.setAdapter(flashSaleAdapter);

// Firestore instance
        db = FirebaseFirestore.getInstance();

/*
// Kiểm tra khung giờ
        if (isFlashSaleTime) {
            db.collection("Product").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                        Collections.shuffle(allDocs); // 🔀 Random

                        flashSaleList.clear();
                        for (int i = 0; i < Math.min(6, allDocs.size()); i++) {
                            FlashSaleProduct product = allDocs.get(i).toObject(FlashSaleProduct.class);
                            flashSaleList.add(product);

                        }

                        flashSaleAdapter.notifyDataSetChanged();
                        recyclerViewFlashSale.setVisibility(View.VISIBLE); // hiện nếu có dữ liệu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi khi tải Flash Sale", Toast.LENGTH_SHORT).show();
                        recyclerViewFlashSale.setVisibility(View.GONE); // ẩn nếu lỗi
                    });
        } else {
            // Ẩn RecyclerView nếu chưa đến giờ
            recyclerViewFlashSale.setVisibility(View.GONE);
        }
*/
        db.collection("Product").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> allDocs = queryDocumentSnapshots.getDocuments();
                    Collections.shuffle(allDocs);

                    flashSaleList.clear();
                    for (int i = 0; i < Math.min(6, allDocs.size()); i++) {
                        FlashSaleProduct product = allDocs.get(i).toObject(FlashSaleProduct.class);
                        flashSaleList.add(product);
                        Log.d("FLASH_SALE", "Đã add sản phẩm: " + product.getName());
                    }


                    flashSaleAdapter.notifyDataSetChanged(); // Phải có dòng này để hiển thị
                    recyclerViewFlashSale.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải Flash Sale", Toast.LENGTH_SHORT).show();
                    recyclerViewFlashSale.setVisibility(View.GONE);
                });



        /* Hot Products */
        RecyclerView recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);
        recyclerViewHotProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        hotProductList = new ArrayList<>();
        hotProductsAdapter = new HotProductsAdapter(hotProductList);
        recyclerViewHotProducts.setAdapter(hotProductsAdapter);

        // Gọi hàm load từ Firestore
        loadHotProducts();


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

        // Khởi tạo ViewPager2 cho Banner
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        imageList = new ArrayList<>();
        imageList.add(R.mipmap.ic_banner);
        imageList.add(R.mipmap.ic_banner2);
        imageList.add(R.mipmap.ic_banner3);
        // Thêm các hình ảnh khác vào đây nếu có

        BannerPagerAdapter bannerAdapter = new BannerPagerAdapter(imageList);
        viewPagerBanner.setAdapter(bannerAdapter);

// Tạo hiệu ứng fade mượt + triệt tiêu hiệu ứng slide ngang
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            // Giữ ảnh đứng yên tại chỗ, không trượt ngang
            page.setTranslationX(0);

            // Fade in – fade out hiệu ứng
            float alpha = 1 - Math.abs(position);
            page.setAlpha(alpha);
        });
        viewPagerBanner.setPageTransformer(transformer);

        viewPagerBanner.setPageTransformer(transformer);

// Bắt đầu tự động trượt
        sliderHandler.postDelayed(sliderRunnable, 4000);


        // Bắt sự kiện click giỏ hàng (imgCart) => Mở Cart Product
        ImageView imgCart = view.findViewById(R.id.imgCart);
        if (imgCart != null) {
            imgCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Mở CartActivity
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);
    }

    private void applyCustomFonts() {
        // Áp dụng font Zregular cho tên sản phẩm
        TextView txtProductName = view.findViewById(R.id.txtProductName);
        if (txtProductName != null) {
            FontUtils.setZregularFont(getContext(), txtProductName);
        }
        EditText edtSearch = view.findViewById(R.id.edtSearch);
        if (txtProductName != null) {
            FontUtils.setRegularFont(getContext(), edtSearch);
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
        } else if (view instanceof TextView && !(view instanceof EditText)) {
            TextView textView = (TextView) view;
            int id = textView.getId();
            // Bỏ qua các TextView đã được set font riêng
            if (id != R.id.txtProductName &&
                    id != R.id.tvProductName &&
                    id != R.id.txtSeeAll &&
                    id != R.id.txtSeeAllHotProducts &&
                    id != R.id.txtSeeAllCollection &&
                    id != R.id.txtCollectionName &&
                    id != R.id.edtSearch) { // Bỏ qua EditText nếu dùng TextView làm base
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
    private void loadCategories() {
        db.collection("Category")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    categoryList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Category category = doc.toObject(Category.class);
                        categoryList.add(category);
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load danh mục", e);
                });
    }
    private void loadHotProducts() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    hotProductList.clear();

                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
                    Collections.shuffle(allDocs); // 🔀 random

                    int limit = Math.min(6, allDocs.size()); // lấy 6 sản phẩm
                    for (int i = 0; i < limit; i++) {
                        HotProducts product = allDocs.get(i).toObject(HotProducts.class);
                        hotProductList.add(product);
                    }

                    hotProductsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
                });
    }


}