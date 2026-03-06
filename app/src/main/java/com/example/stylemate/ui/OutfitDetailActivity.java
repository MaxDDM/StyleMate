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
    private ImageButton btnLike; // Вынесли кнопку в поле класса

    // Поля для логики лайков
    private String currentCollectionId;
    private String currentOutfitId;
    private boolean isLiked = false;
    // Цвета
    private final int COLOR_BLUE = android.graphics.Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = android.graphics.Color.parseColor("#5C5C5C");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        // 1. Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(OutfitDetailViewModel.class);

        // 2. Находим View
        ivMain = findViewById(R.id.ivDetailImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnDetailLike);

        // ВАЖНО: Убедись, что в XML есть эти ID, или добавь их!
        tvTitle = findViewById(R.id.tvOutfitTitle);
        tvTotalPrice = findViewById(R.id.tvTotalPrice); // Если есть поле для общей суммы
        rvProducts = findViewById(R.id.rvProducts);

        // 3. Настраиваем RecyclerView
        setupRecyclerView();

        // Сначала грузим данные, там же инициализируем переменные лайков
        loadDataFromIntent();

        // После загрузки данных обновляем вид кнопки
        updateLikeButtonUI();

        // 4. Получаем данные из Intent и запускаем загрузку
        loadDataFromIntent();

        // 5. Подписываемся на обновления данных (Observer)
        observeViewModel();

        // 6. Кнопки
        btnBack.setOnClickListener(v -> finish());

        // --- ЛОГИКА ЛАЙКА ---
        btnLike.setOnClickListener(v -> {
            // 1. Проверка на гостя (у гостя collectionId == null)
            if (currentCollectionId == null) {
                Toast.makeText(this, "Войдите или зарегистрируйтесь, чтобы сохранять образы", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Инвертируем (было лайкнуто -> стало не лайкнуто, и наоборот)
            isLiked = !isLiked;

            // 3. Обновляем вид кнопки
            updateLikeButtonUI();

            // 4. Отправляем в базу
            viewModel.toggleLike(currentCollectionId, currentOutfitId, isLiked);

            // 5. Обратная связь
            if (isLiked) {
                Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setColorFilter(COLOR_BLUE);
            btnLike.setImageResource(R.drawable.ic_heart_outline); // Если есть заполненное сердце, иначе ic_heart_outline
        } else {
            btnLike.setColorFilter(COLOR_GRAY);
            btnLike.setImageResource(R.drawable.ic_heart_outline);
        }
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
        currentOutfitId = getIntent().getStringExtra("outfit_id"); // Сохраняем ID образа
        String style = getIntent().getStringExtra("style");
        String season = getIntent().getStringExtra("season");
        currentCollectionId = getIntent().getStringExtra("collection_id");
        isLiked = getIntent().getBooleanExtra("is_liked", false);

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
        tempOutfit.setId(currentOutfitId);
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

    @Override
    public void finish() {
        // Подготавливаем данные для возврата
        Intent resultIntent = new Intent();
        resultIntent.putExtra("outfit_id", currentOutfitId);
        resultIntent.putExtra("is_liked", isLiked); // Возвращаем финальное состояние

        // Ставим штамп "Все ок" и прикладываем данные
        setResult(RESULT_OK, resultIntent);

        super.finish(); // Закрываем экран
    }
}