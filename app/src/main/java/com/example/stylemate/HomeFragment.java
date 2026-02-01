package com.example.stylemate;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvCollections;
    private View vOverlay;

    private CollectionsNameAdapter adapter;
    private boolean isListExpanded = false; // Состояние

    // Текущий подтвержденный выбор
    private String currentSelectedName = "Основная";

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        View btnList = view.findViewById(R.id.btnList);
        rvCollections = view.findViewById(R.id.rvCollections);
        vOverlay = view.findViewById(R.id.vOverlay);

        // Список данных
        List<String> myCollections = Arrays.asList(
                "Основная",
                "Спорт",
                "Улица",
                "Офис",
                "Свидание",
                "Дом",
                "Вечеринка"
        );

        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));

        // Создаем адаптер
        adapter = new CollectionsNameAdapter(myCollections, currentSelectedName, clickedName -> {
            if (clickedName == null) {
                // Нажали на заголовок в свернутом виде -> ОТКРЫВАЕМ
                toggleListState();
            } else {
                // Нажали на элемент в развернутом виде -> ВЫБИРАЕМ

                // 1. Просто выделяем синим в списке (список НЕ закрываем)
                adapter.setSelectedName(clickedName);
                currentSelectedName = clickedName; // Запоминаем новый выбор
            }
        });

        rvCollections.setAdapter(adapter);

        // --- ОБРАБОТЧИКИ ---

        // Нажатие на шторку (фон) -> Закрывает список
        vOverlay.setOnClickListener(v -> {
            if (isListExpanded) {
                toggleListState();
            }
        });

        // 3. Ставим слушатель нажатия
        btnList.setOnClickListener(v -> {
            // Создаем нашу шторку
            FiltersBottomSheetFragment filtersFragment = new FiltersBottomSheetFragment();

            // Показываем её.
            // getParentFragmentManager() - менеджер фрагментов
            // "FiltersTag" - просто тег (имя) для логов или поиска
            filtersFragment.show(getParentFragmentManager(), "FiltersTag");
        });

        return view;
    }

    private void toggleListState() {
        if (isListExpanded) {
            // --- ЗАКРЫВАЕМ ---
            isListExpanded = false;

            // 1. Сообщаем адаптеру (он покажет только 1 элемент - currentSelectedName)
            adapter.setExpanded(false);

            // 2. Убираем белый фон и тень (делаем прозрачным)
            rvCollections.setBackgroundResource(android.R.color.transparent);
            // 3. Прячем шторку
            vOverlay.setVisibility(View.GONE);
        } else {
            // --- ОТКРЫВАЕМ ---
            isListExpanded = true;

            // 1. Сообщаем адаптеру (покажет всё)
            adapter.setExpanded(true);

            // 2. Включаем белый фон и тень
            rvCollections.setBackgroundResource(R.drawable.bg_dropdown_list);
            // 3. Показываем шторку
            vOverlay.setVisibility(View.VISIBLE);
        }
    }
}