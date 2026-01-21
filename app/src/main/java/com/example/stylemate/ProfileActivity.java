package com.example.stylemate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Настройка отступов (системные бары)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btnSettings = findViewById(R.id.btnSettings);

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        // 1. НАХОДИМ ЭЛЕМЕНТЫ
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);
        TextView tvFavoritesTitle = findViewById(R.id.tvFavoritesTitle);
        RecyclerView rvFavorites = findViewById(R.id.rvFavorites);
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        TextView tvUserName = findViewById(R.id.tvUserName);

        // 2. СОЗДАЕМ ДАННЫЕ (Важно: используем наш класс FavouriteOutfits)
        List<FavouriteOutfits> myData = new ArrayList<>();

        // --- БЛОК ТЕСТИРОВАНИЯ ---
        // Чтобы проверить ПУСТОЙ экран -> закомментируй строки с myData.add(...)
        // Чтобы проверить ПОЛНЫЙ экран -> раскомментируй их

        // Добавляем первую подборку (используем image1-image4)

        myData.add(new FavouriteOutfits(
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3,
                R.drawable.image4,
                "На спорте \uD83D\uDCAA"
        ));

        // Добавляем вторую подборку (используем image5-image8)
        myData.add(new FavouriteOutfits(
                R.drawable.image5,
                R.drawable.image6,
                R.drawable.image7,
                R.drawable.image8,
                "На свидание \uD83D\uDC80"
        ));

        // Можешь добавить еще копии, чтобы проверить скролл
        myData.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image3, R.drawable.image2, R.drawable.image4, "Для офиса"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        // -------------------------

        // 3. ЛОГИКА ОТОБРАЖЕНИЯ
        if (myData.isEmpty()) {
            // === СЦЕНАРИЙ 1: ПУСТО ===

            // Показываем заглушку
            tvEmptyState.setVisibility(View.VISIBLE);

            // Скрываем список
            tvFavoritesTitle.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.GONE);

            // Аватарка-заглушка (серая)
            // Если у тебя нет специальной иконки, используй системную или одну из картинок
            imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
        } else {
            // === СЦЕНАРИЙ 2: ЕСТЬ ДАННЫЕ ===

            // Скрываем заглушку
            tvEmptyState.setVisibility(View.GONE);

            // Показываем список
            tvFavoritesTitle.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.VISIBLE);

            // Ставим "Настоящую" аватарку (для примера берем image1)
            imgAvatar.setImageResource(R.drawable.avatar);

            // 4. НАСТРАИВАЕМ RecyclerView
            // "this" - это контекст активити
            // "2" - количество колонок
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            rvFavorites.setLayoutManager(layoutManager);

            // 5. ПОДКЛЮЧАЕМ АДАПТЕР
            // Передаем туда наш список myData
            FavoriteOutfitsAdapter adapter = new FavoriteOutfitsAdapter(myData, item -> {
                // Код, который сработает при нажатии:
                Intent intent = new Intent(ProfileActivity.this, CollectionDetailActivity.class);
                // Передаем название подборки ("На спорте" и т.д.)
                intent.putExtra("COLLECTION_TITLE", item.title); // Убедись, что в FavouriteOutfits есть геттер getTitle() или используй item.title
                startActivity(intent);
            });

            rvFavorites.setAdapter(adapter);
        }

    }
}