package com.mobile.models;

import java.util.List;

public class ProductItem {
    private String id;
    private String name;
    private double price;
    private String image; // JSON array as string
    private Ratings ratings = new Ratings(); // Default ratings

    public ProductItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Ratings getRatings() { return ratings; }
    public void setRatings(Ratings ratings) { this.ratings = ratings; }

    public static class Ratings {
        private Double average;
        private List<Detail> details;

        public Double getAverage() { return average; }
        public void setAverage(Double average) { this.average = average; }
        public List<Detail> getDetails() { return details; }
        public void setDetails(List<Detail> details) { this.details = details; }

        public static class Detail {
            private String reviewId;
            private int rating;
            private String comment;
            private String customerName;
            private String createdAt;

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