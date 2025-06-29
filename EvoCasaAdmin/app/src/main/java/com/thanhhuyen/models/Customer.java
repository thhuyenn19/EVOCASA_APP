package com.thanhhuyen.models;

public class Customer {
    private String id;
    private String name;
    private String gender;
    private String mail;
    private String phone;
    private String dob;

    public Customer() {
    }

    public Customer(String id, String name, String gender, String mail, String phone, String dob) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.mail = mail;
        this.phone = phone;
        this.dob = dob;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getMail() { return mail; }
    public String getPhone() { return phone; }
    public String getDob() { return dob; }
}
