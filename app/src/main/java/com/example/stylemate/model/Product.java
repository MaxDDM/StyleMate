package com.example.stylemate.model;

public class Product {
    private String name;
    private String price;
    private String brand;
    private String material;

    public Product(String name, String price, String brand, String material) {
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.material = material;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getBrand() { return brand; }
    public String getMaterial() { return material; }
}