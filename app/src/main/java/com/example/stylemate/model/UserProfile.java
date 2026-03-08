package com.example.stylemate.model;

public class UserProfile {
    public String name;
    public String phone;
    public String email;
    public String birthDate;
    public String avatarUrl;

    public String password;

    public UserProfile() {}

    public UserProfile(String name, String phone, String email, String birthDate, String password, String avatarUrl) {
        this.name = name;
        this.phone = phone;
        this.email = email.replace(".", "|");
        this.birthDate = birthDate;
        this.avatarUrl = avatarUrl;
        this.password = password;
    }
}