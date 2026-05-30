package com.pupkov.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.CollectionDetailViewModel;
import com.pupkov.stylemate.model.Outfit;

import java.util.ArrayList;

/**
 * Экран содержимого конкретной папки с сохраненными образами.
 */
public class CollectionDetailActivity extends AppCompatActivity {

    private CollectionDetailViewModel viewModel;
    private TextView tvTitle;
    private RecyclerView rvGrid;
    private OutfitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        viewModel = new ViewModelProvider(this).get(CollectionDetailViewModel.class);

        String collectionId = getIntent().getStringExtra("COLLECTION_ID");
        String collectionTitle = getIntent().getStringExtra("COLLECTION_TITLE");

        if (collectionId != null) {
            viewModel.init(collectionId, collectionTitle);
        }

        initViews();
        setupRecyclerView();
        observeViewModel();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvCollectionTitle);
        rvGrid = findViewById(R.id.rvOutfitsGrid);

        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Настройка сетки (2 колонки с разной высотой элементов).
     */
    private void setupRecyclerView() {
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        // Стратегия минимизации разрывов и пропусков при динамической перерисовке картинок разной высоты
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvGrid.setLayoutManager(layoutManager);

        adapter = new OutfitAdapter(this, new ArrayList<>(), new OutfitAdapter.OnOutfitClickListener() {
            @Override
            public void onHeartClick(Outfit outfit, int position) {
                // Мгновенное удаление образа из этой подборки (снятие лайка)
                viewModel.removeOutfit(outfit.getId());
            }

            @Override
            public void onImageClick(Outfit outfit) {
                // Формирование Intent для перехода на экран детального просмотра с передачей плоского списка ID вещей
                Intent intent = new Intent(CollectionDetailActivity.this, OutfitDetailActivity.class);
                intent.putExtra("outfit_id", outfit.getId());
                intent.putExtra("image_url", outfit.getImageUrl());
                intent.putExtra("style", outfit.getStyle());
                intent.putExtra("season", outfit.getFilter_season());
                intent.putExtra("is_liked", true); // Внутри папки избранного образ по определению лайкнут

                String currentCollectionId = getIntent().getStringExtra("COLLECTION_ID");
                intent.putExtra("collection_id", currentCollectionId);

                if (outfit.getItems() != null) {
                    intent.putStringArrayListExtra("item_ids", new ArrayList<>(outfit.getItems().keySet()));
                }

                startActivity(intent);
            }
        });
        adapter.setLongClickEnabled(false);
        rvGrid.setAdapter(adapter);
    }

    /**
     * Подписка на LiveData состояния коллекции (динамический заголовок, массив образов).
     */
    private void observeViewModel() {
        viewModel.title.observe(this, newTitle -> tvTitle.setText(newTitle));

        viewModel.outfits.observe(this, outfits -> {
            if (outfits != null) {
                adapter.updateList(outfits);
            }
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Наблюдатель за событием удаления папки: закрывает экран, если репозиторий подтвердил удаление
        viewModel.closeScreenEvent.observe(this, shouldClose -> {
            if (shouldClose) finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Принудительный перезапрос данных из Firebase при возврате на экран (для синхронизации лайков)
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
}