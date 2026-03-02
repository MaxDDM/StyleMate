package com.example.stylemate.model;

public class Outfit {
    private int id;
    private int imageResId; // Ссылка на картинку (R.drawable.pic1)
    private boolean isLiked;

    public Outfit(int id, int imageResId, boolean isLiked) {
        this.id = id;
        this.imageResId = imageResId;
        this.isLiked = isLiked;
    }

    public int getId() { return id; }
    public int getImageResId() { return imageResId; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}