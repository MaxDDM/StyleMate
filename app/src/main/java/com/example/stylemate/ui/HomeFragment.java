package com.example.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager; // Важный импорт

import com.example.stylemate.R;
import com.example.stylemate.model.HomeViewModel;
import com.example.stylemate.model.Outfit;
import com.example.stylemate.repository.ActiveUserInfo;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView rvCollections;
    private RecyclerView rvGrid; // --- НОВОЕ: Ссылка на сетку товаров
    private View vOverlay;
    private View btnCreate;      // --- НОВОЕ: Кнопка создать

    private CollectionsNameAdapter adapter;
    private OutfitAdapter gridAdapter; // --- НОВОЕ: Адаптер для сетки
    private HomeViewModel viewModel;

    private boolean isListExpanded = false;
    private boolean isGuest;     // --- НОВОЕ: Флаг гостя
    private int userStyleId;     // --- НОВОЕ: Стиль юзера

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Инициализация View
        View btnList = view.findViewById(R.id.btnList);
        rvCollections = view.findViewById(R.id.rvCollections);
        vOverlay = view.findViewById(R.id.vOverlay);

        // --- НОВОЕ: Ищем новые элементы
        rvGrid = view.findViewById(R.id.rvOutfitsGrid);
        btnCreate = view.findViewById(R.id.btnCreate);

        // --- НОВОЕ: 1. Получаем данные о пользователе (Гость / Стиль)
        String guestFlag = ActiveUserInfo.getDefaults("is_guest", getContext());
        isGuest = "true".equals(guestFlag);

        String styleStr = ActiveUserInfo.getDefaults("user_style_id", getContext());
        // Если стиль не найден (например, ошибка), ставим 1
        userStyleId = (styleStr != null && !styleStr.isEmpty()) ? Integer.parseInt(styleStr) : 1;


        // --- ЛОГИКА ВЫПАДАЮЩЕГО СПИСКА (ТВОЙ СТАРЫЙ КОД) ---
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollectionsNameAdapter(new ArrayList<>(), "Основная", clickedName -> {
            if (clickedName == null) {
                toggleListState();
            } else {
                adapter.setSelectedName(clickedName);
                viewModel.onCollectionSelected(clickedName);
                toggleListState(); // Закрываем список после выбора
            }
        });
        rvCollections.setAdapter(adapter);


        // --- НОВОЕ: 2. ЛОГИКА СЕТКИ ТОВАРОВ (PINTEREST STYLE) ---

        // Настраиваем StaggeredGridLayoutManager (2 колонки, вертикально)
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Убираем дергание при скролле
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        rvGrid.setLayoutManager(layoutManager);

        // Создаем адаптер
        gridAdapter = new OutfitAdapter(new ArrayList<>(), new OutfitAdapter.OnOutfitClickListener() {
            @Override
            public void onHeartClick(Outfit outfit, int position) {
                // Если гость -> ругаемся, если юзер -> лайкаем
                if (isGuest) {
                    Toast.makeText(getContext(), "Войдите или зарегистрируйтесь, чтобы сохранять", Toast.LENGTH_SHORT).show();
                    // Можно тут открыть AuthActivity
                } else {
                    viewModel.toggleLike(outfit.getId());
                }
            }

            @Override
            public void onImageClick(Outfit outfit) {
                Toast.makeText(getContext(), "Открыть товар: " + outfit.getId(), Toast.LENGTH_SHORT).show();
            }
        });
        rvGrid.setAdapter(gridAdapter);


        // --- ПОДПИСКА НА ДАННЫЕ (OBSERVERS) ---

        // 1. Список названий коллекций (старое)
        viewModel.collections.observe(getViewLifecycleOwner(), list -> {
            adapter.updateList(list);
        });

        // 2. Выбранная коллекция (обновленное)
        viewModel.selectedName.observe(getViewLifecycleOwner(), name -> {
            adapter.setSelectedName(name);

            // --- НОВОЕ: Когда меняется коллекция, грузим для неё картинки
            viewModel.loadOutfitsForStyle(userStyleId); // (можно передать name, если логика зависит от коллекции)
        });

        // --- НОВОЕ: 3. Список картинок (обновляем адаптер сетки)
        viewModel.outfits.observe(getViewLifecycleOwner(), outfits -> {
            gridAdapter.updateList(outfits);
        });


        // --- ОБРАБОТЧИКИ НАЖАТИЙ ---

        vOverlay.setOnClickListener(v -> {
            if (isListExpanded) toggleListState();
        });

        btnList.setOnClickListener(v -> {
            FiltersBottomSheetFragment filtersFragment = new FiltersBottomSheetFragment();
            filtersFragment.show(getParentFragmentManager(), "FiltersTag");
        });

        // --- НОВОЕ: Кнопка "Создать подборку"
        btnCreate.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(getContext(), "Доступно только зарегистрированным пользователям", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Открываем создание...", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(getContext(), CreateCollectionActivity.class);
                // startActivity(intent);
            }
        });

        return view;
    }

    private void toggleListState() {
        if (isListExpanded) {
            isListExpanded = false;
            adapter.setExpanded(false);
            rvCollections.setBackgroundResource(android.R.color.transparent);
            vOverlay.setVisibility(View.GONE);
        } else {
            isListExpanded = true;
            adapter.setExpanded(true);
            rvCollections.setBackgroundResource(R.drawable.bg_dropdown_list); // Убедись, что этот drawable существует
            vOverlay.setVisibility(View.VISIBLE);
        }
    }
}