package com.example.stylemate.ui;

public class FavouriteOutfits {
    String id;      // ID коллекции в Firebase (обязательно!)
    String title;   // Название (например "На работу")

    // Ссылки на картинки (URL). Если картинок меньше 4, поле будет null.
    String photo1;
    String photo2;
    String photo3;
    String photo4;

    public FavouriteOutfits(String id, String title, String photo1, String photo2, String photo3, String photo4) {
        this.id = id;
        this.title = title;
        this.photo1 = photo1;
        this.photo2 = photo2;
        this.photo3 = photo3;
        this.photo4 = photo4;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }

    public boolean hasLikes() {
        // Если первая картинка есть (не null), значит лайки есть
        return photo1 != null;
    }
}