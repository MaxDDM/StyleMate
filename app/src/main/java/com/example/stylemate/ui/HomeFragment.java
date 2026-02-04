package com.example.stylemate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.model.HomeViewModel;
import com.example.stylemate.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView rvCollections;
    private View vOverlay;

    private CollectionsNameAdapter adapter;
    private HomeViewModel viewModel;
    private boolean isListExpanded = false; // Состояние

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View btnList = view.findViewById(R.id.btnList);
        rvCollections = view.findViewById(R.id.rvCollections);
        vOverlay = view.findViewById(R.id.vOverlay);

        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));

        // Создаем адаптер
        adapter = new CollectionsNameAdapter(new ArrayList<>(), "Основная", clickedName -> {
            if (clickedName == null) {
                // Нажали на заголовок в свернутом виде -> ОТКРЫВАЕМ
                toggleListState();
            } else {
                // Нажали на элемент в развернутом виде -> ВЫБИРАЕМ

                // 1. Просто выделяем синим в списке (список НЕ закрываем)
                adapter.setSelectedName(clickedName);
                // СООБЩАЕМ VIEWMODEL О ВЫБОРЕ
                viewModel.onCollectionSelected(clickedName);
            }
        });

        rvCollections.setAdapter(adapter);

        // --- ПОДПИСЫВАЕМСЯ НА ДАННЫЕ (OBSERVE) ---

        // 1. Следим за списком категорий
        viewModel.collections.observe(getViewLifecycleOwner(), list -> {
            // Как только данные загрузятся (из Репозитория), этот код сработает
            adapter.updateList(list); // Тебе нужно добавить метод updateList в адаптер!
        });

        // 2. Следим за выбранным элементом (если вдруг выбор изменится из другого места)
        viewModel.selectedName.observe(getViewLifecycleOwner(), name -> {
            adapter.setSelectedName(name);
        });

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