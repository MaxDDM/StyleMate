package com.example.stylemate.repository;

import java.util.Arrays;
import java.util.List;

public class UserCollectionsRepository {

    // В будущем здесь будет запрос к API
    public List<String> getCollections() {
        return Arrays.asList(
                "Основная",
                "Спорт",
                "Улица",
                "Офис",
                "Свидание",
                "Дом",
                "Вечеринка"
        );
    }

    public void renameCollection(String oldName, String newName) {
        // Здесь будет код: database.update(oldName, newName);
        // Пока пусто, так как бэкенда нет
    }

    public void deleteCollection(String name) {
        // Здесь будет код: database.delete(name);
    }
}