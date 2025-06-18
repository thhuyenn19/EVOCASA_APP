package com.mobile.models;

public class Category {
    private String Image;
    private String Name;
    private String Description;
    private String Slug;

    public Category() {} // Firestore needs this

    public Category(String image, String name, String description, String slug) {
        this.Image = image;
        this.Name = name;
        this.Description = description;
        this.Slug = slug;
    }

    public String getImage() {
        return Image;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getSlug() {
        return Slug;
    }
}
