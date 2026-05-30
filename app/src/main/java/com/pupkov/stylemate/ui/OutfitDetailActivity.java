package com.pupkov.stylemate.ui;

import static com.pupkov.stylemate.analytics.avgOutfitTime.changeAvgTime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pupkov.stylemate.R;
import com.pupkov.stylemate.analytics.AnalyticsManager;
import com.pupkov.stylemate.analytics.CR;
import com.pupkov.stylemate.analytics.CTR;
import com.pupkov.stylemate.model.Outfit;
import com.pupkov.stylemate.model.OutfitDetailViewModel;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.ui.dialogs.ConfirmDislikeDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Экран детального просмотра образа с возможностью лайка и перехода в магазин.
 */
public class OutfitDetailActivity extends AppCompatActivity {

    long start = System.nanoTime();
    private OutfitDetailViewModel viewModel;
    private ItemAdapter adapter;

    private ImageView ivMain;
    private TextView tvTitle;
    private TextView tvTotalPrice;
    private RecyclerView rvProducts;
    private ImageButton btnLike;
    private ImageButton btnDislike;

    private String currentCollectionId;
    private String currentOutfitId;
    private boolean isLiked = false;

    private final int COLOR_BLUE = android.graphics.Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = android.graphics.Color.parseColor("#5C5C5C");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        viewModel = new ViewModelProvider(this).get(OutfitDetailViewModel.class);
        ivMain = findViewById(R.id.ivDetailImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnDetailLike);
        btnDislike = findViewById(R.id.btnDetailDislike);
        tvTitle = findViewById(R.id.tvOutfitTitle);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvProducts = findViewById(R.id.rvProducts);

        setupRecyclerView();
        loadDataFromIntent();
        updateLikeButtonUI();
        observeViewModel();

        // Фиксация показа образа в аналитике CTR
        if (currentOutfitId != null && !currentOutfitId.isEmpty()) {
            try {
                int id = Integer.parseInt(currentOutfitId);
                new CTR().updateOutfitShows(id, () -> {
                    AnalyticsManager.calculateOutfitFavoriteRate(currentOutfitId);
                });
            } catch (NumberFormatException ignored) {
            }
        }

        btnBack.setOnClickListener(v -> finish());

        if (btnDislike != null) {
            if (currentCollectionId == null) {
                Toast.makeText(this, "Войдите или зарегистрируйтесь, чтобы скрывать образы", Toast.LENGTH_SHORT).show();
                return;
            }
            btnDislike.setOnClickListener(v -> {
                ConfirmDislikeDialog dialog = new ConfirmDislikeDialog();
                dialog.setListener(new ConfirmDislikeDialog.OnConfirmDislikeListener() {
                    @Override
                    public void onConfirmHide() {
                        // Заглушка для Firebase
                        Toast.makeText(OutfitDetailActivity.this, "Образ скрыт (заглушка)", Toast.LENGTH_SHORT).show();
                        finish(); // Закрываем экран деталей после скрытия
                    }
                    @Override
                    public void onCancelHide() {
                        // Ничего делать не нужно, диалог просто закроется сам
                    }
                });
                dialog.show(getSupportFragmentManager(), "ConfirmDislikeDialog");
            });
        }

