package com.pupkov.stylemate.model;

public class Item {
    private String id;        // ID (ключ из базы)
    private String type;      // "Худи"
    private String brand;     // "Sela"
    private String price;     // "2399 Р"
    private String material;  // "хлопок"
    private String imageUrl;  // Ссылка на фото
    private String link;

    // Пустой конструктор для Firebase
    public Item() { }

    public Item(String id, String type, String brand, String price, String material, String imageUrl, String link) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.price = price;
        this.material = material;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLink() { return link; } // <--- Геттер для ссылки
    public void setLink(String link) { this.link = link; }
}