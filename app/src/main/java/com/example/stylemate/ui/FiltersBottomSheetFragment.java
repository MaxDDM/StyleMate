package com.example.stylemate.ui;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.model.FiltersViewModel;
import com.example.stylemate.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Set;

public class FiltersBottomSheetFragment extends BottomSheetDialogFragment {
    private FiltersViewModel viewModel;
    // Ссылки на контейнеры, чтобы потом бегать по ним и обновлять цвета
    private GridLayout gridTypes;
    private GridLayout gridColors;
    private GridLayout gridSeasons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters_bottom_sheet, container, false);

        viewModel = new ViewModelProvider(this).get(FiltersViewModel.class);

        // Находим наши сетки (GridLayout)
        gridTypes = view.findViewById(R.id.gridTypes);
        gridColors = view.findViewById(R.id.gridColors);
        gridSeasons = view.findViewById(R.id.gridSeasons);

        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);

        // --- 1. СТРОИМ UI (Создаем кнопки) ---
        // Подписываемся на списки данных. Как только они придут - строим сетки.
        viewModel.typesList.observe(getViewLifecycleOwner(), list -> fillGrid(inflater, gridTypes, list));
        viewModel.colorsList.observe(getViewLifecycleOwner(), list -> fillGrid(inflater, gridColors, list));
        viewModel.seasonsList.observe(getViewLifecycleOwner(), list -> fillGrid(inflater, gridSeasons, list));

        // --- 2. СЛЕДИМ ЗА СОСТОЯНИЕМ (Галочки) ---
        // Как только юзер нажмет кнопку, ViewModel обновит selectedFilters,
        // и этот код сработает -> кнопки перекрасятся.
        viewModel.selectedFilters.observe(getViewLifecycleOwner(), this::updateButtonsState);

        // --- ЛОГИКА КНОПКИ СБРОСИТЬ ---
        btnReset.setOnClickListener(v -> {
            viewModel.resetAll();
            CustomToast.show(getContext(), "Фильтры сброшены");
        });

        // --- ЛОГИКА КНОПКИ ПРИМЕНИТЬ ---
        btnApply.setOnClickListener(v -> viewModel.apply());
        viewModel.applyEvent.observe(getViewLifecycleOwner(), shouldClose -> {
            if (shouldClose) {
                CustomToast.show(getContext(), "Фильтры применены");
                dismiss();
            }
        });

        return view;
    }

    // Метод, который бегает по всем кнопкам и включает/выключает их
    private void updateButtonsState(Set<String> selectedFilters) {
        updateGrid(gridTypes, selectedFilters);
        updateGrid(gridColors, selectedFilters);
        updateGrid(gridSeasons, selectedFilters);
    }

    private void updateGrid(GridLayout grid, Set<String> selectedFilters) {
        if (grid == null) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) child;
                String text = btn.getText().toString();
                // Если текст кнопки есть в списке выбранных - ставим Checked = true
                boolean isSelected = selectedFilters.contains(text);

                // Чтобы не вызывать лишнюю перерисовку
                if (btn.isChecked() != isSelected) {
                    btn.setChecked(isSelected);
                }
            }
        }
    }

    private void fillGrid(LayoutInflater inflater, GridLayout grid, List<String> items) {
        grid.removeAllViews();
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

            // При клике мы не меняем цвет сами! Мы сообщаем ViewModel.
            button.setOnClickListener(v -> viewModel.toggleFilter(item));

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