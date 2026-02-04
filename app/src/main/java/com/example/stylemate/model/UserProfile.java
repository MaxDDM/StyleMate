package com.example.stylemate.model;

public class UserProfile {
    public String name;
    public String phone;
    public String email;
    public String birthDate;
    public int avatarResId; // int, потому что R.drawable.avatar - это число

    public UserProfile(String name, String phone, String email, String birthDate, int avatarResId) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.avatarResId = avatarResId;
    }
}