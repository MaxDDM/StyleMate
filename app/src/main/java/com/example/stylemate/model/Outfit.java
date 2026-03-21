package com.example.stylemate.model;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class Outfit {
    private String id;
    private String style;           // "casual"
    private String imageUrl;        // Ссылка

    // Новые поля для фильтров (как в твоем JSON)
    private String filter_season;   // "лето"
    private String situation;// "any"

    // Firebase хранит списки как Map<String, Boolean>: {"Белый": true, "Голубой": true}
    private Map<String, Boolean> filter_colors = new HashMap<>();
    private Map<String, Boolean> filter_types = new HashMap<>();
    private Map<String, Boolean> items = new HashMap<>(); // Список ID вещей {"66": true}

    // Локальное поле (не из БД), нужно для UI (сердечко)
    private boolean isLiked = false;

    // Обязательный пустой конструктор для Firebase
    public Outfit() { }

    // Конструктор для удобства (если понадобится создавать вручную)
    public Outfit(String id, String imageUrl, String style) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.style = style;
    }

    // --- Геттеры и Сеттеры ---

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