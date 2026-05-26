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

import com.bumptech.glide.Glide;
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
    private TextView tvTitle;
    private TextView tvTotalPrice;
    private RecyclerView rvProducts;
    private ImageButton btnLike;
    ImageButton shareButton;

    private String currentCollectionId;
    private String currentOutfitId;
    private String imageUrl;
    private boolean isLiked = false;
    private final int COLOR_BLUE = android.graphics.Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = android.graphics.Color.parseColor("#5C5C5C");
    private final ItemsRepository repository = new ItemsRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        viewModel = new ViewModelProvider(this).get(OutfitDetailViewModel.class);

        ivMain = findViewById(R.id.ivDetailImage);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnDetailLike);
        shareButton = findViewById(R.id.shareButton);

        tvTitle = findViewById(R.id.tvOutfitTitle);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvProducts = findViewById(R.id.rvProducts);

        setupRecyclerView();
        loadDataFromIntent();
        updateLikeButtonUI();
        observeViewModel();

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

        shareButton.setOnClickListener(v -> share());

        btnLike.setOnClickListener(v -> {
            if (currentCollectionId == null) {
                Toast.makeText(this, "Войдите или зарегистрируйтесь, чтобы сохранять образы", Toast.LENGTH_SHORT).show();
                return;
            }

            isLiked = !isLiked;
            updateLikeButtonUI();

            viewModel.toggleLike(currentCollectionId, currentOutfitId, isLiked);

            if (isLiked) {
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
        btnLike.setColorFilter(isLiked ? COLOR_BLUE : COLOR_GRAY);
        btnLike.setImageResource(R.drawable.ic_heart_outline);
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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

    private void loadDataFromIntent() {
        imageUrl = getIntent().getStringExtra("image_url");
        currentOutfitId = getIntent().getStringExtra("outfit_id");
        String style = getIntent().getStringExtra("style");
        String season = getIntent().getStringExtra("season");
        currentCollectionId = getIntent().getStringExtra("collection_id");
        isLiked = getIntent().getBooleanExtra("is_liked", false);

        ArrayList<String> itemIds = getIntent().getStringArrayListExtra("item_ids");

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(ivMain);
        }

        Outfit tempOutfit = new Outfit();
        tempOutfit.setId(currentOutfitId);
        tempOutfit.setStyle(style);
        tempOutfit.setFilter_season(season);

        Map<String, Boolean> itemsMap = new HashMap<>();
        if (itemIds != null) {
            for (String id : itemIds) {
                itemsMap.put(id, true);
            }
        }
        tempOutfit.setItems(itemsMap);

        viewModel.init(tempOutfit);
    }

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
        Intent resultIntent = new Intent();
        resultIntent.putExtra("outfit_id", currentOutfitId);
        resultIntent.putExtra("is_liked", isLiked);
        setResult(RESULT_OK, resultIntent);

        if (currentOutfitId != null && !currentOutfitId.isEmpty()) {
            try {
                long end = System.nanoTime();
                changeAvgTime(Integer.parseInt(currentOutfitId), (end - start) / 1_000_000_000.0);
            } catch (NumberFormatException ignored) {
            }
        }

        super.finish();
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