package com.thanhhuyen.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Product {
    @Exclude
    private String id;
    private String name;
    private String description;
    private Map<String, Object> category_id;
    private String imageJson;
    private double price;
    private boolean isActive;
    private String origin;
    private List<String> customize;
    private String uses;
    private Map<String, Object> create_date;
    private String store;
    private int quantity;
    private String dimension;
    private Object ratings;

    public Product() {
        // Required empty constructor for Firestore
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("Name")
    public String getName() {
        return name;
    }

    @PropertyName("Name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("Description")
    public String getDescription() {
        return description;
    }

    @PropertyName("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("category_id")
    public String getCategoryId() {
        if (category_id != null && category_id.containsKey("$oid")) {
            return category_id.get("$oid").toString();
        }
        return null;
    }

    @PropertyName("category_id")
    public void setCategoryId(Map<String, Object> categoryId) {
        this.category_id = categoryId;
    }

    @PropertyName("Image")
    public List<String> getImages() {
        if (imageJson == null || imageJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return new Gson().fromJson(imageJson, new TypeToken<List<String>>(){}.getType());
        } catch (Exception e) {
            List<String> result = new ArrayList<>();
            result.add(imageJson);
            return result;
        }
    }

    @PropertyName("Image")
    public void setImages(String imageJson) {
        this.imageJson = imageJson;
    }

    @PropertyName("Price")
    public double getPrice() {
        return price;
    }

    @PropertyName("Price")
    public void setPrice(double price) {
        this.price = price;
    }

    @PropertyName("Origin")
    public String getOrigin() {
        return origin;
    }

    @PropertyName("Origin")
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @PropertyName("Customize")
    public List<String> getCustomize() {
        return customize != null ? customize : new ArrayList<>();
    }

    @PropertyName("Customize")
    public void setCustomize(List<String> customize) {
        this.customize = customize;
    }

    @PropertyName("Uses")
    public String getUses() {
        return uses;
    }

    @PropertyName("Uses")
    public void setUses(String uses) {
        this.uses = uses;
    }

    @PropertyName("Create_date")
    public Map<String, Object> getCreateDate() {
        return create_date;
    }

    @PropertyName("Create_date")
    public void setCreateDate(Map<String, Object> createDate) {
        this.create_date = createDate;
    }

    @PropertyName("Store")
    public String getStore() {
        return store;
    }

    @PropertyName("Store")
    public void setStore(String store) {
        this.store = store;
    }

    @PropertyName("Quantity")
    public int getQuantity() {
        return quantity;
    }

    @PropertyName("Quantity")
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @PropertyName("Dimension")
    public String getDimension() {
        return dimension;
    }

    @PropertyName("Dimension")
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @PropertyName("Ratings")
    public Object getRatings() {
        return ratings;
    }

    @PropertyName("Ratings")
    public void setRatings(Object ratings) {
        this.ratings = ratings;
    }
} 
