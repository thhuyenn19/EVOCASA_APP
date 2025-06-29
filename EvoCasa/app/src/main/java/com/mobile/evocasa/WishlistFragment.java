package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.adapters.WishProductAdapter;
import com.mobile.evocasa.category.ProductPreloadManager;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.HotProducts;
import com.mobile.models.ProductItem;
import com.mobile.models.WishProduct;
import com.mobile.utils.BehaviorLogger;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mobile.adapters.MightLikeAdapter;
import com.mobile.models.MightLike;

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
    private String currentCustomerId;
    private String currentFilter = "all";

    private List<MightLike> mightLikeList;
    private MightLikeAdapter mightLikeAdapter;
    private RecyclerView recyclerViewMightLike;

    // Lưu trữ trạng thái sản phẩm cho từng tab
    private Map<String, List<WishProduct>> tabProductsMap = new HashMap<>();
    private boolean isTabDataLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();
        currentCustomerId = new UserSessionManager(getContext()).getUid();

        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishProductList = new ArrayList<>();
        wishProductAdapter = new WishProductAdapter(wishProductList, position -> {
            // Lấy product để remove khỏi Firestore
            WishProduct productToRemove = wishProductList.get(position);

            // Remove từ Firestore trước
            removeProductFromWishlist(productToRemove, () -> {
                // Callback sau khi remove thành công từ Firestore
                // Remove khỏi UI và từ tabProductsMap
                wishProductList.remove(position);
                wishProductAdapter.notifyItemRemoved(position);

                // Update positions cho các item còn lại
                wishProductAdapter.notifyItemRangeChanged(position, wishProductList.size());

                // Cập nhật tabProductsMap để lưu trạng thái
                tabProductsMap.put(currentFilter, new ArrayList<>(wishProductList));

                // Reload Hot Products vì có thể có sản phẩm mới available
                loadMightLike();
            });
        });

        // Add the OnProductItemClickListener to open Product Details Activity
        wishProductAdapter.setOnProductItemClickListener(product -> {
            String uid = new UserSessionManager(requireContext()).getUid(); // hoặc từ SharedPreferences nếu bạn không dùng FirebaseAuth
            String productId = product.getId();
            BehaviorLogger.record(
                    uid,
                    productId,
                    "click",
                    "wishlist_page",
                    null
            );
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());  // Pass the productId to the details activity
            startActivity(intent);
        });
        recyclerViewWishProduct.setAdapter(wishProductAdapter);

        // Khởi tạo tab products map
        initializeTabProductsMap();

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

                currentFilter = filter;
                displayTabProducts(filter);
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

        /* MightLike */
        recyclerViewMightLike = view.findViewById(R.id.recyclerViewHotProducts);
        recyclerViewMightLike.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mightLikeList = new ArrayList<>();
        mightLikeAdapter = new MightLikeAdapter(mightLikeList, (product, position) -> {
            // Thêm vào Wishlist trong Firestore
            addProductToWishlist(product, () -> {
                // Callback sau khi add thành công
                mightLikeList.remove(product);
                mightLikeAdapter.notifyDataSetChanged();

                // Add a new product to replace the removed one
                addNewProductToMightLike();

                // Reset tab data để load lại từ Firestore
                isTabDataLoaded = false;
                initializeTabProductsMap();
            });
        });

        mightLikeAdapter.setOnItemClickListener(product -> {
            String uid = new UserSessionManager(requireContext()).getUid();
            String productId = product.getId();
            BehaviorLogger.record(
                    uid,
                    productId,
                    "click",
                    "wishlist_page",
                    null
            );
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });

        recyclerViewMightLike.setAdapter(mightLikeAdapter);
        loadMightLike();

        return view;
    }

    private void initializeTabProductsMap() {
        if (!isTabDataLoaded) {
            // Load từ cache bằng đa tiến trình
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                List<String> wishlistIds = ProductPreloadManager.getInstance().getCachedWishlist();
                List<ProductItem> allProducts = ProductPreloadManager.getInstance().getShopAllProducts();

                List<WishProduct> allProductsList = new ArrayList<>();
                for (ProductItem item : allProducts) {
                    if (wishlistIds.contains(item.getId())) {
                        WishProduct wish = new WishProduct();
                        wish.setId(item.getId());
                        wish.setName(item.getName());
                        wish.setImage(item.getImage());
                        wish.setPrice(item.getPrice());
//                        wish.setRating(item.getRatings().getAverage());
                        allProductsList.add(wish);
                    }
                }

                Collections.shuffle(allProductsList); // giống load từ Firestore

                handler.post(() -> {
                    // Phân chia như bên Firestore
                    tabProductsMap.put("all", new ArrayList<>(allProductsList));

                    int saleCount = Math.min(4, allProductsList.size());
                    tabProductsMap.put("sale", new ArrayList<>(allProductsList.subList(0, saleCount)));

                    int lowStockStart = Math.min(saleCount, allProductsList.size());
                    int lowStockEnd = Math.min(lowStockStart + 3, allProductsList.size());
                    if (lowStockStart >= allProductsList.size()) {
                        lowStockStart = 0;
                        lowStockEnd = Math.min(3, allProductsList.size());
                    }
                    tabProductsMap.put("lowStock", new ArrayList<>(allProductsList.subList(lowStockStart, lowStockEnd)));

                    int outStart = Math.min(lowStockEnd, allProductsList.size());
                    int outEnd = Math.min(outStart + 2, allProductsList.size());
                    if (outStart >= allProductsList.size()) {
                        outStart = 0;
                        outEnd = Math.min(2, allProductsList.size());
                    }
                    tabProductsMap.put("outOfStock", new ArrayList<>(allProductsList.subList(outStart, outEnd)));

                    isTabDataLoaded = true;
                    displayTabProducts(currentFilter);
                });
            });
        } else {
            displayTabProducts(currentFilter);
        }
    }

    private void displayTabProducts(String filter) {
        wishProductAdapter.setCurrentTab(filter);

        if (tabProductsMap.containsKey(filter)) {
            // Hiển thị từ cache
            wishProductList.clear();
            wishProductList.addAll(tabProductsMap.get(filter));
            wishProductAdapter.notifyDataSetChanged();
        } else {
            // Nếu chưa có data cho tab này, hiển thị rỗng
            wishProductList.clear();
            wishProductAdapter.notifyDataSetChanged();
        }
    }

    private void loadWishProductFromFirestore() {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", currentCustomerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<String> productIds = (List<String>) querySnapshot.getDocuments().get(0).get("Productid");
                        if (productIds == null || productIds.isEmpty()) {
                            // Wishlist trống
                            tabProductsMap.clear();
                            isTabDataLoaded = true;
                            displayTabProducts(currentFilter);
                            return;
                        }

                        // Shuffle để random sản phẩm
                        Collections.shuffle(productIds);

                        // Load tất cả sản phẩm trước
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String productId : productIds) {
                            tasks.add(db.collection("Product").document(productId).get());
                        }

                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    List<WishProduct> allProducts = new ArrayList<>();
                                    for (Object obj : results) {
                                        DocumentSnapshot productDoc = (DocumentSnapshot) obj;
                                        if (productDoc.exists()) {
                                            WishProduct product = productDoc.toObject(WishProduct.class);
                                            if (product != null) {
                                                product.setId(productDoc.getId());
                                                allProducts.add(product);
                                            }
                                        }
                                    }

                                    // Phân chia sản phẩm cho từng tab
                                    tabProductsMap.put("all", new ArrayList<>(allProducts));

                                    // Sale: 4 sản phẩm đầu
                                    int saleCount = Math.min(4, allProducts.size());
                                    tabProductsMap.put("sale", new ArrayList<>(allProducts.subList(0, saleCount)));

                                    // Low Stock: 3 sản phẩm tiếp theo (hoặc từ đầu nếu không đủ)
                                    int lowStockStart = Math.min(saleCount, allProducts.size());
                                    int lowStockEnd = Math.min(lowStockStart + 3, allProducts.size());
                                    if (lowStockStart >= allProducts.size()) {
                                        lowStockStart = 0;
                                        lowStockEnd = Math.min(3, allProducts.size());
                                    }
                                    tabProductsMap.put("lowStock", new ArrayList<>(allProducts.subList(lowStockStart, lowStockEnd)));

                                    // Out of Stock: 2 sản phẩm tiếp theo
                                    int outOfStockStart = Math.min(lowStockEnd, allProducts.size());
                                    int outOfStockEnd = Math.min(outOfStockStart + 2, allProducts.size());
                                    if (outOfStockStart >= allProducts.size()) {
                                        outOfStockStart = 0;
                                        outOfStockEnd = Math.min(2, allProducts.size());
                                    }
                                    tabProductsMap.put("outOfStock", new ArrayList<>(allProducts.subList(outOfStockStart, outOfStockEnd)));

                                    isTabDataLoaded = true;
                                    displayTabProducts(currentFilter);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Wishlist", "Error loading products", e);
                                    tabProductsMap.clear();
                                    isTabDataLoaded = true;
                                    displayTabProducts(currentFilter);
                                });
                    } else {
                        // Không có wishlist nào cho customer này
                        tabProductsMap.clear();
                        isTabDataLoaded = true;
                        displayTabProducts(currentFilter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error loading wishlist", e);
                    tabProductsMap.clear();
                    isTabDataLoaded = true;
                    displayTabProducts(currentFilter);
                });
    }

    private void addProductToWishlist(MightLike product, Runnable onSuccess) {
        String uid = new UserSessionManager(requireContext()).getUid();
        String product_Id = product.getId();
        BehaviorLogger.record(
                uid,
                product_Id,
                "wishlist",
                "wishlist_page",
                null
        );

        // Tìm product ID từ Firestore
        db.collection("Product")
                .whereEqualTo("Name", product.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String productId = querySnapshot.getDocuments().get(0).getId();

                        // Thêm vào Wishlist collection
                        db.collection("Wishlist")
                                .whereEqualTo("Customer_id", currentCustomerId)
                                .get()
                                .addOnSuccessListener(wishlistQuery -> {
                                    if (!wishlistQuery.isEmpty()) {
                                        // Customer đã có wishlist, thêm product vào
                                        String wishlistDocId = wishlistQuery.getDocuments().get(0).getId();
                                        db.collection("Wishlist").document(wishlistDocId)
                                                .update("Productid", FieldValue.arrayUnion(productId))

                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Wishlist", "Product added to existing wishlist");
                                                    ProductPreloadManager.getInstance().addToWishlistCache(productId);
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error adding to wishlist", e);
                                                });
                                    } else {
                                        // Tạo wishlist mới cho customer
                                        List<String> productIds = new ArrayList<>();
                                        productIds.add(productId);

                                        WishlistDocument newWishlist = new WishlistDocument();
                                        newWishlist.setCustomer_id(currentCustomerId);
                                        newWishlist.setProductid(productIds);

                                        db.collection("Wishlist")
                                                .add(newWishlist)
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d("Wishlist", "New wishlist created");
                                                    ProductPreloadManager.getInstance().addToWishlistCache(productId);
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error creating wishlist", e);
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error finding product", e);
                });
    }

    private void removeProductFromWishlist(WishProduct product, Runnable onSuccess) {
        // Tìm product ID từ Firestore
        db.collection("Product")
                .whereEqualTo("Name", product.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String productId = querySnapshot.getDocuments().get(0).getId();

                        // Remove từ Wishlist collection
                        db.collection("Wishlist")
                                .whereEqualTo("Customer_id", currentCustomerId)
                                .get()
                                .addOnSuccessListener(wishlistQuery -> {
                                    if (!wishlistQuery.isEmpty()) {
                                        String wishlistDocId = wishlistQuery.getDocuments().get(0).getId();
                                        db.collection("Wishlist").document(wishlistDocId)
                                                .update("Productid", FieldValue.arrayRemove(productId))
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Wishlist", "Product removed from wishlist");
                                                    ProductPreloadManager.getInstance().removeFromWishlistCache(productId);
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error removing from wishlist", e);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Wishlist", "Error finding wishlist", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error finding product", e);
                });
    }

    private void setActiveTab(TextView selectedTab) {
        for (TextView tab : allTabs) {
            tab.setBackgroundResource(tab == selectedTab ? R.drawable.filter_button_selector_choose : R.drawable.filter_button_selector);
        }
    }

    // Method to load MightLike from Firestore
    private void loadMightLike() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();

                    db.collection("Wishlist")
                            .whereEqualTo("Customer_id", currentCustomerId)
                            .get()
                            .addOnSuccessListener(wishlistQuery -> {
                                List<String> wishlistProductIds = new ArrayList<>();
                                if (!wishlistQuery.isEmpty()) {
                                    List<String> productIds = (List<String>) wishlistQuery.getDocuments().get(0).get("Productid");
                                    if (productIds != null) {
                                        wishlistProductIds = productIds;
                                    }
                                }

                                List<DocumentSnapshot> availableDocs = new ArrayList<>();
                                for (DocumentSnapshot doc : allDocs) {
                                    if (!wishlistProductIds.contains(doc.getId())) {
                                        availableDocs.add(doc);
                                    }
                                }

                                mightLikeList.clear();
                                Collections.shuffle(availableDocs);

                                int limit = Math.min(4, availableDocs.size());
                                for (int i = 0; i < limit; i++) {
                                    MightLike product = availableDocs.get(i).toObject(MightLike.class);
                                    if (product != null) {
                                        product.setId(availableDocs.get(i).getId());
                                        mightLikeList.add(product);
                                    }
                                }

                                mightLikeAdapter.notifyDataSetChanged();
                                recyclerViewMightLike.setVisibility(mightLikeList.isEmpty() ? View.GONE : View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error loading wishlist for mightlike", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading MightLike", e);
                });
    }

    // Method to add a new product to MightLike after one is removed
    private void addNewProductToMightLike() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();

                    db.collection("Wishlist")
                            .whereEqualTo("Customer_id", currentCustomerId)
                            .get()
                            .addOnSuccessListener(wishlistQuery -> {
                                List<String> wishlistProductIds = new ArrayList<>();
                                if (!wishlistQuery.isEmpty()) {
                                    List<String> productIds = (List<String>) wishlistQuery.getDocuments().get(0).get("Productid");
                                    if (productIds != null) {
                                        wishlistProductIds = productIds;
                                    }
                                }

                                // List of products that are already in MightLike and Wishlist
                                List<String> allExcludedIds = new ArrayList<>(wishlistProductIds);
                                for (MightLike product : mightLikeList) {
                                    allExcludedIds.add(product.getId());
                                }

                                List<DocumentSnapshot> availableDocs = new ArrayList<>();
                                for (DocumentSnapshot doc : allDocs) {
                                    if (!allExcludedIds.contains(doc.getId())) {
                                        availableDocs.add(doc);
                                    }
                                }

                                if (!availableDocs.isEmpty()) {
                                    MightLike newProduct = availableDocs.get(0).toObject(MightLike.class);
                                    if (newProduct != null) {
                                        newProduct.setId(availableDocs.get(0).getId());
                                        mightLikeList.add(newProduct);
                                    }
                                }

                                mightLikeAdapter.notifyDataSetChanged();
                                recyclerViewMightLike.setVisibility(mightLikeList.isEmpty() ? View.GONE : View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error loading wishlist for mightlike", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading MightLike", e);
                });
    }



    // Helper class cho Wishlist document structure
    public static class WishlistDocument {
        private String Customer_id;
        private List<String> Productid;

        public WishlistDocument() {}

        public String getCustomer_id() { return Customer_id; }
        public void setCustomer_id(String customer_id) { Customer_id = customer_id; }

        public List<String> getProductid() { return Productid; }
        public void setProductid(List<String> productid) { Productid = productid; }
    }
}