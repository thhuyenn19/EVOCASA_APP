package com.mobile.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
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

    public ProductItem() {}

    public ProductItem(String id, String name, Double price, String image, String description, String dimensions, String customizeImage, Ratings ratings, Map<String, Object> categoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.dimensions = dimensions;
        this.customizeImage = customizeImage;
        this.ratings = ratings;
        this.categoryId = categoryId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public String getCustomizeImage() { return customizeImage; }
    public void setCustomizeImage(String customizeImage) { this.customizeImage = customizeImage; }

    public Ratings getRatings() { return ratings; }
    public void setRatings(Ratings ratings) { this.ratings = ratings; }

    public Map<String, Object> getCategoryId() { return categoryId; }
    public void setCategoryId(Map<String, Object> categoryId) { this.categoryId = categoryId; }

    public static class Ratings implements Serializable {
        private Double average;
        private List<Detail> details;

        public Ratings() {}

        public Ratings(Double average) {
            this.average = average;
        }

        public Double getAverage() { return average; }
        public void setAverage(Double average) { this.average = average; }

        public List<Detail> getDetails() { return details; }
        public void setDetails(List<Detail> details) { this.details = details; }

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
}
