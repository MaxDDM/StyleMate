package com.pupkov.stylemate.ui;

import static com.pupkov.stylemate.analytics.avgOutfitTime.changeAvgTime;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Не забудь добавить зависимость Glide в build.gradle, если нет
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pupkov.stylemate.R;
import com.pupkov.stylemate.analytics.AnalyticsManager;
import com.pupkov.stylemate.analytics.CR;
import com.pupkov.stylemate.analytics.CTR;
import com.pupkov.stylemate.model.Item;
import com.pupkov.stylemate.model.Outfit;
import com.pupkov.stylemate.model.OutfitDetailViewModel;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.ItemsRepository;
import com.pupkov.stylemate.ui.dialogs.UniversalInfoDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutfitDetailActivity extends AppCompatActivity {

    long start = System.nanoTime();
    private OutfitDetailViewModel viewModel;
    private ItemAdapter adapter;

    private ImageView ivMain;
    private TextView tvTitle;      // "Образ на лето..."
    private TextView tvTotalPrice; // "15 000 Р" (сумма)
    private RecyclerView rvProducts;
    private ImageButton btnLike; // Вынесли кнопку в поле класса
    private ImageButton shareButton;

    // Поля для логики лайков
    private String currentCollectionId;
    private String currentOutfitId;
    private String imageUrl;
    private boolean isLiked = false;
    // Цвета
    private final int COLOR_BLUE = android.graphics.Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = android.graphics.Color.parseColor("#5C5C5C");
    private final ItemsRepository repository = new ItemsRepository();


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
        shareButton = findViewById(R.id.shareButton);

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

        new CTR().updateOutfitShows(Integer.parseInt(currentOutfitId), () -> {
            AnalyticsManager.calculateOutfitFavoriteRate(currentOutfitId);
        });

        // 6. Кнопки
        btnBack.setOnClickListener(v -> finish());

        shareButton.setOnClickListener(v -> share());

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
                // --- НАЧАЛО ВСТАВКИ: ОБУЧЕНИЕ ---
                // Проверяем, показывали ли мы уже обучение про лайки
                String isShown = ActiveUserInfo.getDefaults("IS_FIRST_LIKE_SHOWN", this);

                // Если НЕ показывали (т.е. там null или "false")
                if (!"true".equals(isShown)) {
                    String text = "Вы поставили лайк образу\nТеперь он сохранен в папке в\nличном кабинете.";

                    // Показываем диалог (false = стрелка не нужна)
                    UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, false);
                    dialog.show(getSupportFragmentManager(), "FirstLikeTag");

                    // Записываем, что теперь показали
                    ActiveUserInfo.setDefaults("IS_FIRST_LIKE_SHOWN", "true", this);
                }
            } else {
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private static StringBuilder getMessage(List<Item> loadedItems) {
        StringBuilder message = new StringBuilder("С вами поделились образом из приложения StyleMate\uD83C\uDD92\n\n");
        for (int i = 0; i < loadedItems.size(); ++i) {
            message.append("⚡️").append(loadedItems.get(i).getType()).append(" (").append(loadedItems.get(i).getPrice()).append("₽) – ").append(loadedItems.get(i).getLink()).append("\n");
        }
        message.append("\nЕсли хотите создать свою ленту рекомендаций, скачивайте наше приложение \uD83D\uDC47\uD83C\uDFFB\n\n");
        message.append("Google Play Market: https://play.google.com/store/apps/details?id=com.pupkov.stylemate&hl=ru\n");
        message.append("RuStore: https://www.rustore.ru/catalog/app/com.pupkov.stylemate");
        return message;
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
                CR.updateCountLink(Integer.parseInt(currentOutfitId));
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
        imageUrl = getIntent().getStringExtra("image_url");
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

        long end = System.nanoTime();
        changeAvgTime(Integer.parseInt(currentOutfitId), (end - start) / 1_000_000_000.0);
        super.finish(); // Закрываем экран
    }

    private void share() {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        File cachePath = new File(getCacheDir(), "images");
                        cachePath.mkdirs();

                        File file = new File(cachePath, "shared_image.png");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        try {
                            fos.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        Uri contentUri = FileProvider.getUriForFile(
                                OutfitDetailActivity.this,
                                getPackageName() + ".fileprovider",
                                file
                        );

                        ArrayList<String> itemIds = getIntent().getStringArrayListExtra("item_ids");
                        repository.getItemsByIds(itemIds, new ItemsRepository.ItemsCallback() {
                            @Override
                            public void onItemsLoaded(List<Item> loadedItems) {
                                StringBuilder message = getMessage(loadedItems);

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/png");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Образ из StyleMate");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                startActivity(Intent.createChooser(shareIntent, "Поделиться через…"));
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(OutfitDetailActivity.this, "Возникла ошибка, скорее всего проблемы с соединением", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }
}