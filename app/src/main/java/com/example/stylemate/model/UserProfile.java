package com.example.stylemate.model;

public class UserProfile {
    public String name;
    public String phone;
    public String email;
    public String birthDate;
    public int avatarResId; // int, потому что R.drawable.avatar - это число

    public String password;

    public UserProfile(String name, String phone, String email, String birthDate, String password, int avatarResId) {
        this.name = name;
        this.phone = phone;
        this.email = email.replace(".", "|");
        this.birthDate = birthDate;
        this.avatarResId = avatarResId;
        this.password = password;
    }
}