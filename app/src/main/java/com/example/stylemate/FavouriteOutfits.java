package com.example.stylemate;

// Этот класс - просто контейнер для данных.
// Он описывает, из чего состоит один "Образ" в твоем списке.

public class FavouriteOutfits {

    // Мы используем int (целое число), потому что картинки из папки drawable
    // (например R.drawable.shoes) для Андроида являются числами-ID.
    int photo1;
    int photo2;
    int photo3;
    int photo4;

    String title;

    // Конструктор - это метод, который помогает быстро создавать новый объект
    public FavouriteOutfits(int photo1, int photo2, int photo3, int photo4, String title) {
        this.photo1 = photo1;
        this.photo2 = photo2;
        this.photo3 = photo3;
        this.photo4 = photo4;
        this.title = title;
    }
}