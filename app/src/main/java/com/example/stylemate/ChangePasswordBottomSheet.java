package com.example.stylemate;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private ViewFlipper viewFlipper;
    private EditText etOldPassword;
    private EditText etNewPassword;

    // Флаг, чтобы понимать, на каком мы шаге (для восстановления клавиатуры)
    private boolean isSecondStep = false;
    private final String MOCK_CURRENT_PASSWORD = "123";

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

        viewFlipper = view.findViewById(R.id.viewFlipper);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);

        // === ВАЖНО: Анимация Слайда (вместо Fade) ===
        // Это уберет эффект "перерисовки/глюка". Экран будет уезжать влево.
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));

        setupInputLogic(etOldPassword, true);
        setupInputLogic(etNewPassword, false);

        // Старт
        etOldPassword.requestFocus();
        // Небольшая задержка, чтобы анимация открытия шторки не лагала
        new Handler().postDelayed(this::showKeyboardForCurrentStep, 200);
    }

    private void setupInputLogic(EditText editText, boolean isOldPass) {
        // Убираем иконку при вводе
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Enter на клавиатуре
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                if (isOldPass) checkOldPassword();
                else saveNewPassword();
                return true;
            }
            return false;
        });
    }

    private void checkOldPassword() {
        String input = etOldPassword.getText().toString();
        if (input.equals(MOCK_CURRENT_PASSWORD)) {
            // Успех -> Переход
            isSecondStep = true;
            viewFlipper.showNext(); // Сработает анимация Slide

            // Переносим фокус и клаву
            etNewPassword.requestFocus();
            // Клавиатура может моргнуть, поэтому форсируем показ
            showKeyboardForCurrentStep();
        } else {
            showError(etOldPassword, "Неверный текущий пароль");
        }
    }

    private void saveNewPassword() {
        if (etNewPassword.getText().length() < 4) {
            showError(etNewPassword, "Слишком короткий пароль");
            return;
        }

        CustomToast.show(getContext(), "Пароль изменен");
        hideKeyboardDirectly();
        new Handler().postDelayed(this::dismiss, 150);
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