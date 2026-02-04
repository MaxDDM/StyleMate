package com.example.stylemate.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.example.stylemate.model.ChangePasswordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private ChangePasswordViewModel viewModel; // ViewModel
    private ViewFlipper viewFlipper;
    private EditText etOldPassword;
    private EditText etNewPassword;

    // Флаг, чтобы понимать, на каком мы шаге (для восстановления клавиатуры)
    private boolean isSecondStep = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 1. Высота
                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                bottomSheet.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.75);

                // 2. Behavior
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // === ЛОГИКА КЛАВИАТУРЫ ===
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        // А. Если начали тянуть вниз -> Прячем
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            hideKeyboardDirectly();
                        }
                        // Б. Если отпустили и шторка вернулась вверх (EXPANDED) -> Возвращаем клаву
                        else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            showKeyboardForCurrentStep();
                        }
                        // В. Если шторка закрылась (HIDDEN) -> Прячем (на всякий случай)
                        else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            hideKeyboardDirectly();
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // Можно добавить плавное затухание клавиатуры, но это сложно без WindowInsetsController
                    }
                });
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Подключаем ViewModel
        viewModel = new ViewModelProvider(this).get(ChangePasswordViewModel.class);

        viewFlipper = view.findViewById(R.id.viewFlipper);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);

        // === ВАЖНО: Анимация Слайда (вместо Fade) ===
        // Это уберет эффект "перерисовки/глюка". Экран будет уезжать влево.
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));

        // Настройка полей
        setupInputUI(etOldPassword);
        setupInputUI(etNewPassword);

        // Старт
        etOldPassword.requestFocus();
        // Небольшая задержка, чтобы анимация открытия шторки не лагала
        new Handler().postDelayed(this::showKeyboardForCurrentStep, 200);

        // === ПОДПИСКИ НА VIEWMODEL ===
        observeViewModel();

        // === СЛУШАТЕЛИ ВВОДА ===
        // Нажатие Enter на клавиатуре
        etOldPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                // Передаем ввод во ViewModel
                viewModel.verifyOldPassword(etOldPassword.getText().toString());
                return true;
            }
            return false;
        });

        etNewPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                // Передаем ввод во ViewModel
                viewModel.submitNewPassword(etNewPassword.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        // 1. Успех проверки старого пароля
        viewModel.oldPasswordCorrect.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                isSecondStep = true;
                viewFlipper.showNext(); // Листаем экран

                etNewPassword.requestFocus();
                showKeyboardForCurrentStep();
            }
        });

        // 2. Успех смены пароля
        viewModel.passwordChanged.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                CustomToast.show(getContext(), "Пароль изменен");
                hideKeyboardDirectly();
                new Handler().postDelayed(this::dismiss, 150);
            }
        });

        // 3. Ошибка
        viewModel.errorEvent.observe(getViewLifecycleOwner(), message -> {
            EditText target = isSecondStep ? etNewPassword : etOldPassword;
            showError(target, message);
        });
    }

    private void setupInputUI(EditText editText) {
        // Визуальная логика (убрать иконку ошибки при наборе)
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }



    // Показывает иконку и тост
    private void showError(EditText field, String message) {
        // Иконка в поле (черная или красная)
        field.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error_circle, 0);
        CustomToast.show(getContext(), message);

        // Вибрация (Haptic feedback) для тактильности
        field.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
    }

    // === Управление клавиатурой ===
    private void hideKeyboardDirectly() {
        if (getView() != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void showKeyboardForCurrentStep() {
        EditText target = isSecondStep ? etNewPassword : etOldPassword;
        if (target != null && getContext() != null) {
            target.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
        }
    }

}