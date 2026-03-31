package com.pupkov.stylemate.ui;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pupkov.stylemate.model.FilterCategory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.pupkov.stylemate.model.FiltersViewModel;
import com.pupkov.stylemate.model.FilterState;
import com.pupkov.stylemate.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.Set;

public class FiltersBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String REQUEST_KEY = "request_filter";
    public static final String RESULT_KEY = "filter_result";
    // Ключ для входящих аргументов
    private static final String ARG_CURRENT_STATE = "arg_current_state";
    private FiltersViewModel viewModel;
    // Ссылки на контейнеры, чтобы потом бегать по ним и обновлять цвета
    private FlexboxLayout gridTypes;
    private FlexboxLayout gridColors;
    private FlexboxLayout gridSeasons;

    // Статический метод для удобного создания фрагмента с данными
    public static FiltersBottomSheetFragment newInstance(FilterState currentState) {
        FiltersBottomSheetFragment fragment = new FiltersBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_STATE, currentState);
        fragment.setArguments(args);
        return fragment;
    }

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

        if (getArguments() != null && savedInstanceState == null) {
            FilterState state = (FilterState) getArguments().getSerializable(ARG_CURRENT_STATE);
            viewModel.setInitialState(state);
        }

        // --- 1. ЗАПОЛНЯЕМ UI ---
        // Передаем категорию в метод заполнения!
        viewModel.typesList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridTypes, list, FilterCategory.TYPE));

        viewModel.colorsList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridColors, list, FilterCategory.COLOR));

        viewModel.seasonsList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridSeasons, list, FilterCategory.SEASON));


        // --- 2. СЛЕДИМ ЗА ВЫБОРОМ (ТРИ РАЗНЫХ СПИСКА) ---
        viewModel.selectedTypes.observe(getViewLifecycleOwner(), set -> updateGrid(gridTypes, set));
        viewModel.selectedColors.observe(getViewLifecycleOwner(), set -> updateGrid(gridColors, set));
        viewModel.selectedSeasons.observe(getViewLifecycleOwner(), set -> updateGrid(gridSeasons, set));

        // --- ЛОГИКА КНОПКИ СБРОСИТЬ ---
        btnReset.setOnClickListener(v -> {
            viewModel.resetAll();
            viewModel.apply();
            CustomToast.show(getContext(), "Фильтры сброшены");
        });

        btnApply.setOnClickListener(v -> {
            // Запускаем процесс во ViewModel
            viewModel.apply();
        });

        // Когда ViewModel собрала FilterState, отправляем его в HomeFragment
        viewModel.applyEvent.observe(getViewLifecycleOwner(), filterState -> {
            if (filterState != null) {
                android.util.Log.d("FILTER_DEBUG", "Шторка: Отправляю данные! Типов: " + filterState.getSelectedTypes().size());
                Bundle result = new Bundle();
                result.putSerializable(RESULT_KEY, filterState);

                // Отправляем результат "наверх"
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

                dismiss();
            }
        });

        return view;
    }
    // Обновляет визуальное состояние кнопок (нажата/нет)
    private void updateGrid(FlexboxLayout grid, Set<String> selectedFilters) {
        if (grid == null) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) child;
                String text = btn.getText().toString();
                // Проверяем, есть ли этот текст в переданном наборе
                boolean isSelected = selectedFilters.contains(text);
                if (btn.isChecked() != isSelected) {
                    btn.setChecked(isSelected);
                }
            }
        }
    }

    private void fillGrid(LayoutInflater inflater, FlexboxLayout grid, List<String> items, FilterCategory category) {
        grid.removeAllViews();

        for (String item : items) {
            MaterialButton button = (MaterialButton) inflater.inflate(
                    R.layout.item_filter_button, grid, false
            );
            button.setText(item);

            // --- МАГИЯ РАСТЯГИВАНИЯ ---
            // Создаем параметры именно для FlexboxLayout
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            // flexGrow = 1.0f означает: "Растягивай меня, чтобы заполнить пустоту"
            params.setFlexGrow(1.0f);

            // Задаем отступы МЕЖДУ кнопками через params (а не через ChipGroup)
            int margin = dpToPx(4); // 4dp со всех сторон даст 8dp между кнопками
            params.setMargins(margin, margin, margin, margin);

            button.setLayoutParams(params);
            // ---------------------------

            button.setOnClickListener(v -> viewModel.toggleFilter(item, category));
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
            bottomSheet.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.85);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        }
    }
}