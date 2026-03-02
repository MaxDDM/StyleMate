package com.example.stylemate.repository;

import com.example.stylemate.R; // Импорт ресурсов
import com.example.stylemate.model.Outfit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserCollectionsRepository {

    // Метод 1: Получить список имен категорий
    public List<String> getCollections() {
        return Arrays.asList(
                "Основная",
                "Спорт",
                "Офис",
                "Свидание"
        );
    }

    // Метод 2: Получить картинки для конкретной коллекции (Имитация БД)
    public List<Outfit> getOutfitsForCollection(String collectionName) {
        List<Outfit> list = new ArrayList<>();

        switch (collectionName) {
            case "Спорт":
                // Картинки для спорта
                list.add(new Outfit(101, R.drawable.image1, false)); // Замени на свои R.drawable.ic_...
                list.add(new Outfit(102, R.drawable.image2, true));
                break;

            case "Офис":
                // Картинки для офиса
                list.add(new Outfit(201, R.drawable.image3, false));
                list.add(new Outfit(202, R.drawable.image4, false));
                list.add(new Outfit(203, R.drawable.image5, true));
                break;

            case "Свидание":
                // Картинки для свидания
                list.add(new Outfit(301, R.drawable.image6, true));
                list.add(new Outfit(302, R.drawable.image1, false));
                break;

            case "Основная":
            default:
                // Дефолтный список (смешанный)
                list.add(new Outfit(1, R.drawable.image1, false));
                list.add(new Outfit(2, R.drawable.image2, true));
                list.add(new Outfit(3, R.drawable.image3, false));
                list.add(new Outfit(4, R.drawable.image4, false));
                list.add(new Outfit(5, R.drawable.image5, true));
                list.add(new Outfit(6, R.drawable.image6, false));
                break;
        }
        return list;
    }

    // Методы заглушки для удаления/переименования...
    public void renameCollection(String oldName, String newName) {}
    public void deleteCollection(String name) {}
}