package com.mobile.models;

import com.google.firebase.firestore.Exclude;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Customer {
    private String Name;
    private String Gender;
    private String Mail;
    private String Phone;
    private Object DOB; // Changed from String to Object to handle Map
    private String Address;
    private String Image;

    public Customer() {} // Required for Firestore

    // Getters
    public String getName() { return Name; }
    public String getGender() { return Gender; }
    public String getMail() { return Mail; }
    public String getPhone() { return Phone; }
    public String getAddress() { return Address; }
    public String getImage() { return Image; }

    // Raw DOB getter (for Firestore)
    public Object getDOB() { return DOB; }

    // Helper method to get DOB as formatted string
    @Exclude
    public String getDOBString() {
        if (DOB == null) return "";

        if (DOB instanceof String) {
            return (String) DOB;
        } else if (DOB instanceof Map) {
            Map<String, Object> dobMap = (Map<String, Object>) DOB;
            String dateString = (String) dobMap.get("$date");
            if (dateString != null) {
                try {
                    // Parse ISO date string
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = isoFormat.parse(dateString);

                    // Format to display format
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return displayFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    // Setters
    public void setName(String name) { this.Name = name; }
    public void setGender(String gender) { this.Gender = gender; }
    public void setMail(String mail) { this.Mail = mail; }
    public void setPhone(String phone) { this.Phone = phone; }
    public void setDOB(Object dob) { this.DOB = dob; }
    public void setAddress(String address) { this.Address = address; }
    public void setImage(String image) { this.Image = image; }
}