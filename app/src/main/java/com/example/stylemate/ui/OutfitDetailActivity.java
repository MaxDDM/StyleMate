package com.example.stylemate.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Не забудь добавить зависимость Glide в build.gradle, если нет
import com.example.stylemate.R;
import com.example.stylemate.model.Outfit;
import com.example.stylemate.model.OutfitDetailViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OutfitDetailActivity extends AppCompatActivity {

    private OutfitDetailViewModel viewModel;
    private ItemAdapter adapter;

    private ImageView ivMain;
    private TextView tvTitle;      // "Образ на лето..."
    private TextView tvTotalPrice; // "15 000 Р" (сумма)
    private RecyclerView rvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        // 1. Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(OutfitDetailViewModel.class);

        // 2. Находим View
        ivMain = findViewById(R.id.ivDetailImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnLike = findViewById(R.id.btnDetailLike);

        // ВАЖНО: Убедись, что в XML есть эти ID, или добавь их!
        tvTitle = findViewById(R.id.tvOutfitTitle);
        tvTotalPrice = findViewById(R.id.tvTotalPrice); // Если есть поле для общей суммы
        rvProducts = findViewById(R.id.rvProducts);

        // 3. Настраиваем RecyclerView
        setupRecyclerView();

        // 4. Получаем данные из Intent и запускаем загрузку
        loadDataFromIntent();

        // 5. Подписываемся на обновления данных (Observer)
        observeViewModel();

        // 6. Кнопки
        btnBack.setOnClickListener(v -> finish());

        btnLike.setOnClickListener(v -> {
            // Тут логика лайка (пока визуальная)
            btnLike.setColorFilter(android.graphics.Color.parseColor("#3D7DFF"));
            Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Передаем слушатель клика: открываем ссылку в браузере
        adapter = new ItemAdapter(url -> {
            if (url != null && !url.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Ссылка на товар не найдена", Toast.LENGTH_SHORT).show();
            }
        });
        rvProducts.setAdapter(adapter);
    }

    private void loadDataFromIntent() {
        // Достаем простые типы данных
        String imageUrl = getIntent().getStringExtra("image_url");
        String outfitId = getIntent().getStringExtra("outfit_id");
        String style = getIntent().getStringExtra("style");
        String season = getIntent().getStringExtra("season");

        // Достаем список ID вещей (переданный как ArrayList<String>)
        ArrayList<String> itemIds = getIntent().getStringArrayListExtra("item_ids");

        // Устанавливаем картинку сразу (Glide)
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(ivMain);
        }

        // --- ХИТРОСТЬ ---
        // ViewModel ожидает объект Outfit, но передавать его целиком через Intent сложно (надо Parcelable).
        // Мы соберем "временный" объект Outfit из того, что пришло в Intent.
        Outfit tempOutfit = new Outfit();
        tempOutfit.setId(outfitId);
        tempOutfit.setStyle(style);
        tempOutfit.setFilter_season(season);

        // Превращаем список List обратно в Map для модели
        Map<String, Boolean> itemsMap = new HashMap<>();
        if (itemIds != null) {
            for (String id : itemIds) {
                itemsMap.put(id, true);
            }
        }
        tempOutfit.setItems(itemsMap);

        // Запускаем логику во ViewModel
        viewModel.init(tempOutfit);
    }

    private void observeViewModel() {
        // Список вещей
        viewModel.items.observe(this, items -> {
            adapter.updateList(items);
        });

        // Заголовок "Образ на лето..."
        viewModel.title.observe(this, title -> {
            if (tvTitle != null) tvTitle.setText(title);
        });

        // Общая цена
        viewModel.totalPrice.observe(this, price -> {
            if (tvTotalPrice != null) tvTotalPrice.setText(price);
        });
    }
}