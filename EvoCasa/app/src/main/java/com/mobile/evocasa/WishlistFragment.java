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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.WishProductAdapter;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.WishProduct;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;

    private TextView btnAll, btnSale, btnLowStock, btnOutOfStock;
    private List<TextView> allTabs;

    private ImageView imgWishlistBack;

    private WishProductAdapter wishProductAdapter;
    private FirebaseFirestore db;
    private List<WishProduct> wishProductList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();

        // Khởi tạo RecyclerView và Adapter
        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishProductList = new ArrayList<>();
        wishProductAdapter = new WishProductAdapter(wishProductList, position -> {
            // Khi click vào sản phẩm (hoặc icon yêu thích), xóa sản phẩm khỏi RecyclerView
            wishProductList.remove(position);
            wishProductAdapter.notifyItemRemoved(position);

            // Cập nhật Firestore nếu cần (xóa sản phẩm khỏi wishlist)
            String customerId = new UserSessionManager(getContext()).getUid(); // Lấy customerId từ session
            db.collection("wishlist").document(customerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Giả sử bạn lưu trữ các sản phẩm trực tiếp, không phải product_id
                            List<Map<String, Object>> products = (List<Map<String, Object>>) documentSnapshot.get("products");
                            // Tìm sản phẩm và xóa khỏi danh sách
                            if (products != null) {
                                products.remove(position); // Xóa sản phẩm từ danh sách
                                db.collection("wishlist").document(customerId)
                                        .update("products", products) // Cập nhật lại danh sách sản phẩm trong Firestore
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Wishlist", "Product removed from wishlist in Firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Wishlist", "Error removing product from wishlist in Firestore", e);
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Wishlist", "Error fetching wishlist data", e);
                    });
        });

        recyclerViewWishProduct.setAdapter(wishProductAdapter);

        // Gọi hàm load từ Firestore, mặc định sẽ lấy tất cả sản phẩm
        loadWishProduct("all"); // Mặc định hiển thị tất cả sản phẩm

        // Set font cho các textView
        TextView txtViewRcm = view.findViewById(R.id.txtViewRcm);
        FontUtils.setZboldFont(requireContext(), txtViewRcm);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);
        TextView tvSortBy = view.findViewById(R.id.tvSortBy);
        FontUtils.setMediumFont(requireContext(), tvSortBy);

        // Các button lọc
        btnAll = view.findViewById(R.id.btnAll);
        FontUtils.setMediumFont(requireContext(), btnAll);
        btnSale = view.findViewById(R.id.btnSale);
        FontUtils.setMediumFont(requireContext(), btnSale);
        btnLowStock = view.findViewById(R.id.btnLowStock);
        FontUtils.setMediumFont(requireContext(), btnLowStock);
        btnOutOfStock = view.findViewById(R.id.btnOutOfStock);
        FontUtils.setMediumFont(requireContext(), btnOutOfStock);

        // Danh sách tất cả tab
        allTabs = Arrays.asList(btnAll, btnSale, btnLowStock, btnOutOfStock);

        // Chọn mặc định tab All
        setActiveTab(btnAll);

        // Gán sự kiện cho các tab
        for (TextView tab : allTabs) {
            tab.setOnClickListener(v -> {
                setActiveTab(tab);
                // Lọc theo từng loại sản phẩm
                if (tab == btnAll) {
                    loadWishProduct("all");
                } else if (tab == btnSale) {
                    loadWishProduct("sale");
                } else if (tab == btnLowStock) {
                    loadWishProduct("lowStock");
                } else if (tab == btnOutOfStock) {
                    loadWishProduct("outOfStock");
                }
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

    // Giả lập load sản phẩm cho các tab
    private void loadWishProduct(String filter) {
        wishProductList.clear(); // Xóa sản phẩm cũ

        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
                    Collections.shuffle(allDocs); // 🔀 random

                    List<WishProduct> filteredProducts = new ArrayList<>();
                    int limit = Math.min(6, allDocs.size()); // Hiển thị tối đa 6 sản phẩm

                    // Lọc sản phẩm theo từng tab
                    switch (filter) {
                        case "sale":
                            // Giả lập sản phẩm sale (giảm giá)
                            for (int i = 0; i < 4; i++) { // Hiển thị 4 sản phẩm giả lập giảm giá
                                WishProduct product = allDocs.get(i).toObject(WishProduct.class);
                                filteredProducts.add(product);
                            }
                            break;

                        case "lowStock":
                            // Giả lập sản phẩm còn ít trong kho
                            for (int i = 0; i < 3; i++) { // Hiển thị 3 sản phẩm giả lập còn ít trong kho
                                WishProduct product = allDocs.get(i).toObject(WishProduct.class);
                                filteredProducts.add(product);
                            }
                            break;

                        case "outOfStock":
                            // Giả lập sản phẩm hết hàng
                            for (int i = 0; i < 2; i++) { // Hiển thị 2 sản phẩm giả lập hết hàng
                                WishProduct product = allDocs.get(i).toObject(WishProduct.class);
                                filteredProducts.add(product);
                            }
                            break;

                        case "all":
                        default:
                            // Hiển thị tất cả sản phẩm
                            for (int i = 0; i < 6; i++) { // Hiển thị 6 sản phẩm giả lập
                                WishProduct product = allDocs.get(i).toObject(WishProduct.class);
                                filteredProducts.add(product);
                            }
                            break;
                    }

                    // Cập nhật dữ liệu vào RecyclerView
                    wishProductList.addAll(filteredProducts);
                    wishProductAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load sản phẩm", e);
                });
    }

    // Thay đổi background cho các tab được chọn
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







//package com.mobile.evocasa;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.mobile.adapters.HotProductsAdapter;
//import com.mobile.adapters.WishProductAdapter;
//import com.mobile.evocasa.profile.ProfileFragment;
//import com.mobile.models.HotProducts;
//import com.mobile.models.WishProduct;
//import com.mobile.utils.FontUtils;
//import com.mobile.utils.UserSessionManager;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
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
//    private WishProductAdapter wishProductAdapter;
//    private FirebaseFirestore db;
//    private List<Integer> imageList;
//    private List<WishProduct> wishProductList;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        view = inflater.inflate(R.layout.fragment_wishlist, container, false);
//
//        db = FirebaseFirestore.getInstance();
//
//        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
//        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
//
//        wishProductList = new ArrayList<>();
//        wishProductAdapter = new WishProductAdapter(wishProductList, position -> {
//            // Khi click vào sản phẩm (hoặc icon yêu thích), xóa sản phẩm khỏi RecyclerView
//            wishProductList.remove(position);
//            wishProductAdapter.notifyItemRemoved(position);
//
//            // Cập nhật Firestore nếu cần (xóa sản phẩm khỏi wishlist)
//            // Đây là nơi bạn có thể xử lý Firestore nếu bạn lưu sản phẩm theo cách khác
//            String customerId = new UserSessionManager(getContext()).getUid(); // Lấy customerId từ session
//            db.collection("wishlist").document(customerId)
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            // Giả sử bạn lưu trữ các sản phẩm trực tiếp, không phải product_id
//                            List<Map<String, Object>> products = (List<Map<String, Object>>) documentSnapshot.get("products");
//                            // Tìm sản phẩm và xóa khỏi danh sách
//                            if (products != null) {
//                                products.remove(position); // Xóa sản phẩm từ danh sách
//                                db.collection("wishlist").document(customerId)
//                                        .update("products", products) // Cập nhật lại danh sách sản phẩm trong Firestore
//                                        .addOnSuccessListener(aVoid -> {
//                                            Log.d("Wishlist", "Product removed from wishlist in Firestore");
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            Log.e("Wishlist", "Error removing product from wishlist in Firestore", e);
//                                        });
//                            }
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("Wishlist", "Error fetching wishlist data", e);
//                    });
//        });
//
//        recyclerViewWishProduct.setAdapter(wishProductAdapter);
//
//        // Gọi hàm load từ Firestore
//        loadWishProduct();
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
//    private void loadWishProduct() {
//        db.collection("Product")
//                .get()
//                .addOnSuccessListener(querySnapshots -> {
//                    wishProductList.clear();
//
//                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
//                    Collections.shuffle(allDocs); // 🔀 random
//
//                    int limit = Math.min(6, allDocs.size()); // lấy 6 sản phẩm
//                    for (int i = 0; i < limit; i++) {
//                        WishProduct product = allDocs.get(i).toObject(WishProduct.class);
//                        wishProductList.add(product);
//                    }
//
//                    wishProductAdapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
//                });
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