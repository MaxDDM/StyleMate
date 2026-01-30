package com.example.stylemate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Создаем View из макета
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Находим кнопку списка по ID (тот FrameLayout из твоего XML)
        View btnList = view.findViewById(R.id.btnList);

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
}