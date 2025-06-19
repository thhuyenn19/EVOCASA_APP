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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.adapters.WishProductAdapter;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.HotProducts;
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
    private List<HotProducts> hotProductList;
    private HotProductsAdapter hotProductsAdapter;
    private RecyclerView recyclerViewHotProducts;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishProductList = new ArrayList<>();
        wishProductAdapter = new WishProductAdapter(wishProductList, position -> {
            wishProductList.remove(position);
            wishProductAdapter.notifyItemRemoved(position);
            String customerId = new UserSessionManager(getContext()).getUid();
            db.collection("wishlist").document(customerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> products = (List<Map<String, Object>>) documentSnapshot.get("products");
                            if (products != null) {
                                products.remove(position);
                                db.collection("wishlist").document(customerId)
                                        .update("products", products);
                            }
                        }
                    });
        });

        recyclerViewWishProduct.setAdapter(wishProductAdapter);
        loadWishProduct("all");

        TextView txtViewRcm = view.findViewById(R.id.txtViewRcm);
        FontUtils.setZboldFont(requireContext(), txtViewRcm);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

        btnAll = view.findViewById(R.id.btnAll);
        btnSale = view.findViewById(R.id.btnSale);
        btnLowStock = view.findViewById(R.id.btnLowStock);
        btnOutOfStock = view.findViewById(R.id.btnOutOfStock);

        FontUtils.setMediumFont(requireContext(), btnAll);
        FontUtils.setMediumFont(requireContext(), btnSale);
        FontUtils.setMediumFont(requireContext(), btnLowStock);
        FontUtils.setMediumFont(requireContext(), btnOutOfStock);

        allTabs = Arrays.asList(btnAll, btnSale, btnLowStock, btnOutOfStock);
        setActiveTab(btnAll);

        for (TextView tab : allTabs) {
            tab.setOnClickListener(v -> {
                setActiveTab(tab);
                String filter = "all";
                if (tab == btnSale) filter = "sale";
                else if (tab == btnLowStock) filter = "lowStock";
                else if (tab == btnOutOfStock) filter = "outOfStock";
                loadWishProduct(filter);
            });
        }

        imgWishlistBack = view.findViewById(R.id.imgWishlistBack);
        imgWishlistBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        /* Hot Products */
        recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);
        recyclerViewHotProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        hotProductList = new ArrayList<>();
        hotProductsAdapter = new HotProductsAdapter(hotProductList, (product, position) -> {
            // Tạo và thêm vào Wishlist
            WishProduct wish = new WishProduct();
            wish.setImage(product.getImage());
            wish.setRating(product.getRating());
            wish.setName(product.getName());
            wish.setPrice(product.getPrice());

            wishProductList.add(0, wish);
            wishProductAdapter.notifyItemInserted(0);

            // ❗ Sửa ở đây
            hotProductList.remove(product); // thay vì remove(position)
            hotProductsAdapter.notifyDataSetChanged();

            if (hotProductList.isEmpty()) {
                recyclerViewHotProducts.setVisibility(View.GONE);
            }
        });


        recyclerViewHotProducts.setAdapter(hotProductsAdapter);

        // Gọi hàm load từ Firestore
        loadHotProducts();

        return view;
    }

    private void loadWishProduct(String filter) {
        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setAdapter(wishProductAdapter);
        wishProductList.clear();
        wishProductAdapter.setCurrentTab(filter);

        String customerId = new UserSessionManager(getContext()).getUid();
        db.collection("Wishlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean found = false;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String firestoreCustomerId = doc.getString("Customer_id");
                        if (firestoreCustomerId != null && firestoreCustomerId.equals(customerId)) {
                            found = true;
                            List<String> productIds = (List<String>) doc.get("Productid");
                            if (productIds == null || productIds.isEmpty()) {
                                wishProductAdapter.notifyDataSetChanged();
                                return;
                            }
                            Collections.shuffle(productIds);

                            int maxItems;
                            switch (filter) {
                                case "lowStock":
                                    maxItems = 3;
                                    break;
                                case "outOfStock":
                                    maxItems = 2;
                                    break;
                                case "sale":
                                    maxItems = 4;
                                    break;
                                default:
                                    maxItems = productIds.size(); // load tất cả
                                    break;
                            }

                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (String productId : productIds) {
                                tasks.add(db.collection("Product").document(productId).get());
                            }

                            int finalMaxItems = maxItems;
                            Tasks.whenAllSuccess(tasks)
                                    .addOnSuccessListener(results -> {
                                        List<WishProduct> allProducts = new ArrayList<>();
                                        for (Object obj : results) {
                                            DocumentSnapshot productDoc = (DocumentSnapshot) obj;
                                            if (productDoc.exists()) {
                                                WishProduct product = productDoc.toObject(WishProduct.class);
                                                allProducts.add(product);
                                            }
                                        }
                                        List<WishProduct> limitedList = new ArrayList<>();
                                        for (int i = 0; i < Math.min(finalMaxItems, allProducts.size()); i++) {
                                            limitedList.add(allProducts.get(i));
                                        }
                                        wishProductList.clear();
                                        wishProductList.addAll(limitedList);
                                        wishProductAdapter.notifyDataSetChanged();
                                    });
                            break;
                        }
                    }
                    if (!found) wishProductAdapter.notifyDataSetChanged();
                });
    }

    private void setActiveTab(TextView selectedTab) {
        for (TextView tab : allTabs) {
            tab.setBackgroundResource(tab == selectedTab ? R.drawable.filter_button_selector_choose : R.drawable.filter_button_selector);
        }
    }

    private void loadHotProducts() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    hotProductList.clear();

                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();
                    Collections.shuffle(allDocs); // 🔀 random

                    int limit = Math.min(4, allDocs.size()); // lấy 6 sản phẩm
                    for (int i = 0; i < limit; i++) {
                        HotProducts product = allDocs.get(i).toObject(HotProducts.class);
                        hotProductList.add(product);
                    }

                    hotProductsAdapter.notifyDataSetChanged();

                    // ✅ Hiện lại nếu nó từng bị ẩn
                    RecyclerView recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);
                    recyclerViewHotProducts.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
                });
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