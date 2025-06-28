package com.mobile.evocasa.category;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.models.ProductItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductPreloadManager {
    private static final String TAG = "ProductPreloadManager";
    private static ProductPreloadManager instance;

    private final ExecutorService preloadExecutor;
    private final FirebaseFirestore db;

    // Cache cho products theo category
    private final ConcurrentHashMap<String, List<ProductItem>> categoryProductsCache;
    // Cache cho subcategories theo category
    private final ConcurrentHashMap<String, Map<String, String>> categorySubCategoriesCache;
    // Cache cho products theo subcategory
    private final ConcurrentHashMap<String, List<ProductItem>> subCategoryProductsCache;
    // Cache cho tất cả products (Shop All)
    private List<ProductItem> allProductsCache;

    // Trạng thái loading
    private final ConcurrentHashMap<String, Boolean> loadingStatus;

    // Callbacks cho khi preload hoàn thành
    private final ConcurrentHashMap<String, List<PreloadCallback>> preloadCallbacks;

    public void preloadAllCategoryDataBlocking() {
        Log.d(TAG, "🔁 Blocking preload all category data...");

        try {
            // Lấy toàn bộ Category
            CompletableFuture<Void> future = new CompletableFuture<>();

            db.collection("Category").get()
                    .addOnSuccessListener(querySnapshot -> {
                        Map<String, String> mainCategories = new HashMap<>();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Object parentCategoryObj = doc.get("ParentCategory");
                            String parentCategoryId = extractParentCategoryId(parentCategoryObj);

                            // Đây là category cha nếu ParentCategory == null
                            if (parentCategoryId == null) {
                                String name = doc.getString("Name");
                                String id = doc.getId();
                                if (name != null && id != null) {
                                    mainCategories.put(name, id);
                                }
                            }
                        }

                        // Gọi preload async cho từng main category và chờ hoàn tất
                        List<CompletableFuture<Void>> futures = new ArrayList<>();

                        for (Map.Entry<String, String> entry : mainCategories.entrySet()) {
                            String categoryName = entry.getKey();
                            String categoryId = entry.getValue();

                            CompletableFuture<Void> catFuture = new CompletableFuture<>();
                            futures.add(catFuture);

                            preloadCategoryDataAsync(categoryName, categoryId, new PreloadCallback() {
                                @Override
                                public void onPreloadComplete(String catName) {
                                    catFuture.complete(null);
                                }

                                @Override
                                public void onPreloadError(String catName, Exception error) {
                                    Log.e(TAG, "❌ Preload failed for " + catName, error);
                                    catFuture.complete(null); // vẫn cho qua lỗi để không block
                                }
                            });
                        }

                        // Khi tất cả category preload xong
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                .thenRun(() -> {
                                    Log.d(TAG, "✅ All categories preloaded (blocking)");
                                    future.complete(null);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to load categories for blocking preload", e);
                        future.complete(null);
                    });

            // Block thread hiện tại (MainActivity gọi bên thread riêng)
            future.get(); // Wait until complete

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception in preloadAllCategoryDataBlocking", e);
        }
    }

    public interface PreloadCallback {
        void onPreloadComplete(String categoryName);
        void onPreloadError(String categoryName, Exception error);
    }

    private ProductPreloadManager() {
        this.preloadExecutor = Executors.newFixedThreadPool(4);
        this.db = FirebaseFirestore.getInstance();
        this.categoryProductsCache = new ConcurrentHashMap<>();
        this.categorySubCategoriesCache = new ConcurrentHashMap<>();
        this.subCategoryProductsCache = new ConcurrentHashMap<>();
        this.loadingStatus = new ConcurrentHashMap<>();
        this.preloadCallbacks = new ConcurrentHashMap<>();
        this.allProductsCache = new ArrayList<>();
    }

    public static synchronized ProductPreloadManager getInstance() {
        if (instance == null) {
            instance = new ProductPreloadManager();
        }
        return instance;
    }

    /**
     * Preload tất cả dữ liệu cần thiết cho categories
     */
    public void preloadAllCategoryData(Map<String, String> categoryNameToIdMap) {
        Log.d(TAG, "Starting preload for all category data");

        // Preload Shop All products first (priority)
        preloadShopAllProducts();

        // Preload cho từng category
        for (Map.Entry<String, String> entry : categoryNameToIdMap.entrySet()) {
            String categoryName = entry.getKey();
            String categoryId = entry.getValue();
            preloadCategoryDataAsync(categoryName, categoryId);
        }
    }

    /**
     * Preload dữ liệu cho một category cụ thể với callback
     */
    public void preloadCategoryDataAsync(String categoryName, String categoryId) {
        preloadCategoryDataAsync(categoryName, categoryId, null);
    }

    public void preloadCategoryDataAsync(String categoryName, String categoryId, PreloadCallback callback) {
        String cacheKey = categoryName.toLowerCase();

        // Thêm callback vào danh sách
        if (callback != null) {
            preloadCallbacks.computeIfAbsent(cacheKey, k -> new ArrayList<>()).add(callback);
        }

        // Kiểm tra xem đã preload chưa
        if (isCategoryCached(categoryName)) {
            Log.d(TAG, "Category " + categoryName + " already preloaded");
            notifyCallbacks(cacheKey, false, null);
            return;
        }

        // Kiểm tra trạng thái loading
        if (loadingStatus.getOrDefault(cacheKey, false)) {
            Log.d(TAG, "Category " + categoryName + " is already being loaded");
            return;
        }

        loadingStatus.put(cacheKey, true);

        preloadExecutor.execute(() -> {
            try {
                Log.d(TAG, "Preloading category: " + categoryName + " with ID: " + categoryId);

                // FIXED: Preload subcategories FIRST, then products
                preloadSubCategoriesAsync(categoryName, categoryId, () -> {
                    // After subcategories are loaded, load category products
                    preloadCategoryProductsAsync(categoryName, categoryId, () -> {
                        loadingStatus.put(cacheKey, false);
                        notifyCallbacks(cacheKey, false, null);
                        Log.d(TAG, "Completed preloading for category: " + categoryName);
                    });
                });

            } catch (Exception e) {
                Log.e(TAG, "Error preloading category: " + categoryName, e);
                loadingStatus.put(cacheKey, false);
                notifyCallbacks(cacheKey, true, e);
            }
        });
    }

    private void notifyCallbacks(String cacheKey, boolean isError, Exception error) {
        List<PreloadCallback> callbacks = preloadCallbacks.get(cacheKey);
        if (callbacks != null) {
            for (PreloadCallback callback : callbacks) {
                try {
                    if (isError) {
                        callback.onPreloadError(cacheKey, error);
                    } else {
                        callback.onPreloadComplete(cacheKey);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in callback", e);
                }
            }
            // Clear callbacks sau khi notify
            callbacks.clear();
        }
    }

    /**
     * Preload Shop All products với priority cao
     */
    private void preloadShopAllProducts() {
        if (!allProductsCache.isEmpty()) {
            Log.d(TAG, "Shop All products already cached");
            return;
        }

        if (loadingStatus.getOrDefault("shop_all", false)) {
            Log.d(TAG, "Shop All products already being loaded");
            return;
        }

        loadingStatus.put("shop_all", true);

        preloadExecutor.execute(() -> {
            try {
                Log.d(TAG, "Preloading Shop All products");

                db.collection("Product")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<ProductItem> products = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                try {
                                    ProductItem product = parseProductFromDocument(doc);
                                    if (product != null) {
                                        products.add(product);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing product: " + doc.getId(), e);
                                }
                            }

                            synchronized (allProductsCache) {
                                allProductsCache.clear();
                                allProductsCache.addAll(products);
                            }

                            loadingStatus.put("shop_all", false);
                            Log.d(TAG, "Cached " + products.size() + " products for Shop All");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to preload Shop All products", e);
                            loadingStatus.put("shop_all", false);
                        });

            } catch (Exception e) {
                Log.e(TAG, "Error in preloadShopAllProducts", e);
                loadingStatus.put("shop_all", false);
            }
        });
    }

    /**
     * Preload subcategories cho một category với callback
     */
    private void preloadSubCategoriesAsync(String categoryName, String categoryId, Runnable onComplete) {
        try {
            db.collection("Category")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Map<String, String> subCategoryIds = new HashMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object parentCategoryObj = doc.get("ParentCategory");
                            String parentCategoryId = extractParentCategoryId(parentCategoryObj);

                            if (parentCategoryId != null && parentCategoryId.equals(categoryId)) {
                                String name = doc.getString("Name");
                                String id = doc.getId();
                                if (name != null) {
                                    subCategoryIds.put(name, id);

                                    // Preload products cho subcategory này async
                                    preloadSubCategoryProducts(name, id);
                                }
                            }
                        }

                        // FIXED: Save subcategories to cache BEFORE completing
                        categorySubCategoriesCache.put(categoryName.toLowerCase(), subCategoryIds);
                        Log.d(TAG, "Cached " + subCategoryIds.size() + " subcategories for " + categoryName);

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to preload subcategories for " + categoryName, e);
                        // Even if subcategories fail, save empty map and continue
                        categorySubCategoriesCache.put(categoryName.toLowerCase(), new HashMap<>());
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preloading subcategories for " + categoryName, e);
            categorySubCategoriesCache.put(categoryName.toLowerCase(), new HashMap<>());
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * FIXED: Preload tất cả products cho một category với callback
     */
    private void preloadCategoryProductsAsync(String categoryName, String categoryId, Runnable onComplete) {
        try {
            // Get subcategories from cache (should be available now)
            Map<String, String> subCategoryIds = categorySubCategoriesCache.get(categoryName.toLowerCase());

            if (subCategoryIds == null || subCategoryIds.isEmpty()) {
                Log.w(TAG, "No subcategories found for " + categoryName + ", skipping category products preload");
                categoryProductsCache.put(categoryName.toLowerCase(), new ArrayList<>());
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            Log.d(TAG, "Preloading products for category: " + categoryName + " with " + subCategoryIds.size() + " subcategories");

            db.collection("Product")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<ProductItem> categoryProducts = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> categoryIdObj = (Map<String, Object>) doc.get("category_id");
                            String productCategoryId = categoryIdObj != null ? (String) categoryIdObj.get("$oid") : null;

                            if (productCategoryId != null && subCategoryIds.containsValue(productCategoryId)) {
                                try {
                                    ProductItem product = parseProductFromDocument(doc);
                                    if (product != null) {
                                        categoryProducts.add(product);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing product: " + doc.getId(), e);
                                }
                            }
                        }

                        categoryProductsCache.put(categoryName.toLowerCase(), categoryProducts);
                        Log.d(TAG, "Cached " + categoryProducts.size() + " products for category " + categoryName);

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to preload products for category " + categoryName, e);
                        categoryProductsCache.put(categoryName.toLowerCase(), new ArrayList<>());
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error preloading category products for " + categoryName, e);
            categoryProductsCache.put(categoryName.toLowerCase(), new ArrayList<>());
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Preload products cho một subcategory cụ thể
     */
    private void preloadSubCategoryProducts(String subCategoryName, String subCategoryId) {
        preloadExecutor.execute(() -> {
            try {
                db.collection("Product")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<ProductItem> subCategoryProducts = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Map<String, Object> categoryIdObj = (Map<String, Object>) doc.get("category_id");
                                String productCategoryId = categoryIdObj != null ? (String) categoryIdObj.get("$oid") : null;

                                if (productCategoryId != null && productCategoryId.equals(subCategoryId)) {
                                    try {
                                        ProductItem product = parseProductFromDocument(doc);
                                        if (product != null) {
                                            subCategoryProducts.add(product);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing product: " + doc.getId(), e);
                                    }
                                }
                            }

                            subCategoryProductsCache.put(subCategoryName, subCategoryProducts);
                            Log.d(TAG, "Cached " + subCategoryProducts.size() + " products for subcategory " + subCategoryName);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to preload products for subcategory " + subCategoryName, e);
                        });

            } catch (Exception e) {
                Log.e(TAG, "Error preloading subcategory products for " + subCategoryName, e);
            }
        });
    }

    // ===== GETTER METHODS =====

    /**
     * Lấy Shop All products từ cache
     */
    public List<ProductItem> getShopAllProducts() {
        synchronized (allProductsCache) {
            return new ArrayList<>(allProductsCache);
        }
    }

    /**
     * Lấy subcategories cho một category từ cache
     */
    public Map<String, String> getSubCategories(String categoryName) {
        Map<String, String> cached = categorySubCategoriesCache.get(categoryName.toLowerCase());
        return cached != null ? new HashMap<>(cached) : new HashMap<>();
    }

    /**
     * Lấy tất cả products cho một category từ cache
     */
    public List<ProductItem> getCategoryProducts(String categoryName) {
        List<ProductItem> cached = categoryProductsCache.get(categoryName.toLowerCase());
        return cached != null ? new ArrayList<>(cached) : new ArrayList<>();
    }

    /**
     * Lấy products cho một subcategory từ cache
     */
    public List<ProductItem> getSubCategoryProducts(String subCategoryName) {
        List<ProductItem> cached = subCategoryProductsCache.get(subCategoryName);
        return cached != null ? new ArrayList<>(cached) : new ArrayList<>();
    }

    /**
     * Kiểm tra xem category đã được cache chưa
     */
    public boolean isCategoryCached(String categoryName) {
        String key = categoryName.toLowerCase();
        return categorySubCategoriesCache.containsKey(key) &&
                categoryProductsCache.containsKey(key);
    }

    /**
     * Kiểm tra xem Shop All đã được cache chưa
     */
    public boolean isShopAllCached() {
        synchronized (allProductsCache) {
            return !allProductsCache.isEmpty();
        }
    }

    /**
     * Kiểm tra xem có đang load category không
     */
    public boolean isLoadingCategory(String categoryName) {
        return loadingStatus.getOrDefault(categoryName.toLowerCase(), false);
    }

    // ===== UTILITY METHODS =====

    private String extractParentCategoryId(Object parentCategoryObj) {
        if (parentCategoryObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> parentCategory = (Map<String, Object>) parentCategoryObj;
            return (String) parentCategory.get("$oid");
        } else if (parentCategoryObj instanceof String) {
            return (String) parentCategoryObj;
        }
        return null;
    }

    private ProductItem parseProductFromDocument(QueryDocumentSnapshot doc) {
        try {
            ProductItem product = new ProductItem();
            product.setId(doc.getId());
            product.setName(doc.getString("Name"));
            product.setPrice(doc.getDouble("Price") != null ? doc.getDouble("Price") : 0.0);
            product.setImage(doc.getString("Image"));
            product.setDescription(doc.getString("Description"));
            product.setDimensions(doc.getString("Dimensions"));
            product.setCustomizeImage(doc.getString("CustomizeImage"));

            // Parse ratings
            ProductItem.Ratings ratings = new ProductItem.Ratings();
            Object ratingsObj = doc.get("Ratings");
            if (ratingsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                Object averageObj = ratingsMap.get("Average");
                if (averageObj instanceof Number) {
                    ratings.setAverage(((Number) averageObj).doubleValue());
                }
            }
            product.setRatings(ratings);

            // Parse categoryId
            Object categoryIdObj = doc.get("category_id");
            if (categoryIdObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> categoryIdMap = (Map<String, Object>) categoryIdObj;
                product.setCategoryId(categoryIdMap);
            }

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing product from document: " + doc.getId(), e);
            return null;
        }
    }

    /**
     * Clear cache khi cần thiết
     */
    public void clearCache() {
        categoryProductsCache.clear();
        categorySubCategoriesCache.clear();
        subCategoryProductsCache.clear();
        synchronized (allProductsCache) {
            allProductsCache.clear();
        }
        loadingStatus.clear();
        preloadCallbacks.clear();
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Shutdown executor khi app đóng
     */
    public void shutdown() {
        if (preloadExecutor != null && !preloadExecutor.isShutdown()) {
            preloadExecutor.shutdown();
            Log.d(TAG, "Preload executor shutdown");
        }
    }

}