        btnLike.setOnClickListener(v -> {
            if (currentCollectionId == null) {
                Toast.makeText(this, "Войдите или зарегистрируйтесь, чтобы сохранять образы", Toast.LENGTH_SHORT).show();
                return;
            }

            isLiked = !isLiked;
            updateLikeButtonUI();

            viewModel.toggleLike(currentCollectionId, currentOutfitId, isLiked);

            if (isLiked) {
                // Формируем динамический ключ для текущей подборки
                String prefKey = "IS_FIRST_LIKE_SHOWN_" + currentCollectionId;
                String isShown = ActiveUserInfo.getDefaults(prefKey, this);

                if (!"true".equals(isShown)) {
                    com.google.android.material.snackbar.Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Образ добавлен в папку избранного в личном кабинете.",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                            )
                            .setDuration(5000)
                            .setAction("Перейти", s -> {
                                // Маршрутизация на главный экран с инструкцией открыть ЛК
                                Intent mainIntent = new Intent(this, MainActivity.class);
                                mainIntent.putExtra("OPEN_TAB", "PROFILE");
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(mainIntent);
                                overridePendingTransition(0, 0);
                                finish();
                            })
                            .show();

                    ActiveUserInfo.setDefaults(prefKey, "true", this);
                }
            } else {
                Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Динамическое переключение цвета иконки лайка (выделенный/серый контур).
     */
    private void updateLikeButtonUI() {
        btnLike.setColorFilter(isLiked ? COLOR_BLUE : COLOR_GRAY);
        btnLike.setImageResource(R.drawable.ic_heart_outline);
        if (btnDislike != null) {
            if (isLiked) {
                btnDislike.setVisibility(View.GONE);
            } else {
                btnDislike.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Конфигурация горизонтального списка товаров и обработка кликов по вещам
     */
    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Открытие карточки товара во внешнем браузере с логированием клика в CR-аналитику
        adapter = new ItemAdapter(url -> {
            if (url != null && !url.isEmpty()) {
                if (currentOutfitId != null && !currentOutfitId.isEmpty()) {
                    try {
                        CR.updateCountLink(Integer.parseInt(currentOutfitId));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Ссылка на товар не найдена", Toast.LENGTH_SHORT).show();
            }
        });
        rvProducts.setAdapter(adapter);
    }

    /**
     * Парсинг данных из вызывающего Intent и инициализация слоя ViewModel
     */
    private void loadDataFromIntent() {
        String imageUrl = getIntent().getStringExtra("image_url");
        currentOutfitId = getIntent().getStringExtra("outfit_id");
        String style = getIntent().getStringExtra("style");
        String season = getIntent().getStringExtra("season");
        currentCollectionId = getIntent().getStringExtra("collection_id");
        isLiked = getIntent().getBooleanExtra("is_liked", false);

        ArrayList<String> itemIds = getIntent().getStringArrayListExtra("item_ids");

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(ivMain);
        }

        // Временная сборка сущности Outfit
        Outfit tempOutfit = new Outfit();
        tempOutfit.setId(currentOutfitId);
        tempOutfit.setStyle(style);
        tempOutfit.setFilter_season(season);

        // Конвертация плоского списка ID из Intent в Map-структуру, необходимую для Firebase репозитория
        Map<String, Boolean> itemsMap = new HashMap<>();
        if (itemIds != null) {
            for (String id : itemIds) {
                itemsMap.put(id, true);
            }
        }
        tempOutfit.setItems(itemsMap);

        viewModel.init(tempOutfit);
    }

    /**
     * Подписка на LiveData-потоки состояния экрана (список вещей, название, суммарная стоимость).
     */
    private void observeViewModel() {
        viewModel.items.observe(this, items -> {
            adapter.updateList(items);
        });

        viewModel.title.observe(this, title -> {
            if (tvTitle != null) tvTitle.setText(title);
        });

        viewModel.totalPrice.observe(this, price -> {
            if (tvTotalPrice != null) tvTotalPrice.setText(price);
        });
    }

    @Override
    public void finish() {
        // Передача финального статуса лайка обратно вызывающему экрану для мгновенного обновления в общем списке
        Intent resultIntent = new Intent();
        resultIntent.putExtra("outfit_id", currentOutfitId);
        resultIntent.putExtra("is_liked", isLiked);
        setResult(RESULT_OK, resultIntent);

        // Расчет чистой дельты времени сессии просмотра экрана и отправка в аналитику удержания
        if (currentOutfitId != null && !currentOutfitId.isEmpty()) {
            try {
                long end = System.nanoTime();
                changeAvgTime(Integer.parseInt(currentOutfitId), (end - start) / 1_000_000_000.0);
            } catch (NumberFormatException ignored) {
            }
        }

        super.finish();
    }
}