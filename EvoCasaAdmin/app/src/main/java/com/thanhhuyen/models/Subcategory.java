package com.thanhhuyen.models;

public class Subcategory {
    private String id;
    private String name; // Tên của Subcategory, ánh xạ với field "Name"
    private String parentCategory;  // ID của Category cha, ánh xạ với field "ParentCategory"
    private String image;  // Thuộc tính tùy chọn để lưu ảnh đại diện cho Subcategory

    // Constructors
    public Subcategory() {
        // Constructor rỗng yêu cầu cho Firebase
    }

    public Subcategory(String id, String name, String parentCategory, String image) {
        this.id = id;
        this.name = name;
        this.parentCategory = parentCategory;
        this.image = image;
    }

    // Các phương thức Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(String parentCategory) {
        this.parentCategory = parentCategory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
