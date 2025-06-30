package com.thanhhuyen.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.Map;

public class Category {
    private Map<String, Object> _id;
    private String name;
    private String description;
    private String image;
    private String slug;
    private Map<String, String> parentCategory;

    public Category() {
        // Required empty constructor for Firestore
    }

    @PropertyName("_id")
    public Map<String, Object> get_id() {
        return _id;
    }

    @PropertyName("_id")
    public void set_id(Map<String, Object> _id) {
        this._id = _id;
    }

    public String getId() {
        if (_id != null && _id.containsKey("$oid")) {
            return _id.get("$oid").toString();
        }
        return null;
    }

    public void setId(String id) {
        // This method is used when setting the Firestore document ID
        // We don't need to modify the _id map here as it's for MongoDB compatibility
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

    @PropertyName("Image")
    public String getImage() {
        return image;
    }

    @PropertyName("Image")
    public void setImage(String image) {
        this.image = image;
    }

    @PropertyName("Slug")
    public String getSlug() {
        return slug;
    }

    @PropertyName("Slug")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @PropertyName("ParentCategory")
    public Map<String, String> getParentCategory() {
        return parentCategory;
    }

    @PropertyName("ParentCategory")
    public void setParentCategory(Map<String, String> parentCategory) {
        this.parentCategory = parentCategory;
    }

    public String getParentId() {
        if (parentCategory != null && parentCategory.containsKey("$oid")) {
            return parentCategory.get("$oid");
        }
        return null;
    }
} 