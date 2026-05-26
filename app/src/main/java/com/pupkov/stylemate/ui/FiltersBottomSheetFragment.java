package com.pupkov.stylemate.ui;

import android.app.Dialog;
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
    private static final String ARG_CURRENT_STATE = "arg_current_state";

    private FiltersViewModel viewModel;
    private FlexboxLayout gridTypes;
    private FlexboxLayout gridColors;
    private FlexboxLayout gridSeasons;

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

        gridTypes = view.findViewById(R.id.gridTypes);
        gridColors = view.findViewById(R.id.gridColors);
        gridSeasons = view.findViewById(R.id.gridSeasons);

        MaterialButton btnReset = view.findViewById(R.id.btnReset);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);

        if (getArguments() != null && savedInstanceState == null) {
            FilterState state = (FilterState) getArguments().getSerializable(ARG_CURRENT_STATE);
            viewModel.setInitialState(state);
        }

        viewModel.typesList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridTypes, list, FilterCategory.TYPE));

        viewModel.colorsList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridColors, list, FilterCategory.COLOR));

        viewModel.seasonsList.observe(getViewLifecycleOwner(), list ->
                fillGrid(inflater, gridSeasons, list, FilterCategory.SEASON));


        viewModel.selectedTypes.observe(getViewLifecycleOwner(), set -> updateGrid(gridTypes, set));
        viewModel.selectedColors.observe(getViewLifecycleOwner(), set -> updateGrid(gridColors, set));
        viewModel.selectedSeasons.observe(getViewLifecycleOwner(), set -> updateGrid(gridSeasons, set));

        btnReset.setOnClickListener(v -> {
            viewModel.resetAll();
            viewModel.apply();
            CustomToast.show(getContext(), "Фильтры сброшены");
        });

        btnApply.setOnClickListener(v -> viewModel.apply());

        viewModel.applyEvent.observe(getViewLifecycleOwner(), filterState -> {
            if (filterState != null) {
                android.util.Log.d("FILTER_DEBUG", "Шторка: Отправляю данные! Типов: " + filterState.getSelectedTypes().size());
                Bundle result = new Bundle();
                result.putSerializable(RESULT_KEY, filterState);

                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                dismiss();
            }
        });

        return view;
    }

    private void updateGrid(FlexboxLayout grid, Set<String> selectedFilters) {
        if (grid == null) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) child;
                String text = btn.getText().toString();

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

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            params.setFlexGrow(1.0f);

            int margin = dpToPx(4);
            params.setMargins(margin, margin, margin, margin);
            button.setLayoutParams(params);

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
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();

            bottomSheet.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.85);

            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }
}