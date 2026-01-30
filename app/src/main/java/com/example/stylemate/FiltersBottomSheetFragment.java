package com.example.stylemate;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout; // Используем стандартный GridLayout

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class FiltersBottomSheetFragment extends BottomSheetDialogFragment {

    private final String[] typesData = {
            "бриджи", "футболки", "джинсы", "пиджаки", "брюки", "шорты",
            "жилетки", "поло", "майки", "галстуки", "костюмы", "лонгсливы"
    };

    private final String[] colorsData = {
            "серый", "белый", "голубой", "красный", "зеленый", "желтый",
            "фиолетовый", "синий", "коричневый", "черный", "бежевый", "розовый"
    };

    private final String[] seasonsData = { "весна", "лето", "осень", "зима" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters_bottom_sheet, container, false);

        // Находим наши сетки (GridLayout)
        GridLayout gridTypes = view.findViewById(R.id.gridTypes);
        GridLayout gridColors = view.findViewById(R.id.gridColors);
        GridLayout gridSeasons = view.findViewById(R.id.gridSeasons);

        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);

        // Заполняем их
        fillGrid(inflater, gridTypes, typesData);
        fillGrid(inflater, gridColors, colorsData);
        fillGrid(inflater, gridSeasons, seasonsData);

        // --- ЛОГИКА КНОПКИ СБРОСИТЬ ---
        btnReset.setOnClickListener(v -> {
            // Очищаем каждую сетку
            clearGridSelection(gridTypes);
            clearGridSelection(gridColors);
            clearGridSelection(gridSeasons);

            // Показываем твой кастомный тост
            CustomToast.show(getContext(), "Фильтры сброшены");
        });

        // --- ЛОГИКА КНОПКИ ПРИМЕНИТЬ ---
        btnApply.setOnClickListener(v -> {
            // Тут в будущем будет код передачи данных на главную страницу

            // Показываем уведомление
            CustomToast.show(getContext(), "Фильтры применены");

            // Закрываем шторку
            dismiss();
        });

        return view;
    }

    // Вспомогательный метод для очистки (Сброса)
    private void clearGridSelection(GridLayout grid) {
        // Пробегаем по всем детям внутри GridLayout
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            // Если ребенок - это наша кнопка
            if (child instanceof MaterialButton) {
                // Выключаем её (она станет серой благодаря селектору)
                ((MaterialButton) child).setChecked(false);
            }
        }
    }

    private void fillGrid(LayoutInflater inflater, GridLayout grid, String[] items) {
        // 1. Вычисляем ширину одной кнопки (Ширина экрана / 3)
        // Вычитаем отступы (padding экрана 20+20 + отступы кнопок)
        // Грубый, но надежный расчет: Экран / 3 - немного места на отступы
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        int buttonWidth = (screenWidth / 3) - dpToPx(20); // 16dp запас на отступы

        for (String item : items) {
            // 2. Создаем кнопку из XML
            MaterialButton button = (MaterialButton) inflater.inflate(
                    R.layout.item_filter_button, grid, false
            );

            button.setText(item);

            // 3. Задаем ей вычисленную ширину
            // Теперь кнопка всегда занимает ровно 1/3, независимо от текста
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = buttonWidth;
            params.height = dpToPx(24);
            params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6)); // Отступы вокруг кнопки
            button.setLayoutParams(params);

            // 5. Просто добавляем в сетку. Она сама перенесет на новую строку.
            grid.addView(button);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Настройка высоты шторки
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
            bottomSheet.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.8);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        }
    }
}