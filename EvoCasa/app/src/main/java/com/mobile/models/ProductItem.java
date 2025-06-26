package com.mobile.models;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductItem implements Serializable {
    private String id;
    private String name;
    private Double price;
    private String image;
    private String description;
    private String dimensions;
    private String customizeImage;
    private Ratings ratings;
    private Map<String, Object> categoryId;
    private Map<String, List<CustomizeOption>> customize;

    public ProductItem() {}

    public ProductItem(String id, String name, Double price, String image, String description, String dimensions, String customizeImage, Ratings ratings, Map<String, Object> categoryId, Map<String, List<CustomizeOption>> customize) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.dimensions = dimensions;
        this.customizeImage = customizeImage;
        this.ratings = ratings;
        this.categoryId = categoryId;
        this.customize = customize;
    }

    // ID không có trong Firestore, nên không cần PropertyName
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("Name")
    public String getName() { return name; }
    @PropertyName("Name")
    public void setName(String name) { this.name = name; }

    @PropertyName("Price")
    public Double getPrice() { return price; }
    @PropertyName("Price")
    public void setPrice(Double price) { this.price = price; }

    @PropertyName("Image")
    public String getImage() { return image; }
    @PropertyName("Image")
    public void setImage(String image) { this.image = image; }

    @PropertyName("Description")
    public String getDescription() { return description; }
    @PropertyName("Description")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("Dimension")
    public String getDimensions() { return dimensions; }
    @PropertyName("Dimension")
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    @PropertyName("CustomizeImage")
    public String getCustomizeImage() { return customizeImage; }
    @PropertyName("CustomizeImage")
    public void setCustomizeImage(String customizeImage) { this.customizeImage = customizeImage; }

    @PropertyName("Ratings")
    public Ratings getRatings() { return ratings; }
    @PropertyName("Ratings")
    public void setRatings(Ratings ratings) { this.ratings = ratings; }

    public Map<String, Object> getCategoryId() { return categoryId; }
    public void setCategoryId(Map<String, Object> categoryId) { this.categoryId = categoryId; }

    @PropertyName("Customize")
    public Map<String, List<CustomizeOption>> getCustomize() { return customize; }

    @PropertyName("Customize")
    public void setCustomize(Object customizeObj) {
        if (customizeObj instanceof Map) {
            Map<String, List<Map<String, Object>>> rawMap = (Map<String, List<Map<String, Object>>>) customizeObj;
            this.customize = new HashMap<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : rawMap.entrySet()) {
                List<CustomizeOption> optionList = new ArrayList<>();
                for (Map<String, Object> option : entry.getValue()) {
                    CustomizeOption customizeOption = new CustomizeOption();
                    customizeOption.setType((String) option.get("Type"));
                    customizeOption.setImage((String) option.get("Image"));
                    customizeOption.setPrice(option.get("Price") instanceof Number ? ((Number) option.get("Price")).doubleValue() : 0.0);
                    optionList.add(customizeOption);
                }
                this.customize.put(entry.getKey(), optionList);
            }
        } else if (customizeObj instanceof List) {
            this.customize = new HashMap<>();
        } else {
            this.customize = new HashMap<>();
        }
    }

    // ==== Inner class Ratings ====
    public static class Ratings implements Serializable {
        private Double average;
        private List<Detail> details;

        public Ratings() {}

        public Ratings(Double average) {
            this.average = average;
        }

        @PropertyName("Average")
        public Double getAverage() { return average; }
        @PropertyName("Average")
        public void setAverage(Double average) { this.average = average; }

        @PropertyName("Details")
        public List<Detail> getDetails() { return details; }
        @PropertyName("Details")
        public void setDetails(List<Detail> details) { this.details = details; }

        // ==== Inner class Ratings.Detail ====
        public static class Detail implements Serializable {
            @SerializedName("ReviewId")
            private String reviewId;

            @SerializedName("Rating")
            private int rating;

            @SerializedName("Comment")
            private String comment;

            @SerializedName("CustomerName")
            private String customerName;

            @SerializedName("CreatedAt")
            private String createdAt;

            public Detail() {}

            public String getReviewId() { return reviewId; }
            public void setReviewId(String reviewId) { this.reviewId = reviewId; }

            public int getRating() { return rating; }
            public void setRating(int rating) { this.rating = rating; }

            public String getComment() { return comment; }
            public void setComment(String comment) { this.comment = comment; }

            public String getCustomerName() { return customerName; }
            public void setCustomerName(String customerName) { this.customerName = customerName; }

            public String getCreatedAt() { return createdAt; }
            public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        }
    }

    // ==== Inner class CustomizeOption ====
    public static class CustomizeOption implements Serializable {
        private String type;
        private String image;
        private Double price;

        public CustomizeOption() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
    @PropertyName("SubCategory")
    public String getSubCategory() {
        Object subCategoryObj = categoryId != null ? categoryId.get("SubCategory") : null;
        return subCategoryObj != null ? subCategoryObj.toString() : null;
    }

    @PropertyName("MainCategory")
    public String getMainCategory() {
        Object mainCategoryObj = categoryId != null ? categoryId.get("MainCategory") : null;
        return mainCategoryObj != null ? mainCategoryObj.toString() : null;
    }
    // Convert chuỗi JSON thành danh sách ảnh
    public List<String> getImageList() {
        try {
            return new Gson().fromJson(image, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    public String getFirstImage() {
        List<String> list = getImageList();
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

}
