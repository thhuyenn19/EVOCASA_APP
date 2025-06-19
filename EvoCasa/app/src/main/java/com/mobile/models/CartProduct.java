package com.mobile.models;

import java.util.List;

public class CartProduct {

    private String id;                 // productId từ Firestore
    private String name;              // Tên sản phẩm
    private double price;             // Giá sản phẩm
    private int quantity;             // Số lượng sản phẩm trong giỏ
    private List<String> imageUrls;   // Danh sách URL ảnh sản phẩm
    private boolean isSelected;       // Đã tick chọn để checkout chưa

    // Constructor rỗng (bắt buộc cho Firestore hoặc Gson)
    public CartProduct() {}

    // Constructor tiện dụng (tuỳ ý)
    public CartProduct(String name, double price, int quantity, List<String> imageUrls) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrls = imageUrls;
    }

    // Getter & Setter cho id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter & Setter cho tên sản phẩm
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter & Setter cho giá
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Getter & Setter cho số lượng trong giỏ
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter & Setter cho danh sách ảnh
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    // Getter tiện lợi để lấy ảnh đầu tiên
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    // Getter & Setter cho trạng thái đã tick chọn
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
