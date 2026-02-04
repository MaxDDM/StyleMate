package com.example.stylemate.repository;

import com.example.stylemate.R;
// Убедись, что FavouriteOutfits импортирован правильно.
// Если он лежит просто в корне пакета com.example.stylemate, импорт не нужен.
// Если перенес в model - добавь import.
import com.example.stylemate.ui.FavouriteOutfits;
import com.example.stylemate.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    // Метод: Получить избранные луки
    public List<FavouriteOutfits> getFavoriteOutfits() {
        List<FavouriteOutfits> data = new ArrayList<>();

        // Имитация данных (потом заменим на БД или API)
        data.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4, "На спорте \uD83D\uDCAA"));
        data.add(new FavouriteOutfits(R.drawable.image5, R.drawable.image6, R.drawable.image7, R.drawable.image8, "На свидание \uD83D\uDC80"));
        data.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image3, R.drawable.image2, R.drawable.image4, "Для офиса"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));

        return data;
    }

    public UserProfile getUserProfile() {
        // В будущем тут запрос в БД/Сеть
        return new UserProfile(
                "Марат",
                "+7 (999) 123 45 67",
                "marat@edu.hse.ru",
                "12.01.2001",
                R.drawable.avatar
        );
    }

    public void logout() {
        // Очистка токенов, БД и т.д.
    }

    public boolean checkCurrentPassword(String input) {
        // В будущем тут запрос к API. Пока хардкод.
        return "123".equals(input);
    }

    public void changePassword(String newPassword) {
        // Тут логика отправки нового пароля на сервер
    }
}