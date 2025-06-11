package com.mobile.models;

public class Review {
    private int id;
    private int productId;
    private int accountId;
    private int rating;
    private String comment;
    private String createdAt;
    private String updatedAt;

    // Thông tin hiển thị giao diện
    private String name;       // Tên người đánh giá
    private int avatarRes;     // Resource avatar (R.drawable...)

    public Review() {}

    public Review(int id, int productId, int accountId, int rating, String comment,
                  String createdAt, String updatedAt, String name, int avatarRes) {
        this.id = id;
        this.productId = productId;
        this.accountId = accountId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
        this.avatarRes = avatarRes;
    }

    // Constructor giả lập dữ liệu test
    public Review(String name, String comment, String createdAt, int avatarRes, int rating) {
        this.name = name;
        this.comment = comment;
        this.createdAt = createdAt;
        this.avatarRes = avatarRes;
        this.rating = rating;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAvatarRes() { return avatarRes; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }
}
