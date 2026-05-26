package com.pupkov.stylemate.model;

import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class Outfit {
    private String id;
    private String style;
    private String imageUrl;
    private String filter_season;
    private String situation;

    private Map<String, Boolean> filter_colors = new HashMap<>();
    private Map<String, Boolean> filter_types = new HashMap<>();
    private Map<String, Boolean> items = new HashMap<>();

    private boolean isLiked = false;

    public Outfit() { }

    public Outfit(String id, String imageUrl, String style) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.style = style;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFilter_season() { return filter_season; }
    public void setFilter_season(String filter_season) { this.filter_season = filter_season; }

    @PropertyName("situation")
    public String getFilter_situation() { return situation; }

    @PropertyName("situation")
    public void setFilter_situation(String situation) { this.situation = situation; }

    public Map<String, Boolean> getFilter_colors() { return filter_colors; }
    public void setFilter_colors(Map<String, Boolean> filter_colors) { this.filter_colors = filter_colors; }

    public Map<String, Boolean> getFilter_types() { return filter_types; }
    public void setFilter_types(Map<String, Boolean> filter_types) { this.filter_types = filter_types; }

    public Map<String, Boolean> getItems() { return items; }
    public void setItems(Map<String, Boolean> items) { this.items = items; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}