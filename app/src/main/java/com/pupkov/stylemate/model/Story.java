package com.pupkov.stylemate.model;

public class Story {
    private String id;
    private String imageUrl;
    private String link;

    public Story() {}

    public Story(String id, String imageUrl, String link) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLink() {
        return link;
    }
}