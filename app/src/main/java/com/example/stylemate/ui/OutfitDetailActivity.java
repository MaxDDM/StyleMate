package com.example.stylemate.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stylemate.R;
import com.example.stylemate.model.Product;

import androidx.recyclerview.widget.LinearLayoutManager; // Важно
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OutfitDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        // 1. Находим View
        ImageView ivMain = findViewById(R.id.ivDetailImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        ImageButton btnLike = findViewById(R.id.btnDetailLike);

        // 2. НАСТРАИВАЕМ СПИСОК ТОВАРОВ

        // Создаем данные (Позже будешь получать их из БД)
        List<Product> products = new ArrayList<>();
        products.add(new Product("Футболка", "2399 Р", "Sela", "хлопок"));
        products.add(new Product("Брюки", "5499 Р", "Zara", "лен"));
        products.add(new Product("Кеды", "8999 Р", "Adidas", "кожа"));
        products.add(new Product("Очки", "1500 Р", "H&M", "пластик"));

        // Устанавливаем горизонтальный LayoutManager
        rvProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Подключаем адаптер
        ProductAdapter adapter = new ProductAdapter(products);
        rvProducts.setAdapter(adapter);

        // 2. Получаем данные из Intent (что передали из списка)
        int imageResId = getIntent().getIntExtra("image_res_id", 0);

        // 3. Устанавливаем картинку
        if (imageResId != 0) {
            ivMain.setImageResource(imageResId);
        }

        // 4. Обработка кнопки НАЗАД
        btnBack.setOnClickListener(v -> {
            finish(); // Закрывает активити и возвращает назад
        });

        // 5. Обработка лайка (пока заглушка)
        btnLike.setOnClickListener(v -> {
            // Тут можно менять цвет сердечка
            btnLike.setColorFilter(android.graphics.Color.parseColor("#3D7DFF"));
        });
    }
}