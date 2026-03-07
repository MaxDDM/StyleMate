package com.example.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.stylemate.R;
import com.example.stylemate.model.CollectionDetailViewModel;
import com.example.stylemate.model.Outfit;

import java.util.ArrayList;

public class CollectionDetailActivity extends AppCompatActivity {

    private CollectionDetailViewModel viewModel;
    private TextView tvTitle;
    private RecyclerView rvGrid;
    private OutfitAdapter adapter; // Наш универсальный адаптер

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        // 1. Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(CollectionDetailViewModel.class);

        // 2. Получаем данные из Intent
        String collectionId = getIntent().getStringExtra("COLLECTION_ID");
        String collectionTitle = getIntent().getStringExtra("COLLECTION_TITLE");

        // 3. Передаем их во ViewModel (чтобы начать загрузку)
        if (collectionId != null) {
            viewModel.init(collectionId, collectionTitle);
        }

        initViews();
        setupRecyclerView(); // Настройка сетки
        observeViewModel();  // Подписка на данные
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnDelete = findViewById(R.id.btnDelete);

        tvTitle = findViewById(R.id.tvCollectionTitle);
        rvGrid = findViewById(R.id.rvOutfitsGrid); // Твой новый RecyclerView

        btnBack.setOnClickListener(v -> finish());

        // Логика твоих кнопок (оставил твою реализацию вызова диалогов)
        btnEdit.setOnClickListener(v -> {
            String currentTitle = viewModel.title.getValue();
            EditCollectionBottomSheet bottomSheet = EditCollectionBottomSheet.newInstance(currentTitle);
            bottomSheet.setListener(newTitle -> viewModel.onCollectionRenamed(newTitle));
            bottomSheet.show(getSupportFragmentManager(), "EditCollectionTag");
        });

        btnDelete.setOnClickListener(v -> {
            DeleteCollectionDialog dialog = new DeleteCollectionDialog();
            dialog.setListener(() -> viewModel.onCollectionDeleted());
            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });
    }

    private void setupRecyclerView() {
        // 1. Менеджер как на главной (2 колонки, разная высота)
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        rvGrid.setLayoutManager(layoutManager);

        // 2. Создаем адаптер
        adapter = new OutfitAdapter(this, new ArrayList<>(), new OutfitAdapter.OnOutfitClickListener() {
            @Override
            public void onHeartClick(Outfit outfit, int position) {
                // ПРИ НАЖАТИИ НА ЛАЙК -> МГНОВЕННОЕ УДАЛЕНИЕ
                viewModel.removeOutfit(outfit.getId());
            }

            @Override
            public void onImageClick(Outfit outfit) {
                // ОТКРЫТИЕ КАРТОЧКИ (Код как на главной)
                Intent intent = new Intent(CollectionDetailActivity.this, OutfitDetailActivity.class);
                intent.putExtra("outfit_id", outfit.getId());
                intent.putExtra("image_url", outfit.getImageUrl());
                intent.putExtra("style", outfit.getStyle());
                intent.putExtra("season", outfit.getFilter_season());
                intent.putExtra("is_liked", true); // Тут он всегда лайкнут
                String currentCollectionId = getIntent().getStringExtra("COLLECTION_ID");
                intent.putExtra("collection_id", currentCollectionId);

                // Передаем вещи, если есть
                if (outfit.getItems() != null) {
                    intent.putStringArrayListExtra("item_ids", new ArrayList<>(outfit.getItems().keySet()));
                }

                startActivity(intent);
            }
        });

        rvGrid.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Следим за заголовком
        viewModel.title.observe(this, newTitle -> tvTitle.setText(newTitle));

        // Следим за списком одежды
        viewModel.outfits.observe(this, outfits -> {
            if (outfits != null) {
                adapter.updateList(outfits);
                // Тут можно добавить логику показа "Нет товаров", если список пуст
            }
        });

        // Следим за сообщениями и закрытием
        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.closeScreenEvent.observe(this, shouldClose -> {
            if (shouldClose) finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Как только экран становится видимым (в том числе после возврата назад из карточки),
        // просим ViewModel обновить данные из базы.
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}